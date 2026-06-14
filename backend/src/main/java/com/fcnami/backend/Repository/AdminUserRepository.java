package com.fcnami.backend.Repository;

import com.fcnami.backend.Model.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for managing {@link AdminUser} entities.
 * Handles database operations for authenticating and identifying system administrators.
 */
public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

    /**
     * Precondition: The email parameter must be a non-null, valid email string.
     * Postcondition: Returns an Optional containing the AdminUser matching the email (case-insensitive) if found, or empty otherwise.
     * Side-effect: None
     */
    Optional<AdminUser> findByEmailIgnoreCase(String email);
}
