package com.zippers.sistema_ventas.repository;

import com.zippers.sistema_ventas.entity.ProductoVariante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProductoVarianteRepository extends JpaRepository<ProductoVariante, Integer> {

    // CORRECCIÓN: @Query explícito para evitar que Spring se confunda con el guion bajo de id_producto
    @Query("SELECT pv FROM ProductoVariante pv WHERE pv.producto.id_producto = :idProducto")
    List<ProductoVariante> findByProductoId(@Param("idProducto") Integer idProducto);

    @Modifying
    @Transactional
    @Query("UPDATE ProductoVariante pv SET pv.stockActual = :nuevoStock WHERE pv.id_variante = :idVariante")
    void actualizarStock(@Param("idVariante") Integer idVariante, @Param("nuevoStock") Integer nuevoStock);
}