package pe.com.zippers.web.dto;

import jakarta.validation.constraints.*;

/**
 * Datos para habilitar acceso web (onboarding digital) de un cliente POS.
 * El email aquí es el de LOGIN del usuario portal; puede o no coincidir
 * conceptualmente con el comercial, pero no debe existir ya en usuarios.
 */
public class AccesoWebDTO {

    @NotNull(message = "Cliente requerido")
    private Long idCliente;

    @NotBlank(message = "El correo de acceso es obligatorio")
    @Email(message = "Correo inválido")
    @Size(max = 120, message = "Máximo 120 caracteres")
    private String email;

    @NotBlank(message = "La contraseña temporal es obligatoria")
    @Size(min = 8, max = 64, message = "La contraseña debe tener entre 8 y 64 caracteres")
    private String password;

    public Long getIdCliente() { return idCliente; }
    public void setIdCliente(Long idCliente) { this.idCliente = idCliente; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email != null ? email.trim().toLowerCase() : null; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}