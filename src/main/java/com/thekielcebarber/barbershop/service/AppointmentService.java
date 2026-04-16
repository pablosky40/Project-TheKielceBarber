package com.thekielcebarber.barbershop.service;

import com.thekielcebarber.barbershop.model.Appointment;
import com.thekielcebarber.barbershop.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.time.LocalDate;

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
        
        // --- CORRECCIÓN AQUÍ ---
        // Accedemos al email navegando por el objeto User: appt.getUser().getEmail()
        if (appt.getUser() != null) {
            System.out.println("LOG: Cita guardada con éxito para: " + appt.getUser().getEmail());
        } else {
            System.out.println("LOG: Cita guardada con éxito para un usuario invitado.");
        }
    }

    // 3. Aprobar pago offline
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

 // 5. Método de apoyo para el Controller
 // CAMBIA 'String date' por 'LocalDate date'
 public boolean existsByBarberAndDateAndTime(String barber, LocalDate date, String time) {
     return appointmentRepository.existsByBarberAndDateAndTime(barber, date, time);
 }
}