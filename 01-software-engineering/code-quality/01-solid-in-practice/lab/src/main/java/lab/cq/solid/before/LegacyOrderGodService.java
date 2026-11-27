package lab.cq.solid.before;

import java.util.ArrayList;
import java.util.List;

/**
 * Antipattern God Class violating SRP, OCP, and DIP:
 * mixes input validation, discount pricing policy, tax rule, persistence serialization,
 * concrete audit storage instantiation, and notification formatting in one class.
 */
public class LegacyOrderGodService {

    private final List<String> savedRows = new ArrayList<>();
    private final List<String> sentNotifications = new ArrayList<>();

    public double processOrder(String orderId, String customerTier, double subtotal, boolean express) {
        // 1. Validation responsibility (Actor: API / Ops)
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank");
        }
        if (subtotal < 0) {
            throw new IllegalArgumentException("subtotal must be >= 0");
        }

        // 2. Pricing tier policy hardcoded via if/else (Violates OCP & Finance SRP)
        double discount;
        if ("VIP".equals(customerTier)) {
            discount = subtotal * 0.15;
        } else if ("PARTNER".equals(customerTier)) {
            discount = subtotal * 0.10;
        } else if ("STANDARD".equals(customerTier)) {
            discount = 0.0;
        } else {
            throw new IllegalArgumentException("Unknown tier: " + customerTier);
        }

        // 3. Tax & shipping rule (Finance / Logistics)
        double afterDiscount = subtotal - discount;
        double tax = afterDiscount * 0.08;
        double shipping = express ? 15.0 : 5.0;
        double total = Math.round((afterDiscount + tax + shipping) * 100.0) / 100.0;

        // 4. Concrete storage serialization baked directly inside business logic (Violates DIP & DBA SRP)
        String csvRow = orderId + "," + customerTier + "," + total;
        savedRows.add(csvRow);

        // 5. Notification formatting baked inside (Violates Marketing/Comms SRP)
        sentNotifications.add("[EMAIL] Order " + orderId + " confirmed: total=" + total);

        return total;
    }

    public List<String> getSavedRows() {
        return List.copyOf(savedRows);
    }

    public List<String> getSentNotifications() {
        return List.copyOf(sentNotifications);
    }
}
