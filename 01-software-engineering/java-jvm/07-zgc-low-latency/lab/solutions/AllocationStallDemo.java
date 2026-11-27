import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * L3 Solution demo: demonstrates ZGC Allocation Stall when -Xmx leaves almost no
 * headroom above the live object set while a mutator keeps allocating.
 *
 * Run tight heap (expect Allocation Stall / high latency):
 *   java -Xmx360m -XX:+UseZGC "-Xlog:gc" AllocationStallDemo.java
 * Run with proper headroom:
 *   java -Xmx1g -XX:+UseZGC "-Xlog:gc" AllocationStallDemo.java
 */
public class AllocationStallDemo {

    private static volatile Object blackhole;

    public static void main(String[] args) throws Exception {
        // Hold ~220 MB live graph
        int count = 1_600_000;
        List<byte[]> live = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            live.add(new byte[96]);
        }

        var stop = new AtomicBoolean(false);
        Thread allocator = new Thread(() -> {
            while (!stop.get()) {
                blackhole = new byte[64 * 1024];
            }
        }, "tight-allocator");
        allocator.setDaemon(true);
        allocator.start();

        long worstProbeMs = 0;
        for (int i = 0; i < 400; i++) {
            long t0 = System.nanoTime();
            byte[] probe = new byte[4096];
            blackhole = probe;
            long tookMs = (System.nanoTime() - t0) / 1_000_000L;
            if (tookMs > worstProbeMs) {
                worstProbeMs = tookMs;
            }
            Thread.sleep(2);
        }

        stop.set(true);
        allocator.join(500);
        System.out.printf("Live objects=%d | Worst allocation probe=%d ms%n", live.size(), worstProbeMs);
    }
}
