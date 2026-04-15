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
        User user = userRepository.findByEmail(email).orElseThrow(); // Ya sabemos que existe

        model.addAttribute("userName", user.getName());
        model.addAttribute("userPhoto", principal.getAttribute("picture"));

        if ("BARBER".equals(user.getRole())) {
            // DATOS PARA EL BARBERO: Todas las citas, estadísticas, etc.
            model.addAttribute("appointments", appointmentRepository.findAll());
            model.addAttribute("totalRevenue", appointmentRepository.findAll().size() * 20); // Ejemplo de lógica
            return "dashboard-admin"; // Carga el HTML de administrador
        } else {
            // DATOS PARA EL CLIENTE: Solo sus citas
            model.addAttribute("appointments", appointmentRepository.findByUser(user));
            return "dashboard-user"; // Carga el HTML de cliente
        }
    }
  }