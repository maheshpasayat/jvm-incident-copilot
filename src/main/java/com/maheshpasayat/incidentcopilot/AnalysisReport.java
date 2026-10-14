package com.maheshpasayat.incidentcopilot;

import java.util.List;

/**
 * The result of analysing one uploaded file.
 *
 * @param sourceType   "THREAD_DUMP" or "GC_LOG"
 * @param factsCount   how many facts were parsed (threads or GC events)
 * @param findings     conclusions drawn by the rules, with evidence
 */
public record AnalysisReport(String sourceType, int factsCount, List<Finding> findings) {

    public AnalysisReport {
        findings = List.copyOf(findings);
    }
}