package com.thekielcebarber.barbershop.service;

import com.thekielcebarber.barbershop.model.Product;
import com.thekielcebarber.barbershop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    // --- ESTO ES LO QUE FALTABA PARA EL ERROR DE LA LÍNEA 43 ---
    public Product updateProduct(Long id, Product updatedProduct) {
        return productRepository.findById(id).map(product -> {
            product.setName(updatedProduct.getName());
            product.setPrice(updatedProduct.getPrice());
            // Si tu clase Product tiene descripción o stock, añádelos aquí también
            return productRepository.save(product);
        }).orElse(null);
    }

    // --- ESTO ES LO QUE FALTABA PARA EL ERROR DE LA LÍNEA 54 ---
    public boolean deleteProduct(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }
}