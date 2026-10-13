package com.maheshpasayat.incidentcopilot;

import java.util.List;

/**
 * A conclusion drawn from verified facts.
 *
 * @param ruleId   stable id of the rule that produced it, e.g. "DEADLOCK"
 * @param severity how serious it is
 * @param title    one-line summary
 * @param evidence the facts that prove it (thread names, lock ids, numbers)
 */
public record Finding(String ruleId, Severity severity, String title, List<String> evidence) {

    public Finding {
        evidence = List.copyOf(evidence);
    }
}