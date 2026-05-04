package com.thekielcebarber.barbershop.controller;

import com.thekielcebarber.barbershop.model.Appointment;
import com.thekielcebarber.barbershop.model.Service;
import com.thekielcebarber.barbershop.model.User;
import com.thekielcebarber.barbershop.repository.AppointmentRepository;
import com.thekielcebarber.barbershop.repository.ServiceRepository;
import com.thekielcebarber.barbershop.repository.UserRepository;
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

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRepository serviceRepository;

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

        User barber = userRepository.findById(barberId).get();
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

    // 4. GUARDAR CITA (Ahora redirige al checkout real)
    @PostMapping("/create")
    public String createAppointment(
            @RequestParam Long serviceId,
            @RequestParam Long barberId, 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String time,
            Principal principal) {
        
        Service service = serviceRepository.findById(serviceId).get();
        User barber = userRepository.findById(barberId).get();
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
        // Redirigimos a la vista de checkout con el ID de la cita recién creada
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

    // 6. BLOQUEOS ADMIN
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

    // 7. CANCELAR CITA
    @PostMapping("/cancel/{id}")
    public String cancelAppointment(@PathVariable Long id, @RequestParam(value = "source", required = false) String source) {
        appointmentRepository.deleteById(id);
        if ("admin-dashboard".equals(source)) return "redirect:/dashboard-admin";
        if ("admin-schedule".equals(source)) return "redirect:/appointments/admin";
        return "redirect:/appointments/my-appointments";
    }
    
    // 8. VISTA DE CHECKOUT (Muestra los dos botones de pago)
    @GetMapping("/checkout")
    public String showCheckout(@RequestParam("id") Long appointmentId, Model model, Principal principal) {
        Appointment appt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        model.addAttribute("appointment", appt);
        loadGoogleData(model, principal);
        return "checkout";
    }

    // 9. PAGO OFFLINE (EL BOTÓN DE PÁNICO)
    // Este método lo usas si la pasarela externa falla o el cliente paga en el local.
    @PostMapping("/pay-offline")
    public String payOffline(@RequestParam Long id) {
        Appointment appt = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        appt.setPaymentStatus("PAID_OFFLINE"); // Marcamos como pagado localmente
        appointmentRepository.save(appt);
        
        return "redirect:/appointments/my-appointments?success_offline";
    }

    // --- MÉTODOS DE APOYO ---

    private User getOrCreateUser(String emailOrId, Principal principal) {
        String email = emailOrId;
        if (principal instanceof OAuth2AuthenticationToken token) {
            email = token.getPrincipal().getAttribute("email");
        }
        final String finalEmail = email;
        return userRepository.findByEmail(finalEmail).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(finalEmail);
            if (principal instanceof OAuth2AuthenticationToken token) {
                String googleName = token.getPrincipal().getAttribute("name");
                newUser.setName(googleName != null ? googleName : finalEmail.split("@")[0]);
            } else {
                newUser.setName(finalEmail.split("@")[0]);
            }
            newUser.setRole("USER");
            newUser.setPassword("oauth2_user");
            return userRepository.save(newUser);
        });
    }
    @GetMapping("/payment-success")
public String paymentSuccess(@RequestParam("id") Long id) {
    Appointment appt = appointmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
    
    appt.setPaymentStatus("PAID_ONLINE"); // Actualizamos el estado a pagado
    appointmentRepository.save(appt); // Guardamos en MySQL
    
    return "redirect:/appointments/my-appointments?success";
}

    private void loadGoogleData(Model model, Principal principal) {
        if (principal instanceof OAuth2AuthenticationToken token) {
            model.addAttribute("userPhoto", token.getPrincipal().getAttribute("picture"));
            model.addAttribute("userName", token.getPrincipal().getAttribute("name"));
        }
    }
}