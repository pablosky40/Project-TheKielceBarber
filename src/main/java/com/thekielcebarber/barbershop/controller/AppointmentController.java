package com.thekielcebarber.barbershop.controller;

import com.thekielcebarber.barbershop.service.AppointmentService;
import com.thekielcebarber.barbershop.model.Appointment;
import com.thekielcebarber.barbershop.model.User;
import com.thekielcebarber.barbershop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/new")
    public String showBookingForm() {
        return "booking"; 
    }

    @GetMapping("/check-availability")
    @ResponseBody
    public boolean checkAvailability(
            @RequestParam String barber,
            @RequestParam String date,
            @RequestParam String time) {
        return !appointmentService.existsByBarberAndDateAndTime(barber, date, time);
    }

    @PostMapping("/save")
    public String saveAppointment(
            @RequestParam String service,
            @RequestParam String barber,
            @RequestParam String date,
            @RequestParam String time,
            Model model) {
        
        model.addAttribute("service", service);
        model.addAttribute("barber", barber);
        model.addAttribute("date", date);
        model.addAttribute("time", time);
        
        return "confirmation"; 
    }

    @GetMapping("/checkout")
    public String showCheckout(
            @RequestParam String service,
            @RequestParam String barber,
            @RequestParam String date,
            @RequestParam String time,
            Model model) {
        
        model.addAttribute("service", service);
        model.addAttribute("barber", barber);
        model.addAttribute("date", date);
        model.addAttribute("time", time);
        
        return "checkout";
    }

    @GetMapping("/payment-success")
    public String processPayment(
            @RequestParam String service,
            @RequestParam String barber,
            @RequestParam String date,
            @RequestParam String time,
            @RequestParam(required = false, defaultValue = "unknown") String cardNumber, 
            @AuthenticationPrincipal OAuth2User principal,
            Model model) {

        if (cardNumber.endsWith("0000")) {
            model.addAttribute("errorMessage", "Card declined.");
            return "payment-failed"; 
        }

        try {
            Appointment newAppt = new Appointment();
            newAppt.setService(service);
            newAppt.setBarber(barber);
            newAppt.setDate(date);
            newAppt.setTime(time);
            newAppt.setPaymentStatus("PAID");
            
            // --- LÓGICA DE RELACIÓN CON USUARIO Y ROLES ---
            String email = (principal != null) ? principal.getAttribute("email") : "guest@example.com";
            String name = (principal != null) ? principal.getAttribute("name") : "Guest User";

            // Buscamos si el usuario ya existe, si no, lo creamos con el rol correspondiente
            User user = userRepository.findByEmail(email).orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setName(name);
                
                // ASIGNACIÓN AUTOMÁTICA DE ROL
                if ("pablosantillana34@gmail.com".equalsIgnoreCase(email)) {
                    newUser.setRole("BARBER");
                } else {
                    newUser.setRole("USER");
                }
                
                return userRepository.save(newUser);
            });

            // Asignamos el OBJETO User a la cita
            newAppt.setUser(user);

            appointmentService.createAndNotifyAppointment(newAppt);
            return "redirect:/dashboard";

        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "payment-failed"; 
        }
    }

    @PostMapping("/cancel/{id}")
    public String cancelAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return "redirect:/dashboard";
    }
}