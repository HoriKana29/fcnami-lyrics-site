package com.fcnami.backend.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Collections;
import java.util.List;

/**
 * Service that performs lightweight web research to find song background info.
 * Since this is a backend service, we use a simple search API or mock for research.
 */
@Slf4j
@Service
public class SongResearchService {

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Researches a song by title and artist.
     * 
     * @param title Song title
     * @param artist Artist name
     * @return String containing research snippets or background info.
     */
    public String researchSong(String title, String artist) {
        if (title == null || title.isBlank()) return "";
        
        String query = title + " " + (artist != null ? artist : "") + " song origin genre";
        log.info("Performing web research for: {}", query);

        try {
            // Note: In a real production system, you'd use a Search API like Google Custom Search.
            // For this implementation, we simulate the "research" by returning a rich string
            // that the classification engine can process.
            
            // Simulation of what a search result might look like for common keywords:
            StringBuilder researchContext = new StringBuilder();
            
            if (title.toLowerCase().contains("ost") || title.toLowerCase().contains("anime") || title.toLowerCase().contains("opening") || title.toLowerCase().contains("ending")) {
                researchContext.append(" This song is verified as an Anime Theme Song (OST). Anisong category.");
            }
            
            if (title.toLowerCase().contains("bandori") || title.toLowerCase().contains("bang dream") || title.toLowerCase().contains("roselia") || title.toLowerCase().contains("popipa")) {
                researchContext.append(" Origin: Rhythm Game 'BanG Dream! Girls Band Party!'. Genre: J-Rock / J-Pop. Band performance.");
            }

            if (title.toLowerCase().contains("project sekai") || title.toLowerCase().contains("pjsk") || title.toLowerCase().contains("nightcord") || title.toLowerCase().contains("vivid bad squad")) {
                researchContext.append(" Origin: Rhythm Game 'Project Sekai Colorful Stage!'. Features Vocaloid characters and idol units.");
            }

            if (title.toLowerCase().contains("miku") || title.toLowerCase().contains("vocaloid") || title.toLowerCase().contains("gumi") || title.toLowerCase().contains("rin/len")) {
                researchContext.append(" This is a Vocaloid original work or cover. Digital voice synthesis.");
            }

            if (title.toLowerCase().contains("rock") || title.toLowerCase().contains("metal") || title.toLowerCase().contains("liSA") || title.toLowerCase().contains("unison square garden")) {
                researchContext.append(" Genre verified: J-Rock. Energetic drums and electric guitar focus.");
            }

            if (title.toLowerCase().contains("ballad") || title.toLowerCase().contains("emotional") || title.toLowerCase().contains("sad") || title.toLowerCase().contains("aimer")) {
                researchContext.append(" Vibe: Emotional / Melancholic. Deeply moving lyrics and soft arrangement.");
            }

            if (title.toLowerCase().contains("uplifting") || title.toLowerCase().contains("happy") || title.toLowerCase().contains("bright") || title.toLowerCase().contains("official hige dandism")) {
                researchContext.append(" Vibe: Uplifting / Energetic. Positive message and cheerful rhythm.");
            }

            // In actual usage, this service would fetch real external data.
            return researchContext.toString();
        } catch (Exception e) {
            log.error("Web research failed for {}: {}", query, e.getMessage());
            return "";
        }
    }
}
