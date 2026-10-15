package com.maheshpasayat.incidentcopilot;

import java.util.ArrayList;
import java.util.List;

import com.maheshpasayat.incidentcopilot.core.*;
import org.springframework.stereotype.Service;

/**
 * Runs the parsers and all rules. The parsers and rules themselves stay plain Java.
 */
@Service
public class AnalysisService {

    private final ThreadDumpParser threadDumpParser = new ThreadDumpParser();
    private final GcLogParser gcLogParser = new GcLogParser();

    private final List<ThreadDumpRule> threadDumpRules = List.of(
            new DeadlockRule(),
            new PoolExhaustionRule());

    private final List<GcLogRule> gcLogRules = List.of(
            new LongGcPauseRule(),
            new HeapGrowthRule());

    public AnalysisReport analyzeThreadDump(String text) {
        ThreadDump dump = threadDumpParser.parse(text);
        List<Finding> findings = new ArrayList<>();
        for (ThreadDumpRule rule : threadDumpRules) {
            findings.addAll(rule.evaluate(dump));
        }
        return new AnalysisReport("THREAD_DUMP", dump.threads().size(), findings);
    }

    public AnalysisReport analyzeGcLog(String text) {
        GcLog log = gcLogParser.parse(text);
        List<Finding> findings = new ArrayList<>();
        for (GcLogRule rule : gcLogRules) {
            findings.addAll(rule.evaluate(log));
        }
        return new AnalysisReport("GC_LOG", log.events().size(), findings);
    }
}