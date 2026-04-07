package com.thekielcebarber.barbershop.controller;

import com.thekielcebarber.barbershop.model.Appointment;
import com.thekielcebarber.barbershop.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    // Cambiamos temporalmente a GetMapping para que puedas probarlo escribiendo en el navegador
    @GetMapping("/{id}/approve-payment")
    public Appointment approvePayment(@PathVariable Long id) {
        return appointmentService.approveOfflinePayment(id);
    }
}