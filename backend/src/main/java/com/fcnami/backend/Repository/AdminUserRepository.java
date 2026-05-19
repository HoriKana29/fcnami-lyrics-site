package com.fcnami.backend.Repository;

import com.fcnami.backend.Model.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// ตอนนี้ยัง no usages อยู่
public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

    // Method หา admin จาก email (ไม่สนตัวพิมพ์เล็ก-ใหญ่)
    Optional<AdminUser> findByEmailIgnoreCase(String email);

    // Spring ตีความว่าแบบนี้ใช่มั้ย
//    SELECT * FROM admin_users
//    WHERE LOWER(email) = LOWER(?)
}
