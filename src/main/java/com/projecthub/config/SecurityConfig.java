package com.projecthub.config;

// Importa tipos de Spring Security para configurar autenticación HTTP Basic sin sesión.
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// Define el acceso público y la autenticación requeridos por la API REST.
@Configuration
public class SecurityConfig {

    // Configura una API sin sesión: salud y registro son públicos, el resto requiere login.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Construye la cadena de filtros con las reglas de acceso y Basic Auth.
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    // Proporciona BCrypt para almacenar contraseñas sin conservar el texto original.
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Usa el codificador BCrypt recomendado para credenciales persistidas.
        return new BCryptPasswordEncoder();
    }
}