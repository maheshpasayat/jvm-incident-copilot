package com.maheshpasayat.incidentcopilot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.maheshpasayat.incidentcopilot.core.ThreadDump;
import com.maheshpasayat.incidentcopilot.core.ThreadDumpParser;
import com.maheshpasayat.incidentcopilot.core.ThreadInfo;
import com.maheshpasayat.incidentcopilot.core.ThreadState;
import org.junit.jupiter.api.Test;

class ThreadDumpParserTest {

    private final ThreadDumpParser parser = new ThreadDumpParser();

    private ThreadDump parseSample(String fileName) throws IOException {
        String text = Files.readString(Path.of("samples", "thread-dumps", fileName));
        return parser.parse(text);
    }

    private ThreadInfo thread(ThreadDump dump, String name) {
        return dump.threads().stream()
                .filter(t -> t.name().equals(name))
                .findFirst()
                .orElseThrow();
    }

    // ---- deadlock.txt ----

    @Test
    void deadlock_findsThreeThreads() throws IOException {
        assertEquals(3, parseSample("deadlock.txt").threads().size());
    }

    @Test
    void deadlock_eachWorkerHoldsTheLockTheOtherWants() throws IOException {
        ThreadDump dump = parseSample("deadlock.txt");
        ThreadInfo order = thread(dump, "order-worker-1");
        ThreadInfo payment = thread(dump, "payment-worker-1");

        assertEquals(ThreadState.BLOCKED, order.state());
        assertEquals("0x00000007a1b2c3d0", order.waitingOnLock());
        assertEquals(List.of("0x00000007a1b2c3e0"), order.heldLocks());

        assertEquals(ThreadState.BLOCKED, payment.state());
        assertEquals("0x00000007a1b2c3e0", payment.waitingOnLock());
        assertEquals(List.of("0x00000007a1b2c3d0"), payment.heldLocks());
    }

    @Test
    void deadlock_mainThreadIsWaitingAndHoldsNothing() throws IOException {
        ThreadInfo main = thread(parseSample("deadlock.txt"), "main");
        assertEquals(ThreadState.WAITING, main.state());
        assertNull(main.waitingOnLock());
        assertTrue(main.heldLocks().isEmpty());
        assertEquals(2, main.stackFrames().size());
    }

    // ---- pool-exhaustion.txt ----

    @Test
    void pool_findsEightThreads() throws IOException {
        assertEquals(8, parseSample("pool-exhaustion.txt").threads().size());
    }

    @Test
    void pool_fourThreadsAreBlockedOnTheSameLock() throws IOException {
        ThreadDump dump = parseSample("pool-exhaustion.txt");
        List<ThreadInfo> blocked = dump.threads().stream()
                .filter(t -> t.state() == ThreadState.BLOCKED)
                .toList();

        assertEquals(4, blocked.size());
        assertEquals(1, blocked.stream().map(ThreadInfo::waitingOnLock).distinct().count());
    }

    @Test
    void pool_theRunnableThreadHoldsThatLock() throws IOException {
        ThreadInfo culprit = thread(parseSample("pool-exhaustion.txt"), "http-nio-8080-exec-5");
        assertEquals(ThreadState.RUNNABLE, culprit.state());
        assertEquals(List.of("0x00000007b0000001"), culprit.heldLocks());
        assertEquals(3, culprit.stackFrames().size());
    }

    @Test
    void pool_threadWithoutAStateLineBecomesUnknown() throws IOException {
        ThreadInfo gc = thread(parseSample("pool-exhaustion.txt"), "GC Thread#0");
        assertEquals(ThreadState.UNKNOWN, gc.state());
        assertTrue(gc.stackFrames().isEmpty());
    }

    // ---- bad input ----

    @Test
    void nullAndBlankTextGiveAnEmptyDump() {
        assertTrue(parser.parse(null).threads().isEmpty());
        assertTrue(parser.parse("   \n  ").threads().isEmpty());
    }

    @Test
    void textWithNoThreadsGivesAnEmptyDump() {
        assertTrue(parser.parse("this is not a thread dump\nat nothing").threads().isEmpty());
    }

    // ---- inline text block (Java 15+) ----

    @Test
    void parsesASmallDumpWrittenAsATextBlock() {
        String text = """
                "worker" #1 prio=5
                   java.lang.Thread.State: TIMED_WAITING (sleeping)
                \tat java.base/java.lang.Thread.sleep(Native Method)
                """;

        ThreadDump dump = parser.parse(text);

        assertEquals(1, dump.threads().size());
        ThreadInfo worker = dump.threads().get(0);
        assertEquals("worker", worker.name());
        assertEquals(ThreadState.TIMED_WAITING, worker.state());
        assertEquals(List.of("java.base/java.lang.Thread.sleep(Native Method)"), worker.stackFrames());
    }
}