package lab.cq.testing;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Demonstrates JUnit 5 lifecycle, @Nested grouping, @ParameterizedTest (@CsvSource),
 * custom Extension (ExecutionTimerExtension), AssertJ, and Mockito (@Mock/@Spy/@Captor).
 */
@ExtendWith({MockitoExtension.class, ShippingFeeServiceTest.ExecutionTimerExtension.class})
class ShippingFeeServiceTest {

    static final class ExecutionTimerExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {
        private static final ExtensionContext.Namespace NS = ExtensionContext.Namespace.create(ExecutionTimerExtension.class);

        @Override
        public void beforeTestExecution(ExtensionContext ctx) {
            ctx.getStore(NS).put("startNs", System.nanoTime());
        }

        @Override
        public void afterTestExecution(ExtensionContext ctx) {
            long elapsedMs = (System.nanoTime() - ctx.getStore(NS).get("startNs", long.class)) / 1_000_000L;
            if (elapsedMs > 250) {
                System.out.println("[SLOW TEST] " + ctx.getDisplayName() + " took " + elapsedMs + " ms");
            }
        }
    }

    @Mock
    private ShippingFeeService.ZoneRateCatalog rateCatalog;

    @Mock
    private ShippingFeeService.AuditTracker auditTracker;

    @Spy
    private ShippingFeeService.SurchargeCalculator surchargeSpy = new ShippingFeeService.SurchargeCalculator();

    @Captor
    private ArgumentCaptor<ShippingFeeService.QuoteAuditEvent> eventCaptor;

    private ShippingFeeService service;

    @BeforeAll
    static void initSuite() {
        // Runs once per test class
    }

    @BeforeEach
    void setUp() {
        lenient().when(rateCatalog.baseRateFor(ShippingFeeService.Tier.DOMESTIC)).thenReturn(25_000L);
        lenient().when(rateCatalog.baseRateFor(ShippingFeeService.Tier.EXPRESS)).thenReturn(55_000L);
        lenient().when(rateCatalog.baseRateFor(ShippingFeeService.Tier.INTERNATIONAL)).thenReturn(150_000L);
        service = new ShippingFeeService(rateCatalog, surchargeSpy, auditTracker);
    }

    @Nested
    @DisplayName("Shipping pricing matrix (Parameterized)")
    class PricingMatrixTests {

        @ParameterizedTest(name = "tier={0}, total={1}, weight={2}kg, vip={3} => fee={4}")
        @CsvSource({
                "DOMESTIC,      200000, 1.5, false, 25000",
                "DOMESTIC,      500000, 4.0, true,  0",
                "DOMESTIC,      499999, 4.0, true,  35000",
                "EXPRESS,       600000, 2.0, true,  55000",
                "EXPRESS,       300000, 5.0, false, 70000",
                "INTERNATIONAL, 800000, 3.0, true,  155000"
        })
        void computesExpectedFeeAcrossTiersAndThresholds(
                ShippingFeeService.Tier tier,
                long orderTotal,
                double weightKg,
                boolean vip,
                long expectedFee) {
            long actualFee = service.quoteFee(
                    new ShippingFeeService.QuoteRequest("ORD-PARAM", tier, orderTotal, weightKg, vip));

            assertThat(actualFee).isEqualTo(expectedFee);
        }
    }

    @Nested
    @DisplayName("Interaction & Captor contracts")
    class AuditAndValidationTests {

        @Test
        @DisplayName("VIP domestic >= 500k skips surcharge computation and emits waived audit event")
        void vipFreeDomesticWaivesFeeAndRecordsWaivedEvent() {
            long fee = service.quoteFee(new ShippingFeeService.QuoteRequest(
                    "ORD-VIP-1", ShippingFeeService.Tier.DOMESTIC, 600_000L, 10.0, true));

            assertThat(fee).isZero();
            verify(surchargeSpy, never()).weightSurcharge(10.0);
            verify(auditTracker).record(eventCaptor.capture());

            ShippingFeeService.QuoteAuditEvent event = eventCaptor.getValue();
            assertThat(event.orderId()).isEqualTo("ORD-VIP-1");
            assertThat(event.shippingFeeVnd()).isZero();
            assertThat(event.freeWaiverApplied()).isTrue();
        }

        @Test
        @DisplayName("Spy delegates real logic while allowing verification or selective stubbing")
        void spyDelegatesRealWeightSurchargeForHeavyParcels() {
            when(rateCatalog.baseRateFor(ShippingFeeService.Tier.EXPRESS)).thenReturn(60_000L);

            long fee = service.quoteFee(new ShippingFeeService.QuoteRequest(
                    "ORD-HVY", ShippingFeeService.Tier.EXPRESS, 100_000L, 6.0, false));

            assertThat(fee).isEqualTo(80_000L); // 60,000 + (6 - 2) * 5,000
            verify(surchargeSpy).weightSurcharge(6.0);
        }

        @Test
        @DisplayName("Rejects negative order total without calling audit tracker")
        void throwsWhenOrderTotalIsNegative() {
            assertThatThrownBy(() -> service.quoteFee(
                    new ShippingFeeService.QuoteRequest("BAD", ShippingFeeService.Tier.DOMESTIC, -1L, 1.0, false)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid order total");

            verify(auditTracker, never()).record(org.mockito.ArgumentMatchers.any());
        }
    }
}
