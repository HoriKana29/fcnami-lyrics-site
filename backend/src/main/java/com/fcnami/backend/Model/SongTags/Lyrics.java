package com.fcnami.backend.Model.SongTags;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
// Class Lyrics เป็น Entity ของ JPA ( Java Persistence API )
// ** Entity : Class ที่เป็นตัวแทนของ Table ใน Database ในรูปแบบ Object
// ใช้เก็บข้อมูล "เนื้อเพลง" ของแต่ละเพลง โดยแยกเป็นหลายรูปแบบ (kanji, romaji, thai, notes)
@Entity
@Table(name = "lyrics")
public class Lyrics {

    // @Id เป็น Primary Key เหมือนรหัสของแต่ละแถวในตาราง
    // คุณสมบัติ : ไม่ซ้ำ/ not null / one per table
    @Id
    // ให้ Primary Key เพิ่มค่าเองอัตโนมัติ
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ความสัมพันธ์ OneToOne : 1 เพลง <-> 1 Lyrics
    // fetch Lazy : โหลดข้อมูลเมื่อเรียกใช้
    // *** EAGER โหลดทันที การันตีใช้งาน
    @OneToOne(fetch = FetchType.LAZY)

    // song_id คือ foreign key -> ดึงมาจาก id
    // ห้าม null / ห้ามซ้ำ
    @JoinColumn(name = "song_id", nullable = false, unique = true)
    private Song song;

    // กำหนด type ของแต่ละส่วนเป็น TEXT ~ ราวๆ 65000 ตัวอักษร
    @Column(columnDefinition = "TEXT")
    private String kanji;

    @Column(columnDefinition = "TEXT")
    private String romaji;

    @Column(columnDefinition = "TEXT")
    private String thai;

    // เก็บหมายเหตุเพิ่มเติม เกี่ยวกับตัวเพลง
    @Column(columnDefinition = "TEXT")
    private String notes;

    // เก็บเวลาที่ record นี้ถูกอัปเดตล่าสุด
    private LocalDateTime updatedAt;

    @PrePersist // เรียก Method นี้ก่อน insert เสมอ
    @PreUpdate // เรียก Method นี้ก่อน update เสมอ
    // ให้เวลาเป็นปัจจุบันทุกครั้งที่มีการบันทึกข้อมูล
    protected void touch() {
        updatedAt = LocalDateTime.now();
    }
}
