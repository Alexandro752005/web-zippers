package pe.com.zippers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.com.zippers.domain.ProductoFoto;

import java.util.List;
import java.util.Optional;

public interface ProductoFotoRepository extends JpaRepository<ProductoFoto, Long> {

    @Query("SELECT f FROM ProductoFoto f WHERE f.idProducto = :idProducto AND f.estado = 'ACTIVO' ORDER BY f.orden ASC")
    List<ProductoFoto> findActivasByProducto(@Param("idProducto") Long idProducto);

    long countByIdProductoAndEstado(Long idProducto, String estado);

    @Query("SELECT f FROM ProductoFoto f WHERE f.idProducto = :idProducto AND f.esPrincipal = true AND f.estado = 'ACTIVO'")
    Optional<ProductoFoto> findPrincipal(@Param("idProducto") Long idProducto);
}