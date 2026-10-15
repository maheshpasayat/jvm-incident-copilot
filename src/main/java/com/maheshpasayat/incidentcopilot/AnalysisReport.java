package com.maheshpasayat.incidentcopilot;

import java.util.List;

/**
 * The result of analysing one uploaded file.
 *
 * @param sourceType   "THREAD_DUMP" or "GC_LOG"
 * @param factsCount   how many facts were parsed (threads or GC events)
 * @param findings     conclusions drawn by the rules, with evidence
 */
public record AnalysisReport(String sourceType, int factsCount, List<Finding> findings,
                             List<Explanation> explanations, boolean aiAvailable) {

    public AnalysisReport {
        findings = List.copyOf(findings);
        explanations = List.copyOf(explanations);
    }

    public AnalysisReport(String sourceType, int factsCount, List<Finding> findings) {
        this(sourceType, factsCount, findings, List.of(), false);
    }

    public AnalysisReport withExplanations(List<Explanation> explanations) {
        return new AnalysisReport(sourceType, factsCount, findings, explanations, true);
    }
}