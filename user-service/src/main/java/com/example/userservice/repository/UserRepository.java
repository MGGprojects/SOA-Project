package com.example.userservice.repository;

import com.example.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /** Check if a user with the given email already exists */
    boolean existsByEmail(String email);

    /** Find a user by email */
    Optional<User> findByEmail(String email);

    /** Find a user by auth-service ID */
    Optional<User> findByAuthUserId(Long authUserId);
}
