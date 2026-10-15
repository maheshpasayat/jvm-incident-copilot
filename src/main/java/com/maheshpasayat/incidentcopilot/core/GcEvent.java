package com.maheshpasayat.incidentcopilot.core;

/**
 * One garbage collection pause read from a GC log.
 *
 * @param uptimeSeconds seconds since JVM start when the event was logged
 * @param gcId          the GC(n) number
 * @param type          "Pause Young", "Pause Full", "Pause Remark" and so on
 * @param heapBeforeMb  heap used before the pause, in MB
 * @param heapAfterMb   heap used after the pause, in MB
 * @param heapTotalMb   total heap size, in MB
 * @param pauseMillis   pause duration in milliseconds
 */
public record GcEvent(
        double uptimeSeconds,
        int gcId,
        String type,
        long heapBeforeMb,
        long heapAfterMb,
        long heapTotalMb,
        double pauseMillis) {

    public boolean isFullGc() {
        return type.startsWith("Pause Full");
    }
}