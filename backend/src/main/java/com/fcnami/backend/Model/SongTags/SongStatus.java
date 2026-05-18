package com.fcnami.backend.Model.SongTags;

public enum SongStatus {

    // เพิ่งสร้าง ไม่มีข้อมูลที่ป้อนลงไป
    DRAFT,

    // มีไอเดียว่าจะตัดเพลงนี้ละ
    IDEA,

    // อาจพิจารณาลดเหลือแค่ Editing เพราะเราทำทั้งสองอย่างพร้อมกัน
    TRANSLATING,
    EDITING,

    // พร้อม Upload ลง Youtube แล้ว
    READY_TO_UPLOAD,

    // ลงคลิปแล้ว รอ youtube โหลด(ไม่ต้องมีก็ได้)
    UPLOADED,

    // อาจพิจารณาลบสิ่งนี้ออก เพราะมันเสร็จพร้อมกับคลิป
    TRANSLATED,

    // ลงคลิปแล้ว ไปดูได้เลย
    PUBLISHED,

    // คลิปบิน
    ARCHIVED
}
