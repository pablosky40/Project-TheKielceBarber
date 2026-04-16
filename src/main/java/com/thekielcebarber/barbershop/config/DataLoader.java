package com.thekielcebarber.barbershop.config;

import com.thekielcebarber.barbershop.model.*;
import com.thekielcebarber.barbershop.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired 
    private ServiceRepository serviceRepository;
    
    @Autowired 
    private ProductRepository productRepository;
    
    @Autowired 
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        
        // 1. LOAD SERVICES
        if (serviceRepository.count() == 0) {
            serviceRepository.save(new Service(null, "Haircut", "Professional classic style haircut", 15.0, 30));
            serviceRepository.save(new Service(null, "Beard Trim", "Precision beard grooming and shaping", 10.0, 20));
            serviceRepository.save(new Service(null, "Full Service", "The complete experience: Haircut + Beard", 22.0, 50));
            System.out.println("✅ Services loaded.");
        }

     // 2. LOAD PREMIUM CATALOG (With local images)
        if (productRepository.count() == 0) {
            productRepository.save(new Product(
                null, 
                "Premium Matte Wax", 
                "Uppercut Deluxe", 
                "Provides a medium-strong hold with a natural matte finish. Ideal for textured styles.", 
                "/images/wax.jpg", // <-- RUTA LOCAL
                18.50, 
                15
            ));

            productRepository.save(new Product(
                null, 
                "Sandalwood Beard Oil", 
                "Kielce Traditions", 
                "Hydrates the skin beneath the beard while softening facial hair. Premium wood scent.", 
                "/images/oil.jpg", // <-- RUTA LOCAL (¡Adiós perrito!)
                12.00, 
                20
            ));

            productRepository.save(new Product(
                null, 
                "Aftershave Cooling Gel", 
                "Reuzel", 
                "Instantly soothes skin irritation after shaving. Formulated with aloe vera.", 
                "/images/aftershave.jpg", // <-- RUTA LOCAL
                22.00, 
                10
            ));

            productRepository.save(new Product(
                null, 
                "Professional Sea Salt Spray", 
                "Kielce Style", 
                "Adds volume and a beachy texture to your hair. Perfect for messy styles.", 
                "/images/salt-spray.jpg", // <-- RUTA LOCAL
                16.00, 
                12
            ));

            productRepository.save(new Product(
                null, 
                "Luxury Shaving Cream", 
                "Proraso", 
                "Rich, creamy lather that protects the skin from the blade for an effortless shave.", 
                "/images/shaving-cream.jpg", // <-- RUTA LOCAL
                14.50, 
                25
            ));

            System.out.println("✅ Premium Catalog with LOCAL images loaded.");
        }

        // 3. CONFIGURE MASTER BARBERS (Pablo & Claudia)
        crearBarbero("pablosantillana34@gmail.com", "Pablo Santillana (The Barber)");
        crearBarbero("cllope04@ucm.es", "Claudia López (The Barber)");
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