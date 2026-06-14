package com.fcnami.backend.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * AdminUser represents a system administrator who has permissions to modify resources,
 * such as the song catalog and synchronizing the request queue.
 */
@Getter
@Setter
@Entity
@Table(name = "admin_users", indexes = {
        @Index(name = "idx_admin_email", columnList = "email")
})
public class AdminUser {

    /**
     * The default role assigned to system administrators.
     */
    public static final String DEFAULT_ROLE = "ADMIN";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String role = DEFAULT_ROLE;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Default constructor for JPA.
     *
     * Precondition: None.
     * Postcondition: A new uninitialized AdminUser instance is created.
     * Side-effect: None.
     */
    public AdminUser() {
    }

    /**
     * Entity lifecycle callback executed before the record is persisted.
     *
     * Precondition: The entity is about to be saved for the first time.
     * Postcondition: The createdAt field is populated with the current date and time.
     * Side-effect: None.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

