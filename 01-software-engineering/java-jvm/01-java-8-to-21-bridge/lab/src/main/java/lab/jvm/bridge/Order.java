package lab.jvm.bridge;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Plain immutable domain class compatible with both Java 7 and Java 21 implementations.
 * (Records are introduced separately in JVM-02.)
 */
public final class Order {
    private final String id;
    private final String customerTier;
    private final OrderStatus status;
    private final BigDecimal amount;
    private final String createdDateIso; // yyyy-MM-dd
    private final String promoNote;      // nullable

    public Order(
            String id,
            String customerTier,
            OrderStatus status,
            BigDecimal amount,
            String createdDateIso,
            String promoNote) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.customerTier = Objects.requireNonNull(customerTier, "customerTier must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.amount = Objects.requireNonNull(amount, "amount must not be null");
        this.createdDateIso = Objects.requireNonNull(createdDateIso, "createdDateIso must not be null");
        this.promoNote = promoNote;
    }

    public String getId() {
        return id;
    }

    public String getCustomerTier() {
        return customerTier;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCreatedDateIso() {
        return createdDateIso;
    }

    public String getPromoNote() {
        return promoNote;
    }
}
