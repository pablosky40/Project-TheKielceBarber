package com.thekielcebarber.barbershop.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "services")
@Getter 
@Setter 
@NoArgsConstructor // <--- ESTE ES VITAL PARA JPA

public class Service {
	// Borra @AllArgsConstructor y pon esto a mano dentro de la clase Service:
	public Service(Long id, String name, String description, BigDecimal price, Integer duration) {
	    this.id = id;
	    this.name = name;
	    this.description = description;
	    this.price = price;
	    this.durationMinutes = duration;
	}
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private BigDecimal price;
    private Integer durationMinutes;
}