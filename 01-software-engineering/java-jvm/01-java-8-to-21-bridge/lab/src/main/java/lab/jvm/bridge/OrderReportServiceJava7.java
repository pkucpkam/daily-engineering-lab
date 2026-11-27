package lab.jvm.bridge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Legacy Java 7 style implementation for side-by-side comparison.
 * Avoids lambdas, Stream, Optional, java.time, var, text blocks, switch expressions, and java.net.http.HttpClient.
 */
public class OrderReportServiceJava7 {

    // SimpleDateFormat is NOT thread-safe; wrapping in ThreadLocal is the classic Java 7 workaround.
    private static final ThreadLocal<SimpleDateFormat> ISO_DATE_FORMAT =
            new ThreadLocal<SimpleDateFormat>() {
                @Override
                protected SimpleDateFormat initialValue() {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    sdf.setLenient(false);
                    return sdf;
                }
            };

    public OrderReport buildReport(
            List<Order> orders,
            String referenceDateIso,
            int lookbackDays,
            String taxRateEndpointUrl) throws IOException {

        Date refDate = parseDate(referenceDateIso);
        Calendar cal = Calendar.getInstance();
        cal.setTime(refDate);
        cal.add(Calendar.DAY_OF_MONTH, -lookbackDays);
        Date cutoffDate = cal.getTime();

        // 1. Filter eligible orders in date window
        List<Order> eligible = new ArrayList<Order>();
        if (orders != null) {
            for (Order order : orders) {
                if (order == null) {
                    continue;
                }
                if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.REJECTED) {
                    continue;
                }
                Date created = parseDate(order.getCreatedDateIso());
                if (!created.before(cutoffDate) && !created.after(refDate)) {
                    eligible.add(order);
                }
            }
        }

        // 2. Group and sum gross amount by customer tier (sorted keys via TreeMap)
        Map<String, BigDecimal> grossByTier = new TreeMap<String, BigDecimal>();
        BigDecimal totalGross = BigDecimal.ZERO;
        for (Order order : eligible) {
            String tier = order.getCustomerTier();
            BigDecimal current = grossByTier.get(tier);
            if (current == null) {
                current = BigDecimal.ZERO;
            }
            grossByTier.put(tier, current.add(order.getAmount()));
            totalGross = totalGross.add(order.getAmount());
        }

        // 3. Sort descending by amount using anonymous Comparator to pick top 3 IDs
        List<Order> sorted = new ArrayList<Order>(eligible);
        Collections.sort(sorted, new Comparator<Order>() {
            @Override
            public int compare(Order a, Order b) {
                int byAmount = b.getAmount().compareTo(a.getAmount());
                if (byAmount != 0) {
                    return byAmount;
                }
                return a.getId().compareTo(b.getId());
            }
        });

        List<String> topIds = new ArrayList<String>();
        int limit = Math.min(3, sorted.size());
        for (int i = 0; i < limit; i++) {
            topIds.add(sorted.get(i).getId());
        }

        // 4. Nullable lookup with explicit null check
        String firstPromo = "NO_PROMO";
        if (!sorted.isEmpty()) {
            String note = findPromoNote(sorted.get(0));
            if (note != null) {
                firstPromo = note.trim().toUpperCase();
            }
        }

        // 5. Classic switch statement with break
        OrderStatus leadStatus = sorted.isEmpty() ? OrderStatus.PENDING : sorted.get(0).getStatus();
        String slaPriority = mapSlaPriority(leadStatus);

        // 6. Fetch tax rate via HttpURLConnection
        BigDecimal taxRate = fetchTaxRate(taxRateEndpointUrl);
        BigDecimal totalWithTax = totalGross
                .multiply(BigDecimal.ONE.add(taxRate))
                .setScale(2, RoundingMode.HALF_UP);

        // 7. Manual string concatenation for JSON payload
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"referenceDate\": \"").append(referenceDateIso).append("\",\n");
        sb.append("  \"eligibleCount\": ").append(eligible.size()).append(",\n");
        sb.append("  \"totalWithTax\": ").append(totalWithTax.toPlainString()).append(",\n");
        sb.append("  \"leadPromo\": \"").append(firstPromo).append("\",\n");
        sb.append("  \"slaPriority\": \"").append(slaPriority).append("\"\n");
        sb.append("}");

        return new OrderReport(grossByTier, totalWithTax, topIds, slaPriority, sb.toString());
    }

    private Date parseDate(String isoDate) {
        try {
            return ISO_DATE_FORMAT.get().parse(isoDate);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid ISO date: " + isoDate, e);
        }
    }

    private String findPromoNote(Order order) {
        if (order == null || order.getPromoNote() == null || order.getPromoNote().trim().isEmpty()) {
            return null;
        }
        return order.getPromoNote();
    }

    public String mapSlaPriority(OrderStatus status) {
        String priority;
        switch (status) {
            case PENDING:
                priority = "P1_URGENT";
                break;
            case APPROVED:
                priority = "P2_NORMAL";
                break;
            case PAID:
                priority = "P3_ARCHIVE";
                break;
            case REJECTED:
            case CANCELLED:
                priority = "P4_CLOSED";
                break;
            default:
                throw new IllegalArgumentException("Unknown status: " + status);
        }
        return priority;
    }

    public BigDecimal fetchTaxRate(String endpointUrl) throws IOException {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(endpointUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);

            int statusCode = conn.getResponseCode();
            InputStream stream = statusCode >= 400 ? conn.getErrorStream() : conn.getInputStream();
            String body = readAll(stream);
            if (statusCode != 200) {
                throw new IOException("Tax service returned HTTP " + statusCode + ": " + body);
            }
            return new BigDecimal(body.trim());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String readAll(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }
}
