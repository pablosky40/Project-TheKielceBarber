package com.thekielcebarber.barbershop.repository;

import com.thekielcebarber.barbershop.model.Appointment;
import com.thekielcebarber.barbershop.model.User; // Importante para que reconozca el objeto
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    // Cambiamos findByUserEmail por findByUser para que acepte el objeto completo
    List<Appointment> findByUser(User user);

    // Mantenemos tu validador de disponibilidad
    boolean existsByBarberAndDateAndTime(String barber, String date, String time);
}