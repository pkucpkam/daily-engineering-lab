package lab.jvm.bridge.solutions;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Reference solution for Exercise E8:
 * Reusable Java 21 HttpClient calling two endpoints concurrently with explicit timeouts.
 */
public class E8AsyncRateClient implements AutoCloseable {

    private static final Duration TIMEOUT = Duration.ofMillis(1500);
    private final HttpClient httpClient;

    public E8AsyncRateClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build();
    }

    public CompletableFuture<BigDecimal> fetchCombinedMultiplierAsync(String taxUrl, String fxUrl) {
        CompletableFuture<BigDecimal> taxFuture = getDecimalAsync(taxUrl);
        CompletableFuture<BigDecimal> fxFuture = getDecimalAsync(fxUrl);
        return taxFuture.thenCombine(fxFuture, BigDecimal::multiply);
    }

    private CompletableFuture<BigDecimal> getDecimalAsync(String url) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(TIMEOUT)
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        throw new IllegalStateException(
                                "HTTP " + response.statusCode() + " from " + url + ": " + response.body());
                    }
                    return new BigDecimal(response.body().trim());
                });
    }

    @Override
    public void close() {
        // Java 21 AutoCloseable support
        httpClient.close();
    }
}
