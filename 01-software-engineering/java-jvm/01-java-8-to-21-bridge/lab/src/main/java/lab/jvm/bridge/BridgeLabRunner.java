package lab.jvm.bridge;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Interactive experiment runner for JVM-01:
 *  1) Side-by-side Java 7 vs Java 21 order report equivalence
 *  2) For-loop vs Sequential Stream vs Parallel Stream benchmark
 *  3) Optional.orElse vs orElseGet evaluation count
 *  4) SimpleDateFormat concurrency bug vs DateTimeFormatter safety
 */
public class BridgeLabRunner {

    private static volatile long blackhole;

    public static void main(String[] args) throws Exception {
        System.out.println("=== JVM-01 Lab: Java 7 -> 21 Bridge ===");
        System.out.printf("Java version: %s | Available CPUs: %d | ForkJoinPool.commonPool parallelism: %d%n%n",
                System.getProperty("java.version"),
                Runtime.getRuntime().availableProcessors(),
                ForkJoinPool.commonPool().getParallelism());

        runSideBySideEquivalence();
        runLoopVsStreamBenchmark();
        runOptionalOrElseExperiment();
        runDateFormatterConcurrencyExperiment();
    }

    private static void runSideBySideEquivalence() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/tax-rate", exchange -> {
            byte[] body = "0.10".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();

