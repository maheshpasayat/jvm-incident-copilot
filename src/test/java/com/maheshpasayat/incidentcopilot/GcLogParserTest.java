package com.maheshpasayat.incidentcopilot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class GcLogParserTest {

    private final GcLogParser parser = new GcLogParser();

    private GcLog parseSample(String fileName) throws IOException {
        String text = Files.readString(Path.of("samples", "gc-logs", fileName));
        return parser.parse(text);
    }

    @Test
    void healthy_hasFiveYoungPausesAndNoFullGc() throws IOException {
        GcLog log = parseSample("healthy.log");
        assertEquals(5, log.events().size());
        assertTrue(log.events().stream().noneMatch(GcEvent::isFullGc));
        assertTrue(log.events().stream().allMatch(e -> e.pauseMillis() < 10));
    }

    @Test
    void healthy_readsTheFirstEventExactly() throws IOException {
        GcEvent first = parseSample("healthy.log").events().get(0);
        assertEquals(5.1, first.uptimeSeconds(), 0.0001);
        assertEquals(0, first.gcId());
        assertEquals("Pause Young", first.type());
        assertEquals(64, first.heapBeforeMb());
        assertEquals(12, first.heapAfterMb());
        assertEquals(512, first.heapTotalMb());
        assertEquals(8.1, first.pauseMillis(), 0.0001);
    }

    @Test
    void pressure_hasSixPausesAndTwoFullGcs() throws IOException {
        GcLog log = parseSample("gc-pressure.log");
        assertEquals(6, log.events().size()); // the Concurrent Mark Cycle line is skipped
        assertEquals(2, log.events().stream().filter(GcEvent::isFullGc).count());
    }

    @Test
    void pressure_findsTheLongestPause() throws IOException {
        GcLog log = parseSample("gc-pressure.log");
        double longest = log.events().stream().mapToDouble(GcEvent::pauseMillis).max().orElseThrow();
        assertEquals(2100.7, longest, 0.0001);
    }

    @Test
    void leak_heapAfterEachFullGcKeepsRising() throws IOException {
        GcLog log = parseSample("memory-leak.log");
        assertEquals(5, log.events().size()); // the Safepoint line is skipped
        assertTrue(log.events().stream().allMatch(GcEvent::isFullGc));

        long previous = -1;
        for (GcEvent e : log.events()) {
            assertTrue(e.heapAfterMb() > previous, "heap after GC should rise");
            previous = e.heapAfterMb();
        }
        assertEquals(600, log.events().get(4).heapAfterMb());
    }

    @Test
    void nullBlankAndRubbishGiveAnEmptyLog() {
        assertTrue(parser.parse(null).events().isEmpty());
        assertTrue(parser.parse("  \n ").events().isEmpty());
        assertTrue(parser.parse("not a gc log at all").events().isEmpty());
    }
}