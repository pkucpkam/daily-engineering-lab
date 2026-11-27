package lab.jvm.bridge;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BridgeComparisonTest {

    private HttpServer server;
    private String taxEndpoint;
    private String failingEndpoint;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/tax-rate", exchange -> {
            byte[] body = "0.08".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.createContext("/error", exchange -> {
            byte[] body = "upstream error".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(503, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        int port = server.getAddress().getPort();
        taxEndpoint = "http://127.0.0.1:" + port + "/tax-rate";
        failingEndpoint = "http://127.0.0.1:" + port + "/error";
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void java7AndJava21ProduceIdenticalOrderReports() throws Exception {
        List<Order> orders = List.of(
                new Order("ORD-1", "GOLD", OrderStatus.APPROVED, new BigDecimal("200.00"), "2026-11-26", "  vip "),
                new Order("ORD-2", "SILVER", OrderStatus.PENDING, new BigDecimal("50.00"), "2026-11-27", null),
                new Order("ORD-3", "GOLD", OrderStatus.PAID, new BigDecimal("100.00"), "2026-11-25", ""),
                new Order("ORD-4", "GOLD", OrderStatus.CANCELLED, new BigDecimal("500.00"), "2026-11-27", "skip"),
                new Order("ORD-5", "BRONZE", OrderStatus.PAID, new BigDecimal("300.00"), "2026-11-10", "too_old")
        );

        OrderReport report7 = new OrderReportServiceJava7().buildReport(orders, "2026-11-27", 3, taxEndpoint);
        OrderReport report21 = new OrderReportServiceJava21().buildReport(orders, "2026-11-27", 3, taxEndpoint);

        assertThat(report21).isEqualTo(report7);
        assertThat(report21.getGrossByTier())
                .containsEntry("GOLD", new BigDecimal("300.00"))
                .containsEntry("SILVER", new BigDecimal("50.00"))
                .doesNotContainKey("BRONZE");
        assertThat(report21.getTotalWithTax()).isEqualByComparingTo("378.00");
        assertThat(report21.getTopOrderIds()).containsExactly("ORD-1", "ORD-3", "ORD-2");
        assertThat(report21.getHighestSlaPriority()).isEqualTo("P2_NORMAL");
    }

    @Test
    void httpClientsPropagateNon200ErrorsCleanly() {
        var service7 = new OrderReportServiceJava7();
        var service21 = new OrderReportServiceJava21();

        assertThatThrownBy(() -> service7.fetchTaxRate(failingEndpoint))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("503");

        assertThatThrownBy(() -> service21.fetchTaxRate(failingEndpoint))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("503");
    }

    @Test
    void optionalOrElseEvaluatesEagerlyWhileOrElseGetIsLazy() {
        var eagerCount = new AtomicInteger();
        var lazyCount = new AtomicInteger();
        Optional<String> present = Optional.of("HIT");

        String r1 = present.orElse(countAndReturn(eagerCount));
        String r2 = present.orElseGet(() -> countAndReturn(lazyCount));

        assertThat(r1).isEqualTo("HIT");
        assertThat(r2).isEqualTo("HIT");
        assertThat(eagerCount.get()).isEqualTo(1);
        assertThat(lazyCount.get()).isZero();
    }

    @Test
    void dateTimeFormatterIsThreadSafeAcrossConcurrentThreads() throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        try (var pool = Executors.newFixedThreadPool(8)) {
            List<Callable<String>> jobs = java.util.stream.IntStream.range(0, 200)
                    .mapToObj(i -> (Callable<String>) () ->
                            formatter.format(LocalDate.parse("2026-11-27", formatter)))
                    .toList();

            for (Future<String> future : pool.invokeAll(jobs)) {
                assertThat(future.get()).isEqualTo("2026-11-27");
            }
        }
    }

    @Test
    void zonedDateConversionPreservesBusinessDayAcrossTimezones() {
        // 2026-11-27 01:00 in +07:00 is still 2026-11-26 18:00 in UTC
        ZonedDateTime hcmTime = ZonedDateTime.of(2026, 11, 27, 1, 0, 0, 0, ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDate utcDate = hcmTime.toInstant().atZone(ZoneId.of("UTC")).toLocalDate();
        LocalDate hcmDate = hcmTime.toLocalDate();

        assertThat(hcmDate).isEqualTo(LocalDate.of(2026, 11, 27));
        assertThat(utcDate).isEqualTo(LocalDate.of(2026, 11, 26));
    }

    private String countAndReturn(AtomicInteger counter) {
        counter.incrementAndGet();
        return "DEFAULT";
    }
}
