package com.thekielcebarber.barbershop.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IntentController {
    @GetMapping("/set-intent")
    public void setIntent(@RequestParam String intent, HttpSession session) {
        session.setAttribute("AUTH_INTENT", intent);
    }
}