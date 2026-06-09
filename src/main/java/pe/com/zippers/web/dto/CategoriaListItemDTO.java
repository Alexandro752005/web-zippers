package pe.com.zippers.web.dto;

public class CategoriaListItemDTO {

    private Long idCategoria;
    private String nombre;
    private String descripcion;
    private String estado;

    public CategoriaListItemDTO() { }

    public CategoriaListItemDTO(Long idCategoria, String nombre, String descripcion, String estado) {
        this.idCategoria = idCategoria;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.estado = estado;
    }

    public Long getIdCategoria() { return idCategoria; }
    public void setIdCategoria(Long idCategoria) { this.idCategoria = idCategoria; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}