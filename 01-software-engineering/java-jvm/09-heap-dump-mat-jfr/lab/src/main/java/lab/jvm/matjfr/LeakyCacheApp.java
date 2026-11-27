package lab.jvm.matjfr;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Sample application exhibiting three diagnosable patterns:
 * 1. Static Map memory leak (SESSION_CACHE retains 128 KB entries indefinitely)
 * 2. High allocation hot method (allocateTempAuditPayload) for JFR allocation profiling
 * 3. Contended synchronized lock (contendedAuditLock) for JFR lock profiling
 */
public class LeakyCacheApp {

    public static final Map<String, byte[]> SESSION_CACHE = new ConcurrentHashMap<>();
    private static final Object AUDIT_LOCK = new Object();
    private static volatile Object sink;

    public static void main(String[] args) throws Exception {
        long durationMs = args.length > 0 ? Long.parseLong(args[0]) : 3_000L;
        int leakedEntries = args.length > 1 ? Integer.parseInt(args[1]) : 400;

        populateLeakedSessions(leakedEntries);
        System.out.printf("PID=%d | Leaked %d session entries (~%d MB) in SESSION_CACHE%n",
                ProcessHandle.current().pid(), SESSION_CACHE.size(), (SESSION_CACHE.size() * 128) / 1024);

        runAllocationAndLockWorkload(durationMs);
        System.out.println("Workload complete.");
    }

    public static void populateLeakedSessions(int entries) {
        for (int i = 0; i < entries; i++) {
            SESSION_CACHE.put("session-" + i, new byte[128 * 1024]);
        }
    }

    public static void runAllocationAndLockWorkload(long durationMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + durationMs;
        CountDownLatch startGate = new CountDownLatch(1);
        Thread[] workers = new Thread[4];
        for (int t = 0; t < workers.length; t++) {
            workers[t] = new Thread(() -> {
                try {
                    startGate.await();
                    while (System.currentTimeMillis() < deadline) {
                        allocateTempAuditPayload();
                        contendedAuditLock();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "audit-worker-" + t);
            workers[t].start();
        }
        startGate.countDown();
        for (Thread w : workers) {
            w.join();
        }
    }

    static void allocateTempAuditPayload() {
        // Intentional short-lived allocation hotspot for JFR profiling
        for (int i = 0; i < 8; i++) {
            sink = new byte[16 * 1024];
        }
    }

    static void contendedAuditLock() throws InterruptedException {
        synchronized (AUDIT_LOCK) {
            // Intentional short hold causing monitor contention across 4 worker threads
            Thread.sleep(2);
        }
    }

    public static void clearSessions() {
        SESSION_CACHE.clear();
    }
}
