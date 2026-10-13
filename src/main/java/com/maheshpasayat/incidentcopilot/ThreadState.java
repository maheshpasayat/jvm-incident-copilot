package com.maheshpasayat.incidentcopilot;

/** Thread states as printed in a JVM thread dump (java.lang.Thread.State). */
public enum ThreadState {
    NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED, UNKNOWN;

    /** Converts the text from a dump to an enum value. Anything unrecognised becomes UNKNOWN. */
    public static ThreadState fromText(String text) {
        if (text == null) {
            return UNKNOWN;
        }
        try {
            return valueOf(text.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}