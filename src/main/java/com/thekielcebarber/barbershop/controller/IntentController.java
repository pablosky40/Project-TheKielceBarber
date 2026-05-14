package com.thekielcebarber.barbershop.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IntentController {

    @GetMapping("/set-intent")
    public void setIntent(@RequestParam String intent, HttpServletRequest request) {
        // Guardamos en la sesión si el usuario quiere 'login' o 'register'
        request.getSession().setAttribute("AUTH_INTENT", intent);
        System.out.println("DEBUG: Intención guardada en sesión -> " + intent);
    }
}