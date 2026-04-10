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
                    String intent = (String) request.getSession().getAttribute("AUTH_INTENT");
                    
                    var userOpt = userRepository.findByEmail(email);

                    if (userOpt.isPresent()) {
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
                        // LIMPIAMOS SESIÓN PARA FORZAR QUE EL ERROR APAREZCA SIEMPRE
                        request.getSession().invalidate();
                        response.sendRedirect("/?error=not_registered");
                    }
                })
            )
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(f -> f.disable()));
            
        return http.build();
    }
    
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}