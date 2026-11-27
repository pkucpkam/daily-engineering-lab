package lab.linux.sockets;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thí nghiệm loopback TCP thật cho LNX-09:
 * 1) So sánh mở kết nối mới (Connection: close) vs tái sử dụng bằng HTTP Keep-Alive Pool.
 * 2) Tái hiện rò rỉ socket phía client khi server đã đóng (FIN) nhưng client quên close() -> CLOSE_WAIT.
 */
public final class SocketExhaustionDemo {

    public record HttpComparisonResult(
            int totalRequests,
            int noPoolAcceptedConnections,
            long noPoolDurationMs,
            int pooledAcceptedConnections,
            long pooledDurationMs) {}

    public record LeakSimulationResult(
            int leakedSocketCount,
            int poolLimit,
            boolean exhaustedLimit) {}

    public HttpComparisonResult compareNoPoolVsKeepAlive(int requests) throws Exception {
        try (CountingHttpLoopbackServer server = new CountingHttpLoopbackServer();
             HttpClient sharedClient = HttpClient.newBuilder()
                     .version(HttpClient.Version.HTTP_1_1)
                     .connectTimeout(Duration.ofSeconds(2))
                     .build()) {
            // Warmup ngắn cho cả 2 chế độ trước khi bấm giờ
            runNoPoolRequests(server.port(), 5);
            runPooledRequests(sharedClient, server.port(), 5);
            server.resetAcceptedCount();

            long t0 = System.nanoTime();
            runNoPoolRequests(server.port(), requests);
            long noPoolMs = Math.max(1L, (System.nanoTime() - t0) / 1_000_000L);
            int noPoolConns = server.acceptedConnections();

            server.resetAcceptedCount();
            long t1 = System.nanoTime();
            runPooledRequests(sharedClient, server.port(), requests);
            long pooledMs = Math.max(1L, (System.nanoTime() - t1) / 1_000_000L);
            int pooledConns = server.acceptedConnections();

            return new HttpComparisonResult(requests, noPoolConns, noPoolMs, pooledConns, pooledMs);
        }
    }

    /**
     * Server chủ động đóng kết nối ngay sau khi trả response (gửi FIN -> passive side nhận FIN).
     * Client cố tình đọc header xong nhưng KHÔNG gọi close() (giữ reference trong list như leak thật)
     * -> Trên OS TCP stack, các socket chưa đóng phía client chuyển sang trạng thái CLOSE_WAIT!
     */
    public LeakSimulationResult simulateCloseWaitLeak(int requests, int maxClientFdBudget) throws IOException {
        List<Socket> leakedSockets = new ArrayList<>();
        boolean exhausted = false;
        try (CountingHttpLoopbackServer server = new CountingHttpLoopbackServer()) {
            for (int i = 0; i < requests; i++) {
                if (leakedSockets.size() >= maxClientFdBudget) {
                    exhausted = true;
                    break;
                }
                Socket s = new Socket();
                s.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), server.port()), 2000);
                s.setSoTimeout(2000);
                OutputStream out = s.getOutputStream();
                out.write("GET /close HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n"
                        .getBytes(StandardCharsets.US_ASCII));
                out.flush();

