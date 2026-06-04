package com.zippers.sistema_ventas.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "auditoria_logs")
public class AuditoriaLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_log;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario; // Quién hizo la acción

    @Column(columnDefinition = "ENUM('INSERT', 'UPDATE', 'DELETE')", nullable = false)
    private String accion;
    
    @Column(nullable = false, length = 50)
    private String tabla_afectada;

    @Column(nullable = false)
    private Integer registro_id; // ID del registro que se alteró

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private LocalDateTime fecha_accion = LocalDateTime.now();
}