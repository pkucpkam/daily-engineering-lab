package lab.codereading.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class CacheWalkthroughTest {

    @Test
    void caffeineCoalescesConcurrentColdMissesToSingleLoaderInvocation() throws Exception {
        var loadCount = new AtomicInteger();
        var cache = Caffeine.newBuilder()
                .maximumSize(100)
                .build((String k) -> {
                    loadCount.incrementAndGet();
                    Thread.sleep(60);
                    return "value-" + k;
                });

        try (var pool = Executors.newFixedThreadPool(12)) {
            var gate = new CountDownLatch(1);
            List<Future<String>> futures = new ArrayList<>();
            for (int i = 0; i < 12; i++) {
                futures.add(pool.submit(() -> {
                    gate.await();
                    return cache.get("key-1");
                }));
            }
            gate.countDown();
            for (var f : futures) {
                assertThat(f.get()).isEqualTo("value-key-1");
            }
        }

        assertThat(loadCount.get()).isEqualTo(1);
    }

    @Test
    void guavaLoadingCacheCoalescesConcurrentColdMissesPerKey() throws Exception {
        var loadCount = new AtomicInteger();
        var cache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .build(new CacheLoader<String, String>() {
                    @Override
                    public String load(String key) throws Exception {
                        loadCount.incrementAndGet();
                        Thread.sleep(60);
                        return "guava-" + key;
                    }
                });

        try (var pool = Executors.newFixedThreadPool(12)) {
            var gate = new CountDownLatch(1);
            List<Future<String>> futures = new ArrayList<>();
            for (int i = 0; i < 12; i++) {
                futures.add(pool.submit(() -> {
                    gate.await();
                    return cache.get("key-1");
                }));
            }
            gate.countDown();
            for (var f : futures) {
                assertThat(f.get()).isEqualTo("guava-key-1");
            }
        }

        assertThat(loadCount.get()).isEqualTo(1);
    }

    @Test
    void caffeineRefreshAfterWriteServesStaleImmediatelyWhileReloadingAsync() throws Exception {
        var loads = new AtomicInteger();
        var cache = Caffeine.newBuilder()
                .refreshAfterWrite(Duration.ofMillis(80))
                .build((String k) -> {
                    int n = loads.incrementAndGet();
                    Thread.sleep(100);
                    return "v" + n;
                });

        assertThat(cache.get("k")).isEqualTo("v1");
        Thread.sleep(100);

        long t0 = System.nanoTime();
        String duringRefresh = cache.get("k");
        long callerWaitMs = (System.nanoTime() - t0) / 1_000_000;

        // Caller gets stale value immediately without blocking for the 100ms reload
        assertThat(duringRefresh).isEqualTo("v1");
        assertThat(callerWaitMs).isLessThan(50);

        Thread.sleep(150);
        assertThat(cache.get("k")).isEqualTo("v2");
    }
}
