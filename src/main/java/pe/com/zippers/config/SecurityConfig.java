package pe.com.zippers.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import pe.com.zippers.common.Constants;

/**
 * Configuración de seguridad (Fase 1).
 *
 * - Login form en /login (POST /login), logout en /logout.
 * - CSRF activado con repositorio en cookie (compatible con HTMX).
 * - Recursos estáticos, /uploads/** y /login públicos; el resto autenticado.
 * - @EnableMethodSecurity activo para @PreAuthorize (defensa en profundidad).
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // CSRF en cookie legible por JS, para que HTMX envíe el header X-XSRF-TOKEN.
        CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();
        csrfHandler.setCsrfRequestAttributeName(null); // resuelve token de forma temprana

        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(csrfHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        // MODIFICACIÓN: Se añade "/uploads/**" para permitir acceso público a las imágenes
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/webjars/**",
                                "/uploads/**", Constants.LOGIN, "/error").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage(Constants.LOGIN)
                        .loginProcessingUrl(Constants.LOGIN)
                        .usernameParameter("login")
                        .passwordParameter("password")
                        .defaultSuccessUrl(Constants.DASHBOARD, true)
                        .failureUrl(Constants.LOGIN + "?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl(Constants.LOGIN + "?logout")
                        .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                        .invalidateHttpSession(true)
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedPage(Constants.DASHBOARD + "?denegado")
                );

        return http.build();
    }
}