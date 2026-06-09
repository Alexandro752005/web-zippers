package pe.com.zippers.service;

/**
 * Servicio central de auditoría. Toda mutación sensible y todo evento
 * de seguridad debe pasar por aquí. registro_id nunca es null (0 en masivas).
 */
public interface AuditoriaService {
    
    void registrarOk(String accion, String tabla, Long registroId, String descripcion);
    void registrarDenegado(String accion, String tabla, Long registroId, String descripcion);
    void registrarError(String accion, String tabla, Long registroId, String descripcion);

    /** Registro completo con resultado, registro afectado y referencia opcional. */
    void registrar(Long usuarioId, String accion, String tablaAfectada,
                   Long registroId, String resultado, String descripcion,
                   String referenciaTipo, Long referenciaId);

    /** Atajo para acción exitosa sobre un registro concreto. */
    void ok(Long usuarioId, String accion, String tabla, Long registroId, String descripcion);

    /** Atajo para acción denegada (sin permiso / política). */
    void denegado(Long usuarioId, String accion, String tabla, Long registroId, String descripcion);

    /** Atajo para error de operación. */
    void error(Long usuarioId, String accion, String tabla, Long registroId, String descripcion);
}