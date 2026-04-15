package com.thekielcebarber.barbershop.config;

import com.thekielcebarber.barbershop.model.*;
import com.thekielcebarber.barbershop.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository; // Necesitamos esto para las relaciones

    @Override
    public void run(String... args) throws Exception {
        
        // 1. Cargar Servicios
        if (serviceRepository.count() == 0) {
            serviceRepository.save(new Service(null, "Haircut", "Classic style", 15.0, 30));
            serviceRepository.save(new Service(null, "Beard Trim", "Beard grooming", 10.0, 20));
            serviceRepository.save(new Service(null, "Full Service", "Haircut + Beard", 22.0, 50));
            System.out.println("✅ Servicios iniciales cargados.");
        }

        // 2. Cargar Productos
        if (productRepository.count() == 0) {
            productRepository.save(new Product(null, "Matte Wax", "Uppercut", 18.50, 10));
            productRepository.save(new Product(null, "Beard Oil", "Kielce Traditions", 12.00, 5));
            productRepository.save(new Product(null, "Aftershave", "Reuzel", 22.00, 8));
            productRepository.save(new Product(null, "Cera Mate Premium", "Kielce Style", 15.50, 20));
            productRepository.save(new Product(null, "Aceite para Barba Lux", "Kielce Style", 12.00, 15));
            System.out.println("✅ Productos iniciales cargados.");
        }

        // 3. Cargar Reseñas (CON RELACIÓN A USUARIO)
        if (reviewRepository.count() == 0) {
            // Creamos un usuario "Admin" o "System" para asociar estas reseñas iniciales
            User systemUser = userRepository.findByEmail("admin@kielcebarber.com").orElseGet(() -> {
                User u = new User();
                u.setEmail("admin@kielcebarber.com");
                u.setName("The Kielce Barber System");
                return userRepository.save(u);
            });

            // Creamos las reseñas usando setters para evitar líos con el constructor
            Review r1 = new Review();
            r1.setAuthor("Robert Lewandowski");
            r1.setRating(5);
            r1.setComment("Best haircut in Kielce! Highly recommended.");
            r1.setReviewDate(LocalDate.now());
            r1.setUser(systemUser); // Asignamos el usuario
            reviewRepository.save(r1);

            Review r2 = new Review();
            r2.setAuthor("Frédéric Chopin");
            r2.setRating(4);
            r2.setComment("Great products, the matte wax smells amazing.");
            r2.setReviewDate(LocalDate.now());
            r2.setUser(systemUser);
            reviewRepository.save(r2);

            Review r3 = new Review();
            r3.setAuthor("Juan Pablo II");
            r3.setRating(5);
            r3.setComment("Professional staff and very clean shop.");
            r3.setReviewDate(LocalDate.now().minusDays(2));
            r3.setUser(systemUser);
            reviewRepository.save(r3);

            System.out.println("✅ Reseñas iniciales cargadas con relación de usuario.");
        }
     // Dentro del método run de DataLoader.java
        if (userRepository.findByEmail("pablosantillana34@gmail.com").isEmpty()) {
            User admin = new User();
            admin.setEmail("pablosantillana34@gmail.com");
            admin.setName("Pablo Santillana (The Barber)");
            admin.setRole("BARBER");
            admin.setPassword("no_password_oauth2"); // Al usar Google, no necesita pass real
            userRepository.save(admin);
            System.out.println("✅ Barbero principal configurado: Pablo Santillana");
        }
    }
    
    
}