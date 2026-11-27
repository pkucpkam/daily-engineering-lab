package lab.jvm.bridge.solutions;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Reference solution for Exercise E9 (fixed SettlementJob):
 *  1) Replaces shared mutable SimpleDateFormat with immutable DateTimeFormatter + ZoneId.
 *  2) Moves blocking HTTP webhook calls off ForkJoinPool.commonPool() onto a dedicated bounded executor
 *     (or Virtual Threads in Java 21).
 *  3) Reuses a single HttpClient with explicit connect + request timeouts.
 */
public class E9FixedSettlementJob implements AutoCloseable {

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final HttpClient httpClient;
    private final ExecutorService webhookExecutor;

    public E9FixedSettlementJob() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        this.webhookExecutor = Executors.newVirtualThreadPerTaskExecutor();
    }

    public List<String> settleBatch(List<String> txIds, Instant eventTime, String webhookUrl) {
        LocalDate businessDate = eventTime.atZone(BUSINESS_ZONE).toLocalDate();
        String formattedDate = ISO_DATE.format(businessDate);

        List<CompletableFuture<String>> futures = txIds.stream()
                .map(id -> CompletableFuture.supplyAsync(
                        () -> callPartnerWebhook(webhookUrl, id, formattedDate),
                        webhookExecutor))
                .toList();

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    private String callPartnerWebhook(String webhookUrl, String txId, String date) {
        var body = """
                {"txId": "%s", "settlementDate": "%s"}""".formatted(txId, date);
        var request = HttpRequest.newBuilder()
                .uri(URI.create(webhookUrl))
                .timeout(Duration.ofSeconds(2))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("Webhook failed HTTP " + response.statusCode());
            }
            return response.body();
        } catch (IOException e) {
            throw new IllegalStateException("Webhook I/O failure for " + txId, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Webhook interrupted for " + txId, e);
        }
    }

    @Override
    public void close() {
        webhookExecutor.close();
        httpClient.close();
    }
}
