package com.maheshpasayat.incidentcopilot.core;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Reads a unified-format GC log (Java 9+ -Xlog:gc) into a GcLog. Plain Java, no libraries. */
public class GcLogParser {

    // [2026-10-13T10:15:30.123+0100][12.345s][info][gc] GC(7) Pause Young (Normal) (G1 Evacuation Pause) 512M->128M(1024M) 15.432ms
    private static final Pattern PAUSE_LINE = Pattern.compile(
            "^(?:\\[[^\\]]*\\])?\\[(\\d+(?:\\.\\d+)?)s\\]\\[[^\\]]*\\]\\[gc[^\\]]*\\]\\s+GC\\((\\d+)\\)\\s+"
                    + "(Pause \\w+).*?\\s(\\d+)M->(\\d+)M\\((\\d+)M\\)\\s+(\\d+(?:\\.\\d+)?)ms\\s*$");

    public GcLog parse(String text) {
        List<GcEvent> events = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return new GcLog(events);
        }
        for (String line : text.split("\\R")) {
            Matcher m = PAUSE_LINE.matcher(line);
            if (m.matches()) {
                events.add(new GcEvent(
                        Double.parseDouble(m.group(1)),
                        Integer.parseInt(m.group(2)),
                        m.group(3),
                        Long.parseLong(m.group(4)),
                        Long.parseLong(m.group(5)),
                        Long.parseLong(m.group(6)),
                        Double.parseDouble(m.group(7))));
            }
        }
        return new GcLog(events);
    }
}