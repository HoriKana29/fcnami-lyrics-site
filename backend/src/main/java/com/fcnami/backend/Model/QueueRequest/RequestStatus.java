package com.fcnami.backend.Model.QueueRequest;

public enum RequestStatus {
    WAITING, // รอคิว
    IN_PROGRESS, // กำลังตัดต่ออยู่นะ
    DONE, // เสร็จแล้วนะ
    REJECTED // เราจะเอามาใส่คิวเมื่ออนุมัติก่อนอยู่แล้ว ไม่ต้องมีก็ได้
}