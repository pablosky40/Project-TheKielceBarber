package com.thekielcebarber.barbershop.service;

import com.thekielcebarber.barbershop.model.Appointment;
import com.thekielcebarber.barbershop.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private MessageProducer messageProducer; // Esto es lo que te faltaba declarar arriba

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public Appointment approveOfflinePayment(Long appointmentId) {
        // Buscamos la cita
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        // Cambiamos el estado
        appointment.setPaymentStatus("PAID");
        Appointment saved = appointmentRepository.save(appointment);
        
        // ENVIAR MENSAJE ASÍNCRONO (RabbitMQ)
        messageProducer.sendAppointmentNotification("¡Pago confirmado para la cita ID: " + saved.getId() + "!");
        
        return saved;
    }

    public Appointment createAppointment(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }
}