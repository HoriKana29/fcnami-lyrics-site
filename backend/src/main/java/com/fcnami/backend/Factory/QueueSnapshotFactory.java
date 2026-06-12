package com.fcnami.backend.Factory;

import com.fcnami.backend.Model.QueueRequest.QueueSnapshot;
import com.fcnami.backend.Model.QueueRequest.QueueType;
import com.fcnami.backend.Model.QueueRequest.Request;

import java.util.UUID;

public class QueueSnapshotFactory {
    public static QueueSnapshot create(Request request, QueueType type, int position, String batchId) {
        QueueSnapshot s = new QueueSnapshot();

        s.setRequest(request);
        s.setQueueType(type);
        s.setPosition(position);

        s.setBatchId(batchId);

        return s;
    }

    public static QueueSnapshot create(Request request, QueueType type, int position) {
        return create(request, type, position, "batch-" + UUID.randomUUID());
    }
}
