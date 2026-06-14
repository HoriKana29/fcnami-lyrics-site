package com.fcnami.backend.Factory;

import com.fcnami.backend.Model.QueueRequest.QueueType;
import com.fcnami.backend.Model.QueueRequest.Request;
import com.fcnami.backend.Model.QueueRequest.RequestStatus;
import com.fcnami.backend.Model.User;
import com.fcnami.backend.Support.SlugUtil;

import java.util.UUID;

/**
 * Factory class for creating Request instances.
 * Generates base and custom requests with standard initial properties and identifiers.
 */
public class RequestFactory {
    
    /**
     * Precondition: User must not be null. Song title and artist must be valid strings.
     * Postcondition: Returns a base Request instance with standard initial properties.
     * Side-effect: None.
     */
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

    /**
     * Precondition: User must not be null. Song title and artist must be valid strings.
     * Postcondition: Returns a Request instance initialized using the base creator.
     * Side-effect: None.
     */
    public static Request create(User user, String songTitle, String artist) {
        return createBase(user, songTitle, artist);
    }

    /**
     * Precondition: User and QueueType must not be null. Song title and artist must be valid strings.
     * Postcondition: Returns a Request instance with the specified QueueType.
     * Side-effect: None.
     */
    public static Request create(User user, String songTitle, String artist, QueueType type) {
        Request r = createBase(user, songTitle, artist);
        r.setQueueType(type);
        return r;
    }

    /**
     * Precondition: User and original Request must not be null. Song title and artist must be valid strings.
     * Postcondition: Returns a new Request instance that references the original request being replaced.
     * Side-effect: None.
     */
    public static Request replace(User user, Request original, String songTitle, String artist) {
        Request r = createBase(user, songTitle, artist);
        r.setReplacedRequest(original);
        return r;
    }

    /**
     * Precondition: None.
     * Postcondition: Returns a randomly generated unique requester ID prefixed with 'req_'.
     * Side-effect: None.
     */
    private static String generateRequesterId() {
        return "req_" + UUID.randomUUID();
    }

    /**
     * Precondition: Title and artist strings are provided.
     * Postcondition: Returns a normalized key for the title and artist combination.
     * Side-effect: None.
     */
    private static String generateNormalizedKey(String title, String artist) {
        return SlugUtil.normalizedKey(title, artist);
    }
}
