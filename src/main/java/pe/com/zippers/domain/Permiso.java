package pe.com.zippers.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/** Mapeo de la tabla `permisos` (matriz rol x opción con flags de acción). */
@Entity
@Table(name = "permisos")
@Getter
@Setter
@NoArgsConstructor
public class Permiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permiso")
    private Long idPermiso;

    @Column(name = "id_rol", nullable = false)
    private Long idRol;

    @Column(name = "id_opcion", nullable = false)
    private Long idOpcion;

    @Column(name = "permiso_ver", nullable = false)
    private Boolean permisoVer = false;

    @Column(name = "permiso_crear", nullable = false)
    private Boolean permisoCrear = false;

    @Column(name = "permiso_editar", nullable = false)
    private Boolean permisoEditar = false;

    @Column(name = "permiso_eliminar", nullable = false)
    private Boolean permisoEliminar = false;

    @Column(name = "permiso_exportar", nullable = false)
    private Boolean permisoExportar = false;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "ACTIVO";

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}