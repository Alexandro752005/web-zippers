package pe.com.zippers.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.zippers.common.Constants;
import pe.com.zippers.common.exception.BusinessException;
import pe.com.zippers.domain.Rol;
import pe.com.zippers.domain.Usuario;
import pe.com.zippers.repository.RolRepository;
import pe.com.zippers.repository.UsuarioRepository;
import pe.com.zippers.service.AuditoriaService;
import pe.com.zippers.service.UsuarioService;
import pe.com.zippers.web.dto.UsuarioCreateDTO;
import pe.com.zippers.web.dto.UsuarioListItemDTO;
import pe.com.zippers.web.dto.UsuarioUpdateDTO;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    private static final String TABLA = "usuarios";

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioListItemDTO> listar(String q) {
        return usuarioRepository.buscar(q == null ? "" : q.trim()).stream()
                .map(u -> new UsuarioListItemDTO(
                        u.getIdUsuario(), u.getUsername(), u.getEmail(),
                        u.getNombres(), u.getApellidos(), u.getDni(), u.getTelefono(),
                        u.getRol().getNombre(), u.getRol().getCodigo(),
                        u.getEstado(), u.getCreatedAt()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioUpdateDTO obtenerParaEditar(Long idUsuario) {
        Usuario u = usuarioRepository.findById(Objects.requireNonNull(idUsuario, "El ID de usuario no puede ser nulo"))
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setIdUsuario(u.getIdUsuario());
        dto.setUsername(u.getUsername());
        dto.setEmail(u.getEmail());
        dto.setNombres(u.getNombres());
        dto.setApellidos(u.getApellidos());
        dto.setDni(u.getDni());
        dto.setTelefono(u.getTelefono());
        dto.setIdRol(u.getRol().getIdRol());
        dto.setEstado(u.getEstado());
        return dto;
    }

    @Override
    @Transactional
    public Long crear(UsuarioCreateDTO dto, Long ejecutorId) {
        // Unicidad (defensa en backend, no solo BD).
        if (usuarioRepository.existsByUsername(dto.getUsername()))
            throw new BusinessException("El usuario ya está registrado");
        if (usuarioRepository.existsByEmail(dto.getEmail()))
            throw new BusinessException("El correo ya está registrado");
        if (usuarioRepository.existsByDni(dto.getDni()))
            throw new BusinessException("El DNI ya está registrado");

        Rol rol = cargarRolSeleccionable(dto.getIdRol());

        Usuario u = new Usuario();
        u.setUsername(dto.getUsername().trim());
        u.setEmail(dto.getEmail().trim().toLowerCase());
        u.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        u.setNombres(dto.getNombres().trim());
        u.setApellidos(dto.getApellidos().trim());
        u.setDni(dto.getDni().trim());
        u.setTelefono(dto.getTelefono().trim());
        u.setRol(rol);
        u.setEstado(Constants.ESTADO_ACTIVO);
        u.setIntentosFallidos(0);

        Usuario guardado = usuarioRepository.save(u);

        auditoriaService.ok(ejecutorId, "INSERT", TABLA, guardado.getIdUsuario(),
                "Alta de usuario '" + guardado.getUsername() + "' (rol " + rol.getCodigo() + ")");
        return guardado.getIdUsuario();
    }

    @Override
    @Transactional
    public void actualizar(UsuarioUpdateDTO dto, Long ejecutorId) {
        Usuario u = usuarioRepository.findById(Objects.requireNonNull(dto.getIdUsuario(), "El ID de usuario no puede ser nulo"))
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        // Unicidad contra otros registros.
        if (usuarioRepository.existsByUsernameAndIdUsuarioNot(dto.getUsername(), u.getIdUsuario()))
            throw new BusinessException("El usuario ya está registrado");
        if (usuarioRepository.existsByEmailAndIdUsuarioNot(dto.getEmail(), u.getIdUsuario()))
            throw new BusinessException("El correo ya está registrado");
        if (usuarioRepository.existsByDniAndIdUsuarioNot(dto.getDni(), u.getIdUsuario()))
            throw new BusinessException("El DNI ya está registrado");

        // Regla dura: el ADMIN base no puede quedar INACTIVO.
        if (esAdminBase(u) && Constants.ESTADO_INACTIVO.equals(dto.getEstado())) {
            throw new BusinessException("El usuario Administrador base no puede ser desactivado");
        }

        Rol rol = cargarRolSeleccionable(dto.getIdRol());

        u.setUsername(dto.getUsername().trim());
        u.setEmail(dto.getEmail().trim().toLowerCase());
        u.setNombres(dto.getNombres().trim());
        u.setApellidos(dto.getApellidos().trim());
        u.setDni(dto.getDni().trim());
        u.setTelefono(dto.getTelefono().trim());
        u.setRol(rol);
        u.setEstado(dto.getEstado());

        // Password opcional: solo si viene.
        if (dto.tieneNuevaPassword()) {
            u.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }

        usuarioRepository.save(u);
        auditoriaService.ok(ejecutorId, "UPDATE", TABLA, u.getIdUsuario(),
                "Edición de usuario '" + u.getUsername() + "', estado=" + u.getEstado());
    }

    @Override
    @Transactional
    public void desactivar(Long idUsuario, Long ejecutorId) {
        Usuario u = usuarioRepository.findById(Objects.requireNonNull(idUsuario, "El ID de usuario no puede ser nulo"))
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        if (esAdminBase(u)) {
            auditoriaService.denegado(ejecutorId, "UPDATE", TABLA, u.getIdUsuario(),
                    "Intento de desactivar al Administrador base (denegado)");
            throw new BusinessException("El usuario Administrador base no puede ser desactivado");
        }
        if (Constants.ESTADO_INACTIVO.equals(u.getEstado())) {
            throw new BusinessException("El usuario ya está inactivo");
        }

        u.setEstado(Constants.ESTADO_INACTIVO); // Soft delete (nunca delete físico).
        usuarioRepository.save(u);
        auditoriaService.ok(ejecutorId, "UPDATE", TABLA, u.getIdUsuario(),
                "Baja lógica (INACTIVO) de usuario '" + u.getUsername() + "'");
    }

    @Override
    @Transactional(readOnly = true)
    public List<Rol> rolesSeleccionables() {
        return rolRepository.findSeleccionables();
    }

    /* ----------------------------- helpers ----------------------------- */

    private Rol cargarRolSeleccionable(Long idRol) {
        Rol rol = rolRepository.findById(Objects.requireNonNull(idRol, "El ID de rol no puede ser nulo"))
                .orElseThrow(() -> new BusinessException("El perfil seleccionado no existe"));
        if ("CLIENTE".equals(rol.getCodigo())) {
            throw new BusinessException("No se puede asignar el perfil CLIENTE a un usuario de backoffice");
        }
        if (!Constants.ESTADO_ACTIVO.equals(rol.getEstado())) {
            throw new BusinessException("El perfil seleccionado está inactivo");
        }
        return rol;
    }

    /** Admin base: rol ADMIN o id_usuario = 1 (regla de protección). */
    private boolean esAdminBase(Usuario u) {
        return "ADMIN".equals(u.getRol().getCodigo()) || Long.valueOf(1L).equals(u.getIdUsuario());
    }
}