package pe.com.zippers.web.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductoFormDTO {

    private Long idProducto; // null en creación

    @NotBlank(message = "El SKU base es obligatorio")
    @Size(max = 60, message = "Máximo 60 caracteres")
    private String sku;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "Máximo 150 caracteres")
    private String nombre;

    @Size(max = 80, message = "Máximo 80 caracteres")
    private String marca;

    @Size(max = 40, message = "Máximo 40 caracteres")
    private String temporada;

    @Size(max = 500, message = "Máximo 500 caracteres")
    private String descripcion;

    @NotNull(message = "El precio de compra es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio de compra debe ser mayor a 0")
    private BigDecimal precioCompra;

    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio de venta debe ser mayor a 0")
    private BigDecimal precioVenta;

    @NotNull(message = "El stock mínimo es obligatorio")
    @Min(value = 10, message = "El stock mínimo debe ser al menos 10")
    private Integer stockMinimo = 10;

    @Pattern(regexp = "ACTIVO|INACTIVO|AGOTADO|SUSPENDIDO", message = "Estado inválido")
    private String estado = "ACTIVO";

    private Boolean visibleWeb = false;

    private List<Long> categoriaIds = new ArrayList<>();

    private List<VarianteDTO> variantes = new ArrayList<>();

    public Long getIdProducto() { return idProducto; }
    public void setIdProducto(Long v) { this.idProducto = v; }
    public String getSku() { return sku; }
    public void setSku(String v) { this.sku = v != null ? v.trim() : null; }
    public String getNombre() { return nombre; }
    public void setNombre(String v) { this.nombre = v != null ? v.trim() : null; }
    public String getMarca() { return marca; }
    public void setMarca(String v) { this.marca = (v != null && !v.isBlank()) ? v.trim() : null; }
    public String getTemporada() { return temporada; }
    public void setTemporada(String v) { this.temporada = (v != null && !v.isBlank()) ? v.trim() : null; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String v) { this.descripcion = (v != null && !v.isBlank()) ? v.trim() : null; }
    public BigDecimal getPrecioCompra() { return precioCompra; }
    public void setPrecioCompra(BigDecimal v) { this.precioCompra = v; }
    public BigDecimal getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(BigDecimal v) { this.precioVenta = v; }
    public Integer getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(Integer v) { this.stockMinimo = v; }
    public String getEstado() { return estado; }
    public void setEstado(String v) { this.estado = v; }
    public Boolean getVisibleWeb() { return visibleWeb; }
    public void setVisibleWeb(Boolean v) { this.visibleWeb = v; }
    public List<Long> getCategoriaIds() { return categoriaIds; }
    public void setCategoriaIds(List<Long> v) { this.categoriaIds = v != null ? v : new ArrayList<>(); }
    public List<VarianteDTO> getVariantes() { return variantes; }
    public void setVariantes(List<VarianteDTO> v) { this.variantes = v != null ? v : new ArrayList<>(); }
}