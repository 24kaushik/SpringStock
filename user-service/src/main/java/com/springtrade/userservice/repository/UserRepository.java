package com.springtrade.userservice.repository;

import com.springtrade.userservice.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByEmail(@NotBlank(message = "Email cannot be blank") @Email(message = "Email should be valid") String email);

    Optional<User> findByEmail(@NotBlank(message = "Email cannot be blank") @Email(message = "Email should be valid") String email);
}
