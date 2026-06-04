package com.zippers.sistema_ventas.entity;

import java.io.Serializable;
import java.util.Objects;

public class PermisoId implements Serializable {
    private Integer id_rol;
    private Integer id_opcion;

    public PermisoId() {}

    public PermisoId(Integer id_rol, Integer id_opcion) {
        this.id_rol = id_rol;
        this.id_opcion = id_opcion;
    }

    // equals y hashCode (obligatorios)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PermisoId)) return false;
        PermisoId that = (PermisoId) o;
        return Objects.equals(id_rol, that.id_rol) &&
            Objects.equals(id_opcion, that.id_opcion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_rol, id_opcion);
    }
}