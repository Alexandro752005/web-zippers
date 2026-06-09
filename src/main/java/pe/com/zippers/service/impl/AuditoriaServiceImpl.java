package pe.com.zippers.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pe.com.zippers.domain.AuditoriaLog;
import pe.com.zippers.repository.AuditoriaLogRepository;
import pe.com.zippers.security.SecurityUtils;
import pe.com.zippers.service.AuditoriaService;

import java.time.LocalDateTime;

@Service
public class AuditoriaServiceImpl implements AuditoriaService {

    private final AuditoriaLogRepository auditoriaLogRepository;

    public AuditoriaServiceImpl(AuditoriaLogRepository auditoriaLogRepository) {
        this.auditoriaLogRepository = auditoriaLogRepository;
    }

    /* ---------------- Métodos con obtención de usuario automática ---------------- */

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarOk(String accion, String tabla, Long registroId, String descripcion) {
        guardar(null, accion, tabla, registroId, "OK", descripcion, null, null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarDenegado(String accion, String tabla, Long registroId, String descripcion) {
        guardar(null, accion, tabla, registroId, "DENEGADO", descripcion, null, null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarError(String accion, String tabla, Long registroId, String descripcion) {
        guardar(null, accion, tabla, registroId, "ERROR", descripcion, null, null);
    }

    /* ---------------- Métodos con usuario explícito (Fase 2 / Módulos) ---------------- */

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(Long usuarioId, String accion, String tablaAfectada,
                           Long registroId, String resultado, String descripcion,
                           String referenciaTipo, Long referenciaId) {
        guardar(usuarioId, accion, tablaAfectada, registroId, resultado, descripcion, referenciaTipo, referenciaId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void ok(Long usuarioId, String accion, String tabla, Long registroId, String descripcion) {
        guardar(usuarioId, accion, tabla, registroId, "OK", descripcion, null, null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void denegado(Long usuarioId, String accion, String tabla, Long registroId, String descripcion) {
        guardar(usuarioId, accion, tabla, registroId, "DENEGADO", descripcion, null, null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void error(Long usuarioId, String accion, String tabla, Long registroId, String descripcion) {
        guardar(usuarioId, accion, tabla, registroId, "ERROR", descripcion, null, null);
    }

    /* ---------------- Core de persistencia y extracción de red ---------------- */

    private void guardar(Long usuarioId, String accion, String tabla, Long registroId,
                         String resultado, String descripcion, String referenciaTipo, Long referenciaId) {
        AuditoriaLog log = new AuditoriaLog();
        
        // Si viene un usuarioId explícito de los servicios se usa, sino, se rescata de la sesión actual
        log.setUsuarioId(usuarioId != null ? usuarioId : SecurityUtils.getUsuarioIdOrNull());
        log.setAccion(accion);
        log.setTablaAfectada(tabla);
        
        // REGLA DE NEGOCIO REAL: registro_id NUNCA nulo. En acciones masivas o transversales se guarda 0.
        log.setRegistroId(registroId != null ? registroId : 0L);
        log.setResultado(resultado);
        log.setDescripcion(descripcion);
        log.setIp(obtenerIp());
        log.setUserAgent(obtenerUserAgent());
        
        // Campos opcionales para trazas complejas de auditoría cruzada
        log.setReferenciaTipo(referenciaTipo);
        log.setReferenciaId(referenciaId);
        
        log.setFechaEvento(LocalDateTime.now());
        auditoriaLogRepository.save(log);
    }

    private HttpServletRequest currentRequest() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes sra) {
            return sra.getRequest();
        }
        return null;
    }

    private String obtenerIp() {
        HttpServletRequest req = currentRequest();
        if (req == null) return null;
        String xf = req.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }

    private String obtenerUserAgent() {
        HttpServletRequest req = currentRequest();
        return req != null ? req.getHeader("User-Agent") : null;
    }
}