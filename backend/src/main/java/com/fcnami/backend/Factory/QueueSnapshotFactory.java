package com.fcnami.backend.Factory;

import com.fcnami.backend.Model.QueueRequest.QueueSnapshot;
import com.fcnami.backend.Model.QueueRequest.QueueType;
import com.fcnami.backend.Model.QueueRequest.Request;

import java.util.UUID;

/**
 * Factory class for creating QueueSnapshot instances.
 */
public class QueueSnapshotFactory {
    
    /**
     * Precondition: Request, QueueType, and batch ID must not be null. Position must be non-negative.
     * Postcondition: Returns a fully initialized QueueSnapshot instance.
     * Side-effect: None.
     */
    public static QueueSnapshot create(Request request, QueueType type, int position, String batchId) {
        QueueSnapshot s = new QueueSnapshot();

        s.setRequest(request);
        s.setQueueType(type);
        s.setPosition(position);

        s.setBatchId(batchId);

        return s;
    }

    /**
     * Precondition: Request and QueueType must not be null. Position must be non-negative.
     * Postcondition: Returns a QueueSnapshot instance with a randomly generated batch ID.
     * Side-effect: None.
     */
    public static QueueSnapshot create(Request request, QueueType type, int position) {
        return create(request, type, position, "batch-" + UUID.randomUUID());
    }
}
