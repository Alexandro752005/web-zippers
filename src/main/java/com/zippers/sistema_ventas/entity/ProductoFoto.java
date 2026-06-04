package com.zippers.sistema_ventas.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "producto_fotos")
public class ProductoFoto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_foto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto")
    private Producto producto;

    private String url_foto;

    @Column(name = "es_portada")
    private Boolean esPortada = false;
    
    @Column(name = "mostrar_web")
    private Boolean mostrarWeb = true;

    private String etiqueta;
    
    private Integer orden = 0;

    private LocalDateTime created_at = LocalDateTime.now();
}