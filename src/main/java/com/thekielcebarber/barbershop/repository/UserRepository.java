package com.thekielcebarber.barbershop.repository;

import com.thekielcebarber.barbershop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Mantén el antiguo para que no fallen los otros controladores
    Optional<User> findByEmail(String email);

    // Añade este nuevo para el Security "a prueba de balas"
    Optional<User> findByEmailIgnoreCase(String email);
}