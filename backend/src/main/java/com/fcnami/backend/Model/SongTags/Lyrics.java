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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false, unique = true)
    private Song song;

    @Column(columnDefinition = "TEXT")
    private String kanji;

    @Column(columnDefinition = "TEXT")
    private String romaji;

    @Column(columnDefinition = "TEXT")
    private String thai;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void touch() {
        updatedAt = LocalDateTime.now();
    }
}
