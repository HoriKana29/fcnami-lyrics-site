package com.fcnami.backend.Model.QueueRequest;

import jakarta.persistence.*;
import lombok.*;

/**
 * QueueCounter tracks the last assigned request order or sequence number for a specific QueueType.
 * It is used to generate sequential request orders consistently.
 */
@Entity
@Table(name = "queue_counter")
@Getter
@Setter
@Builder
public class QueueCounter {

    @Id
    @Enumerated(EnumType.STRING)
    private QueueType queueType;

    private Long lastOrder;

    /**
     * Default constructor for JPA.
     *
     * Precondition: None.
     * Postcondition: A new uninitialized QueueCounter instance is created.
     * Side-effect: None.
     */
    public QueueCounter() {
    }

    /**
     * Constructs a QueueCounter with all fields.
     *
     * Precondition: None.
     * Postcondition: A new QueueCounter instance is fully initialized.
     * Side-effect: None.
     *
     * @param queueType the queue type identifier
     * @param lastOrder the last used order number
     */
    public QueueCounter(QueueType queueType, Long lastOrder) {
        this.queueType = queueType;
        this.lastOrder = lastOrder;
    }
}

