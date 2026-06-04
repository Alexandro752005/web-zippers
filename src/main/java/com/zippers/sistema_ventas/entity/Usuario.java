package com.zippers.sistema_ventas.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @Column(nullable = false, length = 150)
    private String nombre_completo;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password_hash;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fecha_creacion;

    @Column(nullable = false, columnDefinition = "ENUM('ACTIVO', 'INACTIVO') DEFAULT 'ACTIVO'")
    private String estado = "ACTIVO";

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private int intentos_fallidos = 0;

    @Column(nullable = true)
    private LocalDateTime bloqueado_hasta;

    // Asigna automáticamente la fecha de creación al registrar un nuevo usuario
    @PrePersist
    protected void onCreate() {
        if (this.fecha_creacion == null) {
            this.fecha_creacion = LocalDateTime.now();
        }
    }
}