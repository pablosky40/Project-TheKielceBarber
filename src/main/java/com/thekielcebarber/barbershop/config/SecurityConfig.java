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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserRepository userRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**"))
                .disable() // Deshabilitado para pruebas, pero ignorando H2 específicamente
            )
            .authorizeHttpRequests(auth -> auth
                // IMPORTANTE: Permitir H2 antes que el resto
                .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
                .requestMatchers("/", "/index.html", "/images/**", "/css/**", "/js/**", "/set-intent/**", "/location", "/contact").permitAll()
                .requestMatchers("/dashboard/**", "/dashboard-admin/**", "/appointments/**", "/products/**").authenticated() 
                .anyRequest().permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/")
                .successHandler((request, response, authentication) -> {
                    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
                    String email = oAuth2User.getAttribute("email");
                    String name = oAuth2User.getAttribute("name");
                    String intent = (String) request.getSession().getAttribute("AUTH_INTENT");
                    
                    var userOpt = userRepository.findByEmail(email);

                    if (userOpt.isPresent()) {
                        // Si el usuario existe, redirigir según su rol si quieres, o al dashboard general
                        response.sendRedirect("/dashboard");
                    } else if ("register".equals(intent)) {
                        com.thekielcebarber.barbershop.model.User newUser = new com.thekielcebarber.barbershop.model.User();
                        newUser.setEmail(email);
                        newUser.setName(name);
                        newUser.setRole("USER");
                        newUser.setPassword(""); 
                        userRepository.save(newUser);
                        response.sendRedirect("/dashboard");
                    } else {
                        request.getSession().invalidate();
                        response.sendRedirect("/?error=not_registered");
                    }
                })
            )
            // Esto es CRUCIAL para que la H2 Console funcione y no dé error de Bean
            .headers(headers -> headers.frameOptions(f -> f.sameOrigin()));
            
        return http.build();
    }
    
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}