import lab.cq.solid.after.OrderModels;
import lab.cq.solid.after.OrderProcessor;

import java.util.HashMap;
import java.util.Map;

/**
 * L3 Solution example: adds a flat capped VIP-Plus policy satisfying LSP invariant
 * (discount stays bounded within [0, subtotal]) and plugs into OrderProcessor via OCP.
 */
public class CappedTierSolution {

    public static OrderModels.PricingPolicy cappedTierPolicy(double rate, double maxDiscount) {
        return subtotal -> Math.min(maxDiscount, Math.max(0.0, subtotal * rate));
    }

    public static void main(String[] args) {
        Map<String, OrderModels.PricingPolicy> policies = new HashMap<>(OrderProcessor.defaultPolicies());
        policies.put("ENTERPRISE", cappedTierPolicy(0.25, 100.0));

        OrderProcessor processor = new OrderProcessor(
                policies,
                receipt -> System.out.println("Saved " + receipt.orderId() + " total=" + receipt.total()),
                receipt -> System.out.println("Notified tier=" + receipt.customerTier())
        );

        processor.process(new OrderModels.OrderRequest("ENT-1", "ENTERPRISE", 1_000.0, false));
    }
}
