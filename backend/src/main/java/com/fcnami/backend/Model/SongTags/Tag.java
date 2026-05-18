package com.fcnami.backend.Model.SongTags;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

// Entity สำหรับ "Tag" ใข้จัดหมวดหมู่เพลง
@Entity
@Setter
@Getter
@Table(name = "tags",
        indexes = {
        // index เพื่อช่วยให้ค้นหาเร็วขึ้น
                @Index(name = "idx_tag_name", columnList = "name"),
                @Index(name = "idx_tag_normalized", columnList = "normalizedName"),
                @Index(name = "idx_tag_type", columnList = "type")
        })
public class Tag {

    // @Id เป็น Primary Key เหมือนรหัสของแต่ละแถวในตาราง
    // คุณสมบัติ : ไม่ซ้ำ/ not null / one per table
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ชื่อ Tag ห้ามซ้ำ
    @Column(nullable = false,unique = true)
    private String name;

    // ชื่อที่ normalize แล้ว
    @Column(unique = true,nullable = false)
    private String normalizedName;

    // ประเภท tag เก็บเป็น enum เผื่อเราจัดประเภทการกรองให้
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TagType type;

    // related by Song เป็นเจ้าของ
    @ManyToMany(mappedBy = "tags",fetch = FetchType.LAZY)
    private Set<Song> songs = new HashSet<>();
}