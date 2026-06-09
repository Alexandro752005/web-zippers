package pe.com.zippers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.com.zippers.domain.Rol;

import java.util.List;
import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Long> {

    Optional<Rol> findByCodigo(String codigo);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdRolNot(String nombre, Long idRol);

    /**
     * Roles seleccionables para asignar a USUARIOS de backoffice:
     * excluye CLIENTE y solo ACTIVOS. (Usado por módulo Usuarios)
     */
    @Query("""
           SELECT r FROM Rol r
           WHERE r.codigo <> 'CLIENTE'
             AND r.estado = 'ACTIVO'
           ORDER BY r.nombre ASC
           """)
    List<Rol> findSeleccionables();

    /**
     * Roles GESTIONABLES en el módulo Perfiles:
     * excluye CLIENTE (portal), incluye ACTIVOS e INACTIVOS para poder reactivarlos.
     */
    @Query("""
           SELECT r FROM Rol r
           WHERE r.codigo <> 'CLIENTE'
           ORDER BY r.idRol ASC
           """)
    List<Rol> findGestionables();

    /**
     * Búsqueda con filtro para la tabla de perfiles (HTMX live search).
     */
    @Query("""
           SELECT r FROM Rol r
           WHERE r.codigo <> 'CLIENTE'
             AND (:q IS NULL OR :q = ''
                  OR LOWER(r.nombre) LIKE LOWER(CONCAT('%', :q, '%'))
                  OR LOWER(r.codigo) LIKE LOWER(CONCAT('%', :q, '%'))
                  OR LOWER(COALESCE(r.descripcion,'')) LIKE LOWER(CONCAT('%', :q, '%')))
           ORDER BY r.idRol ASC
           """)
    List<Rol> buscarGestionables(@Param("q") String q);
}