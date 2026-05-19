package com.fcnami.backend.Model.QueueRequest;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor

// ใช้สร้าง object แบบ Builder Pattern
@Builder
@Entity
@Table(name = "queue_snapshots",
        indexes = {
                //  index สำหรับ ชุดของคิว
                @Index(name = "idx_snapshot_batch", columnList = "batchId"),
                // index สำหรับ Snapshot ข่วงนึงของ Request
                @Index(name = "idx_snapshot_request", columnList = "request_id"),
                // index เรียงตาม เวลา
                @Index(name = "idx_snapshot_time", columnList = "snapshotTime"),
                // index ชุดคิว + เวลา
                @Index(name = "idx_batch_time", columnList = "batchId, snapshotTime"),
                // index ชุดคิว + ประเภท
                @Index(name = "idx_batch_queue", columnList = "batchId, queueType")
        })
public class QueueSnapshot {

    // @Id เป็น Primary Key เหมือนรหัสของแต่ละแถวในตาราง
    // คุณสมบัติ : ไม่ซ้ำ/ not null / one per table
    @Id
    // ให้ Primary Key เพิ่มค่าเองอัตโนมัติ
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // relation

    // บอกว่า Snapshot นี้เป็นของรีเควสไหน
    // Many Snapshot ต่อ 1 Request
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    // snapshot value

    // ประเภทคิวที่ Request นี้อยู่
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QueueType queueType;

    // ตำแหน่งคิว ณ ตอน Snapshot
    @Column(nullable = false)
    private Integer position = 0;

    // group snapshot
    @Column(nullable = false)
    private String batchId;

    // time
    // ห้ามแก้ไขทีหลัง
    @Column(nullable = false, updatable = false)
    private LocalDateTime snapshotTime;

    // ก่อน insert set เวลาอัตโนมัติ
    @PrePersist
    protected void onCreate() {
        snapshotTime = LocalDateTime.now();
    }
}
