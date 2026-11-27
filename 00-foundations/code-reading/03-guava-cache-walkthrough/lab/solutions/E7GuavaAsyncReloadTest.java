package lab.codereading.cache.solutions;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reference solution for Exercise E7:
 * Demonstrates Guava refreshAfterWrite blocking synchronously by default vs
 * asynchronous reload when overriding CacheLoader.reload().
 */
class E7GuavaAsyncReloadTest {

    @Test
    void guavaRefreshBlocksCallerUnlessReloadIsOverriddenAsAsync() throws Exception {
        var syncCounter = new AtomicInteger();
        LoadingCache<String, String> syncRefreshCache = CacheBuilder.newBuilder()
                .refreshAfterWrite(Duration.ofMillis(50))
                .build(new CacheLoader<>() {
                    @Override
                    public String load(String key) throws Exception {
                        int n = syncCounter.incrementAndGet();
                        Thread.sleep(100);
                        return "sync-v" + n;
                    }
                });

        assertThat(syncRefreshCache.get("k")).isEqualTo("sync-v1");
        Thread.sleep(70);

        long t0 = System.nanoTime();
        String syncResult = syncRefreshCache.get("k");
        long syncWaitMs = (System.nanoTime() - t0) / 1_000_000;

        // Default Guava reload() blocks caller synchronously for ~100ms!
        assertThat(syncResult).isEqualTo("sync-v2");
        assertThat(syncWaitMs).isGreaterThanOrEqualTo(80);

        // Async override using ListeningExecutorService
        var asyncCounter = new AtomicInteger();
        ListeningExecutorService reloadExecutor =
                MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
        try {
            LoadingCache<String, String> asyncRefreshCache = CacheBuilder.newBuilder()
                    .refreshAfterWrite(Duration.ofMillis(50))
                    .build(new CacheLoader<>() {
                        @Override
                        public String load(String key) throws Exception {
                            int n = asyncCounter.incrementAndGet();
                            Thread.sleep(100);
                            return "async-v" + n;
                        }

                        @Override
                        public ListenableFuture<String> reload(String key, String oldValue) {
                            return reloadExecutor.submit(() -> load(key));
                        }
                    });

            assertThat(asyncRefreshCache.get("k")).isEqualTo("async-v1");
            Thread.sleep(70);

            long t1 = System.nanoTime();
            String asyncResult = asyncRefreshCache.get("k");
            long asyncWaitMs = (System.nanoTime() - t1) / 1_000_000;

            assertThat(asyncResult).isEqualTo("async-v1");
            assertThat(asyncWaitMs).isLessThan(30);

            Thread.sleep(130);
            assertThat(asyncRefreshCache.get("k")).isEqualTo("async-v2");
        } finally {
            reloadExecutor.shutdown();
        }
    }
}
