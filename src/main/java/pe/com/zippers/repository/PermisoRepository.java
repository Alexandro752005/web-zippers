package pe.com.zippers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.com.zippers.domain.Permiso;

import java.util.List;

public interface PermisoRepository extends JpaRepository<Permiso, Long> {

    @Query("SELECT p FROM Permiso p WHERE p.idRol = :idRol")
    List<Permiso> findByRol(@Param("idRol") Long idRol);

    /**
     * Permisos ACTIVOS de un rol. Base para construir authorities y menú.
     * Usado por UsuarioDetailsService y MenuServiceImpl.
     */
    @Query("SELECT p FROM Permiso p WHERE p.idRol = :idRol AND p.estado = 'ACTIVO'")
    List<Permiso> findActivosByRol(@Param("idRol") Long idRol);

    @Modifying
    @Query("DELETE FROM Permiso p WHERE p.idRol = :idRol")
    int deleteByRol(@Param("idRol") Long idRol);
}