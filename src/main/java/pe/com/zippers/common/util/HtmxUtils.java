package pe.com.zippers.common.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Utilidades para respuestas HTMX. El evento HX-Trigger debe viajar en el
 * HEADER de la respuesta para que HTMX lo dispare en el cliente.
 */
public final class HtmxUtils {

    private HtmxUtils() { }

    /** Escribe el header HX-Trigger directamente en la respuesta. */
    public static void trigger(HttpServletResponse response, String eventJson) {
        if (response != null && eventJson != null) {
            response.setHeader("HX-Trigger", eventJson);
        }
    }

    /**
     * Variante por conveniencia: obtiene la HttpServletResponse del contexto
     * actual a partir del request. Permite a los controllers llamar
     * HtmxUtils.trigger(request, json) sin inyectar la response.
     */
    public static void trigger(HttpServletRequest request, String eventJson) {
        HttpServletResponse response = currentResponse();
        trigger(response, eventJson);
    }

    /** Detecta si la petición proviene de HTMX. */
    public static boolean isHtmx(HttpServletRequest request) {
        return request != null && "true".equalsIgnoreCase(request.getHeader("HX-Request"));
    }

    private static HttpServletResponse currentResponse() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes sra) {
            return sra.getResponse();
        }
        return null;
    }
}