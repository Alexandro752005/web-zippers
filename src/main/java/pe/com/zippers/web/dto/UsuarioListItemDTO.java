package pe.com.zippers.web.dto;

import lombok.Getter;
import java.time.LocalDateTime;

/** Proyección de solo lectura para el listado de usuarios. */
@Getter
public class UsuarioListItemDTO {
    private final Long idUsuario;
    private final String username;
    private final String email;
    private final String nombreCompleto;
    private final String dni;
    private final String telefono;
    private final String rolNombre;
    private final String rolCodigo;
    private final String estado;
    private final LocalDateTime createdAt;

    public UsuarioListItemDTO(Long idUsuario, String username, String email,
                              String nombres, String apellidos, String dni, String telefono,
                              String rolNombre, String rolCodigo, String estado,
                              LocalDateTime createdAt) {
        this.idUsuario = idUsuario;
        this.username = username;
        this.email = email;
        this.nombreCompleto = nombres + " " + apellidos;
        this.dni = dni;
        this.telefono = telefono;
        this.rolNombre = rolNombre;
        this.rolCodigo = rolCodigo;
        this.estado = estado;
        this.createdAt = createdAt;
    }
}