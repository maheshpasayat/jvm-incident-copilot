package com.maheshpasayat.incidentcopilot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.maheshpasayat.incidentcopilot.core.*;
import org.junit.jupiter.api.Test;

class PoolExhaustionRuleTest {

    private final PoolExhaustionRule rule = new PoolExhaustionRule();
    private final ThreadDumpParser parser = new ThreadDumpParser();

    private List<Finding> run(String fileName) throws IOException {
        String text = Files.readString(Path.of("samples", "thread-dumps", fileName));
        return rule.evaluate(parser.parse(text));
    }

    @Test
    void poolSample_givesOneWarningWithHolderAndFourBlockedThreads() throws IOException {
        List<Finding> findings = run("pool-exhaustion.txt");

        assertEquals(1, findings.size());
        Finding f = findings.get(0);
        assertEquals("POOL_EXHAUSTION", f.ruleId());
        assertEquals(Severity.WARNING, f.severity());
        assertTrue(f.title().startsWith("4 threads blocked"));
        assertEquals(5, f.evidence().size()); // 1 holder line + 4 blocked threads
        assertTrue(f.evidence().get(0).contains("http-nio-8080-exec-5"));
    }

    @Test
    void deadlockSample_hasNoPoolExhaustion() throws IOException {
        assertTrue(run("deadlock.txt").isEmpty());
    }

    @Test
    void emptyDump_givesNoFindings() {
        assertTrue(rule.evaluate(new ThreadDump(List.of())).isEmpty());
    }
}