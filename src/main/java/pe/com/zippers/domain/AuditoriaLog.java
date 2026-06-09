package pe.com.zippers.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/** Mapeo de la tabla `auditoria_logs`. registro_id NUNCA es null (0 en masivas). */
@Entity
@Table(name = "auditoria_logs")
@Getter
@Setter
@NoArgsConstructor
public class AuditoriaLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_log")
    private Long idLog;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "accion", nullable = false, length = 80)
    private String accion;

    @Column(name = "tabla_afectada", nullable = false, length = 80)
    private String tablaAfectada;

    @Column(name = "registro_id", nullable = false)
    private Long registroId = 0L;

    @Column(name = "resultado", nullable = false, length = 20)
    private String resultado;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "ip", length = 45)
    private String ip;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "referencia_tipo", length = 60)
    private String referenciaTipo;

    @Column(name = "referencia_id")
    private Long referenciaId;

    @Column(name = "fecha_evento", nullable = false, insertable = false, updatable = false)
    private LocalDateTime fechaEvento;
}