package lab.jvm.bridge;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Idiomatic Java 21 bridge implementation solving the exact same problem as OrderReportServiceJava7.
 * Uses java.time, lambda, Stream, Optional, var, text blocks, switch expressions, and java.net.http.HttpClient.
 */
public class OrderReportServiceJava21 {

    // DateTimeFormatter is immutable and thread-safe; safe to share as static final.
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    public OrderReport buildReport(
            List<Order> orders,
            String referenceDateIso,
            int lookbackDays,
            String taxRateEndpointUrl) throws IOException, InterruptedException {

        var refDate = LocalDate.parse(referenceDateIso, ISO_DATE);
        var cutoffDate = refDate.minusDays(lookbackDays);

        // 1. Filter eligible orders in date window
        var eligible = Optional.ofNullable(orders).orElseGet(List::of).stream()
                .filter(Objects::nonNull)
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED && o.getStatus() != OrderStatus.REJECTED)
                .filter(o -> {
                    var created = LocalDate.parse(o.getCreatedDateIso(), ISO_DATE);
                    return !created.isBefore(cutoffDate) && !created.isAfter(refDate);
                })
                .toList();

        // 2. Group and sum gross amount by customer tier
        Map<String, BigDecimal> grossByTier = eligible.stream()
                .collect(Collectors.groupingBy(
                        Order::getCustomerTier,
                        TreeMap::new,
                        Collectors.reducing(BigDecimal.ZERO, Order::getAmount, BigDecimal::add)));

        var totalGross = eligible.stream()
                .map(Order::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Sort descending by amount and take top 3 IDs
        var sorted = eligible.stream()
                .sorted(Comparator.comparing(Order::getAmount).reversed()
                        .thenComparing(Order::getId))
                .toList();

        var topIds = sorted.stream()
                .limit(3)
                .map(Order::getId)
                .toList();

        // 4. Optional return type chained cleanly without .get()
        var firstPromo = sorted.stream()
                .findFirst()
                .flatMap(this::findPromoNote)
                .map(note -> note.trim().toUpperCase())
                .orElse("NO_PROMO");

        // 5. Switch expression (no break, compiler verifies enum exhaustiveness)
        var leadStatus = sorted.stream()
                .findFirst()
                .map(Order::getStatus)
                .orElse(OrderStatus.PENDING);
        var slaPriority = mapSlaPriority(leadStatus);

        // 6. Fetch tax rate via modern HttpClient
        var taxRate = fetchTaxRate(taxRateEndpointUrl);
        var totalWithTax = totalGross
                .multiply(BigDecimal.ONE.add(taxRate))
                .setScale(2, RoundingMode.HALF_UP);

        // 7. Text block + formatted()
        var jsonPayload = """
                {
                  "referenceDate": "%s",
                  "eligibleCount": %d,
                  "totalWithTax": %s,
                  "leadPromo": "%s",
                  "slaPriority": "%s"
                }""".formatted(
                referenceDateIso,
                eligible.size(),
                totalWithTax.toPlainString(),
                firstPromo,
                slaPriority);

        return new OrderReport(grossByTier, totalWithTax, topIds, slaPriority, jsonPayload);
    }

    public Optional<String> findPromoNote(Order order) {
        return Optional.ofNullable(order)
                .map(Order::getPromoNote)
                .filter(note -> !note.isBlank());
    }

    public String mapSlaPriority(OrderStatus status) {
        return switch (status) {
            case PENDING -> "P1_URGENT";
            case APPROVED -> "P2_NORMAL";
            case PAID -> "P3_ARCHIVE";
            case REJECTED, CANCELLED -> "P4_CLOSED";
        };
    }

    public BigDecimal fetchTaxRate(String endpointUrl) throws IOException, InterruptedException {
        // Java 21: HttpClient implements AutoCloseable. In long-lived apps, reuse one HttpClient instance.
        try (var client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build()) {

            var request = HttpRequest.newBuilder()
                    .uri(URI.create(endpointUrl))
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IOException("Tax service returned HTTP " + response.statusCode() + ": " + response.body());
            }
            return new BigDecimal(response.body().trim());
        }
    }
}
