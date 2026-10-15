package com.maheshpasayat.incidentcopilot.core;

import java.util.List;

/** A rule that inspects a parsed GC log and returns findings (possibly none). */
public interface GcLogRule {

    List<Finding> evaluate(GcLog log);
}