package pe.com.zippers.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Cabecera de una compra (venta COMPLETADA) de un cliente,
 * para el modal "Compras" del módulo Clientes.
 */
public class CompraDTO {

    private Long idVenta;
    private String codigoVenta;
    private LocalDateTime fechaVenta;
    private String estadoVenta;
    private String tipoComprobante;
    private String metodoPago;
    private BigDecimal subtotal;
    private BigDecimal descuentoAplicado;
    private BigDecimal total;
    private List<CompraItemDTO> items = new ArrayList<>();

    public CompraDTO() { }

    public CompraDTO(Long idVenta, String codigoVenta, LocalDateTime fechaVenta,
                     String estadoVenta, String tipoComprobante, String metodoPago,
                     BigDecimal subtotal, BigDecimal descuentoAplicado, BigDecimal total) {
        this.idVenta = idVenta;
        this.codigoVenta = codigoVenta;
        this.fechaVenta = fechaVenta;
        this.estadoVenta = estadoVenta;
        this.tipoComprobante = tipoComprobante;
        this.metodoPago = metodoPago;
        this.subtotal = subtotal;
        this.descuentoAplicado = descuentoAplicado;
        this.total = total;
    }

    public Long getIdVenta() { return idVenta; }
    public void setIdVenta(Long idVenta) { this.idVenta = idVenta; }

    public String getCodigoVenta() { return codigoVenta; }
    public void setCodigoVenta(String codigoVenta) { this.codigoVenta = codigoVenta; }

    public LocalDateTime getFechaVenta() { return fechaVenta; }
    public void setFechaVenta(LocalDateTime fechaVenta) { this.fechaVenta = fechaVenta; }

    public String getEstadoVenta() { return estadoVenta; }
    public void setEstadoVenta(String estadoVenta) { this.estadoVenta = estadoVenta; }

    public String getTipoComprobante() { return tipoComprobante; }
    public void setTipoComprobante(String tipoComprobante) { this.tipoComprobante = tipoComprobante; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDescuentoAplicado() { return descuentoAplicado; }
    public void setDescuentoAplicado(BigDecimal descuentoAplicado) { this.descuentoAplicado = descuentoAplicado; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public List<CompraItemDTO> getItems() { return items; }
    public void setItems(List<CompraItemDTO> items) {
        this.items = (items != null) ? items : new ArrayList<>();
    }

    /** Cantidad de líneas (productos distintos) de la compra. */
    public int getCantidadItems() {
        return items != null ? items.size() : 0;
    }
}