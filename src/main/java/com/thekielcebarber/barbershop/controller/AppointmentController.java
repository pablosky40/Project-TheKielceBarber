package com.thekielcebarber.barbershop.controller;

import com.thekielcebarber.barbershop.service.AppointmentService;
import com.thekielcebarber.barbershop.model.Appointment;
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

    @GetMapping("/new")
    public String showBookingForm() {
        return "booking"; 
    }

    // --- NUEVO MÉTODO PARA VALIDACIÓN EN TIEMPO REAL ---
    @GetMapping("/check-availability")
    @ResponseBody
    public boolean checkAvailability(
            @RequestParam String barber,
            @RequestParam String date,
            @RequestParam String time) {
        // Retorna true si está libre, false si está ocupado
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
            
            String email = (principal != null) ? principal.getAttribute("email") : "guest@example.com";
            newAppt.setUserEmail(email);

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