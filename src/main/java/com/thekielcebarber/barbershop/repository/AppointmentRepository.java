package com.thekielcebarber.barbershop.repository;

import com.thekielcebarber.barbershop.model.Appointment;
import com.thekielcebarber.barbershop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Para ver las citas de un cliente específico en su dashboard
    List<Appointment> findByUser(User user);

    // CRÍTICO: Para el calendario visual (Check availability por barbero)
    // Este método permite al controlador buscar qué horas tiene ocupadas un barbero concreto
    List<Appointment> findByDateAndBarber(LocalDate date, String barber);

    // Para evitar que dos personas reserven lo mismo al mismo tiempo (Validación de seguridad)
    boolean existsByBarberAndDateAndTime(String barber, LocalDate date, String time);
    
    // Para buscar bloqueos globales (cuando el barbero es "The Kielce Barber" o similar)
    List<Appointment> findByDateAndPaymentStatus(LocalDate date, String paymentStatus);
}