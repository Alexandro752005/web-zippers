package com.zippers.sistema_ventas.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "permisos")
@IdClass(PermisoId.class)
public class Permiso {

    @Id
    @Column(name = "id_rol")
    private Integer id_rol;

    @Id
    @Column(name = "id_opcion")
    private Integer id_opcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rol", insertable = false, updatable = false)
    private Rol rol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_opcion", insertable = false, updatable = false)
    private Opcion opcion;

    @Column(nullable = false)
    private Boolean permiso_crear = false;

    @Column(nullable = false)
    private Boolean permiso_leer = false;       // NUEVO

    @Column(nullable = false)
    private Boolean permiso_editar = false;

    @Column(nullable = false)
    private Boolean permiso_eliminar = false;

    @Column(nullable = false)
    private Boolean permiso_exportar = false;   // NUEVO
}