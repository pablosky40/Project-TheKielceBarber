package com.thekielcebarber.barbershop.controller;

import com.thekielcebarber.barbershop.model.Appointment;
import com.thekielcebarber.barbershop.model.User;
import com.thekielcebarber.barbershop.repository.AppointmentRepository;
import com.thekielcebarber.barbershop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * MÉTODO PUENTE: Redirige al usuario a su panel correspondiente según su ROL.
     */
    @GetMapping("/dashboard")
    public String genericDashboard(Principal principal) {
        if (principal instanceof OAuth2AuthenticationToken token) {
            String email = token.getPrincipal().getAttribute("email");
            
            return userRepository.findByEmail(email != null ? email.toLowerCase().trim() : "")
                .map(user -> {
                    if ("ADMIN".equals(user.getRole()) || "BARBER".equals(user.getRole())) {
                        return "redirect:/dashboard-admin";
                    }
                    return "redirect:/dashboard-user";
                })
                .orElse("redirect:/"); 
        }
        return "redirect:/";
    }

    /**
     * PANEL DE ADMINISTRADOR / BARBERO
     */
    @GetMapping("/dashboard-admin")
    public String showAdminDashboard(Model model, Principal principal) {
        List<Appointment> allApps = appointmentRepository.findAll();
        model.addAttribute("appointments", allApps);
        
        long count = allApps.stream()
                .filter(a -> a.getUser() != null)
                .count();
        model.addAttribute("appointmentCount", count);

        loadGoogleData(model, principal);
        return "dashboard-admin"; 
    }

    /**
     * PANEL DE CLIENTE (USER)
     * Ahora busca las citas del usuario logueado para que no desaparezcan al cerrar sesión.
     */
    @GetMapping("/dashboard-user")
    public String userDashboard(Model model, Principal principal) {
        if (principal instanceof OAuth2AuthenticationToken token) {
            String email = token.getPrincipal().getAttribute("email");
            String cleanEmail = (email != null) ? email.toLowerCase().trim() : "";

            // Buscamos al usuario en la BD por su email
            userRepository.findByEmail(cleanEmail).ifPresent(user -> {
                // Buscamos todas las citas que pertenecen a este usuario
                List<Appointment> myAppointments = appointmentRepository.findByUser(user);
                // Las mandamos al HTML con el nombre "appointments"
                model.addAttribute("appointments", myAppointments);
            });
        }

        loadGoogleData(model, principal);
        return "dashboard-user"; 
    }

    /**
     * Carga la foto y el nombre de Google para mostrar en la interfaz
     */
    private void loadGoogleData(Model model, Principal principal) {
        if (principal instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) principal;
            String picture = token.getPrincipal().getAttribute("picture");
            String name = token.getPrincipal().getAttribute("name");
            
            model.addAttribute("userPhoto", picture != null ? picture : "/images/default-user.png");
            model.addAttribute("userName", name != null ? name : "Usuario");
        }
    }
}