package com.fcnami.backend;

import com.fcnami.backend.Model.QueueRequest.*;
import com.fcnami.backend.Model.SongTags.Song;
import com.fcnami.backend.Model.SongTags.SongStatus;
import com.fcnami.backend.Model.SongTags.Tag;
import com.fcnami.backend.Model.SongTags.TagType;
import com.fcnami.backend.Model.User;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TestFactory {
    public static User createUser() {
        return User.builder()
                .userIdentifier("yt_" + UUID.randomUUID()) // unique เสมอ
                .username("user_" + UUID.randomUUID())
                .email(UUID.randomUUID() + "@test.com")
                .totalRequests(0)
                .activeRequests(0)
                .build();
    }

    public static User createUser(String userIdentifier) {
        return User.builder()
                .userIdentifier(userIdentifier)
                .username("user_" + UUID.randomUUID())
                .email(UUID.randomUUID() + "@test.com")
                .totalRequests(0)
                .activeRequests(0)
                .build();
    }

    public static Request createRequest(User user) {
        return Request.builder()
                .requesterId("req_" + UUID.randomUUID())
                .user(user)
                .requesterName("name_" + UUID.randomUUID())
                .songTitle("song_" + UUID.randomUUID())
                .artist("artist_" + UUID.randomUUID())
                .normalizedKey("key_" + UUID.randomUUID())
                .status(RequestStatus.WAITING)
                .queueType(QueueType.MAIN) // ปรับตาม enum จริง
                .depthLevel(0)
                .requestOrder(1)
                .build();
    }

    public static Request createRequest(User user, QueueType type, int order) {
        Request r = createRequest(user);
        r.setQueueType(type);
        r.setRequestOrder(order);
        return r;
    }

    public static Request createRequestWithKey(User user, String key) {
        Request r = createRequest(user);
        r.setNormalizedKey(key);
        return r;
    }

    public static Request createReplacedRequest(User user, Request original) {
        Request r = createRequest(user);
        r.setReplacedRequest(original);
        return r;
    }

    public static Tag createTag() {
        Tag tag = new Tag();

        String name = "tag_" + UUID.randomUUID();

        tag.setName(name);
        tag.setNormalizedName(name.toLowerCase());
        tag.setType(TagType.GENRE); // default safe value

        return tag;
    }

    public static Tag createTag(String name) {
        Tag tag = new Tag();

        tag.setName(name);
        tag.setNormalizedName(name.toLowerCase());
        tag.setType(TagType.GENRE);

        return tag;
    }

    public static Set<Tag> createTags(int size) {
        Set<Tag> tags = new HashSet<>();

        for (int i = 0; i < size; i++) {
            tags.add(createTag());
        }

        return tags;
    }

    public static Song createSong() {
        Song song = new Song();
        song.setTitle("title_" + UUID.randomUUID());
        song.setArtist("artist_" + UUID.randomUUID());
        song.setNormalizedKey(UUID.randomUUID().toString());
        song.setStatus(SongStatus.IDEA);

        song.setTags(createTags(2));

        return song;
    }

    public static Song createSong(String title, String artist) {
        Song song = new Song();
        song.setTitle(title);
        song.setArtist(artist);
        song.setNormalizedKey(title + "_" + artist);
        song.setStatus(SongStatus.IDEA);

        song.setTags(new HashSet<>());

        return song;
    }

    public static Song createSongWithStatus(SongStatus status) {
        Song song = new Song();

        song.setTitle("test-title-" + UUID.randomUUID());
        song.setArtist("test-artist-" + UUID.randomUUID());
        song.setNormalizedKey("nk-" + UUID.randomUUID());

        song.setKanjiLyrics(null);
        song.setRomajiLyrics(null);
        song.setTranslationEn(null);
        song.setTranslationTh(null);

        song.setStatus(status);

        // สำคัญ: ห้ามใส่ Tag ใน factory นี้
        song.setTags(null);

        return song;
    }

    public static Song createSongWithoutTags() {
        Song song = new Song();
        song.setTitle("test");
        song.setArtist("artist");
        song.setNormalizedKey(UUID.randomUUID().toString());
        song.setStatus(SongStatus.IDEA);
        return song;
    }

    public static Song createSongWithTitleAndStatus(String title, SongStatus status) {
        Song song = new Song();

        song.setTitle(title);
        song.setArtist("test-artist-" + UUID.randomUUID());
        song.setNormalizedKey("nk_" + UUID.randomUUID());

        song.setKanjiLyrics(null);
        song.setRomajiLyrics(null);
        song.setTranslationEn(null);
        song.setTranslationTh(null);

        song.setStatus(status);

        // IMPORTANT: ห้ามใส่ Tag ใน factory นี้
        song.setTags(null);

        return song;
    }
    public static Song createSongWithTitleAndArtist(String title, String artist) {
        Song song = new Song();
        song.setTitle(title);
        song.setArtist(artist);
        song.setNormalizedKey(UUID.randomUUID().toString());
        song.setStatus(SongStatus.IDEA);
        song.setTags(null);
        return song;
    }

    public static Tag createTag(String name, TagType type) {
        Tag tag = new Tag();

        tag.setName(name);
        tag.setNormalizedName(name.toLowerCase().replace(" ", "_"));
        tag.setType(type);

        return tag;
    }

    public static QueueSnapshot createSnapshot(Request request, QueueType type, int position) {

        QueueSnapshot snapshot = new QueueSnapshot();

        snapshot.setRequest(request);
        snapshot.setQueueType(type);
        snapshot.setPosition(position);
        snapshot.setBatchId("batch-" + UUID.randomUUID());

        return snapshot;
    }
    public static String createBatchId() {
        return "batch-" + UUID.randomUUID();
    }

    public static QueueSnapshot createSnapshot(
            Request request,
            QueueType type,
            int position,
            String batchId
    ) {

        QueueSnapshot snapshot = new QueueSnapshot();

        snapshot.setRequest(request);
        snapshot.setQueueType(type);
        snapshot.setPosition(position);
        snapshot.setBatchId(batchId);

        return snapshot;
    }

    public static QueueCounter createQueueCounter(QueueType type, Long lastOrder) {
        return QueueCounter.builder()
                .queueType(type)
                .lastOrder(lastOrder)
                .build();
    }

}
