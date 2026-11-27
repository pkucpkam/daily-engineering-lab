package lab.linux.sockets.solutions;

import lab.linux.sockets.SocketExhaustionDemo.CountingHttpLoopbackServer;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Lời giải tham khảo E7 (LNX-09):
 * Dùng một instance HttpClient duy nhất để tái sử dụng connection pool (Keep-Alive),
 * đặt đầy đủ connectTimeout + requestTimeout, và tiêu thụ trọn vẹn response body
 * ngay cả khi server trả HTTP 500 để không bao giờ rò rỉ socket (CLOSE_WAIT).
 */
public final class HttpClientPoolSolution implements AutoCloseable {

    private final HttpClient sharedClient;

    public HttpClientPoolSolution() {
        this.sharedClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    public int getAndDrain(URI uri) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
        HttpResponse<String> response = sharedClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode();
    }

    @Override
    public void close() {
        sharedClient.close();
    }

    public static void main(String[] args) throws Exception {
        try (CountingHttpLoopbackServer server = new CountingHttpLoopbackServer();
             HttpClientPoolSolution client = new HttpClientPoolSolution()) {
            int ok = 0;
            int err = 0;
            for (int i = 0; i < 100; i++) {
                String path = (i % 5 == 0) ? "/err" : "/ok";
                int code = client.getAndDrain(URI.create("http://127.0.0.1:" + server.port() + path));
                if (code == 200) {
                    ok++;
                } else {
                    err++;
                }
            }
            System.out.printf("Completed 100 requests (200=%d, 500=%d) using only %d TCP connection(s)!%n",
                    ok, err, server.acceptedConnections());
        }
    }
}
