package pe.com.zippers.web.dto;

import java.math.BigDecimal;

public class ProductoResumenDTO {
    private long totalProductos;
    private long productosActivos;
    private long marcasRegistradas;
    private BigDecimal precioVentaPromedio;

    public ProductoResumenDTO(long totalProductos, long productosActivos,
                              long marcasRegistradas, BigDecimal precioVentaPromedio) {
        this.totalProductos = totalProductos;
        this.productosActivos = productosActivos;
        this.marcasRegistradas = marcasRegistradas;
        this.precioVentaPromedio = precioVentaPromedio;
    }

    public long getTotalProductos() { return totalProductos; }
    public long getProductosActivos() { return productosActivos; }
    public long getMarcasRegistradas() { return marcasRegistradas; }
    public BigDecimal getPrecioVentaPromedio() { return precioVentaPromedio; }
}