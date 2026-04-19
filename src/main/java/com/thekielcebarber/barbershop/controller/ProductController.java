package com.thekielcebarber.barbershop.controller;

import com.thekielcebarber.barbershop.model.Product;
import com.thekielcebarber.barbershop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    // 1. VISTA PÚBLICA (Catálogo para clientes)
    @GetMapping("/products")
    public String listProducts(Model model) {
        model.addAttribute("products", productRepository.findAll());
        return "products"; 
    }

    // 2. VISTA ADMIN (Panel de gestión para el barbero)
    @GetMapping("/products/admin")
    public String adminProducts(Model model) {
        model.addAttribute("products", productRepository.findAll());
        return "admin-products"; // <--- Este es el nombre del nuevo HTML que crearemos
    }

    // 3. ACCIÓN ACTUALIZAR (Procesa el cambio de precio y stock)
    @PostMapping("/products/admin/update")
    public String updateProduct(@RequestParam Long id, 
                                @RequestParam Double price, 
                                @RequestParam Integer stock) {
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + id));
        
        product.setPrice(price);
        product.setStock(stock);
        
        productRepository.save(product);
        
        // Redirigimos de vuelta al panel de productos para ver los cambios
        return "redirect:/products/admin";
    }

    // 4. ACCIÓN ELIMINAR (Opcional, por si dejas de vender algo)
    @PostMapping("/products/admin/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productRepository.deleteById(id);
        return "redirect:/products/admin";
    }
}