package com.fcnami.backend.Service;

import com.fcnami.backend.Api.QueueDtos.QueueItemResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class QueueCsvParserTest {
    private final QueueCsvParser parser = new QueueCsvParser();
    private final QueueRowNormalizer normalizer = new QueueRowNormalizer();

    @Test
    void shouldParseQuotedCsvCells() {
        List<Map<String, String>> rows = parser.parse("""
                เพลง,ผู้รีเควส,ลิงค์เพลง,แปลรึยัง?,Waiting Days
                "Song, With Comma",@user,"https://youtu.be/a?x=1,2",FALSE,12
                """);

        assertEquals(1, rows.size());
        assertEquals("Song, With Comma", rows.getFirst().get("เพลง"));
        assertEquals("https://youtu.be/a?x=1,2", rows.getFirst().get("ลิงค์เพลง"));
    }

    @Test
    void shouldReturnNoRowsForBlankCsv() {
        assertTrue(parser.parse("").isEmpty());
        assertTrue(parser.parse(null).isEmpty());
    }

    @Test
    void shouldNormalizeTierMarkersAndQueueItems() {
        List<Map<String, String>> rows = parser.parse("""
                คิว,เพลง,ผู้รีเควส,ลิงค์เพลง,แปลรึยัง?,Req.Date,Waiting Days
                1,Done Song,@a,Done Song,TRUE,N/A,Done
                2,Waiting Song,@b,Waiting Song,FALSE,01/04/2026,12
                2nd Request,,,,,,
                ,Deep Song,@c,Deep Song,FALSE,02/04/2026,20
                """);

        List<QueueItemResponse> items = normalizer.normalize(rows);

        assertEquals(3, items.size());
        assertEquals("main-road", items.get(0).tier());
        assertEquals("done", items.get(0).status());
        assertEquals("waiting", items.get(1).status());
        assertEquals("second", items.get(2).tier());
        assertEquals(20, items.get(2).waitingDays());
    }
}
