package com.thekielcebarber.barbershop.controller;

import com.thekielcebarber.barbershop.model.Appointment;
import com.thekielcebarber.barbershop.model.Service;
import com.thekielcebarber.barbershop.model.User;
import com.thekielcebarber.barbershop.repository.AppointmentRepository;
import com.thekielcebarber.barbershop.repository.ServiceRepository;
import com.thekielcebarber.barbershop.repository.UserRepository;
import com.thekielcebarber.barbershop.service.MessageProducer; // Importación necesaria
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/appointments")
public class AppointmentController {

    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ServiceRepository serviceRepository;
    
    @Autowired 
    private MessageProducer messageProducer; // Inyección del productor de RabbitMQ

    // 1. PANEL DE GESTIÓN DE CITAS (Admin)
    @GetMapping("/admin") 
    public String showAdminDashboard(Model model, Principal principal) {
        List<Appointment> allApps = appointmentRepository.findAll();
        model.addAttribute("appointments", allApps);
        model.addAttribute("appointmentCount", allApps.stream().filter(a -> a.getUser() != null).count());
        loadGoogleData(model, principal);
        return "admin-appointments"; 
    }

    // 2. FORMULARIO DE RESERVA
    @GetMapping("/new") 
    public String showBookingForm(Model model, Principal principal) {
        List<Service> services = serviceRepository.findAll().stream()
                .filter(s -> !"BLOQUEO".equalsIgnoreCase(s.getName()))
                .collect(Collectors.toList());
        model.addAttribute("services", services);
        
        List<User> barbers = userRepository.findAll().stream()
                .filter(u -> "BARBER".equals(u.getRole()))
                .collect(Collectors.toList());
        model.addAttribute("barbers", barbers);
        
        loadGoogleData(model, principal);
        return "appointment";
    }

