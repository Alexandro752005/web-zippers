package pe.com.zippers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.com.zippers.domain.Producto;

import java.util.List;

/**
 * Acceso a la tabla puente productos_categorias mediante SQL nativo,
 * sin necesidad de entidad propia (PK compuesta).
 */
public interface ProductoCategoriaRepository extends JpaRepository<Producto, Long> {

    @Modifying
    @Query(value = "DELETE FROM productos_categorias WHERE id_producto = :idProducto", nativeQuery = true)
    void borrarPorProducto(@Param("idProducto") Long idProducto);

    @Modifying
    @Query(value = "INSERT INTO productos_categorias (id_producto, id_categoria) VALUES (:idProducto, :idCategoria)",
           nativeQuery = true)
    void vincular(@Param("idProducto") Long idProducto, @Param("idCategoria") Long idCategoria);

    @Query(value = "SELECT id_categoria FROM productos_categorias WHERE id_producto = :idProducto", nativeQuery = true)
    List<Long> categoriasDeProducto(@Param("idProducto") Long idProducto);
}