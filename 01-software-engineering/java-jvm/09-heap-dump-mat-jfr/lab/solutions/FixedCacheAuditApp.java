import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * L3 Solution: Bounded LRU cache replacing unbounded static ConcurrentHashMap,
 * ThreadLocal scratch buffer removing hot-loop allocations, and moving slow work
 * outside the monitor lock.
 */
public class FixedCacheAuditApp {

    private static final int MAX_SESSIONS = 64;
    public static final Map<String, byte[]> BOUNDED_CACHE = Collections.synchronizedMap(
            new LinkedHashMap<>(MAX_SESSIONS, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, byte[]> eldest) {
                    return size() > MAX_SESSIONS;
                }
            });

    private static final ThreadLocal<byte[]> SCRATCH_BUFFER = ThreadLocal.withInitial(() -> new byte[16 * 1024]);
    private static volatile byte sinkByte;

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 400; i++) {
            BOUNDED_CACHE.put("session-" + i, new byte[128 * 1024]);
        }

        long deadline = System.currentTimeMillis() + 600L;
        CountDownLatch gate = new CountDownLatch(1);
        Thread[] workers = new Thread[4];
        for (int t = 0; t < workers.length; t++) {
            workers[t] = new Thread(() -> {
                try {
                    gate.await();
                    while (System.currentTimeMillis() < deadline) {
                        byte[] buf = SCRATCH_BUFFER.get();
                        buf[0] = 42;
                        sinkByte = buf[0];
                        Thread.sleep(2); // I/O or sleep strictly outside synchronized lock
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    SCRATCH_BUFFER.remove();
                }
            }, "fixed-worker-" + t);
            workers[t].start();
        }
        gate.countDown();
        for (Thread w : workers) {
            w.join();
        }

        System.out.printf("Fixed cache size=%d (max=%d) | sink=%d%n",
                BOUNDED_CACHE.size(), MAX_SESSIONS, sinkByte);
    }
}
