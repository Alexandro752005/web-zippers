package pe.com.zippers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.com.zippers.domain.Opcion;

import java.util.List;

public interface OpcionRepository extends JpaRepository<Opcion, Long> {

    /**
     * Opciones (módulos) activas y visibles en menú, ordenadas.
     * Base para construir la matriz de permisos.
     */
    @Query("""
           SELECT o FROM Opcion o
           WHERE o.estado = 'ACTIVO'
             AND o.visibleMenu = true
           ORDER BY o.orden ASC, o.nombre ASC
           """)
    List<Opcion> findVisiblesOrdenadas();
}