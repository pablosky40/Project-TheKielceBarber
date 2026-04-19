package com.thekielcebarber.barbershop.controller;

import com.thekielcebarber.barbershop.model.Product;
import com.thekielcebarber.barbershop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequestMapping("/products") // Agrupamos las rutas para que sea más limpio
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    // 1. VISTA PÚBLICA
    @GetMapping
    public String listProducts(Model model) {
        model.addAttribute("products", productRepository.findAll());
        return "products"; 
    }

    // 2. VISTA ADMIN
    @GetMapping("/admin")
    public String adminProducts(Model model) {
        model.addAttribute("products", productRepository.findAll());
        return "admin-products";
    }

    // 3. ACCIÓN AÑADIR PRODUCTO (Nueva)
    @PostMapping("/admin/add")
    public String addProduct(@ModelAttribute Product product, 
                             @RequestParam("imageFile") MultipartFile imageFile) {
    	try {
    	    if (!imageFile.isEmpty()) {
    	        String fileName = imageFile.getOriginalFilename();
    	        Path path = Paths.get("src/main/resources/static/images/" + fileName);
    	        
    	        // CORRECCIÓN AQUÍ: Primero el path, luego los bytes del archivo subido
    	        Files.write(path, imageFile.getBytes());
    	        
    	        // Revisa que en tu modelo Product exista el método setImage
    	        product.setImageUrl("/images/" + fileName);
    	    } else {
    	        product.setImageUrl("/images/default-product.jpg");
    	    }
    	    productRepository.save(product);
    	} catch (IOException e) {
    	    e.printStackTrace();
    	
        }
        return "redirect:/products/admin";
    }

    // 4. ACCIÓN ACTUALIZAR
    @PostMapping("/admin/update")
    public String updateProduct(@RequestParam Long id, 
                                @RequestParam Double price, 
                                @RequestParam Integer stock) {
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        
        product.setPrice(price);
        product.setStock(stock);
        
        productRepository.save(product);
        return "redirect:/products/admin";
    }

    // 5. ACCIÓN ELIMINAR
    @PostMapping("/admin/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productRepository.deleteById(id);
        return "redirect:/products/admin";
    }
}