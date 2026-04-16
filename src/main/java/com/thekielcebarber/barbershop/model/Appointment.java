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

    // Esta anotación es CLAVE para que el formulario HTML y Java se entiendan
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private String time;
    private String service;
    private String barber;
    private String paymentStatus = "PENDING";

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // 1. Constructor vacío (Obligatorio para JPA)
    public Appointment() {
    }

    // 2. Constructor completo
    public Appointment(Long id, LocalDate date, String time, String service, String barber, String paymentStatus, User user) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.service = service;
        this.barber = barber;
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

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getBarber() {
        return barber;
    }

    public void setBarber(String barber) {
        this.barber = barber;
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