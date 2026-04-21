package com.thekielcebarber.barbershop.config;

import com.thekielcebarber.barbershop.repository.UserRepository;
import com.thekielcebarber.barbershop.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserRepository userRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html", "/images/**", "/css/**", "/js/**", "/set-intent/**", "/location", "/contact").permitAll()
                .requestMatchers("/dashboard/**", "/dashboard-admin/**", "/appointments/**", "/products/**").authenticated() 
                .anyRequest().permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/?error=not_registered")
                .userInfoEndpoint(userInfo -> userInfo.userService(this.oauth2UserService()))
            )
            .headers(headers -> headers.disable()); 
            
        return http.build();
    }

    private OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        return request -> {
            OAuth2User googleUser = delegate.loadUser(request);
            String email = googleUser.getAttribute("email");
            String name = googleUser.getAttribute("name");

            // Recuperamos la intención (login o register) de la sesión
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpSession session = attr.getRequest().getSession(false);
            String intent = (session != null) ? (String) session.getAttribute("AUTH_INTENT") : "login";

            Optional<User> userOpt = userRepository.findByEmail(email);

            if (userOpt.isPresent()) {
                return googleUser; // Si ya existe, entra siempre
            } else {
                // Si NO existe y la intención es 'register', lo creamos
                if ("register".equals(intent)) {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setName(name);
                    newUser.setRole("USER");
                    newUser.setPassword(""); 
                    userRepository.save(newUser);
                    return googleUser;
                } else {
                    // Si NO existe y la intención es 'login', bloqueamos
                    throw new OAuth2AuthenticationException(
                        new OAuth2Error("not_registered"), "Account not found");
                }
            }
        };
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}