package com.thekielcebarber.barbershop.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne
    @JoinColumn(name = "barber_id", nullable = false)
    private User barber;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    private String status; // PENDING, CONFIRMED, CANCELLED, PAID
}