package pe.com.zippers.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.com.zippers.domain.Producto;

import java.math.BigDecimal;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    boolean existsBySkuIgnoreCase(String sku);
    boolean existsBySkuIgnoreCaseAndIdProductoNot(String sku, Long idProducto);

    @Query("""
           SELECT p FROM Producto p
           WHERE (:q IS NULL OR :q = ''
                  OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :q, '%'))
                  OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :q, '%'))
                  OR LOWER(COALESCE(p.marca,'')) LIKE LOWER(CONCAT('%', :q, '%')))
             AND (:estado IS NULL OR :estado = '' OR p.estado = :estado)
             AND (:marca IS NULL OR :marca = '' OR p.marca = :marca)
             AND (:temporada IS NULL OR :temporada = '' OR p.temporada = :temporada)
           """)
    Page<Producto> buscar(@Param("q") String q,
                          @Param("estado") String estado,
                          @Param("marca") String marca,
                          @Param("temporada") String temporada,
                          Pageable pageable);

    long countByEstado(String estado);

    @Query("SELECT COUNT(DISTINCT p.marca) FROM Producto p WHERE p.marca IS NOT NULL AND p.marca <> ''")
    long contarMarcas();

    @Query("SELECT COALESCE(AVG(p.precioVenta),0) FROM Producto p WHERE p.estado = 'ACTIVO'")
    BigDecimal precioVentaPromedio();
}