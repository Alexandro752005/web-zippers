package pe.com.zippers.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para crear/editar el perfil (datos básicos del rol).
 * No incluye la matriz de permisos (se gestiona aparte).
 */
public class PerfilFormDTO {

    private Long idRol; // null en creación

    @NotBlank(message = "El nombre del perfil es obligatorio")
    @Size(max = 50, message = "El nombre no puede superar 50 caracteres")
    private String nombre;

    @Size(max = 255, message = "La descripción no puede superar 255 caracteres")
    private String descripcion;

    /**
     * Solo se usa en edición. En creación se fuerza ACTIVO.
     * Catálogo cerrado: ACTIVO | INACTIVO.
     */
    @Pattern(regexp = "ACTIVO|INACTIVO", message = "Estado inválido")
    private String estado;

    public Long getIdRol() { return idRol; }
    public void setIdRol(Long idRol) { this.idRol = idRol; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) {
        this.nombre = nombre != null ? nombre.trim() : null;
    }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion != null ? descripcion.trim() : null;
    }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}