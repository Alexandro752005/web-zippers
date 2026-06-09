package pe.com.zippers.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import pe.com.zippers.common.Constants;
import pe.com.zippers.common.util.HtmxUtils;

/**
 * Guardia anti-acceso directo a fragmentos internos de módulos.
 *
 * Regla (política dura del proyecto):
 * Si la URL es de módulo interno (/modulo/**) y:
 * - NO es HX-Request, y
 * - NO viene marcada como forward interno del dispatcher,
 * entonces se redirige a /dashboard?modulo=X para entrar por el flujo correcto.
 *
 * Defensa en profundidad: complementa (no reemplaza) a Spring Security.
 */
@Component
public class HtmxGuardInterceptor implements HandlerInterceptor {

    private static final String PREFIJO_MODULO = "/modulo/";

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {

        final String uri = request.getRequestURI();

        // Solo aplica a rutas internas de módulo.
        if (uri == null || !uri.contains(PREFIJO_MODULO)) {
            return true;
        }

        // CORRECCIÓN: Se cambió 'esHtmx' por 'isHtmx' para sincronizarse con HtmxUtils.java
        final boolean esHtmx = HtmxUtils.isHtmx(request);
        final boolean esForwardInterno =
                Boolean.TRUE.equals(request.getAttribute(Constants.ATTR_FORWARD_INTERNO));

        if (esHtmx || esForwardInterno) {
            return true; // Acceso legítimo.
        }

        // Acceso directo no permitido: redirigir al dashboard con el módulo seleccionado.
        final String modulo = extraerModulo(uri);
        response.sendRedirect(request.getContextPath()
                + Constants.DASHBOARD + "?modulo=" + modulo);
        return false;
    }

    /** Extrae el nombre del módulo desde /modulo/{nombre}/... */
    private String extraerModulo(String uri) {
        int idx = uri.indexOf(PREFIJO_MODULO);
        String resto = uri.substring(idx + PREFIJO_MODULO.length());
        int slash = resto.indexOf('/');
        String modulo = (slash >= 0) ? resto.substring(0, slash) : resto;
        return modulo.isBlank() ? "home" : modulo;
    }
}