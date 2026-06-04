package com.zippers.sistema_ventas.repository;

import com.zippers.sistema_ventas.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"categoria"})
    Page<Producto> findAll(@NonNull Pageable pageable);

    @NonNull
    @EntityGraph(attributePaths = {"categoria"})
    @Query("SELECT p FROM Producto p WHERE " +
            "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(p.codigo) LIKE LOWER(CONCAT('%', :term, '%'))")
    Page<Producto> buscarPorTermino(@Param("term") String term, @NonNull Pageable pageable);

    boolean existsByCodigo(String codigo);

    @Query("SELECT COUNT(p) = 0 FROM Producto p WHERE p.codigo = :codigo AND p.id_producto <> :id")
    boolean esCodigoUnicoExceptoId(@Param("codigo") String codigo, @Param("id") Integer id);

    @Query("SELECT p FROM Producto p WHERE p.estado = 'ACTIVO'")
    List<Producto> findAllActivos();

    // Método para inventario general
    @EntityGraph(attributePaths = {"categoria", "variantes"})
    @Query("SELECT DISTINCT p FROM Producto p WHERE p.estado = 'ACTIVO'")
    List<Producto> findAllActivosConVariantes();

    // ==========================================
    // CORRECCIÓN: Se cargan solo las variantes necesarias para el modal de inventario.
    // No es necesario traer fotos aquí y así evitamos MultipleBagFetchException.
    // ==========================================
    @EntityGraph(attributePaths = {"categoria", "variantes"})
    @Query("SELECT p FROM Producto p WHERE p.id_producto = :id")
    Optional<Producto> findByIdWithVariantesAndFotos(@Param("id") Integer id);

    @EntityGraph(attributePaths = {"categoria"})
    Page<Producto> findByEstado(@Param("estado") String estado, @NonNull Pageable pageable);
}