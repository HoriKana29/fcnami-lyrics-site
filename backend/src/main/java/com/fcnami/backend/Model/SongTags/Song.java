package com.fcnami.backend.Model.SongTags;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import com.fcnami.backend.Support.SlugUtil;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
// Entity หลักของระบบเพลง -> map กับ table "songs"
@Entity
@Table(name = "songs",
        indexes = {
        // index เพื่อช่วยให้ query(ถาม/สั่งงาน) เร็วขึ้น
                @Index(name = "idx_song_title", columnList = "title"),
                @Index(name = "idx_song_artist", columnList = "artist"),
                // ทำให้ search ได้แบบไม่ Sensitive
                @Index(name = "idx_song_normalized", columnList = "normalizedKey"),
                // ตัวห้อยหลัง URL
                @Index(name = "idx_song_slug", columnList = "slug"),
                @Index(name = "idx_song_status", columnList = "status")
        }
)
public class Song {
    // @Id เป็น Primary Key เหมือนรหัสของแต่ละแถวในตาราง
    // คุณสมบัติ : ไม่ซ้ำ/ not null / one per table
    @Id
    // ให้ Primary Key เพิ่มค่าเองอัตโนมัติ
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ชื่อเพลง (required)
    @Column(nullable = false)
    private String title;

    // ชื่อเพลงภาษาญี่ปุ่น (optional)
    private String titleJapanese;

    // ชื่อศิลปิน (required)
    @Column(nullable = false)
    private String artist;

    // key สำหรับใช้ compare / search แบบ insensitive
    @Column(nullable = false,unique = true)
    private String normalizedKey;

    // สำหรับ URL เช่น /song/kimi-no-na-wa-radwimps
    @Column(nullable = false)
    private String slug;

    // แหล่งที่มา ไม่ว่าจะเป็น Anime/Game
    private String sourceAnimeOrGame;

    // ข้อมูลจาก YouTube
    private String youtubeUrl;
    private String youtubeVideoId;
    private String thumbnailUrl;

    // ส่วนของ Lyrics ที่เก็บใน Song โดยตรง (ควรแก้ไขให้เก็บใน Class Lyrics อย่างเดียว)
    @Column(columnDefinition = "TEXT")
    private String kanjiLyrics;

    @Column(columnDefinition = "TEXT")
    private String romajiLyrics;

    @Column(columnDefinition = "TEXT")
    private String translationTh;

    @Column(columnDefinition = "TEXT")
    private String translationEn;

    // InteliJ แนะนำ : Specifying FetchType.LAZY for the non-owning side of the @OneToOne
    // association will not affect the loading. The related entity will still be
    // loaded as if the FetchType.EAGER is defined.

    // inverse OneToOne มี Lyrics เป็น Owner
    // cascade : ALL ถ้าทำอะไรกับ Song จะกระทบ Lyrics ด้วย
    @OneToOne(mappedBy = "song", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Lyrics lyrics;

    // มี Relation กับ Tag
    // Many-to-Many → เพลง 1 เพลงมีหลาย tag และ tag 1 อันมีหลายเพลง
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "song_tags", // table กลาง
            joinColumns = @JoinColumn(name = "song_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    // Enum -> เก็บเป็น String ใน Database เพื่อให้อ่านง่าย
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    // ให้สถานะตั้งต้นเป็น มี idea ที่จะตัดแล้วนะ
    private SongStatus status = SongStatus.IDEA;

    // Timestamp ต่างๆ
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;

    // ก่อน insert
    @PrePersist
    protected void onCreate() {

        // comment เราใช้ ตัวที่เป็น normalizedKey เป็น slug เลยได้มั้ย

        // ถ้ายังไม่มี slug -> gen slug จาก title-artist
        if (slug == null || slug.isBlank()) {
            slug = SlugUtil.slugify(title + "-" + artist);
        }

        // ถ้ายังไม่มี normalizedKey -> generate สำหรับใช้กัน Duplicate/search
        if (normalizedKey == null || normalizedKey.isBlank()) {
            normalizedKey = SlugUtil.normalizedKey(title, artist);
        }

        // set เวลา create + update สำหรับครั้งแรก
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    // ก่อน Update
    @PreUpdate
    protected void onUpdate() {
        // เผื่อ Slug และ normalizeKey หาย
        if (slug == null || slug.isBlank()) {
            slug = SlugUtil.slugify(title + "-" + artist);
        }
        if (normalizedKey == null || normalizedKey.isBlank()) {
            normalizedKey = SlugUtil.normalizedKey(title, artist);
        }
        updatedAt = LocalDateTime.now();
    }
}
