package lab.linux.lb;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Mini L7 Reverse Proxy & Load Balancer chạy trên loopback TCP thật cho LNX-13:
 * - Cân tải Round-Robin hoặc Least Connections.
 * - Passive Health Check (maxFails / failTimeoutMs) + safe retry cho GET (KHÔNG retry POST).
 * - Tái hiện 504 Gateway Timeout khi proxyReadTimeout < thời gian xử lý của backend.
 */
public final class MiniL7ReverseProxy implements AutoCloseable {

    public enum Strategy {
        ROUND_ROBIN,
        LEAST_CONNECTIONS
    }

    public static final class UpstreamNode {
        final String name;
        final int port;
        final AtomicInteger inFlight = new AtomicInteger();
        final AtomicInteger consecutiveFailures = new AtomicInteger();
        final AtomicLong circuitOpenUntilMs = new AtomicLong(0);
        final AtomicInteger proxyAttemptCount = new AtomicInteger();

        UpstreamNode(String name, int port) {
            this.name = name;
            this.port = port;
        }

        boolean isHealthy(long nowMs) {
            return nowMs >= circuitOpenUntilMs.get();
        }

        void recordSuccess() {
            consecutiveFailures.set(0);
            circuitOpenUntilMs.set(0);
        }

        void recordFailure(int maxFails, long failTimeoutMs, long nowMs) {
            if (consecutiveFailures.incrementAndGet() >= maxFails) {
                circuitOpenUntilMs.set(nowMs + failTimeoutMs);
            }
        }
    }

    private final ServerSocket proxySocket;
    private final ExecutorService workers = Executors.newCachedThreadPool();
    private final List<UpstreamNode> upstreams = new CopyOnWriteArrayList<>();
    private final Strategy strategy;
    private final int readTimeoutMs;
    private final int maxFails;
    private final long failTimeoutMs;
    private final AtomicInteger rrIndex = new AtomicInteger();
    private volatile boolean running = true;

    public MiniL7ReverseProxy(
            List<BackendInstance> backends,
            Strategy strategy,
            int readTimeoutMs,
            int maxFails,
            long failTimeoutMs) throws IOException {
        for (BackendInstance b : backends) {
            upstreams.add(new UpstreamNode(b.name(), b.port()));
        }
        this.strategy = strategy;
        this.readTimeoutMs = readTimeoutMs;
        this.maxFails = maxFails;
        this.failTimeoutMs = failTimeoutMs;
        this.proxySocket = new ServerSocket(0, 256, InetAddress.getLoopbackAddress());

        Thread acceptor = new Thread(() -> {
            while (running) {
                try {
                    Socket client = proxySocket.accept();
                    client.setTcpNoDelay(true);
                    workers.submit(() -> handleClient(client));
                } catch (IOException ignored) {
                    if (!running) {
                        return;
                    }
                }
            }
        }, "lnx13-l7-proxy");
        acceptor.setDaemon(true);
        acceptor.start();
    }

    public int port() {
        return proxySocket.getLocalPort();
    }

    public List<UpstreamNode> upstreams() {
        return upstreams;
    }

