package com.zippers.sistema_ventas.repository;

import com.zippers.sistema_ventas.entity.Permiso;
import com.zippers.sistema_ventas.entity.PermisoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PermisoRepository extends JpaRepository<Permiso, PermisoId> {

    // Consulta explícita porque el campo en la entidad es id_rol (guion bajo)
    @Query("SELECT p FROM Permiso p WHERE p.id_rol = :idRol")
    List<Permiso> findByIdRol(@Param("idRol") Integer idRol);

    @Modifying
    @Transactional
    @Query("DELETE FROM Permiso p WHERE p.id_rol = :idRol")
    void deleteByIdRol(@Param("idRol") Integer idRol);
}