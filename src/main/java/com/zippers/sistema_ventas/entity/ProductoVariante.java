package com.zippers.sistema_ventas.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "producto_variantes")
public class ProductoVariante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_variante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @Column(nullable = false, length = 10)
    private String talla;

    @Column(nullable = false, length = 50)
    private String color;
    
    @Column(name = "codigo_hex", length = 10)
    private String codigoHex;

    @Column(name = "medida_cm", nullable = false, precision = 10, scale = 2)
    private BigDecimal medidaCm = BigDecimal.ZERO;

    @Column(name = "precio_venta", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioVenta;

    @Column(name = "precio_costo", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioCosto = BigDecimal.ZERO;

    @Column(name = "stock_actual", nullable = false)
    private Integer stockActual = 0;
}