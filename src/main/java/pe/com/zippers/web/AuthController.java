package pe.com.zippers.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.com.zippers.common.Constants;

/**
 * Vistas de autenticación del backoffice.
 *
 * El mensaje de error se arma a partir de atributos que el listener de
 * autenticación deja en la sesión:
 *   - zp_intentos_restantes : intentos que quedan antes del bloqueo
 *   - zp_bloqueado          : true si la cuenta quedó bloqueada
 * Siempre es genérico (no revela si el usuario existe).
 */
@Controller
@RequiredArgsConstructor
public class AuthController {

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String logout,
                        HttpServletRequest request,
                        Model model) {

        if (error != null) {
            HttpSession session = request.getSession(false);
            Boolean bloqueado = session != null
                    ? (Boolean) session.getAttribute("zp_bloqueado") : null;
            Integer restantes = session != null
                    ? (Integer) session.getAttribute("zp_intentos_restantes") : null;

            String mensaje;
            if (Boolean.TRUE.equals(bloqueado)) {
                mensaje = "Cuenta bloqueada por " + Constants.MINUTOS_BLOQUEO
                        + " minutos debido a múltiples intentos fallidos.";
            } else if (restantes != null && restantes >= 0) {
                mensaje = "Credenciales incorrectas. Verifica tu usuario/correo y contraseña. "
                        + "Te quedan " + restantes + " intentos.";
            } else {
                // Caso genérico (usuario inexistente u otro): no revelar detalles.
                mensaje = "Credenciales incorrectas. Verifica tu usuario/correo y contraseña.";
            }
            model.addAttribute("mensaje", mensaje);
            model.addAttribute("tipoMensaje", "error");

            // Limpiar marcas de la sesión para que no persistan.
            if (session != null) {
                session.removeAttribute("zp_bloqueado");
                session.removeAttribute("zp_intentos_restantes");
            }
        } else if (logout != null) {
            model.addAttribute("mensaje", "Sesión cerrada correctamente.");
            model.addAttribute("tipoMensaje", "ok");
        }
        return "auth/login";
    }
}