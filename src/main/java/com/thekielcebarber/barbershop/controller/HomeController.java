package com.thekielcebarber.barbershop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // 
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index"; // Esto busca el archivo index.html en templates
    }
    @GetMapping("/location")
    public String showLocation() {
        return "location"; // Esto le dice: "Busca un archivo llamado location.html"
    }
}