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

@Controller
public class DashboardController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    // ESTA ES LA RUTA DE ENTRADA (Decide a dónde mandarte)
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return "redirect:/";

        String email = principal.getAttribute("email");
        User user = checkAndUpgradeUser(principal);

        // Si es jefe, lo mandamos a la URL de admin, si no a la de usuario
        if ("BARBER".equals(user.getRole())) {
            return "redirect:/dashboard-admin";
        }
        return "redirect:/dashboard-user";
    }

    // RUTA PARA EL BARBERO (ADMIN) - ¡Ahora sí existe!
    @GetMapping("/dashboard-admin")
    public String showAdminDashboard(Model model, @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return "redirect:/";
        
        User user = checkAndUpgradeUser(principal);
        if (!"BARBER".equals(user.getRole())) return "redirect:/dashboard-user";

        List<Appointment> allAppts = appointmentRepository.findAll();
        model.addAttribute("userName", user.getName());
        model.addAttribute("userPhoto", (String) principal.getAttribute("picture"));
        model.addAttribute("appointments", allAppts);
        model.addAttribute("totalCitas", allAppts.size());
        
        return "dashboard-admin";
    }

    // RUTA PARA EL CLIENTE (USER)
    @GetMapping("/dashboard-user")
    public String showUserDashboard(Model model, @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return "redirect:/";

        User user = checkAndUpgradeUser(principal);
        
        model.addAttribute("userName", user.getName());
        model.addAttribute("userPhoto", (String) principal.getAttribute("picture"));
        model.addAttribute("appointments", appointmentRepository.findByUser(user));
        
        return "dashboard-user";
    }

    // Método interno para no repetir código de comprobación de roles
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

        boolean esJefe = email.equalsIgnoreCase("pablosantillana34@gmail.com") || 
                         email.equalsIgnoreCase("cllope04@ucm.es");

        if (esJefe && !"BARBER".equals(user.getRole())) {
            user.setRole("BARBER");
            user = userRepository.save(user);
        }
        return user;
    }
}