                // Đọc sơ response từ server, sau đó server tự close() gửi FIN về phía client.
                // CỐ TÌNH KHÔNG gọi s.close(): socket này kẹt ở trạng thái CLOSE_WAIT trên OS!
                byte[] buf = new byte[128];
                s.getInputStream().read(buf);
                leakedSockets.add(s);
            }
            return new LeakSimulationResult(leakedSockets.size(), maxClientFdBudget, exhausted);
        } finally {
            for (Socket s : leakedSockets) {
                try {
                    s.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private void runNoPoolRequests(int port, int requests) throws IOException {
        URI uri = URI.create("http://127.0.0.1:" + port + "/close");
        for (int i = 0; i < requests; i++) {
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Connection", "close");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            int status = conn.getResponseCode();
            try (InputStream is = status >= 400 ? conn.getErrorStream() : conn.getInputStream()) {
                if (is != null) {
                    is.readAllBytes();
                }
            }
            conn.disconnect();
        }
    }

    private void runPooledRequests(HttpClient client, int port, int requests)
            throws IOException, InterruptedException {
        URI uri = URI.create("http://127.0.0.1:" + port + "/ok");
        for (int i = 0; i < requests; i++) {
            HttpRequest req = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();
            HttpResponse<Void> resp = client.send(req, HttpResponse.BodyHandlers.discarding());
            if (resp.statusCode() != 200) {
                throw new IllegalStateException("Unexpected status: " + resp.statusCode());
            }
        }
    }

    /**
     * Loopback HTTP/1.1 server nhẹ hỗ trợ cả Keep-Alive lẫn Connection: close và đếm chính xác số accept().
     */
    public static final class CountingHttpLoopbackServer implements AutoCloseable {
        private final ServerSocket serverSocket;
        private final ExecutorService workers = Executors.newCachedThreadPool();
        private final AtomicInteger acceptedCount = new AtomicInteger();
        private volatile boolean running = true;

        public CountingHttpLoopbackServer() throws IOException {
            this.serverSocket = new ServerSocket(0, 512, InetAddress.getLoopbackAddress());
            Thread acceptor = new Thread(() -> {
                while (running) {
                    try {
                        Socket client = serverSocket.accept();
                        acceptedCount.incrementAndGet();
                        client.setTcpNoDelay(true);
                        workers.submit(() -> handleConnection(client));
                    } catch (IOException ignored) {
                        if (!running) {
                            return;
                        }
                    }
                }
            }, "lnx09-loopback-http-acceptor");
            acceptor.setDaemon(true);
            acceptor.start();
        }

        public int port() {
            return serverSocket.getLocalPort();
        }

        public int acceptedConnections() {
            return acceptedCount.get();
        }

        public void resetAcceptedCount() {
            acceptedCount.set(0);
        }

        private void handleConnection(Socket client) {
            try (client;
                 BufferedInputStream in = new BufferedInputStream(client.getInputStream());
                 OutputStream out = client.getOutputStream()) {
                client.setSoTimeout(5000);
                while (running) {
                    String requestHead = readHttpHeaders(in);
                    if (requestHead == null) {
                        return;
                    }
                    String firstLine = requestHead.split("\r\n", 2)[0];
                    boolean wantsClose = firstLine.contains("/close")
                            || requestHead.toLowerCase().contains("connection: close");
                    boolean wantsError = firstLine.contains("/err");

                    byte[] body = (wantsError ? "{\"error\":\"upstream\"}" : "{\"status\":\"ok\"}")
                            .getBytes(StandardCharsets.UTF_8);
                    String statusLine = wantsError ? "HTTP/1.1 500 Internal Server Error\r\n" : "HTTP/1.1 200 OK\r\n";
                    String connHeader = wantsClose ? "Connection: close\r\n" : "Connection: keep-alive\r\n";
                    String headers = statusLine
                            + "Content-Type: application/json\r\n"
                            + "Content-Length: " + body.length + "\r\n"
                            + connHeader
                            + "\r\n";
                    out.write(headers.getBytes(StandardCharsets.US_ASCII));
                    out.write(body);
                    out.flush();

                    if (wantsClose) {
                        // Server chủ động đóng kết nối trước (gửi FIN)
                        return;
                    }
                }
            } catch (IOException ignored) {
            }
        }

        private static String readHttpHeaders(InputStream in) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(256);
            int state = 0;
            while (true) {
                int b = in.read();
                if (b == -1) {
                    return baos.size() == 0 ? null : baos.toString(StandardCharsets.US_ASCII);
                }
                baos.write(b);
                if ((state == 0 && b == '\r')
                        || (state == 1 && b == '\n')
                        || (state == 2 && b == '\r')) {
                    state++;
                } else if (state == 3 && b == '\n') {
                    return baos.toString(StandardCharsets.US_ASCII);
                } else {
                    state = (b == '\r') ? 1 : 0;
                }
            }
        }

        @Override
        public void close() throws IOException {
            running = false;
            serverSocket.close();
            workers.shutdownNow();
        }
    }
}
