package pe.com.zippers.web.dto;

import java.math.BigDecimal;

public class VarianteDTO {
    private Long idVariante;       // null si es nueva
    private String skuVariante;
    private String color;
    private String talla;
    private BigDecimal precioCompra;
    private BigDecimal precioVenta;
    private Integer stockActual;
    private String estado;         // ACTIVO | INACTIVO

    public Long getIdVariante() { return idVariante; }
    public void setIdVariante(Long v) { this.idVariante = v; }
    public String getSkuVariante() { return skuVariante; }
    public void setSkuVariante(String v) { this.skuVariante = v != null ? v.trim() : null; }
    public String getColor() { return color; }
    public void setColor(String v) { this.color = v; }
    public String getTalla() { return talla; }
    public void setTalla(String v) { this.talla = v; }
    public BigDecimal getPrecioCompra() { return precioCompra; }
    public void setPrecioCompra(BigDecimal v) { this.precioCompra = v; }
    public BigDecimal getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(BigDecimal v) { this.precioVenta = v; }
    public Integer getStockActual() { return stockActual; }
    public void setStockActual(Integer v) { this.stockActual = v; }
    public String getEstado() { return estado; }
    public void setEstado(String v) { this.estado = v; }
}