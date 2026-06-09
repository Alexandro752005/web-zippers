package pe.com.zippers.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "productos_variantes")
public class ProductoVariante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_variante")
    private Long idVariante;

    @Column(name = "id_producto", nullable = false)
    private Long idProducto;

    @Column(name = "sku_variante", length = 80, nullable = false)
    private String skuVariante;

    @Column(name = "codigo_barra", length = 80)
    private String codigoBarra;

    @Column(name = "color", length = 50)
    private String color;

    @Column(name = "talla", length = 20)
    private String talla;

    @Column(name = "medida", length = 30)
    private String medida;

    @Column(name = "precio_compra", precision = 12, scale = 2, nullable = false)
    private BigDecimal precioCompra;

    @Column(name = "precio_venta", precision = 12, scale = 2, nullable = false)
    private BigDecimal precioVenta;

    @Column(name = "stock_actual", nullable = false)
    private Integer stockActual;

    @Column(name = "stock_reservado", nullable = false)
    private Integer stockReservado;

    @Column(name = "estado", length = 20, nullable = false)
    private String estado;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public Long getIdVariante() { return idVariante; }
    public void setIdVariante(Long v) { this.idVariante = v; }
    public Long getIdProducto() { return idProducto; }
    public void setIdProducto(Long v) { this.idProducto = v; }
    public String getSkuVariante() { return skuVariante; }
    public void setSkuVariante(String v) { this.skuVariante = v; }
    public String getCodigoBarra() { return codigoBarra; }
    public void setCodigoBarra(String v) { this.codigoBarra = v; }
    public String getColor() { return color; }
    public void setColor(String v) { this.color = v; }
    public String getTalla() { return talla; }
    public void setTalla(String v) { this.talla = v; }
    public String getMedida() { return medida; }
    public void setMedida(String v) { this.medida = v; }
    public BigDecimal getPrecioCompra() { return precioCompra; }
    public void setPrecioCompra(BigDecimal v) { this.precioCompra = v; }
    public BigDecimal getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(BigDecimal v) { this.precioVenta = v; }
    public Integer getStockActual() { return stockActual; }
    public void setStockActual(Integer v) { this.stockActual = v; }
    public Integer getStockReservado() { return stockReservado; }
    public void setStockReservado(Integer v) { this.stockReservado = v; }
    public String getEstado() { return estado; }
    public void setEstado(String v) { this.estado = v; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long v) { this.createdBy = v; }
    public Long getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Long v) { this.updatedBy = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}