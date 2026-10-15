package com.maheshpasayat.incidentcopilot.core;

/** AI-generated explanation of one finding. Optional: the report is valid without it. */
public record Explanation(String ruleId, String text, String runbook) { }