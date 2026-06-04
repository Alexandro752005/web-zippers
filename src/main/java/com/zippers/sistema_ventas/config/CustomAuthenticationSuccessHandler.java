package com.zippers.sistema_ventas.config;

import com.zippers.sistema_ventas.entity.Usuario;
import com.zippers.sistema_ventas.repository.UsuarioRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        try {
            String email = authentication.getName();
            Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
            
            if (usuario != null) {
                // Resetea los intentos fallidos tras un login exitoso
                usuario.setIntentos_fallidos(0);
                usuario.setBloqueado_hasta(null);
                usuarioRepository.save(usuario);
            }
        } catch (Exception e) {
            // Si hay un error en base de datos, lo veremos en rojo en la consola de VS Code
            System.err.println("⚠ Error al registrar acceso en SuccessHandler: " + e.getMessage());
        }

        // Asegura que siempre te mande al inicio después de loguearte correctamente
        setDefaultTargetUrl("/"); 
        super.onAuthenticationSuccess(request, response, authentication);
    }
}