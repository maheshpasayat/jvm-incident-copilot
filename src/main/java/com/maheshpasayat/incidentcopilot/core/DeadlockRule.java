package com.maheshpasayat.incidentcopilot.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Finds lock cycles: thread A waits for a lock held by B, and B (directly or not) waits for a lock held by A.
 */
public class DeadlockRule implements ThreadDumpRule {

    @Override
    public List<Finding> evaluate(ThreadDump dump) {
        Map<String, ThreadInfo> holderOfLock = new HashMap<>();
        for (ThreadInfo t : dump.threads()) {
            for (String lock : t.heldLocks()) {
                holderOfLock.put(lock, t);
            }
        }

        List<Finding> findings = new ArrayList<>();
        Set<String> alreadyReported = new HashSet<>();

        for (ThreadInfo start : dump.threads()) {
            List<ThreadInfo> chain = new ArrayList<>();
            ThreadInfo current = start;

            while (current != null && current.waitingOnLock() != null && !chain.contains(current)) {
                chain.add(current);
                current = holderOfLock.get(current.waitingOnLock());
            }

            if (current != null && chain.contains(current)) {
                List<ThreadInfo> cycle = chain.subList(chain.indexOf(current), chain.size());
                List<String> names = cycle.stream().map(ThreadInfo::name).sorted().toList();
                if (alreadyReported.add(String.join("|", names))) {
                    findings.add(buildFinding(cycle));
                }
            }
        }
        return findings;
    }

    private Finding buildFinding(List<ThreadInfo> cycle) {
        List<String> evidence = new ArrayList<>();
        for (ThreadInfo t : cycle) {
            evidence.add(t.name() + " holds " + t.heldLocks() + " and waits for " + t.waitingOnLock());
        }
        String title = "Deadlock between " + cycle.size() + " threads";
        return new Finding("DEADLOCK", Severity.CRITICAL, title, evidence);
    }
}