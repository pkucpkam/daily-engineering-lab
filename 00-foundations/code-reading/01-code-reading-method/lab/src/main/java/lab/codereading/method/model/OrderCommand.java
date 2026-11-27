package lab.codereading.method.model;

import java.math.BigDecimal;
import java.util.List;

public record OrderCommand(
        String customerId,
        String customerTier,
        String sku,
        int quantity,
        BigDecimal unitPrice,
        String couponCode) {
}
