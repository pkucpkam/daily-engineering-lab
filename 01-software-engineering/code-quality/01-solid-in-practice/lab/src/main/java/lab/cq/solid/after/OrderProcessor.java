package lab.cq.solid.after;

import java.util.Map;

/**
 * Refactored OrderProcessor demonstrating balanced SOLID:
 * - SRP: delegates pricing, persistence, and notification to distinct actors
 * - OCP: new customer tiers plug into the policy map without modifying this class
 * - DIP: depends on abstractions owned by the domain (OrderRepository, OrderNotifier),
 *        while deterministic tax/shipping helper stays a plain concrete method (no over-abstraction)
 */
public class OrderProcessor {

    private static final double VAT_RATE = 0.08;

    private final Map<String, OrderModels.PricingPolicy> policies;
    private final OrderModels.OrderRepository repository;
    private final OrderModels.OrderNotifier notifier;

    public OrderProcessor(
            Map<String, OrderModels.PricingPolicy> policies,
            OrderModels.OrderRepository repository,
            OrderModels.OrderNotifier notifier) {
        this.policies = Map.copyOf(policies);
        this.repository = repository;
        this.notifier = notifier;
    }

    public static Map<String, OrderModels.PricingPolicy> defaultPolicies() {
        return Map.of(
                "STANDARD", subtotal -> 0.0,
                "VIP", subtotal -> subtotal * 0.15,
                "PARTNER", subtotal -> subtotal * 0.10
        );
    }

    public OrderModels.OrderReceipt process(OrderModels.OrderRequest request) {
        OrderModels.PricingPolicy policy = policies.get(request.customerTier());
        if (policy == null) {
            throw new IllegalArgumentException("Unknown tier: " + request.customerTier());
        }

        double discount = policy.calculateDiscount(request.subtotal());
        if (discount < 0 || discount > request.subtotal()) {
            throw new IllegalStateException("LSP contract violated: invalid discount " + discount);
        }

        double afterDiscount = request.subtotal() - discount;
        double tax = afterDiscount * VAT_RATE;
        double shipping = request.express() ? 15.0 : 5.0;
        double total = Math.round((afterDiscount + tax + shipping) * 100.0) / 100.0;

        OrderModels.OrderReceipt receipt = new OrderModels.OrderReceipt(
                request.orderId(), request.customerTier(), discount, tax, shipping, total);
        repository.save(receipt);
        notifier.notifyConfirmed(receipt);
        return receipt;
    }
}
