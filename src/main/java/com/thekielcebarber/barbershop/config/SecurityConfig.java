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

import java.util.Map;
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
                .requestMatchers("/", "/index.html", "/images/**", "/css/**", "/js/**", "/auth-choice/**", "/location", "/contact").permitAll()
                .requestMatchers("/dashboard-admin/**", "/dashboard-user/**").authenticated()
                .anyRequest().permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/")
                .failureUrl("/?error=not_registered")
                .successHandler((request, response, authentication) -> {
                    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
                    String email = oAuth2User.getAttribute("email").toString().toLowerCase().trim();

                    userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
                        try {
                            if ("ADMIN".equals(user.getRole()) || "BARBER".equals(user.getRole())) {
                                response.sendRedirect("/dashboard-admin");
                            } else {
                                response.sendRedirect("/dashboard-user");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                })
                .userInfoEndpoint(userInfo -> userInfo.userService(this.oauth2UserService()))
            )
            .logout(logout -> logout.logoutSuccessUrl("/").permitAll());

        return http.build();
    }

    private OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        return request -> {
            OAuth2User googleUser = delegate.loadUser(request);
            Map<String, Object> attributes = googleUser.getAttributes();
            String email = attributes.get("email").toString().toLowerCase().trim();
            String name = attributes.get("name").toString();

            Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);

            if (userOpt.isPresent()) {
                System.out.println("DEBUG Service: Usuario existe. Entrando...");
                return googleUser;
            }

            // Recuperamos la sesión para ver si el intent es 'register'
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpSession session = attr.getRequest().getSession(false);
            Object intentAttr = (session != null) ? session.getAttribute("AUTH_INTENT") : null;
            String intent = (intentAttr != null) ? intentAttr.toString() : "";

            System.out.println("DEBUG Service: Usuario nuevo. Intent en Session: [" + intent + "]");

            if ("register".equals(intent)) {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setName(name);
                newUser.setPassword("oauth2_user");
                newUser.setRole((email.equals("pablosantillana34@gmail.com") || email.equals("clope04@ucm.es")) ? "BARBER" : "USER");
                userRepository.save(newUser);
                if(session != null) session.removeAttribute("AUTH_INTENT");
                return googleUser;
            } else {
                System.out.println("DEBUG Service: Bloqueo. No registrado y no viene de /auth-choice?intent=register");
                throw new OAuth2AuthenticationException(new OAuth2Error("not_registered"), "No registrado");
            }
        };
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}