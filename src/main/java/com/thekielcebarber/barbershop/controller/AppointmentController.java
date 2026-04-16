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
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    // 1. MOSTRAR EL FORMULARIO DE RESERVA (Solo una vez y con la lista para bloquear horas)
    @GetMapping("/new")
    public String showBookingForm(Model model) {
        model.addAttribute("appointment", new Appointment());

        // Pasamos todas las citas existentes para que el JavaScript bloquee las horas ocupadas
        List<Appointment> existingAppointments = appointmentRepository.findAll();
        model.addAttribute("existingAppointments", existingAppointments);

        return "appointment";
    }

    // 2. GUARDAR LA CITA CON TODAS LAS VALIDACIONES
    @PostMapping("/save")
    public String saveAppointment(@ModelAttribute("appointment") Appointment appointment, 
                                  @AuthenticationPrincipal OAuth2User principal) {
        
        // A. Seguridad: Asignar el usuario que ha iniciado sesión
        if (principal != null) {
            String email = principal.getAttribute("email");
            Optional<User> user = userRepository.findByEmail(email);
            user.ifPresent(appointment::setUser);
        }

        // B. Validación de Servidor: No permitir fechas pasadas
        if (appointment.getDate() == null || appointment.getDate().isBefore(LocalDate.now())) {
            return "redirect:/appointments/new?error=pastdate";
        }

        // C. Validación de Duplicados
        boolean exists = appointmentRepository.existsByBarberAndDateAndTime(
                appointment.getBarber(), 
                appointment.getDate(), 
                appointment.getTime()
        );

        if (exists) {
            return "redirect:/appointments/new?error=already_booked";
        }

        // D. Guardar
        appointmentRepository.save(appointment);
        return "redirect:/dashboard";
    }

    // 3. APROBAR PAGO (Solo para Administradores)
    @GetMapping("/approve/{id}")
    public String approvePayment(@PathVariable Long id) {
        Optional<Appointment> apptOpt = appointmentRepository.findById(id);
        if (apptOpt.isPresent()) {
            Appointment appt = apptOpt.get();
            appt.setPaymentStatus("PAID");
            appointmentRepository.save(appt);
        }
        return "redirect:/dashboard";
    }

    // 4. CANCELAR CITA
    @PostMapping("/cancel/{id}")
    public String cancelAppointment(@PathVariable Long id) {
        appointmentRepository.deleteById(id);
        return "redirect:/dashboard";
    }
}