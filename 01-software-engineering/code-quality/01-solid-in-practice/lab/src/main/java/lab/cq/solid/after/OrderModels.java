package lab.cq.solid.after;

/**
 * Pure value records and focused small ports (no pointless interface over simple tax math).
 */
public final class OrderModels {

    private OrderModels() {
    }

    public record OrderRequest(String orderId, String customerTier, double subtotal, boolean express) {
        public OrderRequest {
            if (orderId == null || orderId.isBlank()) {
                throw new IllegalArgumentException("orderId must not be blank");
            }
            if (subtotal < 0) {
                throw new IllegalArgumentException("subtotal must be >= 0");
            }
        }
    }

    public record OrderReceipt(String orderId, String customerTier, double discount, double tax, double shipping, double total) {
    }

    /**
     * Focused Pricing contract (OCP + LSP): discount must never be negative or exceed subtotal.
     */
    @FunctionalInterface
    public interface PricingPolicy {
        double calculateDiscount(double subtotal);
    }

    /**
     * Focused persistence port owned by the order application layer (DIP + ISP).
     */
    public interface OrderRepository {
        void save(OrderReceipt receipt);
    }

    /**
     * Segregated notification port (ISP): keeps callers decoupled from DB/billing concerns.
     */
    @FunctionalInterface
    public interface OrderNotifier {
        void notifyConfirmed(OrderReceipt receipt);
    }
}
