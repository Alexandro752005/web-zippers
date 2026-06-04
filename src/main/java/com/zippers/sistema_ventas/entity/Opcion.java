package com.zippers.sistema_ventas.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "opciones")
public class Opcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_opcion;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 255)
    private String url;

    @Column(length = 50)
    private String icono;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "padre_id")
    private Opcion padre;
}