package com.maheshpasayat.incidentcopilot;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads the text of a jstack / jcmd Thread.print dump into a ThreadDump. Plain Java, no libraries.
 */
public class ThreadDumpParser {

    private static final Pattern THREAD_HEADER = Pattern.compile("^\"(.+?)\".*");
    private static final Pattern STATE_LINE = Pattern.compile("^\\s*java\\.lang\\.Thread\\.State:\\s*(\\w+).*");
    private static final Pattern WAITING_LOCK = Pattern.compile("^\\s*- waiting to lock <(0x[0-9a-fA-F]+)>.*");
    private static final Pattern HELD_LOCK = Pattern.compile("^\\s*- locked <(0x[0-9a-fA-F]+)>.*");
    private static final Pattern FRAME = Pattern.compile("^\\s*at\\s+(.+)$");

    public ThreadDump parse(String text) {
        List<ThreadInfo> threads = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return new ThreadDump(threads);
        }

        String name = null;
        ThreadState state = ThreadState.UNKNOWN;
        List<String> frames = new ArrayList<>();
        List<String> held = new ArrayList<>();
        String waiting = null;

        for (String line : text.split("\\R")) {
            Matcher header = THREAD_HEADER.matcher(line);
            if (header.matches()) {
                if (name != null) {
                    threads.add(new ThreadInfo(name, state, frames, waiting, held));
                }
                name = header.group(1);
                state = ThreadState.UNKNOWN;
                frames = new ArrayList<>();
                held = new ArrayList<>();
                waiting = null;
                continue;
            }
            if (name == null) {
                continue; // text before the first thread, e.g. the dump's title line
            }
            Matcher m;
            if ((m = STATE_LINE.matcher(line)).matches()) {
                state = ThreadState.fromText(m.group(1));
            } else if ((m = WAITING_LOCK.matcher(line)).matches()) {
                waiting = m.group(1);
            } else if ((m = HELD_LOCK.matcher(line)).matches()) {
                held.add(m.group(1));
            } else if ((m = FRAME.matcher(line)).matches()) {
                frames.add(m.group(1));
            }
        }
        if (name != null) {
            threads.add(new ThreadInfo(name, state, frames, waiting, held));
        }
        return new ThreadDump(threads);
    }
}