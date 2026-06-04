package com.zippers.sistema_ventas.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "productos")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_producto;

    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, unique = true)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    // --- NUEVOS CAMPOS ---
    private String marca;

    @Column(columnDefinition = "ENUM('Otoño/Invierno','Primavera/Verano','Atemporal') DEFAULT 'Atemporal'")
    private String temporada = "Atemporal";

    private String material_principal;
    
    private LocalDateTime fecha_inicio_oferta;
    private LocalDateTime fecha_fin_oferta;
    // ----------------------

    @Column(name = "precio_costo", precision = 10, scale = 2)
    private BigDecimal precio_costo;
    
    private Double precio_venta = 0.0;
    private Double oferta = 0.0;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer stock = 0;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 10")
    private Integer stock_minimo = 10;

    private Double contorno_pecho;

    private String talla;

    private String genero;

    @Column(columnDefinition = "ENUM('ACTIVO','INACTIVO') DEFAULT 'ACTIVO'")
    private String estado = "ACTIVO";

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductoFoto> fotos;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductoVariante> variantes;
}