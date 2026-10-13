package com.maheshpasayat.incidentcopilot;

import java.util.ArrayList;
import java.util.List;

/** Reports a likely memory leak: heap after each Full GC keeps rising. */
public class HeapGrowthRule implements GcLogRule {

    static final int MIN_FULL_GCS = 3;

    @Override
    public List<Finding> evaluate(GcLog log) {
        List<GcEvent> fullGcs = log.events().stream()
                .filter(GcEvent::isFullGc)
                .toList();
        if (fullGcs.size() < MIN_FULL_GCS) {
            return List.of();
        }

        for (int i = 1; i < fullGcs.size(); i++) {
            if (fullGcs.get(i).heapAfterMb() <= fullGcs.get(i - 1).heapAfterMb()) {
                return List.of();
            }
        }

        List<String> evidence = new ArrayList<>();
        for (GcEvent e : fullGcs) {
            evidence.add("GC(" + e.gcId() + ") heap after Full GC: " + e.heapAfterMb() + " MB");
        }
        long first = fullGcs.get(0).heapAfterMb();
        long last = fullGcs.get(fullGcs.size() - 1).heapAfterMb();
        String title = "Heap after Full GC rose from " + first + " MB to " + last
                + " MB over " + fullGcs.size() + " collections";
        return List.of(new Finding("HEAP_GROWTH", Severity.CRITICAL, title, evidence));
    }
}