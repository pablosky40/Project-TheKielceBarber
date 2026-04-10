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

    // 1. Obtener todas las citas (General)
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    // 2. Crear y Notificar (Con validación de duplicados)
    public void createAndNotifyAppointment(Appointment appt) {
        
        // VALIDACIÓN: ¿Ya existe este barbero a esta hora este día?
        // Nota: Asegúrate de que los nombres coincidan con tu modelo (barber o barberName)
        boolean exists = appointmentRepository.existsByBarberAndDateAndTime(
            appt.getBarber(), 
            appt.getDate(), 
            appt.getTime()
        );

        if (exists) {
            // Si ya existe, lanzamos una excepción que capturará el Controller
            throw new IllegalStateException("Lo sentimos, este horario ya está reservado para este barbero.");
        }

        // Si no existe, guardamos la cita
        appointmentRepository.save(appt);
        
        // Aquí es donde RabbitMQ enviaría la notificación (si lo tienes configurado)
        System.out.println("LOG: Cita guardada con éxito para: " + appt.getUserEmail());
    }

    // 3. Aprobar pago offline (Si lo usas en tu flujo)
    public void approveOfflinePayment(Long id) {
        appointmentRepository.findById(id).ifPresent(appt -> {
            appt.setPaymentStatus("PAID");
            appointmentRepository.save(appt);
        });
    }

    // 4. Borrar cita (Para el botón CANCEL del Dashboard)
    public void deleteAppointment(Long id) {
        if (appointmentRepository.existsById(id)) {
            appointmentRepository.deleteById(id);
            System.out.println("LOG: Cita ID " + id + " cancelada correctamente.");
        }
    }
 // Añade esto dentro de tu AppointmentService.java
    public boolean existsByBarberAndDateAndTime(String barber, String date, String time) {
        return appointmentRepository.existsByBarberAndDateAndTime(barber, date, time);
    }
}