    // 3. API DISPONIBILIDAD
    @GetMapping("/check-availability")
    @ResponseBody
    public List<String> checkAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Long barberId) {

        User barber = userRepository.findById(barberId)
                .orElseThrow(() -> new RuntimeException("Barber not found"));

        List<Appointment> dayEvents = appointmentRepository.findAll().stream()
                .filter(appt -> appt.getDate().equals(date))
                .collect(Collectors.toList());

        return dayEvents.stream()
                .filter(appt -> 
                    "FULL DAY".equals(appt.getTime()) || 
                    "BLOCKED".equals(appt.getPaymentStatus()) || 
                    (appt.getUser() != null && barber.getName().equals(appt.getBarber()))
                )
                .map(Appointment::getTime)
                .distinct()
                .collect(Collectors.toList());
    }

    // 4. GUARDAR CITA (Redirige al checkout)
    @PostMapping("/create")
    public String createAppointment(
            @RequestParam Long serviceId,
            @RequestParam Long barberId, 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String time,
            Principal principal) {
        
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        User barber = userRepository.findById(barberId)
                .orElseThrow(() -> new RuntimeException("Barber not found"));
        User customer = getOrCreateUser(principal.getName(), principal);

        Appointment appt = new Appointment();
        appt.setUser(customer);
        appt.setService(service);
        appt.setDate(date);
        appt.setTime(time);
        appt.setBarber(barber.getName());
        appt.setPrice(service.getPrice());
        appt.setPaymentStatus("PENDING");
        
        appointmentRepository.save(appt);

        // ENVIAR A RABBITMQ: Notificar que la reserva se ha creado (Pendiente de pago)
        messageProducer.sendAppointmentNotification(customer.getEmail() + "|Tu reserva ha sido creada. Estado: PENDIENTE DE PAGO. Día: " + date + " a las " + time);

        return "redirect:/appointments/checkout?id=" + appt.getId();
    }

    // 5. DASHBOARD USUARIO
    @GetMapping("/my-appointments")
    public String showMyAppointments(Model model, Principal principal, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        User user = getOrCreateUser(principal.getName(), principal);
        List<Appointment> userAppointments = appointmentRepository.findByUser(user);
        
        model.addAttribute("appointments", userAppointments);
        loadGoogleData(model, principal);
        return "dashboard-user";
    }

    // 6. BLOQUEOS ADMIN (HORA SUELTA)
    @PostMapping("/admin/block")
    public String blockSchedule(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, 
                                @RequestParam String time) {
        if (date.isBefore(LocalDate.now())) {
            return "redirect:/appointments/admin?error=past_date";
        }

        Appointment block = new Appointment();
        block.setDate(date);
        block.setTime(time);
        serviceRepository.findByName("BLOQUEO").ifPresent(block::setService);
        block.setBarber("The Kielce Barber");
        block.setPrice(0.0);
        block.setPaymentStatus("BLOCKED");
        appointmentRepository.save(block);
        return "redirect:/appointments/admin";
    }

    // 6.2 BLOQUEO DE DÍA COMPLETO
    @PostMapping("/admin/block-day")
    public String blockFullDay(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            return "redirect:/appointments/admin?error=past_date";
        }

        Appointment blockDay = new Appointment();
        blockDay.setDate(date);
        blockDay.setTime("FULL DAY");
        serviceRepository.findByName("BLOQUEO").ifPresent(blockDay::setService);
        blockDay.setBarber("The Kielce Barber");
        blockDay.setPrice(0.0);
        blockDay.setPaymentStatus("BLOCKED");
        appointmentRepository.save(blockDay);
        return "redirect:/appointments/admin?success_blocked_day";
    }

    // 7. CANCELAR CITA
    @PostMapping("/cancel/{id}")
    public String cancelAppointment(@PathVariable Long id, @RequestParam(value = "source", required = false) String source) {
        appointmentRepository.deleteById(id);
        if ("admin-dashboard".equals(source)) return "redirect:/dashboard-admin";
        if ("admin-schedule".equals(source)) return "redirect:/appointments/admin";
        return "redirect:/appointments/my-appointments";
    }
    
    // 8. VISTA DE CHECKOUT
    @GetMapping("/checkout")
    public String showCheckout(@RequestParam("id") Long appointmentId, Model model, Principal principal) {
        Appointment appt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        model.addAttribute("appointment", appt);
        loadGoogleData(model, principal);
        return "checkout";
    }

    // 9. PAGO OFFLINE
    @PostMapping("/pay-offline")
    public String payOffline(@RequestParam Long id) {
        Appointment appt = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        appt.setPaymentStatus("PAID_OFFLINE");
        appointmentRepository.save(appt);

        // ENVIAR A RABBITMQ: Notificar confirmación de pago en tienda
        messageProducer.sendAppointmentNotification(appt.getUser().getEmail() + "|Confirmado: Pago en tienda seleccionado para tu cita el " + appt.getDate());
        
        return "redirect:/appointments/my-appointments?success_offline";
    }

    // 10. ÉXITO PAGO ONLINE
    @GetMapping("/payment-success")
    public String paymentSuccess(@RequestParam("id") Long id) {
        Appointment appt = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment does not exist"));
        
        appt.setPaymentStatus("PAID_ONLINE");
        appointmentRepository.save(appt);

        // ENVIAR A RABBITMQ: Notificar confirmación de pago online con Stripe
        messageProducer.sendAppointmentNotification(appt.getUser().getEmail() + "|¡Pago Recibido! Tu cita el día " + appt.getDate() + " ha sido confirmada online.");
        
        return "redirect:/appointments/my-appointments?success";
    }

    // --- MÉTODOS DE APOYO ---

    private User getOrCreateUser(String emailOrId, Principal principal) {
        String email = emailOrId;
        String name = emailOrId.split("@")[0];

        if (principal instanceof OAuth2AuthenticationToken token) {
            email = token.getPrincipal().getAttribute("email");
            name = token.getPrincipal().getAttribute("name");
        }

        final String finalEmail = (email != null) ? email.toLowerCase().trim() : "";
        final String finalName = name;

        return userRepository.findByEmail(finalEmail).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(finalEmail);
            newUser.setName(finalName != null ? finalName : finalEmail.split("@")[0]);
            newUser.setRole("USER");
            newUser.setPassword("oauth2_user");
            return userRepository.save(newUser);
        });
    }

    private void loadGoogleData(Model model, Principal principal) {
        if (principal instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) principal;
            model.addAttribute("userPhoto", token.getPrincipal().getAttribute("picture"));
            model.addAttribute("userName", token.getPrincipal().getAttribute("name"));
        }
    }
}