package pe.com.zippers.web.dto;

import java.math.BigDecimal;

/**
 * Línea de detalle de una compra (snapshot histórico de detalle_venta).
 * Usa los nombres "snapshot" para no depender del estado actual del producto.
 */
public class CompraItemDTO {

    private Long idDetalle;
    private String productoNombre;   // producto_nombre_snapshot
    private String variante;         // variante_snapshot (puede ser null)
    private Integer cantidad;        // entero >= 1
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    public CompraItemDTO() { }

    public CompraItemDTO(Long idDetalle, String productoNombre, String variante,
                         Integer cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {
        this.idDetalle = idDetalle;
        this.productoNombre = productoNombre;
        this.variante = variante;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    public Long getIdDetalle() { return idDetalle; }
    public void setIdDetalle(Long idDetalle) { this.idDetalle = idDetalle; }

    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }

    public String getVariante() { return variante; }
    public void setVariante(String variante) { this.variante = variante; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}