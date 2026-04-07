package com.thekielcebarber.barbershop.config;

import com.thekielcebarber.barbershop.model.Product;
import com.thekielcebarber.barbershop.model.Review;
import com.thekielcebarber.barbershop.model.Service;
import com.thekielcebarber.barbershop.model.Appointment;
import com.thekielcebarber.barbershop.repository.ProductRepository;
import com.thekielcebarber.barbershop.repository.ReviewRepository;
import com.thekielcebarber.barbershop.repository.ServiceRepository;
import com.thekielcebarber.barbershop.repository.AppointmentRepository;
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

    @Override
    public void run(String... args) throws Exception {
        
        // 1. Cargar Servicios si la tabla está vacía
        if (serviceRepository.count() == 0) {
            serviceRepository.save(new Service(null, "Haircut", "Classic style", 15.0, 30));
            serviceRepository.save(new Service(null, "Beard Trim", "Beard grooming", 10.0, 20));
            serviceRepository.save(new Service(null, "Full Service", "Haircut + Beard", 22.0, 50));
            System.out.println(" Servicios iniciales cargados.");
        }

        // 2. Cargar Productos si la tabla está vacía
        if (productRepository.count() == 0) {
            productRepository.save(new Product(null, "Matte Wax", "Uppercut", 18.50, 10));
            productRepository.save(new Product(null, "Beard Oil", "Kielce Traditions", 12.00, 5));
            productRepository.save(new Product(null, "Aftershave", "Reuzel", 22.00, 8));
            System.out.println(" Productos iniciales cargados.");
        }

        // 3. Cargar Reseñas si la tabla está vacía
        if (reviewRepository.count() == 0) {
            reviewRepository.save(new Review(null, 5, "Best haircut in Kielce! Highly recommended.", LocalDate.now()));
            reviewRepository.save(new Review(null, 4, "Great products, the matte wax smells amazing.", LocalDate.now()));
            reviewRepository.save(new Review(null, 5, "Professional staff and very clean shop.", LocalDate.now().minusDays(2)));
            System.out.println(" Reseñas iniciales cargadas.");
        }
     // Importante: Asegúrate de tener inyectado el appointmentRepository arriba con @Autowired

        if (appointmentRepository.count() == 0) {
            // Creamos una cita de ejemplo para hoy
            Appointment testApp = new Appointment();
            testApp.setDateTime(java.time.LocalDateTime.now());
            testApp.setStatus("CONFIRMED");
            testApp.setPaymentStatus("PENDING"); // Requisito de pago offline
            
            appointmentRepository.save(testApp);
            System.out.println("✅ Cita de prueba cargada.");
        }
    }
}