package com.thekielcebarber.barbershop.repository;

import com.thekielcebarber.barbershop.model.Appointment;
import com.thekielcebarber.barbershop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Método seguro: Busca las citas usando el objeto Usuario completo
    // Esto evita el error "No property 'email' found for type 'User'"
    List<Appointment> findByUser(User user);

    // Para el calendario: busca citas por fecha y barbero específico
    List<Appointment> findByDateAndBarber(LocalDate date, String barber);

    // Validación: evita duplicados (mismo barbero, misma hora, mismo día)
    boolean existsByBarberAndDateAndTime(String barber, LocalDate date, String time);
    
    // Para buscar bloqueos globales o estados específicos
    List<Appointment> findByDateAndPaymentStatus(LocalDate date, String paymentStatus);
}