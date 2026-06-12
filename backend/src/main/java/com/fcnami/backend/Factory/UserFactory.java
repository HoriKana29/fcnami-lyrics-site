package com.fcnami.backend.Factory;

import com.fcnami.backend.Model.User;

import java.util.UUID;

public class UserFactory {
    public static User create(String userIdentifier, String username, String email) {
        return User.builder()
                .userIdentifier(requireText(userIdentifier, "userIdentifier"))
                .username(requireText(username, "username"))
                .email(trimOrNull(email))
                .totalRequests(0)
                .activeRequests(0)
                .build();
    }
    public static User createDefault() {
        return create(
                generateYoutubeUserIdentifier(),
                generateDefaultUsername(),
                generateDefaultEmail()
        );
    }

    private static String generateDefaultEmail() {
        return UUID.randomUUID().toString().substring(0, 8) + "@mail.com";
    }

    private static String generateDefaultUsername() {
        return "user_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private static String generateYoutubeUserIdentifier() {
        return "yt_" + UUID.randomUUID();
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    private static String trimOrNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
