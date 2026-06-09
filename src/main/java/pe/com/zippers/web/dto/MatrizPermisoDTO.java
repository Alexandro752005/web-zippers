package pe.com.zippers.web.dto;

/**
 * Una fila de la matriz de permisos: una opción (módulo) con sus 5 flags.
 * Se usa tanto para PRE-CARGAR (lectura) como para construir la vista.
 */
public class MatrizPermisoDTO {

    private Long idOpcion;
    private String codigoOpcion;
    private String nombreOpcion;

    private boolean ver;
    private boolean crear;
    private boolean editar;
    private boolean eliminar;
    private boolean exportar;

    public MatrizPermisoDTO() {
    }

    public MatrizPermisoDTO(Long idOpcion, String codigoOpcion, String nombreOpcion) {
        this.idOpcion = idOpcion;
        this.codigoOpcion = codigoOpcion;
        this.nombreOpcion = nombreOpcion;
    }

    public Long getIdOpcion() { return idOpcion; }
    public void setIdOpcion(Long idOpcion) { this.idOpcion = idOpcion; }

    public String getCodigoOpcion() { return codigoOpcion; }
    public void setCodigoOpcion(String codigoOpcion) { this.codigoOpcion = codigoOpcion; }

    public String getNombreOpcion() { return nombreOpcion; }
    public void setNombreOpcion(String nombreOpcion) { this.nombreOpcion = nombreOpcion; }

    public boolean isVer() { return ver; }
    public void setVer(boolean ver) { this.ver = ver; }

    public boolean isCrear() { return crear; }
    public void setCrear(boolean crear) { this.crear = crear; }

    public boolean isEditar() { return editar; }
    public void setEditar(boolean editar) { this.editar = editar; }

    public boolean isEliminar() { return eliminar; }
    public void setEliminar(boolean eliminar) { this.eliminar = eliminar; }

    public boolean isExportar() { return exportar; }
    public void setExportar(boolean exportar) { this.exportar = exportar; }
}