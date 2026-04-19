package com.thekielcebarber.barbershop.repository;

import com.thekielcebarber.barbershop.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional; // <--- ESTA IMPORTACIÓN ES CLAVE

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    
    Optional<Service> findByName(String name); // <--- TIENE QUE ESTAR AQUÍ
}