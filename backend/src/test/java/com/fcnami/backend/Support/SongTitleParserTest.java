package com.fcnami.backend.Support;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SongTitleParserTest {

    @Test
    void testParseComplexTitle() {
        String input = "Last regrets ～acoustic version～ / Ayana ซับไทย Covered by nayuta [ Kanon ]";
        SongTitleParser.ParsedTitle result = SongTitleParser.parse(input);

        assertEquals("Last regrets ～acoustic version～", result.getTitle());
        assertEquals("Ayana", result.getArtist());
        assertEquals("nayuta", result.getCoveredBy());
        assertEquals("Kanon", result.getSource());
    }

    @Test
    void testParseSimpleTitle() {
        String input = "Brave Shine - Aimer [ Fate/stay night ]";
        SongTitleParser.ParsedTitle result = SongTitleParser.parse(input);

        assertEquals("Brave Shine", result.getTitle());
        assertEquals("Aimer", result.getArtist());
        assertEquals("Fate/stay night", result.getSource());
        assertNull(result.getCoveredBy());
    }

    @Test
    void testParseTitleNoArtist() {
        String input = "Only Title [ Some Source ]";
        SongTitleParser.ParsedTitle result = SongTitleParser.parse(input);

        assertEquals("Only Title", result.getTitle());
        assertEquals("FCNami T_T", result.getArtist());
        assertEquals("Some Source", result.getSource());
    }

    @Test
    void testExtractTags() {
        String input = "This is a description with #Anime and #Vocaloid tags.";
        java.util.Set<String> tags = SongTitleParser.extractTags(input);
        
        assertEquals(2, tags.size());
        assertTrue(tags.contains("Anime"));
        assertTrue(tags.contains("Vocaloid"));
    }
}
