package com.thekielcebarber.barbershop.service;

import com.thekielcebarber.barbershop.model.Appointment;
import com.thekielcebarber.barbershop.repository.AppointmentRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public void createAndNotifyAppointment(Appointment appt) {
        // Guardar en H2
        appointmentRepository.save(appt);

        // Crear el mensaje detallado para RabbitMQ
        String message = String.format(
            "NEW RESERVATION | Customer: %s | Service: %s | Barber: %s | Date: %s at %s",
            appt.getUserEmail(), 
            appt.getService(), 
            appt.getBarber(), 
            appt.getDate(), 
            appt.getTime()
        );

        // Enviar a la cola
        rabbitTemplate.convertAndSend("appointmentQueue", message);

        System.out.println("--------------------------------------------------");
        System.out.println("DATABASE: Appointment saved successfully!");
        System.out.println("RABBITMQ: Message sent: " + message);
        System.out.println("--------------------------------------------------");
    }
 // En AppointmentService.java
    public void approveOfflinePayment(Long id) {
        appointmentRepository.findById(id).ifPresent(appt -> {
            appt.setPaymentStatus("PAID");
            appointmentRepository.save(appt);
            
            // Creamos el mismo formato de mensaje detallado que en la reserva
            String message = String.format(
                "MANUAL APPROVAL | ID: %d | Customer: %s | Service: %s | Barber: %s",
                appt.getId(),
                appt.getUserEmail(),
                appt.getService(),
                appt.getBarber()
            );
            
            rabbitTemplate.convertAndSend("appointmentQueue", message);
            System.out.println("RABBITMQ: Sent Manual Approval Message: " + message);
        });
    
    }
}