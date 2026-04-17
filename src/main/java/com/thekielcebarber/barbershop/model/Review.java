package com.thekielcebarber.barbershop.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer rating;
    private String comment;
    private LocalDate reviewDate;
    private String author; // Nombre para mostrar rápido
 // Dentro de tu clase Review.java
    private String response;

   
    // RELACIÓN: Muchas reseñas para UN usuario
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Review() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public LocalDate getReviewDate() { return reviewDate; }
    public void setReviewDate(LocalDate reviewDate) { this.reviewDate = reviewDate; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
}