package pe.com.zippers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.com.zippers.domain.Cliente;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByDni(String dni);
    boolean existsByDniAndIdClienteNot(String dni, Long idCliente);

    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdClienteNot(String email, Long idCliente);

    @Query("""
           SELECT c FROM Cliente c
           WHERE (:q IS NULL OR :q = ''
                  OR c.dni LIKE CONCAT('%', :q, '%')
                  OR LOWER(c.nombres) LIKE LOWER(CONCAT('%', :q, '%'))
                  OR LOWER(c.apellidos) LIKE LOWER(CONCAT('%', :q, '%'))
                  OR LOWER(c.email) LIKE LOWER(CONCAT('%', :q, '%')))
           ORDER BY c.idCliente DESC
           """)
    List<Cliente> buscar(@Param("q") String q);

    // ============================================================
    // MÓDULO DE VENTAS Y COMPRAS (Consultas Nativas)
    // Se utiliza SQL nativo porque Venta/DetalleVenta no están 
    // mapeados como entidades JPA. Se proyecta a Object[].
    // ============================================================

    /** * Cantidad de compras COMPLETADAS del cliente. 
     * Se utiliza para mostrar en la columna "Compras" de la tabla principal.
     */
    @Query(value = "SELECT COUNT(*) FROM ventas WHERE id_cliente = :idCliente AND estado_venta = 'COMPLETADA'", 
           nativeQuery = true)
    long contarComprasCompletadas(@Param("idCliente") Long idCliente);

    /**
     * Cabeceras de las compras COMPLETADAS del cliente, más recientes primero.
     * Orden de columnas devueltas en Object[]:
     * [0] id_venta, [1] codigo_venta, [2] fecha_venta, [3] estado_venta,
     * [4] tipo_comprobante, [5] metodo_pago, [6] subtotal, 
     * [7] descuento_aplicado, [8] total
     */
    @Query(value = """
           SELECT v.id_venta, v.codigo_venta, v.fecha_venta, v.estado_venta,
                  v.tipo_comprobante, v.metodo_pago,
                  v.subtotal, v.descuento_aplicado, v.total
           FROM ventas v
           WHERE v.id_cliente = :idCliente
             AND v.estado_venta = 'COMPLETADA'
           ORDER BY v.fecha_venta DESC, v.id_venta DESC
           """, nativeQuery = true)
    List<Object[]> cabecerasComprasDe(@Param("idCliente") Long idCliente);

    /**
     * Detalle (líneas) de una venta concreta.
     * Orden de columnas devueltas en Object[]:
     * [0] id_detalle_venta, [1] producto_nombre_snapshot, [2] variante_snapshot,
     * [3] cantidad, [4] precio_unitario, [5] subtotal
     */
    @Query(value = """
           SELECT d.id_detalle_venta, d.producto_nombre_snapshot, d.variante_snapshot,
                  d.cantidad, d.precio_unitario, d.subtotal
           FROM detalle_venta d
           WHERE d.id_venta = :idVenta
           ORDER BY d.id_detalle_venta ASC
           """, nativeQuery = true)
    List<Object[]> detalleDeVenta(@Param("idVenta") Long idVenta);
}