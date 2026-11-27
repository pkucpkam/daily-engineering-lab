package lab.linux.lb;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

class LoadBalancerLabTest {

    @Test
    void leastConnectionsRoutesFewerRequestsToSlowBackendThanRoundRobin() throws Exception {
        int rrSlowCount;
        try (var b1 = new MiniL7ReverseProxy.BackendInstance("slow", 150);
             var b2 = new MiniL7ReverseProxy.BackendInstance("fast2", 5);
             var b3 = new MiniL7ReverseProxy.BackendInstance("fast3", 5);
             var proxy = new MiniL7ReverseProxy(List.of(b1, b2, b3), MiniL7ReverseProxy.Strategy.ROUND_ROBIN, 3000, 2, 2000);
             HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build()) {
            sendClosedLoopBatch(client, proxy.port(), 6, 4);
            rrSlowCount = b1.requestCount();
        }

        int lcSlowCount;
        try (var b1 = new MiniL7ReverseProxy.BackendInstance("slow", 150);
             var b2 = new MiniL7ReverseProxy.BackendInstance("fast2", 5);
             var b3 = new MiniL7ReverseProxy.BackendInstance("fast3", 5);
             var proxy = new MiniL7ReverseProxy(List.of(b1, b2, b3), MiniL7ReverseProxy.Strategy.LEAST_CONNECTIONS, 3000, 2, 2000);
             HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build()) {
            sendClosedLoopBatch(client, proxy.port(), 6, 4);
            lcSlowCount = b1.requestCount();
        }

        assertThat(rrSlowCount).isEqualTo(8);
        assertThat(lcSlowCount).isLessThan(rrSlowCount);
    }

    @Test
    void passiveHealthCheckIsolatesKilledBackendAndServesAllGetRequests() throws Exception {
        var b1 = new MiniL7ReverseProxy.BackendInstance("b1", 5);
        var b2 = new MiniL7ReverseProxy.BackendInstance("b2", 5);
        var b3 = new MiniL7ReverseProxy.BackendInstance("b3", 5);
        try (b1; b3;
             var proxy = new MiniL7ReverseProxy(List.of(b1, b2, b3), MiniL7ReverseProxy.Strategy.ROUND_ROBIN, 2000, 2, 5000);
             HttpClient client = HttpClient.newHttpClient()) {
            b2.close();
            URI uri = URI.create("http://127.0.0.1:" + proxy.port() + "/api/ping");
            for (int i = 0; i < 12; i++) {
                var resp = client.send(HttpRequest.newBuilder(uri).GET().build(), HttpResponse.BodyHandlers.ofString());
                assertThat(resp.statusCode()).isEqualTo(200);
            }
            // max_fails=2 nên node-2 chỉ bị thử đúng 2 lần rồi bị cách ly khỏi vòng chia tải
            assertThat(proxy.upstreams().get(1).proxyAttemptCount.get()).isEqualTo(2);
        }
    }

    @Test
    void proxyReadTimeoutShorterThanBackendLatencyReturns504WhileBackendStillCommits() throws Exception {
        try (var backend = new MiniL7ReverseProxy.BackendInstance("pay-node", 180);
             var proxy = new MiniL7ReverseProxy(List.of(backend), MiniL7ReverseProxy.Strategy.ROUND_ROBIN, 60, 2, 2000);
             HttpClient client = HttpClient.newHttpClient()) {
            URI uri = URI.create("http://127.0.0.1:" + proxy.port() + "/api/pay");
            var resp = client.send(
                    HttpRequest.newBuilder(uri).POST(HttpRequest.BodyPublishers.noBody()).build(),
                    HttpResponse.BodyHandlers.ofString());

            assertThat(resp.statusCode()).isEqualTo(504);
            Thread.sleep(220);
            assertThat(backend.completedCommits()).isEqualTo(1);
        }
    }

    private static void sendClosedLoopBatch(HttpClient client, int port, int workers, int requestsPerWorker) {
        URI uri = URI.create("http://127.0.0.1:" + port + "/api/ping");
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int w = 0; w < workers; w++) {
            futures.add(CompletableFuture.runAsync(() -> {
                for (int i = 0; i < requestsPerWorker; i++) {
                    try {
                        var resp = client.send(
                                HttpRequest.newBuilder(uri).GET().build(),
                                HttpResponse.BodyHandlers.discarding());
                        if (resp.statusCode() != 200) {
                            throw new IllegalStateException("Unexpected status: " + resp.statusCode());
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }));
        }
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
    }
}
