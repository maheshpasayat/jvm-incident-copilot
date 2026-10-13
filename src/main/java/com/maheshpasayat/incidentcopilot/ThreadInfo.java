package com.maheshpasayat.incidentcopilot;

import java.util.List;

/**
 * One thread read from a dump.
 *
 * @param name         thread name, e.g. "http-nio-8080-exec-1"
 * @param state        thread state
 * @param stackFrames  stack frames, top frame first
 * @param waitingOnLock lock id this thread is waiting for (BLOCKED), or null
 * @param heldLocks    lock ids this thread currently holds
 */
public record ThreadInfo(
        String name,
        ThreadState state,
        List<String> stackFrames,
        String waitingOnLock,
        List<String> heldLocks) {

    public ThreadInfo {
        stackFrames = List.copyOf(stackFrames);
        heldLocks = List.copyOf(heldLocks);
    }
}