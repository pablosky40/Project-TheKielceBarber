package com.thekielcebarber.barbershop.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal OAuth2User principal) {
        if (principal != null) {
            // Sacamos el nombre de la cuenta de Google
            model.addAttribute("userName", principal.getAttribute("name"));
        }
        return "dashboard"; // Llama a dashboard.html
    }
}