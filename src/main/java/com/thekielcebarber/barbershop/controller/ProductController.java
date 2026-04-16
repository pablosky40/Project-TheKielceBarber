package com.thekielcebarber.barbershop.controller;

import com.thekielcebarber.barbershop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller; // Asegúrate de que sea @Controller
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // <-- CAMBIA @RestController por @Controller
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/products")
    public String listProducts(Model model) {
        // Pasamos la lista de productos al HTML
        model.addAttribute("products", productRepository.findAll());
        return "products"; // <-- Esto busca el archivo templates/products.html
    }
}