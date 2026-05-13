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

                .requestMatchers("/", "/index.html", "/images/**", "/css/**", "/js/**", "/set-intent/**", "/location", "/contact").permitAll()

                .requestMatchers("/dashboard-admin/**", "/dashboard-user/**").authenticated()

                .anyRequest().permitAll()

            )

            .oauth2Login(oauth2 -> oauth2

                .loginPage("/")

                .failureUrl("/?error=not_registered")

                .successHandler((request, response, authentication) -> {

                    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

                    Map<String, Object> attributes = oAuth2User.getAttributes();

                   

                    // Extracción ultra-segura para evitar ClassCastException

                    String email = attributes.get("email") != null ? attributes.get("email").toString().toLowerCase().trim() : "";

                   

                    System.out.println("DEBUG: Login exitoso. Comprobando rol para: " + email);

                   

                    User user = userRepository.findByEmailIgnoreCase(email).orElse(null);

                   

                    if (user != null && ("ADMIN".equals(user.getRole()) || "BARBER".equals(user.getRole()))) {

                        response.sendRedirect("/dashboard-admin");

                    } else {

                        response.sendRedirect("/dashboard-user");

                    }

                })

                .userInfoEndpoint(userInfo -> userInfo.userService(this.oauth2UserService()))

            )

            .logout(logout -> logout

                .logoutSuccessUrl("/")

                .invalidateHttpSession(true)

                .deleteCookies("JSESSIONID")

                .permitAll()

            );

           

        return http.build();

    }



    private OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {

        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

        return request -> {

            OAuth2User googleUser = delegate.loadUser(request);

            Map<String, Object> attributes = googleUser.getAttributes();

           

            // Usamos .toString() para evitar errores de casting de tipos internos de Google

            String email = attributes.get("email") != null ? attributes.get("email").toString().toLowerCase().trim() : "";

            String name = attributes.get("name") != null ? attributes.get("name").toString() : "";



            System.out.println("DEBUG: Iniciando proceso OAuth2 para: " + email);



            // 1. BUSCAR SI EL USUARIO YA ESTÁ EN LA BASE DE DATOS

            Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);

            if (userOpt.isPresent()) {

                System.out.println("DEBUG: Usuario encontrado en BD. Acceso permitido.");

                return googleUser;

            }



            // 2. SI NO ESTÁ, MIRAMOS SI VIENE DE 'CREATE ACCOUNT'

            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            HttpSession session = (attr != null) ? attr.getRequest().getSession(false) : null;

           

            Object intentAttr = (session != null) ? session.getAttribute("AUTH_INTENT") : null;

            String intent = (intentAttr != null) ? intentAttr.toString() : "";



            System.out.println("DEBUG: Usuario NO encontrado. Intent detectado: " + intent);



            if ("register".equals(intent)) {

                try {

                    System.out.println("DEBUG: Creando nuevo usuario...");

                    User newUser = new User();

                    newUser.setEmail(email);

                    newUser.setName(name);

                    newUser.setPassword("oauth2_user");

                   

                    // Asignación de roles según email

                    if (email.equals("pablosantillana34@gmail.com") || email.equals("clope04@ucm.es")) {

                        newUser.setRole("BARBER");

                    } else {

                        newUser.setRole("USER");

                    }

                   

                    userRepository.saveAndFlush(newUser);

                    System.out.println("DEBUG: Usuario guardado en MySQL con éxito.");

                   

                    if (session != null) session.removeAttribute("AUTH_INTENT");

                    return googleUser;

                } catch (Exception e) {

                    System.err.println("DEBUG ERROR BD: " + e.getMessage());

                    throw new OAuth2AuthenticationException(new OAuth2Error("db_error"), e.getMessage());

                }

            }



            // 3. SI NO ES REGISTRO Y NO ESTÁ EN BD: Lanzamos error para mostrar cartel rojo

            System.out.println("DEBUG: Bloqueando acceso por falta de registro.");

            throw new OAuth2AuthenticationException(new OAuth2Error("not_registered"), "Debes registrarte primero");

        };

    }



    @Bean

    public BCryptPasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();

    }

}