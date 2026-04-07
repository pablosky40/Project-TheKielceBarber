package com.thekielcebarber.barbershop.controller;

import com.thekielcebarber.barbershop.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller; // CAMBIO: de @RestController a @Controller
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    // Listar todas las citas en la tabla bonita
    @GetMapping
    public String listAppointments(Model model) {
        model.addAttribute("appointments", appointmentService.getAllAppointments());
        return "appointments"; // Busca appointments.html
    }

    // Aprobar el pago y redirigir de nuevo a la lista
    @GetMapping("/approve/{id}")
    public String approvePayment(@PathVariable Long id) {
        appointmentService.approveOfflinePayment(id);
        return "redirect:/appointments"; // Recarga la página para ver el cambio
    }
}