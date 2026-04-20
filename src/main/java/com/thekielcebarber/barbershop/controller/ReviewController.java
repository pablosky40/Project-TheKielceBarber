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

    // 1. VISTA PÚBLICA (Para que los clientes vean y escriban)
    @GetMapping
    public String showPublicReviews(Model model) {
        model.addAttribute("reviews", reviewRepository.findAll());
        return "reviews"; // Carga reviews.html
    }

    // 2. VISTA ADMIN (Para que tú gestiones las reseñas)
    @GetMapping("/admin")
    public String showAdminReviews(Model model) {
        model.addAttribute("reviews", reviewRepository.findAll());
        model.addAttribute("reviewCount", reviewRepository.count());
        return "admin-reviews"; // Carga admin-reviews.html
    }

    // 3. ACCIÓN BORRAR RESEÑA (Solo para Admin)
    @PostMapping("/admin/delete/{id}")
    public String deleteReview(@PathVariable Long id) {
        reviewRepository.deleteById(id);
        return "redirect:/reviews/admin";
    }
}