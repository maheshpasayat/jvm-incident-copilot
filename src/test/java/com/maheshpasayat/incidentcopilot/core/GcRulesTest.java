package com.maheshpasayat.incidentcopilot.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class GcRulesTest {

    private final GcLogParser parser = new GcLogParser();

    private GcLog log(String fileName) throws IOException {
        return parser.parse(Files.readString(Path.of("samples", "gc-logs", fileName)));
    }

    @Test
    void longPause_pressureLogGivesCriticalFinding() throws IOException {
        List<Finding> findings = new LongGcPauseRule().evaluate(log("gc-pressure.log"));

        assertEquals(1, findings.size());
        assertEquals("LONG_GC_PAUSE", findings.get(0).ruleId());
        assertEquals(Severity.CRITICAL, findings.get(0).severity());
        assertEquals(3, findings.get(0).evidence().size()); // 120.5 and 135.2 are under 200
    }

    @Test
    void longPause_healthyLogGivesNothing() throws IOException {
        assertTrue(new LongGcPauseRule().evaluate(log("healthy.log")).isEmpty());
    }

    @Test
    void heapGrowth_leakLogGivesCriticalFinding() throws IOException {
        List<Finding> findings = new HeapGrowthRule().evaluate(log("memory-leak.log"));

        assertEquals(1, findings.size());
        assertEquals("HEAP_GROWTH", findings.get(0).ruleId());
        assertEquals(Severity.CRITICAL, findings.get(0).severity());
        assertEquals(5, findings.get(0).evidence().size());
    }

    @Test
    void heapGrowth_healthyLogGivesNothing() throws IOException {
        assertTrue(new HeapGrowthRule().evaluate(log("healthy.log")).isEmpty());
    }

    @Test
    void heapGrowth_pressureLogGivesNothingBecauseHeapIsNotStrictlyRisingAfterEachFullGc() throws IOException {
        // pressure log has only 2 Full GCs, below the minimum of 3
        assertTrue(new HeapGrowthRule().evaluate(log("gc-pressure.log")).isEmpty());
    }
}