package com.thekielcebarber.barbershop.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Campos principales de la cita
    private LocalDateTime dateTime;
    private String status;         // Ejemplo: "CONFIRMED", "CANCELLED"
    private String paymentStatus;  // Ejemplo: "PAID", "PENDING" (Requisito de pago offline)

    // Si en el futuro quieres filtrar por cliente/barbero, 
    // añade estos campos para que el Repository no de error:
    private Long clientId;
    private Long barberId;

    // Constructor vacío (Obligatorio para JPA/Hibernate)
    public Appointment() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public Long getBarberId() { return barberId; }
    public void setBarberId(Long barberId) { this.barberId = barberId; }
}