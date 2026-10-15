package com.maheshpasayat.incidentcopilot.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Finds many threads BLOCKED on the same lock: a pile-up behind one slow holder.
 */
public class PoolExhaustionRule implements ThreadDumpRule {

    static final int MIN_BLOCKED_THREADS = 3;

    @Override
    public List<Finding> evaluate(ThreadDump dump) {
        Map<String, List<ThreadInfo>> blockedByLock = dump.threads().stream()
                .filter(t -> t.state() == ThreadState.BLOCKED && t.waitingOnLock() != null)
                .collect(Collectors.groupingBy(
                        ThreadInfo::waitingOnLock, LinkedHashMap::new, Collectors.toList()));

        List<Finding> findings = new ArrayList<>();
        for (Map.Entry<String, List<ThreadInfo>> entry : blockedByLock.entrySet()) {
            if (entry.getValue().size() >= MIN_BLOCKED_THREADS) {
                findings.add(buildFinding(dump, entry.getKey(), entry.getValue()));
            }
        }
        return findings;
    }

    private Finding buildFinding(ThreadDump dump, String lock, List<ThreadInfo> blocked) {
        String holder = dump.threads().stream()
                .filter(t -> t.heldLocks().contains(lock))
                .map(ThreadInfo::name)
                .findFirst()
                .map(name -> "Lock " + lock + " is held by " + name)
                .orElse("Lock " + lock + ": holder not found in dump");

        List<String> evidence = new ArrayList<>();
        evidence.add(holder);
        for (ThreadInfo t : blocked) {
            evidence.add(t.name() + " is BLOCKED waiting for " + lock);
        }

        String title = blocked.size() + " threads blocked on lock " + lock;
        return new Finding("POOL_EXHAUSTION", Severity.WARNING, title, evidence);
    }
}