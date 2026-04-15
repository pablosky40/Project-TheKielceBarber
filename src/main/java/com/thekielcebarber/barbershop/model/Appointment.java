package com.thekielcebarber.barbershop.model;

import jakarta.persistence.*;

@Entity
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String service;
    private String barber;
    private String date;
    private String time;
    private String paymentStatus;

    // RELACIÓN: Muchas citas pertenecen a UN usuario
    @ManyToOne(fetch = FetchType.EAGER) // Eager asegura que cargue el usuario al traer la cita
    @JoinColumn(name = "user_id", nullable = true) // Permite que haya citas sin usuario (invitados)
    private User user;
    public Appointment() {}

    // Getters y Setters actualizados
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getService() { return service; }
    public void setService(String service) { this.service = service; }
    public String getBarber() { return barber; }
    public void setBarber(String barber) { this.barber = barber; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}