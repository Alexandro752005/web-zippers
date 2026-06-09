package pe.com.zippers.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pe.com.zippers.common.Constants;
import pe.com.zippers.repository.UsuarioRepository;
import pe.com.zippers.service.AuditoriaService;

import java.time.LocalDateTime;

/**
 * Escucha eventos de autenticación para gestionar:
 *  - Login exitoso: reset de intentos, desbloqueo y registro de último acceso.
 *  - Login fallido: incremento de intentos, bloqueo de 5 min al alcanzar el máximo,
 *    auditoría estructurada y datos para el mensaje de UI (intentos restantes / bloqueo).
 *
 * El bloqueo aplica a cualquier usuario (incluido admin). El desbloqueo automático
 * se resuelve aquí (al expirar) y en isAccountNonLocked() del principal.
 */
@Component
@RequiredArgsConstructor
public class AuthAuditListener {

    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;

    @EventListener
    @Transactional
    public void onSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        usuarioRepository.findByUsername(username).ifPresent(u -> {
            u.setIntentosFallidos(0);
            u.setBloqueadoHasta(null);
            if (Constants.ESTADO_BLOQUEADO.equals(u.getEstado())) {
                u.setEstado(Constants.ESTADO_ACTIVO);
            }
            u.setUltimoAcceso(LocalDateTime.now());
            usuarioRepository.save(u);
            auditoriaService.ok(u.getIdUsuario(), "LOGIN", "usuarios",
                    u.getIdUsuario(), "[SEGURIDAD] Inicio de sesión correcto");
        });
    }

    @EventListener
    @Transactional
    public void onFailure(AuthenticationFailureBadCredentialsEvent event) {
        String login = String.valueOf(event.getAuthentication().getPrincipal());

        usuarioRepository.findByUsernameOrEmail(login).ifPresentOrElse(u -> {

            // Si el bloqueo previo ya expiró, reiniciamos antes de contar.
            if (u.getBloqueadoHasta() != null && u.getBloqueadoHasta().isBefore(LocalDateTime.now())) {
                u.setIntentosFallidos(0);
                u.setBloqueadoHasta(null);
                if (Constants.ESTADO_BLOQUEADO.equals(u.getEstado())) {
                    u.setEstado(Constants.ESTADO_ACTIVO);
                }
            }

            int intentos = (u.getIntentosFallidos() == null ? 0 : u.getIntentosFallidos()) + 1;
            u.setIntentosFallidos(intentos);

            boolean bloqueado = false;
            String desc;
            if (intentos >= Constants.MAX_INTENTOS_FALLIDOS) {
                u.setEstado(Constants.ESTADO_BLOQUEADO);
                u.setBloqueadoHasta(LocalDateTime.now().plusMinutes(Constants.MINUTOS_BLOQUEO));
                bloqueado = true;
                desc = "[SEGURIDAD] Usuario bloqueado por intentos fallidos ("
                        + intentos + "/" + Constants.MAX_INTENTOS_FALLIDOS + ")";
            } else {
                desc = "[SEGURIDAD] Intento de acceso fallido "
                        + intentos + "/" + Constants.MAX_INTENTOS_FALLIDOS;
            }
            usuarioRepository.save(u);

            // Auditoría: registro_id = id_usuario, descripción estructurada.
            auditoriaService.error(u.getIdUsuario(), "LOGIN", "usuarios", u.getIdUsuario(), desc);

            // Datos para el mensaje de UI (genérico, no revela existencia).
            int restantes = Math.max(0, Constants.MAX_INTENTOS_FALLIDOS - intentos);
            guardarEnSesion(restantes, bloqueado);

        }, () -> {
            // Usuario inexistente: auditamos sin id y NO damos pistas en UI.
            auditoriaService.error(null, "LOGIN", "usuarios",
                    Constants.REGISTRO_MASIVO, "[SEGURIDAD] Intento con credencial inexistente");
            guardarEnSesion(-1, false); // -1 => mensaje genérico sin conteo
        });
    }

    /** Deja en la sesión HTTP los datos que AuthController usa para el mensaje. */
    private void guardarEnSesion(int intentosRestantes, boolean bloqueado) {
        HttpServletRequest req = obtenerRequest();
        if (req == null) return;
        req.getSession(true).setAttribute("zp_intentos_restantes", intentosRestantes);
        req.getSession(true).setAttribute("zp_bloqueado", bloqueado);
    }

    private HttpServletRequest obtenerRequest() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attrs != null ? attrs.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }
}