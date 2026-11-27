package lab.linux.http.solutions;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Lời giải tham khảo (LNX-10):
 * Dùng 1 instance HttpClient duy nhất cấu hình Version.HTTP_2 để gửi song song nhiều request
 * qua CompletableFuture.allOf(...) -> chia sẻ 1 kết nối TCP/TLS & Dynamic Table HPACK.
 */
public final class ConcurrentHttp2ClientSolution implements AutoCloseable {

    private final HttpClient client;

    public ConcurrentHttp2ClientSolution() {
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    public List<Integer> fetchAllConcurrently(List<URI> uris) {
        List<CompletableFuture<Integer>> futures = uris.stream()
                .map(uri -> {
                    HttpRequest req = HttpRequest.newBuilder(uri)
                            .timeout(Duration.ofSeconds(5))
                            .GET()
                            .build();
                    return client.sendAsync(req, HttpResponse.BodyHandlers.discarding())
                            .thenApply(HttpResponse::statusCode);
                })
                .toList();

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
        return futures.stream().map(CompletableFuture::join).toList();
    }

    @Override
    public void close() {
        client.close();
    }
}
