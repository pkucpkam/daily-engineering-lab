package lab.ai.playbook;

import lab.ai.playbook.buggy.InvoiceJdbcHelperBuggy;
import lab.ai.playbook.buggy.OrderRateLimiterBuggy;
import lab.ai.playbook.buggy.PaymentClientBuggy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiBugReproductionTest {

    @Test
    void rateLimiterBuggyAllowsMoreThanMaxUnderConcurrency() throws Exception {
        final int maxRequests = 5;
        final int threads = 24;
        int worstAllowed = 0;

        for (int round = 0; round < 15; round++) {
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
                        if (limiter.allowRequest("vip-user")) {
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
            worstAllowed = Math.max(worstAllowed, allowed.get());
        }

        // Chứng minh race condition check-then-act vượt quá giới hạn maxRequests=5
        assertThat(worstAllowed).isGreaterThan(maxRequests);
    }

    @Test
    void invoiceHelperBuggyLeaksResourcesAndThrowsOnNonTerminatingDivision() {
        InvoiceJdbcHelperBuggy helper = new InvoiceJdbcHelperBuggy();
        AtomicBoolean statementClosed = new AtomicBoolean(false);
        AtomicBoolean connectionClosed = new AtomicBoolean(false);

        InvoiceJdbcHelperBuggy.FakeStatement fakeStatement = new InvoiceJdbcHelperBuggy.FakeStatement() {
            @Override
            public List<BigDecimal> executeQuery(String sql) {
                return InvoiceJdbcHelperBuggy.sampleAmounts();
            }

            @Override
            public boolean isClosed() {
                return statementClosed.get();
            }

            @Override
            public void close() {
                statementClosed.set(true);
            }
        };

        InvoiceJdbcHelperBuggy.FakeConnection fakeConnection = new InvoiceJdbcHelperBuggy.FakeConnection() {
            @Override
            public InvoiceJdbcHelperBuggy.FakeStatement createStatement() {
                return fakeStatement;
            }

            @Override
            public boolean isClosed() {
                return connectionClosed.get();
            }

            @Override
            public void close() {
                connectionClosed.set(true);
            }
        };

        assertThatThrownBy(() -> helper.fetchAverageAmount(fakeConnection, "CUST-1"))
                .isInstanceOf(ArithmeticException.class);

        // Vì không dùng try-with-resources, cả statement lẫn connection đều bị leak sau exception!
        assertThat(statementClosed.get()).isFalse();
        assertThat(connectionClosed.get()).isFalse();
    }

    @Test
    void paymentClientBuggyRetries4xxAndSwallowsInterrupt() throws Exception {
        PaymentClientBuggy client = new PaymentClientBuggy();
        AtomicInteger callCount = new AtomicInteger();

        int status = client.sendWithRetry(d -> {
            callCount.incrementAndGet();
            return 400;
        }, "2026-11-27", 3);

        // Lỗi 400 không thể tự khỏi nhưng vẫn bị retry 3 lần
        assertThat(status).isEqualTo(400);
        assertThat(callCount.get()).isEqualTo(3);

        // Kiểm chứng nuốt InterruptedException: interrupt flag của thread bị mất (false)
        int interruptResult = client.sendWithRetry(d -> {
            throw new InterruptedException("shutdown");
        }, "2026-11-27", 3);
        assertThat(interruptResult).isEqualTo(-1);
        assertThat(Thread.currentThread().isInterrupted()).isFalse();
    }

    @Test
    void apiVerifierDistinguishesVersionDriftFromHallucination() {
        ApiVerifier verifier = new ApiVerifier();
        List<ApiVerifier.VerificationResult> results = ApiVerifier.defaultProbes().stream()
                .map(p -> verifier.verify(p, 7))
                .toList();

        assertThat(results)
                .extracting(r -> r.probe().methodName(), ApiVerifier.VerificationResult::classification)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("trim", ApiVerifier.Classification.AVAILABLE_SINCE_JAVA_7),
                        org.assertj.core.groups.Tuple.tuple("isBlank", ApiVerifier.Classification.VERSION_DRIFT_NEWER_JDK),
                        org.assertj.core.groups.Tuple.tuple("of", ApiVerifier.Classification.VERSION_DRIFT_NEWER_JDK),
                        org.assertj.core.groups.Tuple.tuple("orElseThrow", ApiVerifier.Classification.VERSION_DRIFT_NEWER_JDK),
                        org.assertj.core.groups.Tuple.tuple("retryPolicy", ApiVerifier.Classification.HALLUCINATED_NON_EXISTENT),
                        org.assertj.core.groups.Tuple.tuple("ofVirtual", ApiVerifier.Classification.VERSION_DRIFT_NEWER_JDK)
                );
    }
}
