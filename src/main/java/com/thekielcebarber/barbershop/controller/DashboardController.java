package com.thekielcebarber.barbershop.controller;

import com.thekielcebarber.barbershop.model.Appointment;
import com.thekielcebarber.barbershop.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal OAuth2User principal) {
        if (principal != null) {
            // Datos del usuario de Google
            String name = principal.getAttribute("name");
            String email = principal.getAttribute("email");
            
            // IMPORTANTE: Recuperamos la foto de Google (atributo 'picture')
            String photoUrl = principal.getAttribute("picture"); 

            model.addAttribute("userName", name);
            model.addAttribute("userPhoto", photoUrl);

            // Cargar citas del usuario
            List<Appointment> userAppointments = appointmentRepository.findByUserEmail(email);
            model.addAttribute("appointments", userAppointments);
        }
        
        return "dashboard";
    }
}