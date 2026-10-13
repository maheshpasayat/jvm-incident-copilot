package com.maheshpasayat.incidentcopilot;

import java.util.ArrayList;
import java.util.List;

/**
 * Reports GC pauses that are long enough to hurt request latency.
 */
public class LongGcPauseRule implements GcLogRule {

    static final double WARNING_MILLIS = 200;
    static final double CRITICAL_MILLIS = 1000;

    @Override
    public List<Finding> evaluate(GcLog log) {
        List<GcEvent> longPauses = log.events().stream()
                .filter(e -> e.pauseMillis() >= WARNING_MILLIS)
                .toList();
        if (longPauses.isEmpty()) {
            return List.of();
        }

        GcEvent worst = longPauses.stream()
                .max((a, b) -> Double.compare(a.pauseMillis(), b.pauseMillis()))
                .orElseThrow();
        Severity severity = worst.pauseMillis() >= CRITICAL_MILLIS ? Severity.CRITICAL : Severity.WARNING;

        List<String> evidence = new ArrayList<>();
        for (GcEvent e : longPauses) {
            evidence.add("GC(" + e.gcId() + ") " + e.type() + " paused " + e.pauseMillis()
                    + " ms at " + e.uptimeSeconds() + " s");
        }
        String title = longPauses.size() + " GC pauses of " + (int) WARNING_MILLIS
                + " ms or more (longest " + worst.pauseMillis() + " ms)";
        return List.of(new Finding("LONG_GC_PAUSE", severity, title, evidence));
    }
}