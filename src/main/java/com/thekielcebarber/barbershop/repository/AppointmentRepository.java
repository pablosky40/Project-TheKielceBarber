package com.thekielcebarber.barbershop.repository;

import com.thekielcebarber.barbershop.model.Appointment;
import com.thekielcebarber.barbershop.model.User; // ¡Asegúrate de importar User!
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDate;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // ESTA ES LA LÍNEA QUE TE FALTA Y QUE ARREGLA EL ERROR
    List<Appointment> findByUser(User user);

    // El método que añadimos antes para los duplicados
    boolean existsByBarberAndDateAndTime(String barber, LocalDate date, String time);
}