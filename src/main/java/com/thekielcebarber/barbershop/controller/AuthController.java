package com.thekielcebarber.barbershop.controller;

import com.thekielcebarber.barbershop.model.User;
import com.thekielcebarber.barbershop.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // --- NUEVO MÉTODO PARA GOOGLE (OAUTH2) ---
    @GetMapping("/auth-choice")
    public String saveIntent(@RequestParam String intent, HttpSession session) {
        // Guardamos si el usuario quiere login o register en la sesión
        session.setAttribute("AUTH_INTENT", intent);
        System.out.println("DEBUG AuthController: Intención OAuth2 guardada -> " + intent);
        
        // Redirigimos al flujo de Google de Spring Security
        return "redirect:/oauth2/authorization/google";
    }

    // --- TU MÉTODO ANTERIOR PARA REGISTRO MANUAL ---
    @PostMapping("/register")
    public String registerUser(@RequestParam String name, 
                               @RequestParam String email, 
                               @RequestParam String password) {
        
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        // Encriptamos la contraseña para seguridad
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("USER"); 
        
        userRepository.save(user);
        
        return "redirect:/?registered=true";
    }
}