package com.fcnami.backend.Factory;

import com.fcnami.backend.Model.SongTags.Song;
import com.fcnami.backend.Model.SongTags.SongStatus;
import com.fcnami.backend.Model.SongTags.Tag;
import com.fcnami.backend.Support.SlugUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
// Class 'SongFactory' is never used
// ไม่มี validation
public class SongFactory {
    public static Song create(String title, String artist) {
        Song song = new Song();

        song.setTitle(title);
        song.setArtist(artist);

        // Factory กับ Entity ใช้ logic ไม่เหมือนกัน อยากให้ยืดตาม Entity เป็นหลัก
        song.setNormalizedKey(SlugUtil.normalizedKey(title, artist));
        song.setSlug(SlugUtil.slugify(title));

        // only set if you WANT factory-level default control
        song.setStatus(SongStatus.IDEA);

        // prevent null issues for ManyToMany
        // แก้ tag → กระทบหลาย object
        song.setTags(new HashSet<>());

        return song;
    }

    public static Song createDefault() {
        return create(
                "title_" + UUID.randomUUID(),
                "artist_" + UUID.randomUUID()
        );
    }

    public static Song createWithStatus(String title, String artist, SongStatus status) {
        Song song = create(title, artist);
        song.setStatus(status);
        return song;
    }

    public static Song createWithTags(String title, String artist, Set<Tag> tags) {
        Song song = create(title, artist);
        song.setTags(tags != null ? tags : new HashSet<>());
        return song;
    }

}
