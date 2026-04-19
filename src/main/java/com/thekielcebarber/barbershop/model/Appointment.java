package com.thekielcebarber.barbershop.model;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private String time;

    // RELACIÓN CON SERVICIO (Según tu diagrama service_id -> Service.id)
    @ManyToOne
    @JoinColumn(name = "service_id")
    private Service service;

    private String barber;
    
    // Mantenemos el precio por si quieres guardar el precio histórico 
    // (por si el servicio sube de precio en el futuro, que la cita no cambie)
    private Double price; 
    
    private String paymentStatus = "PENDING";

    // RELACIÓN CON USUARIO (user_id -> User.id)
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // 1. Constructor vacío (Obligatorio para JPA)
    public Appointment() {
    }

    // 2. Constructor completo (Actualizado)
    public Appointment(Long id, LocalDate date, String time, Service service, String barber, Double price, String paymentStatus, User user) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.service = service;
        this.barber = barber;
        this.price = price;
        this.paymentStatus = paymentStatus;
        this.user = user;
    }

    // 3. Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public String getBarber() {
        return barber;
    }

    public void setBarber(String barber) {
        this.barber = barber;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}