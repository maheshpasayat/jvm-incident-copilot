package com.maheshpasayat.incidentcopilot;

import java.util.List;

/** The whole parsed GC log: the list of pause events, in log order. */
public record GcLog(List<GcEvent> events) {

    public GcLog {
        events = List.copyOf(events);
    }
}