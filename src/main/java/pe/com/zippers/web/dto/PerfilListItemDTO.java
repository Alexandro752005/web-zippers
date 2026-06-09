package pe.com.zippers.web.dto;

public class PerfilListItemDTO {

    private Long idRol;
    private String codigo;
    private String nombre;
    private String descripcion;
    private String estado;
    private boolean base; // true si es Administrador Base (no desactivable)

    public PerfilListItemDTO() {
    }

    public PerfilListItemDTO(Long idRol, String codigo, String nombre,
                             String descripcion, String estado, boolean base) {
        this.idRol = idRol;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.estado = estado;
        this.base = base;
    }

    public Long getIdRol() { return idRol; }
    public void setIdRol(Long idRol) { this.idRol = idRol; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public boolean isBase() { return base; }
    public void setBase(boolean base) { this.base = base; }
}