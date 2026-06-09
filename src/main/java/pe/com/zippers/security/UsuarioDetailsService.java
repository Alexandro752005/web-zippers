package pe.com.zippers.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.zippers.domain.Opcion;
import pe.com.zippers.domain.Permiso;
import pe.com.zippers.domain.Usuario;
import pe.com.zippers.repository.OpcionRepository;
import pe.com.zippers.repository.PermisoRepository;
import pe.com.zippers.repository.UsuarioRepository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Carga el usuario y construye sus authorities a partir de:
 *   - ROLE_<codigoRol>                (autoridad de rol)
 *   - <CODIGO_OPCION>_<ACCION>        (autoridad fina por permiso)
 *     ej.: USUARIOS_VER, USUARIOS_CREAR, USUARIOS_EDITAR, USUARIOS_ELIMINAR, USUARIOS_EXPORTAR
 *
 * El código de opción se normaliza a MAYÚSCULAS sin caracteres conflictivos.
 */
@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PermisoRepository permisoRepository;
    private final OpcionRepository opcionRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsernameOrEmail(login)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));

        Set<GrantedAuthority> authorities = construirAuthorities(usuario);
        return new UsuarioPrincipal(usuario, authorities);
    }

    private Set<GrantedAuthority> construirAuthorities(Usuario usuario) {
        Set<GrantedAuthority> auths = new HashSet<>();

        // Autoridad de rol.
        auths.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getCodigo()));

        // Mapa idOpcion -> codigo (para nombrar las authorities finas).
        Map<Long, String> codigoPorOpcion = new HashMap<>();
        for (Opcion o : opcionRepository.findAll()) {
            codigoPorOpcion.put(o.getIdOpcion(), normalizar(o.getCodigo()));
        }

        // Permisos activos del rol -> authorities por acción.
        List<Permiso> permisos = permisoRepository.findActivosByRol(usuario.getRol().getIdRol());
        for (Permiso p : permisos) {
            String cod = codigoPorOpcion.get(p.getIdOpcion());
            if (cod == null) continue;
            if (Boolean.TRUE.equals(p.getPermisoVer()))      auths.add(auth(cod, "VER"));
            if (Boolean.TRUE.equals(p.getPermisoCrear()))    auths.add(auth(cod, "CREAR"));
            if (Boolean.TRUE.equals(p.getPermisoEditar()))   auths.add(auth(cod, "EDITAR"));
            if (Boolean.TRUE.equals(p.getPermisoEliminar())) auths.add(auth(cod, "ELIMINAR"));
            if (Boolean.TRUE.equals(p.getPermisoExportar())) auths.add(auth(cod, "EXPORTAR"));
        }
        return auths;
    }

    private GrantedAuthority auth(String codigoOpcion, String accion) {
        return new SimpleGrantedAuthority(codigoOpcion + "_" + accion);
    }

    /** Normaliza el código de opción: MAYÚSCULAS y separadores a guion bajo. */
    private String normalizar(String codigo) {
        return codigo == null ? "" : codigo.trim().toUpperCase()
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
    }
}