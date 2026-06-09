package pe.com.zippers.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CategoriaFormDTO {

    private Long idCategoria; // null en creación

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    @Size(max = 255, message = "La descripción no puede superar 255 caracteres")
    private String descripcion;

    @Pattern(regexp = "ACTIVO|INACTIVO", message = "Estado inválido")
    private String estado;

    public Long getIdCategoria() { return idCategoria; }
    public void setIdCategoria(Long idCategoria) { this.idCategoria = idCategoria; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) {
        // colapsa espacios múltiples y recorta; evita "nombres absurdos / solo espacios"
        this.nombre = nombre != null ? nombre.trim().replaceAll("\\s+", " ") : null;
    }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) {
        this.descripcion = (descripcion != null && !descripcion.isBlank())
                ? descripcion.trim() : null;
    }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}