package com.zippers.sistema_ventas.service;

import com.zippers.sistema_ventas.entity.Usuario;
import com.zippers.sistema_ventas.repository.UsuarioRepository;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        // Verificar si la cuenta está bloqueada temporalmente
        if (usuario.getBloqueado_hasta() != null && usuario.getBloqueado_hasta().isAfter(LocalDateTime.now())) {
            throw new LockedException("Cuenta bloqueada temporalmente. Intente de nuevo después de " + usuario.getBloqueado_hasta());
        }

        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword_hash())
                .roles(usuario.getRol().getNombre())  // Rol tiene explícitamente getNombre()
                .build();
    }
}