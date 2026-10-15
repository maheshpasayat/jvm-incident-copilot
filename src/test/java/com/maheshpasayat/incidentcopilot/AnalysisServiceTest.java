package com.maheshpasayat.incidentcopilot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.maheshpasayat.incidentcopilot.core.AnalysisReport;
import org.junit.jupiter.api.Test;

class AnalysisServiceTest {

    private final AnalysisService service = new AnalysisService();

    @Test
    void deadlockDump_reportsOneDeadlock() throws IOException {
        String text = Files.readString(Path.of("samples", "thread-dumps", "deadlock.txt"));
        AnalysisReport report = service.analyzeThreadDump(text);

        assertEquals("THREAD_DUMP", report.sourceType());
        assertEquals(3, report.factsCount());
        assertEquals(1, report.findings().size());
        assertEquals("DEADLOCK", report.findings().get(0).ruleId());
    }

    @Test
    void leakLog_reportsHeapGrowth() throws IOException {
        String text = Files.readString(Path.of("samples", "gc-logs", "memory-leak.log"));
        AnalysisReport report = service.analyzeGcLog(text);

        assertEquals("GC_LOG", report.sourceType());
        assertEquals(5, report.factsCount());
        assertTrue(report.findings().stream().anyMatch(f -> f.ruleId().equals("HEAP_GROWTH")));
    }

    @Test
    void emptyText_givesEmptyReport() {
        assertEquals(0, service.analyzeThreadDump("").findings().size());
        assertEquals(0, service.analyzeGcLog("").factsCount());
    }
}