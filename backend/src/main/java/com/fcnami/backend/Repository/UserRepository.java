package com.fcnami.backend.Repository;

import com.fcnami.backend.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for managing {@link User} entities.
 * Provides data access operations for user accounts and attributes.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Precondition: The userIdentifier parameter must be a non-null, valid string.
     * Postcondition: Returns an Optional containing the User if found, or empty if not.
     * Side-effect: None
     */
    Optional<User> findByUserIdentifier(String userIdentifier);

    /**
     * Precondition: The userIdentifier parameter must be a non-null, valid string.
     * Postcondition: Returns true if a User with the given identifier exists, false otherwise.
     * Side-effect: None
     */
    boolean existsByUserIdentifier(String userIdentifier);
}
