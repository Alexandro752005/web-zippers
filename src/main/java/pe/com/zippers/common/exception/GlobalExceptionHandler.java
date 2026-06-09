package pe.com.zippers.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pe.com.zippers.common.Constants;
import pe.com.zippers.common.util.HtmxUtils;

/**
 * Manejador global de excepciones.
 *
 * - HTMX: responde con header HX-Trigger (toast) y un fragmento mínimo,
 * evitando romper la navegación SPA.
 * - Navegación normal: muestra página de error o redirige al dashboard.
 * - Traduce violaciones de los triggers de unicidad cruzada (SQLSTATE 45000)
 * a mensajes de negocio claros.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /* ----------- Acceso directo a fragmento interno ----------- */
    @ExceptionHandler(AccessDirectoException.class)
    public String accesoDirecto(AccessDirectoException ex,
                                HttpServletResponse response) {
        return "redirect:" + Constants.DASHBOARD + "?modulo=" + ex.getModulo();
    }

    /* ----------------- Reglas de negocio ---------------------- */
    @ExceptionHandler(BusinessException.class)
    public Object negocio(BusinessException ex,
                          HttpServletRequest request,
                          HttpServletResponse response,
                          Model model) {
        if (HtmxUtils.isHtmx(request)) {
            HtmxUtils.trigger(response, crearToastJson("error", ex.getMessage()));
            response.setStatus(HttpStatus.OK.value()); // HTMX procesa el trigger igual.
            return "fragments/empty :: vacio";
        }
        model.addAttribute("mensaje", ex.getMessage());
        return "error/access-error";
    }

    /* ------------- Acceso denegado (permisos) ----------------- */
    @ExceptionHandler(AccessDeniedException.class)
    public Object denegado(AccessDeniedException ex,
                           HttpServletRequest request,
                           HttpServletResponse response,
                           Model model) {
        String msg = "No tiene permisos para realizar esta acción.";
        if (HtmxUtils.isHtmx(request)) {
            HtmxUtils.trigger(response, crearToastJson("error", msg));
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return "fragments/empty :: vacio";
        }
        model.addAttribute("mensaje", msg);
        return "error/access-error";
    }

    /* --- Integridad de datos / triggers SQLSTATE 45000 -------- */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Object integridad(DataIntegrityViolationException ex,
                             HttpServletRequest request,
                             HttpServletResponse response,
                             Model model) {
        String msg = traducirIntegridad(ex);
        if (HtmxUtils.isHtmx(request)) {
            HtmxUtils.trigger(response, crearToastJson("error", msg));
            response.setStatus(HttpStatus.OK.value());
            return "fragments/empty :: vacio";
        }
        model.addAttribute("mensaje", msg);
        return "error/access-error";
    }

    /* --------------------- Genérica --------------------------- */
    @ExceptionHandler(Exception.class)
    public Object generica(Exception ex,
                           HttpServletRequest request,
                           HttpServletResponse response,
                           Model model) {
        String msg = "Ocurrió un error inesperado. Intente nuevamente.";
        if (HtmxUtils.isHtmx(request)) {
            HtmxUtils.trigger(response, crearToastJson("error", msg));
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return "fragments/empty :: vacio";
        }
        model.addAttribute("mensaje", msg);
        return "error/access-error";
    }

    /** Traduce mensajes de los triggers de unicidad cruzada usuarios/clientes. */
    private String traducirIntegridad(DataIntegrityViolationException ex) {
        String causa = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage() : "";
        if (causa == null) {
            return "Violación de integridad de datos.";
        }
        if (causa.contains("DNI ya registrado")) {
            return "El DNI ya está registrado en el sistema.";
        }
        if (causa.contains("Email ya registrado")) {
            return "El correo ya está registrado en el sistema.";
        }
        if (causa.toLowerCase().contains("duplicate")) {
            return "El registro ya existe (dato duplicado).";
        }
        return "No se pudo completar la operación por integridad de datos.";
    }

    /** Helper para crear el JSON del evento de notificación (Toast) de HTMX.
     * UNIFICADO al evento "zpToast" con campos {tipo,msg}, igual que los controllers. */
    private String crearToastJson(String tipo, String mensaje) {
        String mensajeSeguro = mensaje != null ? mensaje.replace("\"", "\\\"") : "";
        return "{\"zpToast\": {\"tipo\": \"" + tipo + "\", \"msg\": \"" + mensajeSeguro + "\"}}";
    }
}