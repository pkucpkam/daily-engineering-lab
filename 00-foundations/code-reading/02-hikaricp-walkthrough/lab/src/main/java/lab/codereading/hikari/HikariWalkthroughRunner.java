package lab.codereading.hikari;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.util.FastList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Interactive walkthrough lab for READ-02 using real HikariCP 5.1.0 + H2:
 *  1) Pool exhaustion & getConnection() timeout with pool size = 2 and 5 concurrent threads
 *  2) Leak detection threshold logging (ProxyLeakTask)
 *  3) FastList vs ArrayList micro-comparison (why HikariCP avoids ArrayList range checks & iterator alloc)
 */
public class HikariWalkthroughRunner {

    public static void main(String[] args) throws Exception {
        System.out.println("=== READ-02 Lab: HikariCP Source Walkthrough ===");
        runContentionWithPoolSize2();
        runLeakDetectionDemo();
        runFastListVsArrayListDemo();
    }

    private static void runContentionWithPoolSize2() throws Exception {
        System.out.println("\n[1] Pool size = 2, 5 concurrent threads (each holds connection 150ms), connectionTimeout = 350ms:");
        var config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:walkthrough1;DB_CLOSE_DELAY=-1");
        config.setMaximumPoolSize(2);
        config.setConnectionTimeout(350);
        config.setPoolName("lab-pool-2");

        try (var ds = new HikariDataSource(config);
             var exec = Executors.newFixedThreadPool(5)) {

            var startGate = new CountDownLatch(1);
            var okCount = new AtomicInteger();
            var timeoutCount = new AtomicInteger();
            List<Future<?>> futures = new ArrayList<>();

            for (int i = 0; i < 5; i++) {
                final int workerId = i + 1;
                futures.add(exec.submit(() -> {
                    startGate.await();
                    long t0 = System.nanoTime();
                    try (Connection conn = ds.getConnection();
                         PreparedStatement ps = conn.prepareStatement("SELECT 1")) {
                        // Put a breakpoint on ds.getConnection() or HikariPool.getConnection() to step through!
                        try (ResultSet rs = ps.executeQuery()) {
                            rs.next();
                        }
                        Thread.sleep(150);
                        long elapsed = (System.nanoTime() - t0) / 1_000_000;
                        okCount.incrementAndGet();
                        System.out.printf("  Worker-%d acquired & released in %d ms (class=%s)%n",
                                workerId, elapsed, conn.getClass().getSimpleName());
                    } catch (SQLException ex) {
                        long elapsed = (System.nanoTime() - t0) / 1_000_000;
                        timeoutCount.incrementAndGet();
                        System.out.printf("  Worker-%d TIMED OUT after %d ms: %s%n",
                                workerId, elapsed, ex.getMessage());
                    }
                    return null;
                }));
            }

            startGate.countDown();
            for (var f : futures) {
                f.get();
            }
            System.out.printf("  Summary: succeeded=%d, timedOut=%d (2 batches of 2 fit in 350ms; 5th waited >350ms)%n",
                    okCount.get(), timeoutCount.get());
        }
    }

    private static void runLeakDetectionDemo() throws Exception {
        System.out.println("\n[2] Leak detection demo (leakDetectionThreshold = 2000ms, holding conn for 2200ms):");
        var config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:walkthrough2;DB_CLOSE_DELAY=-1");
        config.setMaximumPoolSize(2);
        config.setLeakDetectionThreshold(2000);
        config.setPoolName("leak-pool");

        try (var ds = new HikariDataSource(config)) {
            Connection leaked = ds.getConnection();
            System.out.println("  Borrowed connection without closing; waiting 2.2s for ProxyLeakTask warning...");
            Thread.sleep(2200);
            leaked.close();
            System.out.println("  Connection closed (notice 'Previously reported leaked connection' info log if slf4j enabled).");
        }
    }

    private static void runFastListVsArrayListDemo() {
        System.out.println("\n[3] FastList (reverse tail-scan) vs ArrayList (head-scan) on 32 open statements:");
        int iterations = 100_000;
        var arrayList = new ArrayList<String>(32);
        var fastList = new FastList<>(String.class, 32);
        String[] stmts = new String[32];
        for (int i = 0; i < 32; i++) {
            stmts[i] = "stmt-" + i;
        }

        // Warmup
        for (int i = 0; i < 10_000; i++) {
            for (String s : stmts) { arrayList.add(s); fastList.add(s); }
            for (int k = 31; k >= 0; k--) { arrayList.remove(stmts[k]); fastList.remove(stmts[k]); }
        }

        long tArray = measureMs(() -> {
            for (int i = 0; i < iterations; i++) {
                for (String s : stmts) arrayList.add(s);
                for (int k = 31; k >= 0; k--) arrayList.remove(stmts[k]);
            }
        });
        long tFast = measureMs(() -> {
            for (int i = 0; i < iterations; i++) {
                for (String s : stmts) fastList.add(s);
                for (int k = 31; k >= 0; k--) fastList.remove(stmts[k]);
            }
        });
        System.out.printf("  100k x 32 LIFO statement close -> ArrayList: %d ms | FastList: %d ms%n", tArray, tFast);
    }

    private static long measureMs(Runnable r) {
        long t0 = System.nanoTime();
        r.run();
        return (System.nanoTime() - t0) / 1_000_000;
    }
}
