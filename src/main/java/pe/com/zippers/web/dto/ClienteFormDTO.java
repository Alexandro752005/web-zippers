package pe.com.zippers.web.dto;

import jakarta.validation.constraints.*;

public class ClienteFormDTO {

    private Long idCliente; // null en creación

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "^[0-9]{8}$", message = "El DNI debe tener exactamente 8 dígitos")
    private String dni;

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 100, message = "Máximo 100 caracteres")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Máximo 100 caracteres")
    private String apellidos;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email inválido")
    @Size(max = 120, message = "Máximo 120 caracteres")
    private String email;

    // Teléfono Perú: 9 dígitos exactos. Opcional, pero si viene debe cumplir.
    @Pattern(regexp = "^$|^[0-9]{9}$", message = "El teléfono debe tener exactamente 9 dígitos")
    private String telefono;

    @Size(max = 255, message = "Máximo 255 caracteres")
    private String direccion;

    @Size(max = 255, message = "Máximo 255 caracteres")
    private String referenciaDireccion;

    @Pattern(regexp = "ACTIVO|INACTIVO", message = "Estado inválido")
    private String estado;

    public Long getIdCliente() { return idCliente; }
    public void setIdCliente(Long idCliente) { this.idCliente = idCliente; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni != null ? dni.trim() : null; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres != null ? nombres.trim() : null; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos != null ? apellidos.trim() : null; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email != null ? email.trim().toLowerCase() : null; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) {
        this.telefono = (telefono != null && !telefono.isBlank()) ? telefono.trim() : null;
    }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) {
        this.direccion = (direccion != null && !direccion.isBlank()) ? direccion.trim() : null;
    }

    public String getReferenciaDireccion() { return referenciaDireccion; }
    public void setReferenciaDireccion(String r) {
        this.referenciaDireccion = (r != null && !r.isBlank()) ? r.trim() : null;
    }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}