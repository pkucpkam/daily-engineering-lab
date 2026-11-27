package lab.ai.playbook;

import lab.ai.playbook.buggy.InvoiceJdbcHelperBuggy;
import lab.ai.playbook.buggy.OrderRateLimiterBuggy;
import lab.ai.playbook.buggy.PaymentClientBuggy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Runner minh họa trực tiếp 3 lỗi AI tinh vi + kiểm tra API hallucination vs version drift.
 */
public final class PlaybookLabRunner {

    public static void main(String[] args) throws Exception {
        System.out.println("=== AI-01 Playbook Lab ===");
        runRateLimiterConcurrencyDemo();
        runInvoiceRoundingDemo();
        runPaymentThreadSafetyDemo();
        runApiHallucinationCheck();
    }

    private static void runRateLimiterConcurrencyDemo() throws Exception {
        final int maxRequests = 10;
        final int threads = 16;
        int maxObservedAllowed = 0;

        for (int round = 0; round < 5; round++) {
            OrderRateLimiterBuggy limiter = new OrderRateLimiterBuggy(maxRequests);
            ExecutorService pool = Executors.newFixedThreadPool(threads);
            CountDownLatch ready = new CountDownLatch(threads);
            CountDownLatch start = new CountDownLatch(1);
            AtomicInteger allowed = new AtomicInteger();

            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                futures.add(pool.submit(() -> {
                    ready.countDown();
                    try {
                        start.await();
                        if (limiter.allowRequest("user-42")) {
                            allowed.incrementAndGet();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }));
            }
            ready.await();
            start.countDown();
            for (Future<?> f : futures) {
                f.get();
            }
            pool.shutdown();
            maxObservedAllowed = Math.max(maxObservedAllowed, allowed.get());
        }
        System.out.printf("[1] OrderRateLimiterBuggy (max=%d, %d threads): max allowed in 5 rounds = %d%n",
                maxRequests, threads, maxObservedAllowed);
    }

    private static void runInvoiceRoundingDemo() {
        InvoiceJdbcHelperBuggy helper = new InvoiceJdbcHelperBuggy();
        String sql = helper.buildUnsafeQuery("' OR '1'='1");
        System.out.println("[2] InvoiceJdbcHelperBuggy SQLi payload => " + sql);
        try {
            InvoiceJdbcHelperBuggy.sampleAmounts().stream()
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)
                    .divide(java.math.BigDecimal.valueOf(3));
        } catch (ArithmeticException ex) {
            System.out.println("[2] BigDecimal.divide() without RoundingMode => " + ex.getClass().getSimpleName()
                    + ": " + ex.getMessage());
        }
    }

    private static void runPaymentThreadSafetyDemo() {
        PaymentClientBuggy client = new PaymentClientBuggy();
        int[] attempts = {0};
        try {
            int status = client.sendWithRetry(d -> {
                attempts[0]++;
                return 400; // Bad Request: không nên retry!
            }, "2026-11-27", 3);
            System.out.printf("[3] PaymentClientBuggy retried 400 Bad Request %d times (lastStatus=%d)%n",
                    attempts[0], status);
        } catch (Exception ex) {
            System.out.println("[3] Unexpected exception: " + ex);
        }
    }

    private static void runApiHallucinationCheck() {
        ApiVerifier verifier = new ApiVerifier();
        System.out.println("[4] API Verification against Java 7 target:");
        for (ApiVerifier.ApiProbe probe : ApiVerifier.defaultProbes()) {
            ApiVerifier.VerificationResult result = verifier.verify(probe, 7);
            System.out.printf("    %-38s | existsInCurrentJvm=%-5s | %s%n",
                    probe.description(), result.existsInCurrentJvm(), result.classification());
        }
    }
}
