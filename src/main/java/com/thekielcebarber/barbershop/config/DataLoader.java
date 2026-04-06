package com.thekielcebarber.barbershop.config;

import com.thekielcebarber.barbershop.model.Service;
import com.thekielcebarber.barbershop.repository.ServiceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initDatabase(ServiceRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.save(new Service(null, "Corte Clásico", "Corte con tijera", new BigDecimal("15.00"), 30));
                repository.save(new Service(null, "Barba", "Arreglo completo", new BigDecimal("10.00"), 20));
                System.out.println(">> Base de datos inicializada con servicios.");
            }
        };
    }
}