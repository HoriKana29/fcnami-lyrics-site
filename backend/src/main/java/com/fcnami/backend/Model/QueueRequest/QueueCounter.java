package com.fcnami.backend.Model.QueueRequest;

import jakarta.persistence.*;
import lombok.*;

// Entity นี้ใช้เก็บ "ตัวนับคิวล่าสุด" ของแต่ละ QueueType
// เพื่อใช้ Generate RequestOrder แบบต่อเนื่อง
@Entity
@Table(name = "queue_counter")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueueCounter {

    // ใช้ QueueType เป็น Primary Key
    @Id
    @Enumerated(EnumType.STRING)
    private QueueType queueType;

    // เก็บเลขลำดับล่าสดของ Queue นี้
    private Long lastOrder;
}
