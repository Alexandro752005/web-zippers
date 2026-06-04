package com.zippers.sistema_ventas.repository;

import com.zippers.sistema_ventas.entity.ProductoFoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoFotoRepository extends JpaRepository<ProductoFoto, Integer> {

    @Query("SELECT f FROM ProductoFoto f WHERE f.producto.id_producto = :idProducto")
    List<ProductoFoto> findByProductoIdProducto(@Param("idProducto") Integer idProducto);

    @Query("SELECT COUNT(f) FROM ProductoFoto f WHERE f.producto.id_producto = :idProducto")
    long countByProductoIdProducto(@Param("idProducto") Integer idProducto);

    @Modifying
    @Query("UPDATE ProductoFoto f SET f.esPortada = false WHERE f.producto.id_producto = :idProducto")
    void desmarcarPortadas(@Param("idProducto") Integer idProducto);
}