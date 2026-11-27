package lab.linux.lb;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Runner thực nghiệm LNX-13:
 * 1) So sánh Round-Robin vs Least Connections khi 1 backend chậm.
 * 2) Kill 1 backend trong 3 node & quan sát Passive Health Check failover.
 * 3) Tái hiện 504 Gateway Timeout khi proxyReadTimeout < backend latency.
 */
public final class LoadBalancerRunner {

    public static void main(String[] args) throws Exception {
        runStrategyComparison(MiniL7ReverseProxy.Strategy.ROUND_ROBIN);
        runStrategyComparison(MiniL7ReverseProxy.Strategy.LEAST_CONNECTIONS);
        runFailoverDemo();
        runTimeoutMismatchDemo();
    }

    private static void runStrategyComparison(MiniL7ReverseProxy.Strategy strategy) throws Exception {
        try (var b1 = new MiniL7ReverseProxy.BackendInstance("jboss-slow-1", 150);
             var b2 = new MiniL7ReverseProxy.BackendInstance("jboss-fast-2", 5);
             var b3 = new MiniL7ReverseProxy.BackendInstance("jboss-fast-3", 5);
             var proxy = new MiniL7ReverseProxy(List.of(b1, b2, b3), strategy, 3000, 2, 2000);
             HttpClient client = HttpClient.newBuilder()
                     .version(HttpClient.Version.HTTP_1_1)
                     .connectTimeout(Duration.ofSeconds(2))
                     .build()) {

            URI uri = URI.create("http://127.0.0.1:" + proxy.port() + "/api/ping");
            long t0 = System.nanoTime();
            // 6 worker song song, mỗi worker gửi tuần tự 5 request (tổng 30 reqs)
            List<CompletableFuture<Void>> workers = new ArrayList<>();
            for (int w = 0; w < 6; w++) {
                workers.add(CompletableFuture.runAsync(() -> {
                    for (int i = 0; i < 5; i++) {
                        try {
                            var req = HttpRequest.newBuilder(uri).GET().build();
                            client.send(req, HttpResponse.BodyHandlers.discarding());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }));
            }
            CompletableFuture.allOf(workers.toArray(CompletableFuture[]::new)).join();
            long elapsedMs = (System.nanoTime() - t0) / 1_000_000L;

            System.out.printf("[%s] 30 reqs (6 workers x 5) -> %d ms | slow-1=%d | fast-2=%d | fast-3=%d%n",
                    strategy, elapsedMs, b1.requestCount(), b2.requestCount(), b3.requestCount());
        }
    }

    private static void runFailoverDemo() throws Exception {
        var b1 = new MiniL7ReverseProxy.BackendInstance("node-1", 5);
        var b2 = new MiniL7ReverseProxy.BackendInstance("node-2", 5);
        var b3 = new MiniL7ReverseProxy.BackendInstance("node-3", 5);
        try (b1; b3;
             var proxy = new MiniL7ReverseProxy(List.of(b1, b2, b3), MiniL7ReverseProxy.Strategy.ROUND_ROBIN, 2000, 2, 5000);
             HttpClient client = HttpClient.newHttpClient()) {
            URI uri = URI.create("http://127.0.0.1:" + proxy.port() + "/api/ping");
            b2.close(); // Kill đột ngột node-2!

            int ok200 = 0;
            for (int i = 0; i < 15; i++) {
                var resp = client.send(HttpRequest.newBuilder(uri).GET().build(), HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 200) {
                    ok200++;
                }
            }
            System.out.printf("[Failover max_fails=2] Kill node-2 -> 15 GET reqs: 200_OK=%d | node-2 attempts=%d%n",
                    ok200, proxy.upstreams().get(1).proxyAttemptCount.get());
        }
    }

    private static void runTimeoutMismatchDemo() throws Exception {
        try (var b1 = new MiniL7ReverseProxy.BackendInstance("jboss-commit-180ms", 180);
             var proxy = new MiniL7ReverseProxy(List.of(b1), MiniL7ReverseProxy.Strategy.ROUND_ROBIN, 60, 2, 2000);
             HttpClient client = HttpClient.newHttpClient()) {
            URI uri = URI.create("http://127.0.0.1:" + proxy.port() + "/api/pay");
            var resp = client.send(
                    HttpRequest.newBuilder(uri).POST(HttpRequest.BodyPublishers.noBody()).build(),
                    HttpResponse.BodyHandlers.ofString());
            Thread.sleep(220);
            System.out.printf("[Timeout Mismatch] proxyReadTimeout=60ms vs backend=180ms -> clientStatus=%d | backendCompletedCommits=%d%n",
                    resp.statusCode(), b1.completedCommits());
        }
    }
}
