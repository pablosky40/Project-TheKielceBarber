package com.thekielcebarber.barbershop.controller;

import com.thekielcebarber.barbershop.model.Appointment;
import com.thekielcebarber.barbershop.model.User;
import com.thekielcebarber.barbershop.repository.AppointmentRepository;
import com.thekielcebarber.barbershop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return "redirect:/";

        String email = principal.getAttribute("email");
        String name = principal.getAttribute("name");
        String photoUrl = principal.getAttribute("picture");

        // 1. Buscamos al usuario o lo creamos si es nuevo
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName(name);
            newUser.setRole("USER"); // Por defecto todos son clientes
            return userRepository.save(newUser);
        });

        // 2. EL FILTRO DE PODER: Solo Pablo y Claudia son BARBER
        boolean esJefe = email.equalsIgnoreCase("pablosantillana34@gmail.com") || 
                         email.equalsIgnoreCase("cllope04@ucm.es");

        if (esJefe) {
            if (!"BARBER".equals(user.getRole())) {
                user.setRole("BARBER");
                user = userRepository.save(user);
            }
        } else {
            // Por si acaso el admin "fantasma" logueara, se quedaría como USER
            if (!"USER".equals(user.getRole())) {
                user.setRole("USER");
                user = userRepository.save(user);
            }
        }

        model.addAttribute("userName", user.getName());
        model.addAttribute("userPhoto", photoUrl);

        if ("BARBER".equals(user.getRole())) {
            List<Appointment> allAppts = appointmentRepository.findAll();
            model.addAttribute("appointments", allAppts);
            model.addAttribute("totalCitas", allAppts.size());
            model.addAttribute("totalRevenue", allAppts.size() * 20); 
            return "dashboard-admin"; 
        } else {
            model.addAttribute("appointments", appointmentRepository.findByUser(user));
            return "dashboard-user";
        }
    }
}