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
@Entity
@Table(name = "users",
        indexes = {
                @Index(name = "idx_user_identifier",
                        columnList = "userIdentifier")
        })
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

    @Builder.Default
    private Integer totalRequests = 0;
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