package lab.jvm.gclog;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates predictable allocation patterns for GC log analysis:
 * - "normal": high short-lived churn with flat live baseline
 * - "leak": retains part of every batch until heap hits capacity pressure
 */
public class GcWorkloadRunner {

    private static final List<byte[]> LEAKED_CACHE = new ArrayList<>();
    private static volatile Object sink;

    public static void main(String[] args) {
        String mode = args.length > 0 ? args[0] : "normal";
        int iterations = args.length > 1 ? Integer.parseInt(args[1]) : 16_000;

        // Baseline ~24 MB live graph present in both modes
        List<byte[]> baseline = new ArrayList<>(384);
        for (int i = 0; i < 384; i++) {
            baseline.add(new byte[64 * 1024]);
        }

        if ("leak".equalsIgnoreCase(mode)) {
            runLeak(iterations);
        } else {
            runNormal(iterations);
        }

        System.out.printf("Completed mode=%s | baseline=%d | leakedEntries=%d%n",
                mode, baseline.size(), LEAKED_CACHE.size());
    }

    private static void runNormal(int iterations) {
        for (int i = 0; i < iterations; i++) {
            // Pure garbage churn: dies before next Young GC
            sink = new byte[64 * 1024];
            if (sink == null) {
                throw new IllegalStateException();
            }
        }
    }

    private static void runLeak(int iterations) {
        for (int i = 0; i < iterations; i++) {
            byte[] block = new byte[64 * 1024];
            sink = block;
            // Retain ~1 out of 8 allocations (~128 MB over 16k iterations)
            if (i % 8 == 0 && LEAKED_CACHE.size() < 2_400) {
                LEAKED_CACHE.add(block);
            }
        }
    }
}
