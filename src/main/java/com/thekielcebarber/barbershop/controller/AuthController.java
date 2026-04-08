package com.thekielcebarber.barbershop.controller;

import com.thekielcebarber.barbershop.model.User;
import com.thekielcebarber.barbershop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public String registerUser(@RequestParam String name, 
                               @RequestParam String email, 
                               @RequestParam String password) {
        
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        // Encriptamos la contraseña para que CustomUserDetailsService pueda leerla
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("USER"); // Asignamos el rol por defecto
        
        userRepository.save(user);
        
        // Redirigimos al index con un parámetro de éxito
        return "redirect:/?registered=true";
    }
}