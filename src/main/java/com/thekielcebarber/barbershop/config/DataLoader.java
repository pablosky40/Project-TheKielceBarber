package com.thekielcebarber.barbershop.config;

import com.thekielcebarber.barbershop.model.Product;
import com.thekielcebarber.barbershop.model.Service;
import com.thekielcebarber.barbershop.repository.ProductRepository;
import com.thekielcebarber.barbershop.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        
        // Cargar Servicios si la tabla está vacía
        if (serviceRepository.count() == 0) {
        	serviceRepository.save(new Service(null, "Haircut", "Classic style", 15.0, 30));
        	serviceRepository.save(new Service(null, "Beard Trim", "Beard grooming", 10.0, 20));
            System.out.println(" Servicios iniciales cargados.");
        }

        // Cargar Productos si la tabla está vacía
        if (productRepository.count() == 0) {
            productRepository.save(new Product(null, "Matte Wax", "Uppercut", 18.50, 10));
            productRepository.save(new Product(null, "Beard Oil", "Kielce Traditions", 12.00, 5));
            productRepository.save(new Product(null, "Aftershave", "Reuzel", 22.00, 8));
            System.out.println("Productos iniciales cargados.");
        }
    }
}