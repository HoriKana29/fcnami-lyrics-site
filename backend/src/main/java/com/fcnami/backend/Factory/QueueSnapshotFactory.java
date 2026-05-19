package com.fcnami.backend.Factory;

import com.fcnami.backend.Model.QueueRequest.QueueSnapshot;
import com.fcnami.backend.Model.QueueRequest.QueueType;
import com.fcnami.backend.Model.QueueRequest.Request;

// Class 'QueueSnapshotFactory' is never used
public class QueueSnapshotFactory {
    public static QueueSnapshot create(Request request, QueueType type, int position, String batchId) {
        QueueSnapshot s = new QueueSnapshot();

        s.setRequest(request);
        s.setQueueType(type);
        s.setPosition(position);

        s.setBatchId(batchId);

        return s;
    }

    // Convenience overload (still requires batchId)
    // @Column(nullable = false) รึเปล่า -> DB อาจ error ตอน save
    // overload → กรณีไม่อยากใส่ batchId
    public static QueueSnapshot create(Request request, QueueType type, int position) {
        return create(request, type, position, null);
    }
}
