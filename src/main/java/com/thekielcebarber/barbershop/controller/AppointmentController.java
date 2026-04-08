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

    // 1. Listar todas las citas (Ruta: /appointments)
    @GetMapping
    public String listAppointments(Model model) {
        model.addAttribute("appointments", appointmentService.getAllAppointments());
        return "appointments"; // Busca appointments.html en templates
    }

    // 2. Mostrar el formulario de reserva (Ruta: /appointments/new)
    @GetMapping("/new")
    public String showBookingForm() {
        return "booking"; // Busca booking.html en templates
    }

    // 3. Guardar la nueva cita (Ruta: /appointments/save)
    
    @PostMapping("/save")
    public String saveAppointment(
            @RequestParam String service,
            @RequestParam String barber,
            @RequestParam String date,
            @RequestParam String time,
            Model model) {
        
        // 1. Pasamos los datos a la pantalla de confirmación
        model.addAttribute("service", service);
        model.addAttribute("barber", barber);
        model.addAttribute("date", date);
        model.addAttribute("time", time);
        
        // 2. Aquí es donde RabbitMQ enviaría la notificación en segundo plano
        System.out.println("LOG: Reserva pendiente de pago para " + service);
        
        return "confirmation"; // <-- Ahora nos lleva a la pantalla bonita
    }

    // 4. Aprobar el pago (Ruta: /appointments/approve/{id})
    @GetMapping("/approve/{id}")
    public String approvePayment(@PathVariable Long id) {
        appointmentService.approveOfflinePayment(id);
        return "redirect:/appointments";
    }
   
    @GetMapping("/checkout")
    public String showCheckout(
            @RequestParam(required = false, defaultValue = "Service Not Selected") String service,
            @RequestParam(required = false, defaultValue = "No Barber") String barber,
            @RequestParam(required = false, defaultValue = "TBD") String date,
            @RequestParam(required = false, defaultValue = "TBD") String time,
            Model model) {
        
        model.addAttribute("service", service);
        model.addAttribute("barber", barber);
        model.addAttribute("date", date);
        model.addAttribute("time", time);
        
        return "checkout";
    }
 // SUSTITUYE LOS DOS MÉTODOS REPETIDOS POR ESTE ÚNICO:
    @GetMapping("/payment-success")
    public String processPayment(
            @RequestParam String service,
            @RequestParam String barber,
            @RequestParam String date,
            @RequestParam String time,
            @RequestParam(required = false, defaultValue = "unknown") String cardNumber, 
            @AuthenticationPrincipal OAuth2User principal) {

        // 1. LÓGICA DE ESCENARIO NEGATIVO (Tarjeta 0000)
        if (cardNumber.endsWith("0000")) {
            System.out.println("ALERT: Payment failed for card ending in 0000");
            return "payment-failed"; 
        }

        // 2. LÓGICA DE PAGO CORRECTO
        Appointment newAppt = new Appointment();
        newAppt.setService(service);
        newAppt.setBarber(barber);
        newAppt.setDate(date);
        newAppt.setTime(time);
        newAppt.setPaymentStatus("PAID");
        
        String email = (principal != null) ? principal.getAttribute("email") : "guest@example.com";
        newAppt.setUserEmail(email);

        // Guardar en DB y enviar a RabbitMQ
        appointmentService.createAndNotifyAppointment(newAppt);

        return "success";
    }
}