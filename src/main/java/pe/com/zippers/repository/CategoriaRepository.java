package pe.com.zippers.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.com.zippers.domain.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCaseAndIdCategoriaNot(String nombre, Long idCategoria);

    /**
     * Listado paginado con búsqueda por nombre/descripcion/estado.
     * El Pageable controla page, size y sort (orden por columnas).
     */
    @Query("""
           SELECT c FROM Categoria c
           WHERE (:q IS NULL OR :q = ''
                  OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :q, '%'))
                  OR LOWER(COALESCE(c.descripcion,'')) LIKE LOWER(CONCAT('%', :q, '%'))
                  OR LOWER(c.estado) LIKE LOWER(CONCAT('%', :q, '%')))
           """)
    Page<Categoria> buscar(@Param("q") String q, Pageable pageable);

    /**
     * Cuenta productos ACTIVOS asociados a la categoría (validación de
     * dependencias antes de desactivar). Usa la tabla puente productos_categorias.
     */
    @Query(value = """
           SELECT COUNT(*) FROM productos_categorias pc
           JOIN productos p ON p.id_producto = pc.id_producto
           WHERE pc.id_categoria = :idCategoria
             AND p.estado = 'ACTIVO'
           """, nativeQuery = true)
    long contarProductosActivos(@Param("idCategoria") Long idCategoria);
}