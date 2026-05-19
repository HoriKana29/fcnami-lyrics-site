package com.fcnami.backend.Model;

import com.fcnami.backend.Model.QueueRequest.Request;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

// Entity สำหรับ "ผู้ใช้ที่ขอเพลง"
// ใช้แทน YouTube user)
@Entity
@Table(name = "users",
        indexes = {
                // ใช้ค้นหาจาก userIdentifier
                @Index(name = "idx_user_identifier",
                        columnList = "userIdentifier")
        })
// แสดงว่าใช้ id ในการเปรียบเทียบเท่านั้น
@EqualsAndHashCode(of = "id")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userIdentifier;
    // YouTube Channel ID

    @Column(nullable = false)
    private String username; // display name

    private String email; // optional

    // จำนวน request ทั้งหมดที่เคยส่ง(เดี๋ยวจะให้ไปไล่เช็คใน Youtube ช่องเราอีกครั้ง)
    @Builder.Default
    private Integer totalRequests = 0;

    // จำนวน Request ที่อยู่ในคิว
    @Builder.Default
    private Integer activeRequests = 0;

    // Request's Bond
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL,
             fetch = FetchType.LAZY)
    private Set<Request> requests;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}