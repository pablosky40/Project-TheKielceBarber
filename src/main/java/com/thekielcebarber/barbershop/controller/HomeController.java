package com.thekielcebarber.barbershop.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String welcome() {
        return "<h1>Welcome to The Kielce Barber API</h1>" +
               "<p>Explore our endpoints:</p>" +
               "<ul>" +
               "<li><a href='/api/services'>View Services</a></li>" +
               "<li><a href='/api/products'>View Products</a></li>" +
               "<li><a href='/api/reviews'>View Reviews</a></li>" +
               "</ul>";
    }
}