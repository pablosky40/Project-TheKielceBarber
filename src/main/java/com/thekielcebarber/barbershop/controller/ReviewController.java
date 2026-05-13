package com.thekielcebarber.barbershop.controller;

import com.thekielcebarber.barbershop.model.Review;
import com.thekielcebarber.barbershop.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    // 1. VISTA PÚBLICA
    @GetMapping
    public String showPublicReviews(Model model) {
        model.addAttribute("reviews", reviewRepository.findAll());
        return "reviews"; 
    }

    // 2. VISTA ADMIN
    @GetMapping("/admin")
    public String showAdminReviews(Model model) {
        model.addAttribute("reviews", reviewRepository.findAll());
        model.addAttribute("reviewCount", reviewRepository.count());
        return "admin-reviews"; 
    }

    // 3. ACCIÓN BORRAR RESEÑA
    @PostMapping("/admin/delete/{id}")
    public String deleteReview(@PathVariable Long id) {
        reviewRepository.deleteById(id);
        return "redirect:/reviews/admin";
    }

    // 4. NUEVA ACCIÓN: RESPONDER/ACTUALIZAR RESEÑA
    // Esta es la que te estaba dando el error 404
    @PostMapping("/admin/update")
    public String updateReview(@RequestParam Long id, @RequestParam String response) {
        // Buscamos la reseña por ID, le ponemos tu respuesta y guardamos
        reviewRepository.findById(id).ifPresent(review -> {
            review.setResponse(response);
            reviewRepository.save(review);
        });
        
        // La clave: Redirigir de vuelta a la lista de reseñas de admin
        return "redirect:/reviews/admin";
    }
}