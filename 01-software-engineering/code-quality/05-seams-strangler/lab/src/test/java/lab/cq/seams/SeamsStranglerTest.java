package lab.cq.seams;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SeamsStranglerTest {

    @Test
    void sproutClassRulesAreIndependentlyTestableWithoutLegacyWiring() {
        var policy = new TaxExemptionSproutPolicy();

        assertThat(policy.evaluate("HN", 1_100_000L, true).exemptAmountVnd()).isEqualTo(110_000L);
        assertThat(policy.evaluate("REMOTE_ZONE", 1_100_000L, false).exemptAmountVnd()).isEqualTo(50_000L);
        assertThat(policy.evaluate("REMOTE_ZONE", 500_000L, false).exemptAmountVnd()).isZero();
    }

    @Test
    void wrapMethodAndObjectSeamRecordAuditWithoutRewritingCoreLogic() {
        List<String> captured = new ArrayList<>();
        var processor = new LegacyInvoiceProcessor(captured::add);

        long net = processor.calculateAndAudit("INV-99", "REMOTE_ZONE", 1_000_000L, false);

        // 1,000,000 + 10% VAT = 1,100,000 - 50,000 remote zone rebate = 1,050,000
        assertThat(net).isEqualTo(1_050_000L);
        assertThat(captured).containsExactly("INVOICE:INV-99:NET=1050000");
    }

    @Test
    void stranglerRouterMigratesCohortsIncrementally() {
        var legacy = new LegacyInvoiceProcessor();
        var router = new LegacyInvoiceProcessor.InvoiceStranglerRouter(
                legacy,
                (tenant, region, sub, np) -> legacy.calculateNetPayable(region, sub, np) - 10_000L // new loyalty discount
        );

        long tOld = router.route("CORP-OLD", false, "HN", 1_000_000L, false);
        long tNew = router.route("CORP-PILOT", true, "HN", 1_000_000L, false);

        assertThat(tOld).isEqualTo(1_100_000L);
        assertThat(tNew).isEqualTo(1_090_000L);
        assertThat(router.getRoutedPathLog()).containsExactly("CORP-OLD->LEGACY", "CORP-PILOT->MODERN");
    }
}
