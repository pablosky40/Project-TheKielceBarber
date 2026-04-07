package com.thekielcebarber.barbershop.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.HashMap;

@RestController
public class HomeController {

    @GetMapping("/")
    public String welcome() {
        return "<h1>Bienvenido a TheKielceBarber API</h1><p>Estado: ONLINE</p>";
    }

    @GetMapping("/api/status")
    public Map<String, String> status() {
        HashMap<String, String> map = new HashMap<>();
        map.put("project", "TheKielceBarber");
        map.put("status", "Running");
        map.put("version", "1.0.0-SNAPSHOT");
        return map;
    }
}