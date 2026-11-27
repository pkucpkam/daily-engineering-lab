package lab.dsa.hashmap.solutions;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Lời giải tham khảo (DSA-02):
 * 1) Key bất biến chuẩn (Java 21 record & Java 7 final class pattern).
 * 2) Cache dùng ConcurrentHashMap thay cho HashMap tĩnh để tránh Java 7 resize infinite loop & mất dữ liệu.
 */
public final class ImmutableMapKeySolution {

    // Java 16+ / 21: immutable record tự sinh equals/hashCode chuẩn
    public record FeeKey(String productCode, int tier, String currency) {}

    // Java 7 equivalent: final class, mọi field private final, không setter
    public static final class LegacyFeeKey {
        private final String productCode;
        private final int tier;
        private final String currency;

        public LegacyFeeKey(String productCode, int tier, String currency) {
            this.productCode = productCode;
            this.tier = tier;
            this.currency = currency;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LegacyFeeKey that)) return false;
            return tier == that.tier
                    && Objects.equals(productCode, that.productCode)
                    && Objects.equals(currency, that.currency);
        }

        @Override
        public int hashCode() {
            return Objects.hash(productCode, tier, currency);
        }
    }

    private final ConcurrentMap<FeeKey, BigDecimal> feeCache = new ConcurrentHashMap<>(1024);

    public BigDecimal getOrLoad(FeeKey key, BigDecimal fallbackFee) {
        return feeCache.computeIfAbsent(key, k -> fallbackFee);
    }
}
