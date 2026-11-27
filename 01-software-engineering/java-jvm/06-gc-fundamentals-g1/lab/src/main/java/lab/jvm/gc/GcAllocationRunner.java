package lab.jvm.gc;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Interactive G1 GC lab runner for JVM-06:
 *  Mode "normal"   : allocates small 4 KB short-lived buffers + small sliding retention window
 *  Mode "humongous": allocates 600 KB arrays (> 50% of 1 MB G1 region) triggering Humongous Allocations
 * Run with:
 *   java -Xms64m -Xmx64m -XX:+UseG1GC -XX:G1HeapRegionSize=1m -Xlog:gc -cp target/classes lab.jvm.gc.GcAllocationRunner humongous
 */
public class GcAllocationRunner {

    private static volatile byte[] blackhole;

    public static void main(String[] args) {
        String mode = args.length > 0 ? args[0] : "both";
        System.out.println("=== JVM-06 Lab: G1 GC Fundamentals & Humongous Objects (mode=" + mode + ") ===");

        if ("normal".equalsIgnoreCase(mode) || "both".equalsIgnoreCase(mode)) {
            runWorkload("NORMAL (4 KB buffers, 128 MB total)", 4 * 1024, 32_768, 512);
        }
        if ("humongous".equalsIgnoreCase(mode) || "both".equalsIgnoreCase(mode)) {
            // With -XX:G1HeapRegionSize=1m, any object > 512 KB is Humongous!
            runWorkload("HUMONGOUS (600 KB buffers, 128 MB total)", 600 * 1024, 218, 24);
        }
    }

    private static void runWorkload(String label, int chunkBytes, int totalChunks, int retainWindow) {
        long beforeCount = totalGcCount();
        long beforeTime = totalGcTimeMs();
        Deque<byte[]> window = new ArrayDeque<>(retainWindow);

        long t0 = System.nanoTime();
        for (int i = 0; i < totalChunks; i++) {
            byte[] buf = new byte[chunkBytes];
            buf[0] = (byte) i;
            blackhole = buf;
            window.addLast(buf);
            if (window.size() > retainWindow) {
                window.removeFirst();
            }
        }
        long elapsedMs = (System.nanoTime() - t0) / 1_000_000;

        long deltaCount = totalGcCount() - beforeCount;
        long deltaTime = totalGcTimeMs() - beforeTime;
        System.out.printf("  [%s] elapsed=%d ms | GC events=%d | total STW GC time=%d ms | retained=%d chunks%n",
                label, elapsedMs, deltaCount, deltaTime, window.size());
    }

    public static List<String> activeCollectorNames() {
        List<String> names = new ArrayList<>();
        for (GarbageCollectorMXBean bean : ManagementFactory.getGarbageCollectorMXBeans()) {
            names.add(bean.getName());
        }
        return names;
    }

    private static long totalGcCount() {
        return ManagementFactory.getGarbageCollectorMXBeans().stream()
                .mapToLong(b -> Math.max(0, b.getCollectionCount()))
                .sum();
    }

    private static long totalGcTimeMs() {
        return ManagementFactory.getGarbageCollectorMXBeans().stream()
                .mapToLong(b -> Math.max(0, b.getCollectionTime()))
                .sum();
    }
}
