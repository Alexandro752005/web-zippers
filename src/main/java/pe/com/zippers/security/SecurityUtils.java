package pe.com.zippers.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Utilidades para obtener el usuario autenticado en services/controllers. */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static UsuarioPrincipal actual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioPrincipal p) {
            return p;
        }
        return null;
    }

    /** id del usuario autenticado o null (para auditoría de eventos sin sesión). */
    public static Long idUsuarioActual() {
        UsuarioPrincipal p = actual();
        return p != null ? p.getIdUsuario() : null;
    }

    /** Método auxiliar utilizado por AuditoriaServiceImpl para obtener el ID de usuario o nulo. */
    public static Long getUsuarioIdOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof UsuarioPrincipal up) {
            return up.getIdUsuario();
        }
        return null;
    }
}