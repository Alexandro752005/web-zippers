package pe.com.zippers.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pe.com.zippers.domain.Usuario;
import java.time.LocalDateTime;
import java.util.Collection;

/**
 * Principal autenticado del backoffice. Envuelve la entidad Usuario
 * y expone las authorities (rol + permisos) calculadas en el service.
 */
@Getter
public class UsuarioPrincipal implements UserDetails {

    private final Long idUsuario;
    private final String username;
    private final String passwordHash;
    private final String nombreCompleto;
    private final String rolCodigo;
    private final String estado;
    private final LocalDateTime bloqueadoHasta;
    private final Collection<? extends GrantedAuthority> authorities;

    public UsuarioPrincipal(Usuario u, Collection<? extends GrantedAuthority> authorities) {
        this.idUsuario = u.getIdUsuario();
        this.username = u.getUsername();
        this.passwordHash = u.getPasswordHash();
        this.nombreCompleto = u.getNombreCompleto();
        this.rolCodigo = u.getRol().getCodigo();
        this.estado = u.getEstado();
        this.bloqueadoHasta = u.getBloqueadoHasta();
        this.authorities = authorities;
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return passwordHash; }
    @Override public String getUsername() { return username; }

    @Override public boolean isAccountNonExpired() { return true; }

    /** Bloqueado si estado=BLOQUEADO y aún no expira el bloqueo. */
    @Override
    public boolean isAccountNonLocked() {
        if (!"BLOQUEADO".equals(estado)) return true;
        return bloqueadoHasta != null && bloqueadoHasta.isBefore(LocalDateTime.now());
    }

    @Override public boolean isCredentialsNonExpired() { return true; }

    /** Habilitado solo si estado=ACTIVO (soft delete = INACTIVO bloquea login). */
    @Override public boolean isEnabled() { return "ACTIVO".equals(estado); }
}