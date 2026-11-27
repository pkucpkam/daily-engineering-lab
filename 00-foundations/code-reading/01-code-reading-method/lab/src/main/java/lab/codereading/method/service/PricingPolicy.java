package lab.codereading.method.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PricingPolicy {

    /**
     * Applies tier discount first, then flat coupon, floored at 0.
     * Note for code-reading exercise: check whether rounding happens before or after coupon!
     */
    public BigDecimal calculateTotal(String tier, BigDecimal unitPrice, int quantity, String couponCode) {
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal discountRate = switch (tier == null ? "" : tier.toUpperCase()) {
            case "VIP" -> new BigDecimal("0.10");
            case "STAFF" -> new BigDecimal("0.20");
            default -> BigDecimal.ZERO;
        };
        BigDecimal afterTier = subtotal.multiply(BigDecimal.ONE.subtract(discountRate));
        BigDecimal couponDiscount = "SAVE50".equalsIgnoreCase(couponCode)
                ? new BigDecimal("50")
                : BigDecimal.ZERO;
        BigDecimal net = afterTier.subtract(couponDiscount).max(BigDecimal.ZERO);
        return net.setScale(2, RoundingMode.HALF_UP);
    }
}
