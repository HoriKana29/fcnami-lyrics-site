package com.fcnami.backend.Config;

import com.fcnami.backend.Model.AdminUser;
import com.fcnami.backend.Repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * AdminDataSeeder ensures at least one admin user exists in the system.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminDataSeeder implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String adminEmail = "admin@fcnami.com";
        if (adminUserRepository.findByEmailIgnoreCase(adminEmail).isEmpty()) {
            log.info("No admin user found. Creating default admin account...");
            
            AdminUser admin = new AdminUser();
            admin.setEmail(adminEmail);
            admin.setPasswordHash(passwordEncoder.encode("admin1234"));
            admin.setRole("ADMIN");
            
            adminUserRepository.save(admin);
            log.info("Default admin account created: {}", adminEmail);
        } else {
            log.info("Admin account already exists.");
        }
    }
}
