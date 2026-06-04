package com.zippers.sistema_ventas.config;

import com.zippers.sistema_ventas.entity.Usuario;
import com.zippers.sistema_ventas.repository.UsuarioRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String email = request.getParameter("username");
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (usuario.getBloqueado_hasta() == null || usuario.getBloqueado_hasta().isBefore(LocalDateTime.now())) {
                int intentos = usuario.getIntentos_fallidos() + 1;
                usuario.setIntentos_fallidos(intentos);
                if (intentos >= 5) {
                    usuario.setBloqueado_hasta(LocalDateTime.now().plusMinutes(5));
                    usuario.setIntentos_fallidos(0);
                }
                usuarioRepository.save(usuario);
            }
        }

        String redirectUrl = "/login?error";
        if (exception instanceof LockedException ||
            (usuarioOpt.isPresent() &&
             usuarioOpt.get().getBloqueado_hasta() != null &&
             usuarioOpt.get().getBloqueado_hasta().isAfter(LocalDateTime.now()))) {
            redirectUrl = "/login?bloqueado";
        }

        super.setDefaultFailureUrl(redirectUrl);
        super.onAuthenticationFailure(request, response, exception);
    }
}