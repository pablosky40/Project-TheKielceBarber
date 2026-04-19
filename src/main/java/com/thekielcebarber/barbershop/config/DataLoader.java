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
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Override
    public void run(String... args) throws Exception {
        
        // 1. LOAD SERVICES
        if (serviceRepository.count() == 0) {
            serviceRepository.save(new Service(null, "Haircut", "Professional classic style haircut", 15.0, 30));
            serviceRepository.save(new Service(null, "Beard Trim", "Precision beard grooming and shaping", 10.0, 20));
            serviceRepository.save(new Service(null, "Full Service", "The complete experience: Haircut + Beard", 22.0, 50));
            
            // SERVICIO ESPECIAL PARA BLOQUEOS (Obligatorio para que el admin no de error)
            serviceRepository.save(new Service(null, "BLOQUEO", "Bloqueo de seguridad / Tienda cerrada", 0.0, 0));
            
            System.out.println("✅ Services loaded (including BLOQUEO).");
        }

        // 2. LOAD PREMIUM CATALOG
        if (productRepository.count() == 0) {
            productRepository.save(new Product(
                null, 
                "Premium Matte Wax", 
                "Uppercut Deluxe", 
                "Provides a medium-strong hold with a natural matte finish. Ideal for textured styles.", 
                "/images/wax.jpg", 
                18.50, 
                15
            ));

            productRepository.save(new Product(
                null, 
                "Sandalwood Beard Oil", 
                "Kielce Traditions", 
                "Hydrates the skin beneath the beard while softening facial hair. Premium wood scent.", 
                "/images/oil.jpg", 
                12.00, 
                20
            ));

            productRepository.save(new Product(
                null, 
                "Aftershave Cooling Gel", 
                "Reuzel", 
                "Instantly soothes skin irritation after shaving. Formulated with aloe vera.", 
                "/images/aftershave.jpg", 
                22.00, 
                10
            ));

            productRepository.save(new Product(
                null, 
                "Professional Sea Salt Spray", 
                "Kielce Style", 
                "Adds volume and a beachy texture to your hair. Perfect for messy styles.", 
                "/images/salt-spray.jpg", 
                16.00, 
                12
            ));

            productRepository.save(new Product(
                null, 
                "Luxury Shaving Cream", 
                "Proraso", 
                "Rich, creamy lather that protects the skin from the blade for an effortless shave.", 
                "/images/shaving-cream.jpg", 
                14.50, 
                25
            ));

            System.out.println("✅ Premium Catalog with LOCAL images loaded.");
        }

        // 3. CONFIGURE MASTER BARBERS
        crearBarbero("pablosantillana34@gmail.com", "Pablo Santillana (The Barber)");
        crearBarbero("cllope04@ucm.es", "Claudia López (The Barber)");

        // 4. LOAD LEGENDARY REVIEWS
        if (reviewRepository.count() == 0) {
            
            Review r1 = new Review();
            r1.setAuthor("Robert Lewandowski");
            r1.setRating(5);
            r1.setComment("The best haircut in Kielce. Perfect for a Champions League night!");
            r1.setReviewDate(LocalDate.now().minusDays(2));
            reviewRepository.save(r1);

            Review r2 = new Review();
            r2.setAuthor("Frédéric Chopin");
            r2.setRating(5);
            r2.setComment("A very delicate and artistic touch. My hair feels like a nocturne.");
            r2.setReviewDate(LocalDate.now().minusDays(5));
            reviewRepository.save(r2);

            Review r3 = new Review();
            r3.setAuthor("Juan Pablo II");
            r3.setRating(5);
            r3.setComment("A truly peaceful and welcoming place. Highly recommended.");
            r3.setReviewDate(LocalDate.now().minusMonths(1));
            reviewRepository.save(r3);

            Review r4 = new Review();
            r4.setAuthor("Nicolaus Copernicus");
            r4.setRating(5);
            r4.setComment("The barber doesn't just cut hair, he makes the world revolve around you!");
            r4.setReviewDate(LocalDate.now().minusDays(10));
            reviewRepository.save(r4);

            System.out.println("✅ Legendary Reviews loaded successfully.");
        }
    }

    private void crearBarbero(String email, String nombre) {
        if (userRepository.findByEmail(email).isEmpty()) {
            User admin = new User();
            admin.setEmail(email);
            admin.setName(nombre);
            admin.setRole("BARBER");
            admin.setPassword("no_password_oauth2");
            userRepository.save(admin);
            System.out.println("✅ Barber configured: " + nombre);
        }
    }
}