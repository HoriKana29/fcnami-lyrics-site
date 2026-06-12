package com.fcnami.backend.Factory;

import com.fcnami.backend.Model.QueueRequest.QueueType;
import com.fcnami.backend.Model.QueueRequest.Request;
import com.fcnami.backend.Model.QueueRequest.RequestStatus;
import com.fcnami.backend.Model.User;
import com.fcnami.backend.Support.SlugUtil;

import java.util.UUID;

public class RequestFactory {
    public static Request createBase(User user, String songTitle, String artist) {
        Request r = new Request();

        r.setUser(user);

        r.setRequesterId(generateRequesterId());
        r.setRequesterName(user.getUsername());

        r.setSongTitle(songTitle);
        r.setArtist(artist);

        r.setNormalizedKey(generateNormalizedKey(songTitle, artist));

        r.setStatus(RequestStatus.WAITING);
        r.setQueueType(QueueType.MAIN);

        r.setDepthLevel(0);

        r.setRequestOrder(null);

        return r;
    }

    public static Request create(User user, String songTitle, String artist) {
        return createBase(user, songTitle, artist);
    }

    public static Request create(User user, String songTitle, String artist, QueueType type) {
        Request r = createBase(user, songTitle, artist);
        r.setQueueType(type);
        return r;
    }

    public static Request replace(User user, Request original, String songTitle, String artist) {
        Request r = createBase(user, songTitle, artist);
        r.setReplacedRequest(original);
        return r;
    }

    private static String generateRequesterId() {
        return "req_" + UUID.randomUUID();
    }

    private static String generateNormalizedKey(String title, String artist) {
        return SlugUtil.normalizedKey(title, artist);
    }
}
