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

import java.util.List;
import java.util.stream.Collectors; // IMPORTANTE: Para el filtrado

@Controller
public class DashboardController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    // 1. RUTA DE ENTRADA
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return "redirect:/";

        User user = checkAndUpgradeUser(principal);

        if ("BARBER".equals(user.getRole())) {
            return "redirect:/dashboard-admin";
        }
        return "redirect:/dashboard-user";
    }

    // 2. RUTA PARA EL BARBERO (ADMIN)
    @GetMapping("/dashboard-admin")
    public String showAdminDashboard(Model model, @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return "redirect:/";
        
        User user = checkAndUpgradeUser(principal);
        if (!"BARBER".equals(user.getRole())) return "redirect:/dashboard-user";

        // --- FILTRADO MÁGICO ---
        // Obtenemos todas las citas pero EXCLUIMOS las que son de tipo "BLOQUEO"
        List<Appointment> onlyClientAppts = appointmentRepository.findAll().stream()
                .filter(appt -> !"BLOQUEO".equals(appt.getService()))
                .collect(Collectors.toList());

        model.addAttribute("userName", user.getName());
        model.addAttribute("userPhoto", (String) principal.getAttribute("picture"));
        
        // Enviamos la lista filtrada a la tabla de actividad reciente
        model.addAttribute("appointments", onlyClientAppts);
        model.addAttribute("totalCitas", onlyClientAppts.size());
        
        return "dashboard-admin";
    }

    // 3. RUTA PARA EL CLIENTE (USER)
    @GetMapping("/dashboard-user")
    public String showUserDashboard(Model model, @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return "redirect:/";

        User user = checkAndUpgradeUser(principal);
        
        model.addAttribute("userName", user.getName());
        model.addAttribute("userPhoto", (String) principal.getAttribute("picture"));
        
        // Aquí solo mostramos las citas propias del usuario logueado
        model.addAttribute("appointments", appointmentRepository.findByUser(user));
        
        return "dashboard-user";
    }

    // Método interno para gestión de usuarios y roles
    private User checkAndUpgradeUser(OAuth2User principal) {
        String email = principal.getAttribute("email");
        String name = principal.getAttribute("name");

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName(name);
            newUser.setRole("USER");
            return userRepository.save(newUser);
        });

        // Configuración de los administradores jefes
        boolean esJefe = email.equalsIgnoreCase("pablosantillana34@gmail.com") || 
                         email.equalsIgnoreCase("cllope04@ucm.es");

        if (esJefe && !"BARBER".equals(user.getRole())) {
            user.setRole("BARBER");
            user = userRepository.save(user);
        }
        return user;
    }
}