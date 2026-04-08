package com.thekielcebarber.barbershop.config;

import com.thekielcebarber.barbershop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserRepository userRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // HEMOS AÑADIDO "/images/**" para que la foto de fondo sea pública
                .requestMatchers("/", "/index.html", "/images/**", "/h2-console/**", "/css/**", "/js/**", "/set-intent/**").permitAll()
                .requestMatchers("/dashboard/**").authenticated()
                .anyRequest().permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/")
                .successHandler((request, response, authentication) -> {
                    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
                    String email = oAuth2User.getAttribute("email");
                    String name = oAuth2User.getAttribute("name");

                    // Recuperamos la intención de la sesión (Login o Register)
                    String intent = (String) request.getSession().getAttribute("AUTH_INTENT");
                    
                    var userOpt = userRepository.findByEmail(email);

                    if (userOpt.isPresent()) {
                        // Si el usuario ya existe, entra directo
                        response.sendRedirect("/dashboard");
                    } else if ("register".equals(intent)) {
                        // Si NO existe pero pulsó "Register Account", lo creamos
                        com.thekielcebarber.barbershop.model.User newUser = new com.thekielcebarber.barbershop.model.User();
                        newUser.setEmail(email);
                        newUser.setName(name);
                        newUser.setRole("USER");
                        newUser.setPassword(""); // OAuth2 no requiere password local
                        userRepository.save(newUser);
                        
                        response.sendRedirect("/dashboard");
                    } else {
                        // Si NO existe y pulsó "Sign In", mandamos el error en inglés
                        response.sendRedirect("/?error=not_registered");
                    }
                })
            )
            // Configuración necesaria para que la consola H2 funcione
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(f -> f.disable()));
            
        return http.build();
    }
    
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}