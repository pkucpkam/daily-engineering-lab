package lab.codereading.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Interactive lab for READ-03:
 *  1) Zipf + scan workload hit-rate: Guava Cache (Segmented LRU) vs Caffeine (W-TinyLFU)
 *  2) expireAfterWrite vs refreshAfterWrite latency under concurrent load
 *  3) Stampede coalescing: LoadingCache.get(K) vs naive getIfPresent + put
 */
public class CacheWalkthroughRunner {

    public static void main(String[] args) throws Exception {
        System.out.println("=== READ-03 Lab: Guava Cache vs Caffeine ===");
        runZipfHitRateComparison();
        runExpireVsRefreshExperiment();
        runStampedeCoalescingExperiment();
    }

    private static void runZipfHitRateComparison() {
        int cacheCapacity = 200;
        int keyUniverse = 2_000;
        int accesses = 60_000;

        int[] trace = generateZipfPlusScanTrace(keyUniverse, accesses);

        LoadingCache<Integer, String> guava = CacheBuilder.newBuilder()
                .maximumSize(cacheCapacity)
                .recordStats()
                .build(new CacheLoader<>() {
                    @Override
                    public String load(Integer key) {
                        return "val-" + key;
                    }
                });

        com.github.benmanes.caffeine.cache.LoadingCache<Integer, String> caffeine = Caffeine.newBuilder()
                .maximumSize(cacheCapacity)
                .recordStats()
                .build(key -> "val-" + key);

        long tGuava = measureMs(() -> {
            for (int k : trace) {
                guava.getUnchecked(k);
            }
        });
        long tCaffeine = measureMs(() -> {
            for (int k : trace) {
                caffeine.get(k);
            }
        });
        // Flush Caffeine's asynchronous maintenance buffer before reading final stats
        caffeine.cleanUp();

        System.out.println("\n[1] Zipf (skew=1.05, 2k keys) + periodic cold scans, maximumSize=200:");
        System.out.printf("  Guava (Segmented LRU) -> hitRate=%.2f%% | hits=%d | misses=%d | time=%d ms%n",
                guava.stats().hitRate() * 100.0, guava.stats().hitCount(), guava.stats().missCount(), tGuava);
        System.out.printf("  Caffeine (W-TinyLFU)  -> hitRate=%.2f%% | hits=%d | misses=%d | time=%d ms%n",
                caffeine.stats().hitRate() * 100.0, caffeine.stats().hitCount(), caffeine.stats().missCount(), tCaffeine);
    }

    private static void runExpireVsRefreshExperiment() throws Exception {
        System.out.println("\n[2] expireAfterWrite(100ms) vs refreshAfterWrite(100ms) with 120ms slow loader:");
        var expireLoadCount = new AtomicInteger();
        var refreshLoadCount = new AtomicInteger();

        var expireCache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMillis(100))
                .build((String k) -> {
                    expireLoadCount.incrementAndGet();
                    Thread.sleep(120);
                    return "v-" + expireLoadCount.get();
                });

        var refreshCache = Caffeine.newBuilder()
                .refreshAfterWrite(Duration.ofMillis(100))
                .build((String k) -> {
                    int n = refreshLoadCount.incrementAndGet();
                    Thread.sleep(120);
                    return "v-" + n;
                });

        // Prime both caches
        expireCache.get("hot");
        refreshCache.get("hot");
        // Wait past 100ms threshold
        Thread.sleep(130);

        List<Long> expireLatencies = hitConcurrently(8, () -> expireCache.get("hot"));
        List<Long> refreshLatencies = hitConcurrently(8, () -> refreshCache.get("hot"));
        Thread.sleep(150); // allow background refresh to complete
        String afterRefresh = refreshCache.get("hot");

        System.out.printf("  expireAfterWrite  -> 8 callers waited (max=%d ms, min=%d ms) | loads=%d%n",
                max(expireLatencies), min(expireLatencies), expireLoadCount.get());
        System.out.printf("  refreshAfterWrite -> 8 callers got stale immediately (max=%d ms, min=%d ms) | value after bg=%s%n",
                max(refreshLatencies), min(refreshLatencies), afterRefresh);
    }

    private static void runStampedeCoalescingExperiment() throws Exception {
        System.out.println("\n[3] Cold-key stampede (16 concurrent threads requesting same missing key):");
        var safeLoads = new AtomicInteger();
        var naiveLoads = new AtomicInteger();

        var caffeine = Caffeine.newBuilder()
                .maximumSize(100)
                .build((String k) -> {
                    safeLoads.incrementAndGet();
                    Thread.sleep(60);
                    return "loaded";
                });

        var naiveCache = Caffeine.newBuilder().maximumSize(100).<String, String>build();

        hitConcurrently(16, () -> caffeine.get("cold-1"));
        hitConcurrently(16, () -> {
            String v = naiveCache.getIfPresent("cold-1");
            if (v == null) {
                naiveLoads.incrementAndGet();
                try {
                    Thread.sleep(60);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                v = "loaded";
                naiveCache.put("cold-1", v);
            }
        });

        System.out.printf("  LoadingCache.get(K)      -> loader invoked %d time(s) for 16 threads%n", safeLoads.get());
        System.out.printf("  Naive getIfPresent + put -> loader invoked %d time(s) for 16 threads%n", naiveLoads.get());
    }

    private static List<Long> hitConcurrently(int threads, Runnable action) throws Exception {
        List<Long> latencies = new ArrayList<>();
        try (var pool = Executors.newFixedThreadPool(threads)) {
            var gate = new CountDownLatch(1);
            List<Future<Long>> futures = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                futures.add(pool.submit(() -> {
                    gate.await();
                    long t0 = System.nanoTime();
                    action.run();
                    return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0);
                }));
            }
            gate.countDown();
            for (var f : futures) {
                latencies.add(f.get());
            }
        }
        return latencies;
    }

    /**
     * Generates a Zipf-like workload interleaved with sequential cold scans
     * (simulating a nightly batch scan hitting cold keys while hot API keys stay active).
     */
    private static int[] generateZipfPlusScanTrace(int universe, int total) {
        double[] cdf = new double[universe];
        double sum = 0;
        for (int i = 1; i <= universe; i++) {
            sum += 1.0 / Math.pow(i, 1.05);
            cdf[i - 1] = sum;
        }
        for (int i = 0; i < universe; i++) {
            cdf[i] /= sum;
        }

        var rng = new Random(42);
        int[] trace = new int[total];
        int scanCursor = 500;
        for (int i = 0; i < total; i++) {
            // Every 200 accesses, inject a 40-key one-time cold scan burst
            if ((i % 200) < 40) {
                trace[i] = 500 + (scanCursor++ % 1500);
            } else {
                double u = rng.nextDouble();
                int idx = java.util.Arrays.binarySearch(cdf, u);
                trace[i] = idx >= 0 ? idx : Math.min(universe - 1, -idx - 1);
            }
        }
        return trace;
    }

    private static long measureMs(Runnable r) {
        long t0 = System.nanoTime();
        r.run();
        return (System.nanoTime() - t0) / 1_000_000;
    }

    private static long max(List<Long> xs) {
        return xs.stream().mapToLong(Long::longValue).max().orElse(0);
    }

    private static long min(List<Long> xs) {
        return xs.stream().mapToLong(Long::longValue).min().orElse(0);
    }
}
