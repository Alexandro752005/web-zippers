package pe.com.zippers.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/** Mapeo de la tabla `opciones` (menú jerárquico del backoffice). */
@Entity
@Table(name = "opciones")
@Getter
@Setter
@NoArgsConstructor
public class Opcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_opcion")
    private Long idOpcion;

    @Column(name = "id_opcion_padre")
    private Long idOpcionPadre;

    @Column(name = "codigo", nullable = false, length = 80, unique = true)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "ruta", nullable = false, length = 150, unique = true)
    private String ruta;

    @Column(name = "icono", length = 80)
    private String icono;

    @Column(name = "orden", nullable = false)
    private Integer orden = 0;

    @Column(name = "visible_menu", nullable = false)
    private Boolean visibleMenu = true;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "ACTIVO";

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}