        try {
            String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/tax-rate";
            List<Order> sample = List.of(
                    new Order("ORD-101", "GOLD", OrderStatus.APPROVED, new BigDecimal("120.00"), "2026-11-25", " vip_q4 "),
                    new Order("ORD-102", "SILVER", OrderStatus.PENDING, new BigDecimal("80.00"), "2026-11-26", null),
                    new Order("ORD-103", "GOLD", OrderStatus.PAID, new BigDecimal("50.00"), "2026-11-20", "old_window"),
                    new Order("ORD-104", "GOLD", OrderStatus.CANCELLED, new BigDecimal("999.00"), "2026-11-26", "ignored")
            );

            OrderReport report7 = new OrderReportServiceJava7().buildReport(sample, "2026-11-27", 3, url);
            OrderReport report21 = new OrderReportServiceJava21().buildReport(sample, "2026-11-27", 3, url);

            System.out.println("[1] Side-by-side Java 7 vs Java 21 Report:");
            System.out.println("Outputs identical? " + report7.equals(report21));
            System.out.println(report21.getJsonPayload());
            System.out.println();
        } finally {
            server.stop(0);
        }
    }

    private static void runLoopVsStreamBenchmark() {
        System.out.println("[2] For-loop vs Sequential Stream vs Parallel Stream:");

        // Case A: Small list (n = 100), many iterations
        int smallIterations = 100_000;
        List<Integer> smallList = buildIntegers(100);
        // Warmup JIT
        for (int i = 0; i < 10_000; i++) {
            blackhole += sumEvenSquaresForLoop(smallList);
            blackhole += sumEvenSquaresStream(smallList);
            blackhole += sumEvenSquaresParallel(smallList);
        }

        long tForSmall = measureMs(() -> {
            long acc = 0;
            for (int i = 0; i < smallIterations; i++) acc += sumEvenSquaresForLoop(smallList);
            blackhole += acc;
        });
        long tSeqSmall = measureMs(() -> {
            long acc = 0;
            for (int i = 0; i < smallIterations; i++) acc += sumEvenSquaresStream(smallList);
            blackhole += acc;
        });
        long tParSmall = measureMs(() -> {
            long acc = 0;
            for (int i = 0; i < smallIterations; i++) acc += sumEvenSquaresParallel(smallList);
            blackhole += acc;
        });
        System.out.printf("  Small (n=100, 100k runs)   -> for-loop: %4d ms | stream: %4d ms | parallelStream: %4d ms%n",
                tForSmall, tSeqSmall, tParSmall);

        // Case B: Large CPU-heavy list (n = 1,000,000)
        List<Integer> largeList = buildIntegers(1_000_000);
        for (int i = 0; i < 3; i++) {
            blackhole += sumCpuHeavyForLoop(largeList);
            blackhole += sumCpuHeavyStream(largeList);
            blackhole += sumCpuHeavyParallel(largeList);
        }

        long tForLarge = measureMs(() -> blackhole += sumCpuHeavyForLoop(largeList));
        long tSeqLarge = measureMs(() -> blackhole += sumCpuHeavyStream(largeList));
        long tParLarge = measureMs(() -> blackhole += sumCpuHeavyParallel(largeList));
        System.out.printf("  Large CPU (n=1M, 1 run)    -> for-loop: %4d ms | stream: %4d ms | parallelStream: %4d ms%n",
                tForLarge, tSeqLarge, tParLarge);

        // Case C: Blocking I/O simulation (n = 24 tasks, 20ms sleep each)
        List<Integer> ioTasks = buildIntegers(24);
        long tIoSeq = measureMs(() -> blackhole += ioTasks.stream().mapToLong(BridgeLabRunner::simulateBlockingIo).sum());
        long tIoPar = measureMs(() -> blackhole += ioTasks.parallelStream().mapToLong(BridgeLabRunner::simulateBlockingIo).sum());
        System.out.printf("  Blocking IO (24x20ms)      -> stream:   %4d ms | parallelStream: %4d ms (pool=%d)%n%n",
                tIoSeq, tIoPar, ForkJoinPool.commonPool().getParallelism());
    }

    private static void runOptionalOrElseExperiment() {
        System.out.println("[3] Optional.orElse vs orElseGet when value IS PRESENT:");
        AtomicInteger orElseCalls = new AtomicInteger();
        AtomicInteger orElseGetCalls = new AtomicInteger();

        Optional<String> present = Optional.of("CACHED_CODE");
        String v1 = present.orElse(expensiveFallback(orElseCalls));
        String v2 = present.orElseGet(() -> expensiveFallback(orElseGetCalls));

        System.out.printf("  orElse result=%s (fallback invoked %d time)%n", v1, orElseCalls.get());
        System.out.printf("  orElseGet result=%s (fallback invoked %d time)%n%n", v2, orElseGetCalls.get());
    }

    private static void runDateFormatterConcurrencyExperiment() throws Exception {
        System.out.println("[4] SimpleDateFormat (shared) vs DateTimeFormatter across 8 threads:");
        SimpleDateFormat unsafeSdf = new SimpleDateFormat("yyyy-MM-dd");
        DateTimeFormatter safeDtf = DateTimeFormatter.ISO_LOCAL_DATE;

        String[] inputs = {"2026-01-15", "2026-06-30", "2026-11-27", "2025-12-31"};
        int tasks = 200;

        AtomicInteger sdfErrors = new AtomicInteger();
        AtomicInteger dtfErrors = new AtomicInteger();

        try (var pool = Executors.newFixedThreadPool(8)) {
            List<Callable<Void>> sdfJobs = new ArrayList<>();
            List<Callable<Void>> dtfJobs = new ArrayList<>();

            for (int i = 0; i < tasks; i++) {
                String expected = inputs[i % inputs.length];
                sdfJobs.add(() -> {
                    try {
                        String roundTrip = unsafeSdf.format(unsafeSdf.parse(expected));
                        if (!expected.equals(roundTrip)) {
                            sdfErrors.incrementAndGet();
                        }
                    } catch (Exception e) {
                        sdfErrors.incrementAndGet();
                    }
                    return null;
                });
                dtfJobs.add(() -> {
                    try {
                        String roundTrip = safeDtf.format(LocalDate.parse(expected, safeDtf));
                        if (!expected.equals(roundTrip)) {
                            dtfErrors.incrementAndGet();
                        }
                    } catch (Exception e) {
                        dtfErrors.incrementAndGet();
                    }
                    return null;
                });
            }

            for (Future<Void> f : pool.invokeAll(sdfJobs)) f.get();
            for (Future<Void> f : pool.invokeAll(dtfJobs)) f.get();
        }

        System.out.printf("  Shared SimpleDateFormat mismatches/exceptions: %d / %d%n", sdfErrors.get(), tasks);
        System.out.printf("  Shared DateTimeFormatter mismatches/exceptions: %d / %d%n", dtfErrors.get(), tasks);
    }

    private static List<Integer> buildIntegers(int n) {
        var list = new ArrayList<Integer>(n);
        for (int i = 0; i < n; i++) {
            list.add(i);
        }
        return list;
    }

    private static long sumEvenSquaresForLoop(List<Integer> list) {
        long sum = 0;
        for (int i = 0; i < list.size(); i++) {
            int v = list.get(i);
            if ((v & 1) == 0) {
                sum += (long) v * v;
            }
        }
        return sum;
    }

    private static long sumEvenSquaresStream(List<Integer> list) {
        return list.stream()
                .filter(v -> (v & 1) == 0)
                .mapToLong(v -> (long) v * v)
                .sum();
    }

    private static long sumEvenSquaresParallel(List<Integer> list) {
        return list.parallelStream()
                .filter(v -> (v & 1) == 0)
                .mapToLong(v -> (long) v * v)
                .sum();
    }

    private static long cpuWork(int v) {
        // Pure CPU mixing to outweigh loop iteration cost
        long x = v + 1L;
        for (int k = 0; k < 20; k++) {
            x = (x * 6364136223846793005L + 1442695040888963407L) >>> 1;
        }
        return x & 0xFF;
    }

    private static long sumCpuHeavyForLoop(List<Integer> list) {
        long sum = 0;
        for (int i = 0; i < list.size(); i++) {
            sum += cpuWork(list.get(i));
        }
        return sum;
    }

    private static long sumCpuHeavyStream(List<Integer> list) {
        return list.stream().mapToLong(BridgeLabRunner::cpuWork).sum();
    }

    private static long sumCpuHeavyParallel(List<Integer> list) {
        return list.parallelStream().mapToLong(BridgeLabRunner::cpuWork).sum();
    }

    private static long simulateBlockingIo(int id) {
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return id;
    }

    private static String expensiveFallback(AtomicInteger counter) {
        counter.incrementAndGet();
        return "COMPUTED_FALLBACK";
    }

    private static long measureMs(Runnable r) {
        long start = System.nanoTime();
        r.run();
        return (System.nanoTime() - start) / 1_000_000;
    }
}
