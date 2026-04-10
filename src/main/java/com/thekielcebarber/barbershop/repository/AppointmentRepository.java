package com.thekielcebarber.barbershop.repository;

import com.thekielcebarber.barbershop.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List; // Importante este import

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByUserEmail(String email);

    // NUEVO: Busca si ya hay una cita con estos tres parámetros
    boolean existsByBarberAndDateAndTime(String barber, String date, String time);
}