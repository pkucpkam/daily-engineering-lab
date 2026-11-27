package lab.jvm.bridge;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class OrderReport {
    private final Map<String, BigDecimal> grossByTier;
    private final BigDecimal totalWithTax;
    private final List<String> topOrderIds;
    private final String highestSlaPriority;
    private final String jsonPayload;

    public OrderReport(
            Map<String, BigDecimal> grossByTier,
            BigDecimal totalWithTax,
            List<String> topOrderIds,
            String highestSlaPriority,
            String jsonPayload) {
        this.grossByTier = Collections.unmodifiableMap(new LinkedHashMap<>(grossByTier));
        this.totalWithTax = totalWithTax;
        this.topOrderIds = List.copyOf(topOrderIds);
        this.highestSlaPriority = highestSlaPriority;
        this.jsonPayload = jsonPayload;
    }

    public Map<String, BigDecimal> getGrossByTier() {
        return grossByTier;
    }

    public BigDecimal getTotalWithTax() {
        return totalWithTax;
    }

    public List<String> getTopOrderIds() {
        return topOrderIds;
    }

    public String getHighestSlaPriority() {
        return highestSlaPriority;
    }

    public String getJsonPayload() {
        return jsonPayload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderReport that)) return false;
        return Objects.equals(grossByTier, that.grossByTier)
                && totalWithTax.compareTo(that.totalWithTax) == 0
                && Objects.equals(topOrderIds, that.topOrderIds)
                && Objects.equals(highestSlaPriority, that.highestSlaPriority)
                && Objects.equals(jsonPayload, that.jsonPayload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(grossByTier, totalWithTax.stripTrailingZeros(), topOrderIds, highestSlaPriority, jsonPayload);
    }
}
