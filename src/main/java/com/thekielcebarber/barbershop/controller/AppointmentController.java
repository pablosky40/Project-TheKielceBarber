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

    // 1. PANEL PRINCIPAL (DASHBOARD ADMIN)
     // Asegúrate de importar esto

    @GetMapping("/admin-dashboard")
    public String showMasterPanel(Model model, Principal principal, HttpServletResponse response) {
        // ESTO EVITA QUE LA PÁGINA SE QUEDE CONGELADA AL CAMBIAR DE USUARIO
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
        response.setHeader("Expires", "0"); // Proxies.

        List<Appointment> allAppointments = appointmentRepository.findAll();
        
        // Tu filtro de bloqueos que ya funciona
        List<Appointment> onlyRealClients = allAppointments.stream()
                .filter(appt -> appt.getUser() != null)
                .filter(appt -> !"BLOCKED".equalsIgnoreCase(appt.getPaymentStatus()))
                .collect(Collectors.toList());

        model.addAttribute("appointments", onlyRealClients);
        model.addAttribute("totalCitas", onlyRealClients.size());
        
        loadGoogleData(model, principal);
        return "dashboard-admin";
    }

    // 2. PANEL DE GESTIÓN (SCHEDULE MASTER) - AQUÍ SALE TODO (INCLUIDO BLOQUEOS)
    @GetMapping("/admin")
    public String showAdminPanel(Model model, Principal principal) {
        model.addAttribute("appointments", appointmentRepository.findAll());
        loadGoogleData(model, principal);
        return "admin-appointments";
    }

    // 3. FORMULARIO DE RESERVA PARA USUARIOS (ACTUALIZADO PARA EL NUEVO CALENDARIO)
    @GetMapping("/new")
    public String showBookingForm(Model model, Principal principal) {
        // Filtramos los servicios para excluir el "BLOQUEO" de la vista del cliente
        List<Service> services = serviceRepository.findAll().stream()
                .filter(s -> !"BLOQUEO".equalsIgnoreCase(s.getName()))
                .collect(Collectors.toList());
        model.addAttribute("services", services);
        
        // Obtenemos solo los usuarios con rol BARBER
        List<User> barbers = userRepository.findAll().stream()
                .filter(u -> "BARBER".equals(u.getRole()))
                .collect(Collectors.toList());
        model.addAttribute("barbers", barbers);
        
        loadGoogleData(model, principal);
        return "appointment"; 
    }

    // 4. API DE DISPONIBILIDAD (PARA EL CALENDARIO VISUAL)
    @GetMapping("/check-availability")
    @ResponseBody
    public List<String> checkAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Long barberId) {

        // 1. Buscamos al barbero seleccionado
        User barber = userRepository.findById(barberId).get();

        // 2. Traemos TODAS las citas y bloqueos de ese día
        List<Appointment> dayEvents = appointmentRepository.findAll().stream()
                .filter(appt -> appt.getDate().equals(date))
                .collect(Collectors.toList());

        // 3. Filtramos las horas que deben desactivarse:
        return dayEvents.stream()
                .filter(appt -> 
                    // Caso A: Es un bloqueo de DÍA COMPLETO (venga de quien venga)
                    "FULL DAY".equals(appt.getTime()) || 
                    
                    // Caso B: Es un bloqueo de una hora específica
                    "BLOCKED".equals(appt.getPaymentStatus()) || 
                    
                    // Caso C: Es una cita de un cliente con ESE barbero específico
                    (appt.getUser() != null && barber.getName().equals(appt.getBarber()))
                )
                .map(Appointment::getTime)
                .distinct()
                .collect(Collectors.toList());
    }

    // 5. GUARDAR CITA CLIENTE
    @PostMapping("/create")
    public String createAppointment(
            @RequestParam Long serviceId,
            @RequestParam Long barberId, // Recibimos el barbero elegido
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
        appt.setBarber(barber.getName()); // <--- CRÍTICO: Guardamos el nombre del barbero
        appt.setPrice(service.getPrice());
        appt.setPaymentStatus("PENDING");
        
        appointmentRepository.save(appt);
        return "redirect:/appointments/checkout?id=" + appt.getId();
    }

    // 6. VER MIS CITAS (USUARIO)
    @GetMapping("/my-appointments")
    public String showMyAppointments(Model model, Principal principal, HttpServletResponse response) {
        // Forzamos que no haya caché para evitar ver datos del admin
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        
        // 1. Buscamos al usuario de forma segura por su email de Google
        User user = getOrCreateUser(principal.getName(), principal);
        
        // 2. Buscamos las citas de ESE usuario específico
        List<Appointment> userAppointments = appointmentRepository.findByUser(user);
        
        model.addAttribute("appointments", userAppointments);
        loadGoogleData(model, principal);
        return "dashboard-user";
    }

    // 7. BLOQUEAR HORA (ADMIN)
    @PostMapping("/admin/block")
    public String blockSchedule(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, 
                                @RequestParam String time) {
        // VALIDACIÓN: Solo permitir bloqueos de hoy en adelante
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

    // 8. BLOQUEAR DÍA COMPLETO (ADMIN)
    @PostMapping("/admin/block-day")
    public String blockFullDay(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            return "redirect:/appointments/admin?error=past_date";
        }

        Appointment block = new Appointment();
        block.setDate(date);
        block.setTime("FULL DAY");
        block.setPaymentStatus("BLOCKED");
        block.setBarber("The Kielce Barber");
        appointmentRepository.save(block);
        return "redirect:/appointments/admin";
    }

    // 9. CANCELAR / ELIMINAR
    @PostMapping("/cancel/{id}")
    public String cancelAppointment(@PathVariable Long id, 
                                    @RequestParam(value = "source", required = false) String source) {
        appointmentRepository.deleteById(id);
        if ("admin".equals(source)) {
            return "redirect:/appointments/admin";
        }
        return "redirect:/appointments/my-appointments";
    }
    //10 pagina de pagos
    @GetMapping("/checkout")
    public String showCheckout(@RequestParam("id") Long appointmentId, Model model, Principal principal) {
        // Buscamos la cita que se acaba de crear
        Appointment appt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        model.addAttribute("appointment", appt);
        loadGoogleData(model, principal);
        return "checkout"; // Esto buscará el archivo checkout.html
    }
    //comprobacion
    @PostMapping("/process-payment")
    public String processPayment(@RequestParam Long appointmentId, 
                                 @RequestParam String paymentMethod,
                                 @RequestParam(required = false) String cardNumber) {
        
        Appointment appt = appointmentRepository.findById(appointmentId).get();
        
        if ("ONLINE".equals(paymentMethod)) {
            // ESCENARIO NEGATIVO: Si el usuario pone "0000", simulamos fallo de pago
            if (cardNumber != null && cardNumber.contains("0000")) {
                return "redirect:/appointments/checkout?id=" + appointmentId + "&error=payment_failed";
            }
            appt.setPaymentStatus("PAID_ONLINE");
        } else {
            appt.setPaymentStatus("PENDING_LOCAL"); // Pago offline que requiere aprobación admin
        }
        
        appointmentRepository.save(appt);
        return "redirect:/appointments/my-appointments?success";
    }

    // --- MÉTODOS AUXILIARES ---

    private User getOrCreateUser(String emailOrId, Principal principal) {
        String email = emailOrId;

        // Si recibimos un token de Google, intentamos sacar el email real
        if (principal instanceof OAuth2AuthenticationToken token) {
            email = token.getPrincipal().getAttribute("email");
        }

        final String finalEmail = email;

        return userRepository.findByEmail(finalEmail).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(finalEmail);
            
            // Intentar sacar el nombre real para que no salga el ID numérico
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
    private void loadGoogleData(Model model, Principal principal) {
        if (principal instanceof OAuth2AuthenticationToken token) {
            model.addAttribute("userPhoto", token.getPrincipal().getAttribute("picture"));
            model.addAttribute("userName", token.getPrincipal().getAttribute("name"));
        }
    }
}