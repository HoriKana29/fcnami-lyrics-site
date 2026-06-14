package com.fcnami.backend.Factory;

import com.fcnami.backend.Model.User;

import java.util.UUID;

/**
 * Factory class for creating User instances.
 */
public class UserFactory {
    
    public static final int RANDOM_SUBSTRING_LENGTH = 8;

    /**
     * Precondition: User identifier and username must be non-null and non-blank.
     * Postcondition: Returns a new User instance initialized via builder.
     * Side-effect: None.
     */
    public static User create(String userIdentifier, String username, String email) {
        return User.builder()
                .userIdentifier(requireText(userIdentifier, "userIdentifier"))
                .username(requireText(username, "username"))
                .email(trimOrNull(email))
                .totalRequests(0)
                .activeRequests(0)
                .build();
    }
    
    /**
     * Precondition: None.
     * Postcondition: Returns a User instance initialized with random default identifier, username, and email.
     * Side-effect: None.
     */
    public static User createDefault() {
        return create(
                generateYoutubeUserIdentifier(),
                generateDefaultUsername(),
                generateDefaultEmail()
        );
    }

    /**
     * Precondition: None.
     * Postcondition: Returns a randomly generated default email address.
     * Side-effect: None.
     */
    private static String generateDefaultEmail() {
        return UUID.randomUUID().toString().substring(0, RANDOM_SUBSTRING_LENGTH) + "@mail.com";
    }

    /**
     * Precondition: None.
     * Postcondition: Returns a randomly generated default username.
     * Side-effect: None.
     */
    private static String generateDefaultUsername() {
        return "user_" + UUID.randomUUID().toString().substring(0, RANDOM_SUBSTRING_LENGTH);
    }

    /**
     * Precondition: None.
     * Postcondition: Returns a randomly generated unique YouTube user identifier prefixed with 'yt_'.
     * Side-effect: None.
     */
    private static String generateYoutubeUserIdentifier() {
        return "yt_" + UUID.randomUUID();
    }

    /**
     * Precondition: Value and field name are provided.
     * Postcondition: Returns the trimmed value, or throws IllegalArgumentException if empty/null.
     * Side-effect: None.
     */
    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    /**
     * Precondition: Value string is provided.
     * Postcondition: Returns the trimmed value, or null if empty/null.
     * Side-effect: None.
     */
    private static String trimOrNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
