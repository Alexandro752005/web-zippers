package pe.com.zippers.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Long idProducto;

    @Column(name = "sku", length = 60, nullable = false)
    private String sku;

    @Column(name = "nombre", length = 150, nullable = false)
    private String nombre;

    @Column(name = "marca", length = 80)
    private String marca;

    @Column(name = "temporada", length = 40)
    private String temporada;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "precio_compra", precision = 12, scale = 2, nullable = false)
    private BigDecimal precioCompra;

    @Column(name = "precio_venta", precision = 12, scale = 2, nullable = false)
    private BigDecimal precioVenta;

    @Column(name = "oferta_porcentaje", precision = 5, scale = 2, nullable = false)
    private BigDecimal ofertaPorcentaje;

    @Column(name = "fecha_inicio_oferta")
    private LocalDateTime fechaInicioOferta;

    @Column(name = "fecha_fin_oferta")
    private LocalDateTime fechaFinOferta;

    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo;

    @Column(name = "estado", length = 20, nullable = false)
    private String estado;

    @Column(name = "visible_web", nullable = false)
    private Boolean visibleWeb;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    // Getters/Setters
    public Long getIdProducto() { return idProducto; }
    public void setIdProducto(Long idProducto) { this.idProducto = idProducto; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getTemporada() { return temporada; }
    public void setTemporada(String temporada) { this.temporada = temporada; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public BigDecimal getPrecioCompra() { return precioCompra; }
    public void setPrecioCompra(BigDecimal v) { this.precioCompra = v; }
    public BigDecimal getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(BigDecimal v) { this.precioVenta = v; }
    public BigDecimal getOfertaPorcentaje() { return ofertaPorcentaje; }
    public void setOfertaPorcentaje(BigDecimal v) { this.ofertaPorcentaje = v; }
    public LocalDateTime getFechaInicioOferta() { return fechaInicioOferta; }
    public void setFechaInicioOferta(LocalDateTime v) { this.fechaInicioOferta = v; }
    public LocalDateTime getFechaFinOferta() { return fechaFinOferta; }
    public void setFechaFinOferta(LocalDateTime v) { this.fechaFinOferta = v; }
    public Integer getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(Integer v) { this.stockMinimo = v; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Boolean getVisibleWeb() { return visibleWeb; }
    public void setVisibleWeb(Boolean v) { this.visibleWeb = v; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long v) { this.createdBy = v; }
    public Long getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Long v) { this.updatedBy = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}