    private void handleClient(Socket client) {
        try (client;
             BufferedInputStream clientIn = new BufferedInputStream(client.getInputStream());
             OutputStream clientOut = client.getOutputStream()) {
            client.setSoTimeout(5000);
            String reqHead = readHeaders(clientIn);
            if (reqHead == null) {
                return;
            }
            String firstLine = reqHead.split("\r\n", 2)[0];
            String method = firstLine.split(" ", 2)[0];
            boolean idempotent = "GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method);

            int maxAttempts = idempotent ? upstreams.size() : 1;
            for (int attempt = 0; attempt < maxAttempts; attempt++) {
                UpstreamNode node = pickUpstream();
                if (node == null) {
                    writeSimpleResponse(clientOut, 502, "502 Bad Gateway: no healthy upstreams");
                    return;
                }
                node.proxyAttemptCount.incrementAndGet();
                node.inFlight.incrementAndGet();
                try (Socket upstream = new Socket()) {
                    upstream.setTcpNoDelay(true);
                    upstream.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), node.port), 500);
                    upstream.setSoTimeout(readTimeoutMs);

                    OutputStream upOut = upstream.getOutputStream();
                    upOut.write(reqHead.getBytes(StandardCharsets.US_ASCII));
                    upOut.flush();

                    byte[] response = upstream.getInputStream().readAllBytes();
                    if (response.length == 0) {
                        throw new IOException("Empty upstream response");
                    }
                    node.recordSuccess();
                    clientOut.write(response);
                    clientOut.flush();
                    return;
                } catch (SocketTimeoutException timeoutEx) {
                    // Timeout đọc response: node đã nhận request -> với POST tuyệt đối KHÔNG retry!
                    if (!idempotent) {
                        writeSimpleResponse(clientOut, 504, "504 Gateway Timeout (non-idempotent NOT retried)");
                        return;
                    }
                    node.recordFailure(maxFails, failTimeoutMs, System.currentTimeMillis());
                } catch (IOException ioEx) {
                    node.recordFailure(maxFails, failTimeoutMs, System.currentTimeMillis());
                } finally {
                    node.inFlight.decrementAndGet();
                }
            }
            writeSimpleResponse(clientOut, 502, "502 Bad Gateway: all retries failed");
        } catch (IOException ignored) {
        }
    }

    private UpstreamNode pickUpstream() {
        long now = System.currentTimeMillis();
        List<UpstreamNode> healthy = upstreams.stream().filter(n -> n.isHealthy(now)).toList();
        if (healthy.isEmpty()) {
            return null;
        }
        if (strategy == Strategy.ROUND_ROBIN) {
            int idx = Math.floorMod(rrIndex.getAndIncrement(), healthy.size());
            return healthy.get(idx);
        }
        // LEAST_CONNECTIONS
        UpstreamNode best = healthy.get(0);
        int bestInFlight = best.inFlight.get();
        for (int i = 1; i < healthy.size(); i++) {
            UpstreamNode candidate = healthy.get(i);
            int cur = candidate.inFlight.get();
            if (cur < bestInFlight) {
                best = candidate;
                bestInFlight = cur;
            }
        }
        return best;
    }

    private static void writeSimpleResponse(OutputStream out, int code, String bodyText) throws IOException {
        byte[] body = bodyText.getBytes(StandardCharsets.UTF_8);
        String reason = switch (code) {
            case 504 -> "Gateway Timeout";
            case 502 -> "Bad Gateway";
            default -> "OK";
        };
        String head = "HTTP/1.1 " + code + " " + reason + "\r\n"
                + "Content-Type: text/plain\r\n"
                + "Content-Length: " + body.length + "\r\n"
                + "Connection: close\r\n\r\n";
        out.write(head.getBytes(StandardCharsets.US_ASCII));
        out.write(body);
        out.flush();
    }

    static String readHeaders(InputStream in) throws IOException {
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
        proxySocket.close();
        workers.shutdownNow();
    }

    /**
     * Loopback HTTP Backend Instance giả lập một node JBoss thật.
     */
    public static final class BackendInstance implements AutoCloseable {
        private final String name;
        private final ServerSocket serverSocket;
        private final ExecutorService workers = Executors.newCachedThreadPool();
        private final int processingDelayMs;
        private final AtomicInteger requestCount = new AtomicInteger();
        private final AtomicInteger completedCommits = new AtomicInteger();
        private volatile boolean running = true;

        public BackendInstance(String name, int processingDelayMs) throws IOException {
            this.name = name;
            this.processingDelayMs = processingDelayMs;
            this.serverSocket = new ServerSocket(0, 256, InetAddress.getLoopbackAddress());
            Thread acceptor = new Thread(() -> {
                while (running) {
                    try {
                        Socket s = serverSocket.accept();
                        s.setTcpNoDelay(true);
                        workers.submit(() -> serve(s));
                    } catch (IOException ignored) {
                        if (!running) {
                            return;
                        }
                    }
                }
            }, "lnx13-backend-" + name);
            acceptor.setDaemon(true);
            acceptor.start();
        }

        public String name() {
            return name;
        }

        public int port() {
            return serverSocket.getLocalPort();
        }

        public int requestCount() {
            return requestCount.get();
        }

        public int completedCommits() {
            return completedCommits.get();
        }

        private void serve(Socket s) {
            try (s;
                 BufferedInputStream in = new BufferedInputStream(s.getInputStream());
                 OutputStream out = s.getOutputStream()) {
                String req = readHeaders(in);
                if (req == null) {
                    return;
                }
                requestCount.incrementAndGet();
                if (processingDelayMs > 0) {
                    Thread.sleep(processingDelayMs);
                }
                completedCommits.incrementAndGet();
                writeSimpleResponse(out, 200, "backend=" + name);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            } catch (IOException ignored) {
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
