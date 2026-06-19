package com.fcnami.backend.Service;

import org.springframework.stereotype.Service;
import java.util.*;

/**
 * Service responsible for classifying songs into the 7 master tags:
 * Anime, J-Pop, Rhythm Game, Vocaloid, Uplifting, Emotional, Rock.
 */
@Service
public class TagClassificationService {

    private static final Set<String> MASTER_TAGS = Set.of(
        "Anime", "J-Pop", "Rhythm Game", "Vocaloid", "Uplifting", "Emotional", "Rock"
    );

    /**
     * Maps raw keywords and context into the 7 master tags.
     * 
     * @param context All collected information (description, title, web research snippets).
     * @return A set of verified master tags.
     */
    public Set<String> classify(String context) {
        if (context == null || context.isBlank()) return Collections.emptySet();
        
        Set<String> tags = new HashSet<>();
        String lowerContext = context.toLowerCase();

        // 1. Anime
        if (lowerContext.contains("anime") || lowerContext.contains("ost") || 
            lowerContext.contains("opening") || lowerContext.contains("ending") ||
            lowerContext.contains("op/ed") || lowerContext.contains("anisong")) {
            tags.add("Anime");
        }

        // 2. Rhythm Game
        if (lowerContext.contains("rhythm game") || lowerContext.contains("bandori") || 
            lowerContext.contains("project sekai") || lowerContext.contains("bang dream") ||
            lowerContext.contains("d4dj") || lowerContext.contains("love live") ||
            lowerContext.contains("pjsk") || lowerContext.contains("idolmaster")) {
            tags.add("Rhythm Game");
        }

        // 3. Vocaloid
        if (lowerContext.contains("vocaloid") || lowerContext.contains("hatsune miku") || 
            lowerContext.contains("project diva") || lowerContext.contains("utau") ||
            lowerContext.contains("producer") || lowerContext.contains("original song")) {
            tags.add("Vocaloid");
        }

        // 4. Rock
        if (lowerContext.contains("rock") || lowerContext.contains("metal") || 
            lowerContext.contains("punk") || lowerContext.contains("band") ||
            lowerContext.contains("guitar") || lowerContext.contains("drums")) {
            tags.add("Rock");
        }

        // 5. Emotional
        if (lowerContext.contains("emotional") || lowerContext.contains("sad") || 
            lowerContext.contains("ballad") || lowerContext.contains("piano") ||
            lowerContext.contains("tears") || lowerContext.contains("heartbreak") ||
            lowerContext.contains("nostalgic") || lowerContext.contains("soft")) {
            tags.add("Emotional");
        }

        // 6. Uplifting
        if (lowerContext.contains("uplifting") || lowerContext.contains("happy") || 
            lowerContext.contains("energetic") || lowerContext.contains("hopeful") ||
            lowerContext.contains("cheerful") || lowerContext.contains("bright") ||
            lowerContext.contains("fun") || lowerContext.contains("pop-rock")) {
            tags.add("Uplifting");
        }

        // 7. J-Pop (Default catch-all for Japanese music if not otherwise specified, or explicitly mentioned)
        if (lowerContext.contains("j-pop") || lowerContext.contains("jpop") || 
            lowerContext.contains("japanese pop") || tags.isEmpty()) {
            tags.add("J-Pop");
        }

        return tags;
    }
}
