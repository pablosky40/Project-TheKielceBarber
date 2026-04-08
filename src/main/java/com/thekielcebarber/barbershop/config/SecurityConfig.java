package com.thekielcebarber.barbershop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // PERMITIR TODO EL MUNDO: Home, recursos y consola H2
                .requestMatchers("/", "/index.html", "/h2-console/**").permitAll()
                // REQUERIR LOGIN: Todo lo que empiece por /api/
                .requestMatchers("/api/**").authenticated()
                // El resto también lo permitimos por ahora para que no te bloquee
                .anyRequest().permitAll()
            )
         // Dentro de securityFilterChain cambia la línea:
            .oauth2Login(oauth2 -> oauth2
                .defaultSuccessUrl("/dashboard", true) // ¡Ahora a la pantalla de opciones!
            
            )
            // Esto es necesario para que la consola H2 funcione
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frame -> frame.disable()));
            
        return http.build();
    }
}