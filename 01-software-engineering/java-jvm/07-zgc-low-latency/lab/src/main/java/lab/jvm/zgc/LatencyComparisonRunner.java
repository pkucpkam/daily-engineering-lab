package lab.jvm.zgc;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Compares application-level pause jitter (p50 / p99 / max) under G1 vs ZGC
 * while maintaining a large live object graph (~300 MB) and heavy concurrent allocation.
 */
public class LatencyComparisonRunner {

    private static volatile Object blackhole;

    public static void main(String[] args) throws Exception {
        String gcNames = ManagementFactory.getGarbageCollectorMXBeans().stream()
                .map(GarbageCollectorMXBean::getName)
                .toList()
                .toString();
        System.out.println("=== JVM-07 Lab: Latency Jitter Comparison | Active GCs: " + gcNames + " ===");

        // Hold ~250 MB live object graph with many cross-references to stress marking/evacuation
        int liveNodes = 2_000_000;
        List<byte[]> livePool = new ArrayList<>(liveNodes);
        for (int i = 0; i < liveNodes; i++) {
            livePool.add(new byte[96]);
        }

        var running = new AtomicBoolean(true);

        // Background allocator generating continuous Young + medium-lived churn (~1.5 GB total)
        Thread allocator = new Thread(() -> {
            byte[][] ring = new byte[4_096][];
            int idx = 0;
            while (running.get()) {
                byte[] block = new byte[32 * 1024];
                block[0] = (byte) idx;
                ring[idx & 4095] = block;
                blackhole = block;
                idx++;
            }
        }, "churn-allocator");
        allocator.setDaemon(true);
        allocator.start();

        // Warmup 400ms
        Thread.sleep(400);

        // Measure 2,000 periodic 1ms probes to detect any STW pause spikes
        int samples = 2_000;
        long[] jitterUs = new long[samples];
        for (int i = 0; i < samples; i++) {
            long targetSleepNs = 1_000_000L; // 1 ms
            long t0 = System.nanoTime();
            Thread.sleep(1);
            long elapsedNs = System.nanoTime() - t0;
            jitterUs[i] = Math.max(0L, (elapsedNs - targetSleepNs) / 1_000L);
        }

        running.set(false);
        allocator.join(500);

        Arrays.sort(jitterUs);
        long p50 = jitterUs[samples / 2];
        long p99 = jitterUs[(int) (samples * 0.99)];
        long max = jitterUs[samples - 1];

        long gcCount = ManagementFactory.getGarbageCollectorMXBeans().stream()
                .mapToLong(b -> Math.max(0, b.getCollectionCount())).sum();
        long gcTimeMs = ManagementFactory.getGarbageCollectorMXBeans().stream()
                .mapToLong(b -> Math.max(0, b.getCollectionTime())).sum();

        System.out.printf("  Live set=%d objects | Probe jitter -> p50=%d us | p99=%d us | max=%d us (%.2f ms)%n",
                livePool.size(), p50, p99, max, max / 1000.0);
        System.out.printf("  Reported GC MXBeans -> events=%d | total pause/cycle time=%d ms%n",
                gcCount, gcTimeMs);
        if (blackhole == null && livePool.isEmpty()) {
            System.out.println("unreachable");
        }
    }
}
