package com.fcnami.backend.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
// สำหรับ Admin เอง
// สำหรับ Login / จัดการ Approve เพลง / จัดการคิว
@Entity
@Table(name = "admin_users", indexes = {
        @Index(name = "idx_admin_email", columnList = "email")
})
public class AdminUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Email ฉันเพื่อ Login ไว้
    @Column(nullable = false, unique = true)
    private String email;

    // เก็บ Password ที่ Hash แล้ว
    @Column(nullable = false)
    private String passwordHash;

    // Role เป็น ENUM ดีกว่ารึเปล่า??
    @Column(nullable = false)
    private String role = "ADMIN";

    // เวลาที่สร้าง Account
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // อาจจะต้องมีรู้ว่า account ถูกแก้ไขเมื่อไหร่
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
