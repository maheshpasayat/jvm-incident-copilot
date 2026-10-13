package com.maheshpasayat.incidentcopilot;

import java.util.List;

/** The whole parsed dump: just the list of threads. */
public record ThreadDump(List<ThreadInfo> threads) {

    public ThreadDump {
        threads = List.copyOf(threads);
    }
}