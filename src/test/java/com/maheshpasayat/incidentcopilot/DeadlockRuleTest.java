package com.maheshpasayat.incidentcopilot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class DeadlockRuleTest {

    private final DeadlockRule rule = new DeadlockRule();
    private final ThreadDumpParser parser = new ThreadDumpParser();

    private List<Finding> run(String fileName) throws IOException {
        String text = Files.readString(Path.of("samples", "thread-dumps", fileName));
        return rule.evaluate(parser.parse(text));
    }

    @Test
    void deadlockSample_givesOneCriticalFinding() throws IOException {
        List<Finding> findings = run("deadlock.txt");

        assertEquals(1, findings.size());
        Finding f = findings.get(0);
        assertEquals("DEADLOCK", f.ruleId());
        assertEquals(Severity.CRITICAL, f.severity());
        assertEquals(2, f.evidence().size());
        assertTrue(f.evidence().stream().anyMatch(e -> e.startsWith("order-worker-1")));
        assertTrue(f.evidence().stream().anyMatch(e -> e.startsWith("payment-worker-1")));
    }

    @Test
    void poolExhaustionSample_isNotADeadlock() throws IOException {
        assertTrue(run("pool-exhaustion.txt").isEmpty());
    }

    @Test
    void emptyDump_givesNoFindings() {
        assertTrue(rule.evaluate(new ThreadDump(List.of())).isEmpty());
    }
}