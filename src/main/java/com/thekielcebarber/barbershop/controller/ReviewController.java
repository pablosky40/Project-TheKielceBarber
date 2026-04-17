package com.thekielcebarber.barbershop.controller;

import com.thekielcebarber.barbershop.model.Review;
import com.thekielcebarber.barbershop.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    // 1. VISTA PÚBLICA: Mostrar todas las reseñas a los clientes
    @GetMapping
    public String showReviews(Model model, @AuthenticationPrincipal OAuth2User principal) {
        List<Review> allReviews = reviewRepository.findAll();
        model.addAttribute("reviews", allReviews);
        
        // Obtenemos el nombre para el saludo en la navbar
        String name = (principal != null) ? principal.getAttribute("name") : "Guest";
        model.addAttribute("userName", name);
        
        return "reviews";
    }

    // 2. ACCIÓN CLIENTE: Guardar una nueva reseña desde la web
    @PostMapping("/save")
    public String saveReview(@RequestParam String author, 
                             @RequestParam Integer rating, 
                             @RequestParam String comment) {
        Review review = new Review();
        review.setAuthor(author);
        review.setRating(rating);
        review.setComment(comment);
        review.setReviewDate(LocalDate.now());

        reviewRepository.save(review);
        return "redirect:/reviews";
    }

    // ======================================================
    // SECCIÓN DE ADMINISTRACIÓN: GESTIÓN PARA EL BARBERO
    // ======================================================

    // 3. VISTA ADMIN: Listado de moderación para el barbero
    @GetMapping("/admin")
    public String adminManageReviews(Model model) {
        List<Review> allReviews = reviewRepository.findAll();
        model.addAttribute("reviews", allReviews);
        // Retorna el archivo admin-reviews.html que tienes en templates
        return "admin-reviews"; 
    }

    // 4. ACCIÓN ADMIN: Publicar una respuesta oficial a una reseña
    // Solo actualiza el campo 'response', manteniendo el 'comment' intacto
    @PostMapping("/admin/update")
    public String replyToReview(@RequestParam Long id, 
                                @RequestParam String response) {
        
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid review Id:" + id));
        
        // Seteamos la respuesta del barbero sin tocar el comentario original
        review.setResponse(response);
        
        reviewRepository.save(review);
        return "redirect:/reviews/admin";
    }

    // 5. ACCIÓN ADMIN: Eliminar reseña (opcional, por si hay spam)
    @PostMapping("/admin/delete/{id}")
    public String adminDeleteReview(@PathVariable Long id) {
        reviewRepository.deleteById(id);
        return "redirect:/reviews/admin";
    }
}