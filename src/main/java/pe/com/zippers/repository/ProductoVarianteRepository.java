package pe.com.zippers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.com.zippers.domain.ProductoVariante;

import java.util.List;

public interface ProductoVarianteRepository extends JpaRepository<ProductoVariante, Long> {

    @Query("SELECT v FROM ProductoVariante v WHERE v.idProducto = :idProducto ORDER BY v.idVariante ASC")
    List<ProductoVariante> findByProducto(@Param("idProducto") Long idProducto);

    boolean existsBySkuVarianteIgnoreCase(String skuVariante);
    boolean existsBySkuVarianteIgnoreCaseAndIdVarianteNot(String skuVariante, Long idVariante);

    @Query("SELECT COUNT(v) FROM ProductoVariante v WHERE v.idProducto = :idProducto AND v.estado = 'ACTIVO'")
    long contarActivasPorProducto(@Param("idProducto") Long idProducto);
}