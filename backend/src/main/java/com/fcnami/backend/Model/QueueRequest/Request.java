package com.fcnami.backend.Model.QueueRequest;

import com.fcnami.backend.Model.SongTags.Song;
import com.fcnami.backend.Model.User;
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
@Table(name = "requests",
        indexes = {
                @Index(name = "idx_queue_order", columnList = "queueType, requestOrder"),
                @Index(name = "idx_requester", columnList = "requesterId"),
                @Index(name = "idx_normalized_key", columnList = "normalizedKey"),
                @Index(name = "idx_status", columnList = "status"),
                @Index(name = "idx_depth", columnList = "depthLevel"),
                @Index(name = "idx_replaced", columnList = "replaced_request_id"),
                @Index(name = "idx_user", columnList = "user_id")
        })
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Requester's ID
    @Column(nullable = false)
    private String requesterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // name to show
    private String requesterName;

    @Column(nullable = false)
    private String songTitle;

    @Column(nullable = false)
    private String artist;

    @ManyToOne
    @JoinColumn(name = "song_id")
    private Song song;

    // กันซ้ำ
    @Column(nullable = false,unique = true)
    private String normalizedKey;

    // Request's Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.WAITING;

    // Queue Type
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QueueType queueType;
    private Integer depthLevel;

    // Label
    @Column(nullable = false)
    private Integer requestOrder;

    // Replace Queue
    @ManyToOne
    @JoinColumn(name = "replaced_request_id")
    private Request replacedRequest;

    // ใครดึงไป??
    @OneToMany(mappedBy = "replacedRequest", fetch = FetchType.LAZY)
    private Set<Request> replacedBy;

    // Time
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Auto set time
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}