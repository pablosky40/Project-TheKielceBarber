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

    // 1. Mostrar todas las reseñas
    @GetMapping
    public String showReviews(Model model, @AuthenticationPrincipal OAuth2User principal) {
        List<Review> allReviews = reviewRepository.findAll();
        model.addAttribute("reviews", allReviews);
        
        // OBTENEMOS EL NOMBRE DEL USUARIO LOGUEADO
        String name = (principal != null) ? principal.getAttribute("name") : "Guest";
        model.addAttribute("userName", name);
        
        return "reviews";
    }

    // 2. Guardar nueva reseña
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

    // 3. Eliminar reseña
    @PostMapping("/delete/{id}")
    public String deleteReview(@PathVariable Long id) {
        reviewRepository.deleteById(id);
        return "redirect:/reviews";
    }

    // 4. Editar reseña existente
    @PostMapping("/edit/{id}")
    public String editReview(@PathVariable Long id, 
                             @RequestParam Integer rating, 
                             @RequestParam String comment) {
        
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid review Id:" + id));
        
        review.setRating(rating);
        review.setComment(comment);
        review.setReviewDate(LocalDate.now()); // Actualiza la fecha al editar
        
        reviewRepository.save(review);
        return "redirect:/reviews";
    }
}