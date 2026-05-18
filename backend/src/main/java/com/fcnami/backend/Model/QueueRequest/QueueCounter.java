package com.fcnami.backend.Model.QueueRequest;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "queue_counter")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueueCounter {
    @Id
    @Enumerated(EnumType.STRING)
    private QueueType queueType;

    private Long lastOrder;
}
