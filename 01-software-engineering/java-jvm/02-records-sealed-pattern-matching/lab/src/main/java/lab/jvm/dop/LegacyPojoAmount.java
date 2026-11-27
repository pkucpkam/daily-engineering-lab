package lab.jvm.dop;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Classic POJO used solely to demonstrate how standard Java deserialization
 * bypasses constructor invariants (unlike Java records).
 */
public final class LegacyPojoAmount implements Serializable {
    private static final long serialVersionUID = 1L;

    private final BigDecimal amount;

    public LegacyPojoAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LegacyPojoAmount that)) return false;
        return Objects.equals(amount, that.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }
}
