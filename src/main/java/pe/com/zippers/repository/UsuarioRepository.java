package pe.com.zippers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.com.zippers.domain.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @Query("SELECT u FROM Usuario u JOIN FETCH u.rol " +
           "WHERE u.username = :login OR u.email = :login")
    Optional<Usuario> findByUsernameOrEmail(@Param("login") String login);

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByDni(String dni);

    /** Métodos de validación ignorando mayúsculas/minúsculas (Fase de control estricto) */
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByUsernameIgnoreCase(String username);

    /** Unicidad en edición: existe otro usuario (distinto id) con ese valor. */
    boolean existsByUsernameAndIdUsuarioNot(String username, Long idUsuario);
    boolean existsByEmailAndIdUsuarioNot(String email, Long idUsuario);
    boolean existsByDniAndIdUsuarioNot(String dni, Long idUsuario);

    /**
     * Listado con búsqueda en vivo (username, email, nombres, apellidos, dni).
     * Trae el rol con fetch join para evitar N+1 al pintar la tabla.
     */
    @Query("SELECT u FROM Usuario u JOIN FETCH u.rol r " +
           "WHERE (:q IS NULL OR :q = '' " +
           "   OR LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "   OR LOWER(u.nombres) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "   OR LOWER(u.apellidos) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "   OR u.dni LIKE CONCAT('%', :q, '%')) " +
           "ORDER BY u.createdAt DESC")
    List<Usuario> buscar(@Param("q") String q);
}