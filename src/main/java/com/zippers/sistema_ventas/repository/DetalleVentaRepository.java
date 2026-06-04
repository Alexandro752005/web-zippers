package com.zippers.sistema_ventas.repository;

import com.zippers.sistema_ventas.entity.DetalleVenta;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Integer> {
    
    @Query("SELECT dv.cantidad, dv.precio_unitario, pv.talla, pv.color, v.fecha_venta, c.nombre " +
           "FROM DetalleVenta dv " +
           "JOIN dv.variante pv " +
           "JOIN dv.venta v " +
           "LEFT JOIN v.cliente c " +
           "WHERE pv.producto.id_producto = :idProducto " +
           "ORDER BY v.fecha_venta DESC")
    List<Object[]> obtenerUltimasVentasPorProducto(@Param("idProducto") Integer idProducto, Pageable pageable);
}