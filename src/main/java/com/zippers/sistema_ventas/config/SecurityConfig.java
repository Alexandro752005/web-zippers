package com.zippers.sistema_ventas.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private CustomAuthenticationSuccessHandler successHandler;
    
    @Autowired
    private CustomAuthenticationFailureHandler failureHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Permitir todos los GET del módulo inventario para usuarios autenticados
                .requestMatchers(HttpMethod.GET, "/dashboard/inventario/**").authenticated()
       
                // Módulo de inventario (operaciones modificadoras): solo ADMIN o GESTOR
                .requestMatchers(HttpMethod.POST, "/dashboard/inventario/**").hasAnyRole("ADMIN", "GESTOR")

                // --- SOLUCIÓN APLICADA AQUÍ ---
                // Se agregó "/uploads/**" a la lista blanca para que Spring Security no bloquee la galería
                .requestMatchers("/css/**", "/js/**", "/img/**", "/uploads/**", "/login", "/registro").permitAll()
                // ------------------------------
                
                // Dashboard requiere autenticación
                .requestMatchers("/dashboard/**").authenticated()
                
                // Cualquier otra petición denegada
                .anyRequest().denyAll()
            )
           
            .formLogin(login -> login
                .loginPage("/login")
                .successHandler(successHandler)
                .failureHandler(failureHandler)
                .defaultSuccessUrl("/dashboard", true)
                .permitAll()
            )
    
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );
        return http.build();
    }
}