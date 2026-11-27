package lab.cq.solid;

import lab.cq.solid.after.OrderModels;
import lab.cq.solid.after.OrderProcessor;
import lab.cq.solid.before.LegacyOrderGodService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SolidRefactorTest {

    @Test
    void refactoredOrderProcessorMatchesLegacyBehaviorExactly() {
        LegacyOrderGodService legacy = new LegacyOrderGodService();
        List<OrderModels.OrderReceipt> saved = new ArrayList<>();
        List<String> alerts = new ArrayList<>();
        OrderProcessor processor = new OrderProcessor(
                OrderProcessor.defaultPolicies(),
                saved::add,
                r -> alerts.add(r.orderId() + ":" + r.total())
        );

        String[] tiers = {"STANDARD", "VIP", "PARTNER"};
        for (int i = 0; i < tiers.length; i++) {
            double legacyTotal = legacy.processOrder("ORD-" + i, tiers[i], 200.0, i % 2 == 0);
            var receipt = processor.process(new OrderModels.OrderRequest("ORD-" + i, tiers[i], 200.0, i % 2 == 0));
            assertThat(receipt.total()).isEqualTo(legacyTotal);
        }

        assertThat(saved).hasSize(3);
        assertThat(alerts).containsExactly("ORD-0:231.0", "ORD-1:188.6", "ORD-2:209.4");
    }

    @Test
    void ocpExtensionAddsNewTierWithoutTouchingProcessorCode() {
        Map<String, OrderModels.PricingPolicy> extended = new HashMap<>(OrderProcessor.defaultPolicies());
        extended.put("EMPLOYEE", subtotal -> subtotal * 0.30);

        OrderProcessor processor = new OrderProcessor(extended, r -> {}, r -> {});
        var receipt = processor.process(new OrderModels.OrderRequest("EMP-1", "EMPLOYEE", 100.0, false));

        assertThat(receipt.discount()).isEqualTo(30.0);
        assertThat(receipt.total()).isEqualTo(80.6); // (70 * 1.08) + 5
    }

    @Test
    void lspViolationThrowsWhenSubtypeBreaksDiscountPostcondition() {
        Map<String, OrderModels.PricingPolicy> broken = Map.of(
                "CORRUPT", subtotal -> subtotal + 50.0 // violates 0 <= discount <= subtotal
        );
        OrderProcessor processor = new OrderProcessor(broken, r -> {}, r -> {});

        assertThatThrownBy(() -> processor.process(new OrderModels.OrderRequest("X-1", "CORRUPT", 100.0, false)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("LSP contract violated");
    }
}
