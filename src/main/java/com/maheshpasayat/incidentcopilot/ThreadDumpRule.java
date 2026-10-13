package com.maheshpasayat.incidentcopilot;

import java.util.List;

/** A rule that inspects a parsed thread dump and returns findings (possibly none). */
public interface ThreadDumpRule {

    List<Finding> evaluate(ThreadDump dump);
}