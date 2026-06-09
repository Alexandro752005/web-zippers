package pe.com.zippers.web.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Datos para editar un usuario. La contraseña es OPCIONAL:
 * si viene vacía/null, no se cambia (regla de UX solicitada).
 */
@Getter
@Setter
public class UsuarioUpdateDTO {

    @NotNull(message = "Identificador requerido")
    private Long idUsuario;

    @NotBlank(message = "El usuario es obligatorio")
    @Size(max = 60, message = "Máximo 60 caracteres")
    private String username;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Correo inválido")
    @Size(max = 120, message = "Máximo 120 caracteres")
    private String email;

    /** Opcional en edición. Si se completa, debe cumplir longitud. */
    @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
    private String password;

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 100, message = "Máximo 100 caracteres")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Máximo 100 caracteres")
    private String apellidos;

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "^[0-9]{8}$", message = "El DNI debe tener exactamente 8 dígitos")
    private String dni;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9]{9}$", message = "El teléfono debe tener exactamente 9 dígitos")
    private String telefono;

    @NotNull(message = "Debe seleccionar un perfil")
    private Long idRol;

    @NotBlank(message = "Estado requerido")
    @Pattern(regexp = "ACTIVO|INACTIVO", message = "Estado inválido")
    private String estado;

    /** true si la contraseña fue ingresada (para no sobreescribir con vacío). */
    public boolean tieneNuevaPassword() {
        return password != null && !password.isBlank();
    }
}