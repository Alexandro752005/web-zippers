package pe.com.zippers.web.dto;

import java.math.BigDecimal;

public class ProductoListItemDTO {
    private Long idProducto;
    private String sku;
    private String nombre;
    private String descripcion;
    private String marca;
    private String temporada;
    private BigDecimal precioVenta;
    private String estado;
    private boolean visibleWeb;
    private long totalVariantes;
    private String fotoPrincipal;

    public ProductoListItemDTO() { }

    public Long getIdProducto() { return idProducto; }
    public void setIdProducto(Long v) { this.idProducto = v; }
    public String getSku() { return sku; }
    public void setSku(String v) { this.sku = v; }
    public String getNombre() { return nombre; }
    public void setNombre(String v) { this.nombre = v; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String v) { this.descripcion = v; }
    public String getMarca() { return marca; }
    public void setMarca(String v) { this.marca = v; }
    public String getTemporada() { return temporada; }
    public void setTemporada(String v) { this.temporada = v; }
    public BigDecimal getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(BigDecimal v) { this.precioVenta = v; }
    public String getEstado() { return estado; }
    public void setEstado(String v) { this.estado = v; }
    public boolean isVisibleWeb() { return visibleWeb; }
    public void setVisibleWeb(boolean v) { this.visibleWeb = v; }
    public long getTotalVariantes() { return totalVariantes; }
    public void setTotalVariantes(long v) { this.totalVariantes = v; }
    public String getFotoPrincipal() { return fotoPrincipal; }
    public void setFotoPrincipal(String v) { this.fotoPrincipal = v; }
}