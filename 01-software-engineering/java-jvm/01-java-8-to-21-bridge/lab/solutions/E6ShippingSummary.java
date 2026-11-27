package lab.jvm.bridge.solutions;

import lab.jvm.bridge.Order;
import lab.jvm.bridge.OrderStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Reference solution for Exercise E6 (shipping summary in Java 21).
 */
public class E6ShippingSummary {

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    public ShippingResult summarize(List<Order> orders, String refDateIso) {
        var refDate = LocalDate.parse(refDateIso, ISO_DATE);
        var cutoff = refDate.minusDays(7);

        var eligible = Optional.ofNullable(orders).orElseGet(List::of).stream()
                .filter(Objects::nonNull)
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .filter(o -> {
                    var created = LocalDate.parse(o.getCreatedDateIso(), ISO_DATE);
                    return !created.isBefore(cutoff) && !created.isAfter(refDate);
                })
                .toList();

        Map<OrderStatus, Long> countByStatus = eligible.stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));

        // Max by amount; on tie, pick lexicographically smallest id
        Optional<Order> topOrder = eligible.stream()
                .min(Comparator.comparing(Order::getAmount).reversed().thenComparing(Order::getId));

        BigDecimal estimatedShipping = eligible.stream()
                .map(o -> o.getAmount().multiply(shippingRateForTier(o.getCustomerTier())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        String topOrderId = topOrder.map(Order::getId).orElse("NONE");
        String summaryJson = """
                {
                  "refDate": "%s",
                  "eligibleOrders": %d,
                  "topOrderId": "%s",
                  "estimatedShipping": %s
                }""".formatted(refDateIso, eligible.size(), topOrderId, estimatedShipping.toPlainString());

        return new ShippingResult(countByStatus, topOrder, estimatedShipping, summaryJson);
    }

    public BigDecimal shippingRateForTier(String tier) {
        var normalized = Optional.ofNullable(tier).map(String::trim).map(String::toUpperCase).orElse("");
        return switch (normalized) {
            case "GOLD" -> BigDecimal.ZERO;
            case "SILVER" -> new BigDecimal("0.05");
            default -> new BigDecimal("0.10");
        };
    }

    public record ShippingResult(
            Map<OrderStatus, Long> countByStatus,
            Optional<Order> topOrder,
            BigDecimal estimatedShipping,
            String summaryJson) {
    }
}
