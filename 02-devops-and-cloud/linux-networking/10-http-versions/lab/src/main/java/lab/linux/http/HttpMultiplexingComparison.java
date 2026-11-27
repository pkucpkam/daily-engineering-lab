package lab.linux.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thí nghiệm loopback TCP thật cho LNX-10:
 * Gửi 1 request chậm (slowMs = 200ms) cùng lúc với 10 request nhanh (fastMs = 5ms)
 * qua 3 chế độ trên TCP loopback:
 * 1) HTTP/1.1 trên 1 kết nối TCP (Application HOL blocking rõ rệt)
 * 2) HTTP/1.1 với 6 kết nối TCP song song (kiểu browser workaround)
 * 3) Binary Frame Multiplexing kiểu HTTP/2 trên ĐÚNG 1 kết nối TCP
 */
public final class HttpMultiplexingComparison {

    public record ModeResult(
            String modeName,
            int tcpConnectionsOpened,
            long totalWallTimeMs,
            long medianFastRequestMs,
            long slowRequestMs) {}

    public List<ModeResult> runAll(int fastCount, int slowMs, int fastMs) throws Exception {
        List<ModeResult> results = new ArrayList<>();
        results.add(runHttp1SingleConnection(fastCount, slowMs, fastMs));
        results.add(runHttp1PoolConnections(fastCount, slowMs, fastMs, 6));
        results.add(runMultiplexedSingleConnection(fastCount, slowMs, fastMs));
        return results;
    }

    private ModeResult runHttp1SingleConnection(int fastCount, int slowMs, int fastMs) throws Exception {
        try (FrameServer server = new FrameServer(slowMs, fastMs)) {
            long[] fastLatencies = new long[fastCount];
            long tStart = System.nanoTime();
            long slowLatencyMs;

            try (Socket s = new Socket()) {
                s.setTcpNoDelay(true);
                s.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), server.port()), 2000);
                DataOutputStream out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
                DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()));

                // Request #1 chậm đi trước, tất cả 10 request nhanh phải xếp hàng chờ sau nó trên 1 TCP socket
                long tSlow = System.nanoTime();
                sendFrame(out, 1, "SLOW");
                readFrame(in);
                slowLatencyMs = Math.max(1L, (System.nanoTime() - tSlow) / 1_000_000L);

                for (int i = 0; i < fastCount; i++) {
                    sendFrame(out, 3 + i * 2, "FAST");
                    readFrame(in);
                    fastLatencies[i] = Math.max(1L, (System.nanoTime() - tStart) / 1_000_000L);
                }
            }
            long wallMs = Math.max(1L, (System.nanoTime() - tStart) / 1_000_000L);
            return new ModeResult("HTTP/1.1 (1 TCP conn)", server.acceptedConnections(), wallMs, median(fastLatencies), slowLatencyMs);
        }
    }

    private ModeResult runHttp1PoolConnections(int fastCount, int slowMs, int fastMs, int poolSize) throws Exception {
        try (FrameServer server = new FrameServer(slowMs, fastMs)) {
            List<Long> fastLatencies = Collections.synchronizedList(new ArrayList<>());
            long tStart = System.nanoTime();
            long[] slowHolder = new long[1];

            try (ExecutorService clientPool = Executors.newFixedThreadPool(poolSize)) {
                List<Socket> sockets = new ArrayList<>();
                for (int i = 0; i < poolSize; i++) {
                    Socket s = new Socket();
                    s.setTcpNoDelay(true);
                    s.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), server.port()), 2000);
                    sockets.add(s);
                }

                List<CompletableFuture<Void>> futures = new ArrayList<>();
                // Socket 0 chạy request SLOW
                futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        Socket s = sockets.get(0);
                        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
                        DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
                        sendFrame(out, 1, "SLOW");
                        readFrame(in);
                        slowHolder[0] = Math.max(1L, (System.nanoTime() - tStart) / 1_000_000L);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }, clientPool));

                // Các socket 1..5 chạy 10 request FAST nối đuôi nhau qua các socket còn lại
                for (int i = 0; i < fastCount; i++) {
                    final int idx = i;
                    final Socket sock = sockets.get(1 + (i % (poolSize - 1)));
                    futures.add(CompletableFuture.runAsync(() -> {
                        synchronized (sock) {
                            try {
                                DataOutputStream out = new DataOutputStream(new BufferedOutputStream(sock.getOutputStream()));
                                DataInputStream in = new DataInputStream(new BufferedInputStream(sock.getInputStream()));
                                sendFrame(out, 3 + idx * 2, "FAST");
                                readFrame(in);
                                fastLatencies.add(Math.max(1L, (System.nanoTime() - tStart) / 1_000_000L));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }, clientPool));
                }

                CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
                for (Socket s : sockets) {
                    s.close();
                }
            }
            long wallMs = Math.max(1L, (System.nanoTime() - tStart) / 1_000_000L);
            return new ModeResult(
                    "HTTP/1.1 (" + poolSize + " TCP conns)",
                    server.acceptedConnections(),
                    wallMs,
                    median(fastLatencies.stream().mapToLong(Long::longValue).toArray()),
                    slowHolder[0]);
        }
    }

    private ModeResult runMultiplexedSingleConnection(int fastCount, int slowMs, int fastMs) throws Exception {
        try (FrameServer server = new FrameServer(slowMs, fastMs)) {
            long tStart = System.nanoTime();
            long[] fastLatencies = new long[fastCount];
            long slowLatencyMs;

            try (Socket s = new Socket()) {
                s.setTcpNoDelay(true);
                s.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), server.port()), 2000);
                DataOutputStream out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
                DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()));

                Map<Integer, CompletableFuture<Long>> pending = new ConcurrentHashMap<>();
                CompletableFuture<Long> slowFuture = new CompletableFuture<>();
                List<CompletableFuture<Long>> fastFutures = new ArrayList<>(fastCount);
                pending.put(1, slowFuture);
                for (int i = 0; i < fastCount; i++) {
                    CompletableFuture<Long> f = new CompletableFuture<>();
                    fastFutures.add(f);
                    pending.put(3 + i * 2, f);
                }

                // Reader thread tách frame theo Stream ID khi về từ cùng 1 TCP socket
                AtomicInteger remaining = new AtomicInteger(1 + fastCount);
                Thread reader = new Thread(() -> {
                    try {
                        while (remaining.get() > 0) {
                            Frame frame = readFrame(in);
                            if (frame == null) {
                                break;
                            }
                            CompletableFuture<Long> f = pending.get(frame.streamId());
                            if (f != null && f.complete(Math.max(1L, (System.nanoTime() - tStart) / 1_000_000L))) {
                                remaining.decrementAndGet();
                            }
                        }
                    } catch (IOException ignored) {
                    }
                }, "h2-frame-reader");
                reader.setDaemon(true);
                reader.start();

                // Đẩy toàn bộ frame của Stream 1 (SLOW) và Stream 3..21 (FAST) trên CÙNG 1 TCP connection
                synchronized (out) {
                    sendFrame(out, 1, "SLOW");
                    for (int i = 0; i < fastCount; i++) {
                        sendFrame(out, 3 + i * 2, "FAST");
                    }
                }

                for (int i = 0; i < fastCount; i++) {
                    // Đọc xong trước cả khi Stream 1 trả lời!
                    fastLatencies[i] = fastFutures.get(i).get(5, TimeUnit.SECONDS);
                }
                slowLatencyMs = slowFuture.get(5, TimeUnit.SECONDS);
                reader.join(1000);
            }
            long wallMs = Math.max(1L, (System.nanoTime() - tStart) / 1_000_000L);
            return new ModeResult("HTTP/2 Multiplexed (1 TCP conn)", server.acceptedConnections(), wallMs, median(fastLatencies), slowLatencyMs);
        }
    }

    private record Frame(int streamId, String payload) {}

    private static void sendFrame(DataOutputStream out, int streamId, String payload) throws IOException {
        byte[] bytes = payload.getBytes(StandardCharsets.US_ASCII);
        out.writeInt(streamId);
        out.writeInt(bytes.length);
        out.write(bytes);
        out.flush();
    }

    private static Frame readFrame(DataInputStream in) throws IOException {
        try {
            int streamId = in.readInt();
            int len = in.readInt();
            byte[] buf = in.readNBytes(len);
            if (buf.length < len) {
                return null;
            }
            return new Frame(streamId, new String(buf, StandardCharsets.US_ASCII));
        } catch (EOFException e) {
            return null;
        }
    }

    private static long median(long[] arr) {
        if (arr.length == 0) {
            return 0;
        }
        long[] copy = Arrays.copyOf(arr, arr.length);
        Arrays.sort(copy);
        return copy[copy.length / 2];
    }

    static final class FrameServer implements AutoCloseable {
        private final ServerSocket serverSocket;
        private final ExecutorService workers = Executors.newCachedThreadPool();
        private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
        private final AtomicInteger acceptedCount = new AtomicInteger();
        private final int slowMs;
        private final int fastMs;
        private volatile boolean running = true;

        FrameServer(int slowMs, int fastMs) throws IOException {
            this.slowMs = slowMs;
            this.fastMs = fastMs;
            this.serverSocket = new ServerSocket(0, 128, InetAddress.getLoopbackAddress());
            Thread acceptor = new Thread(() -> {
                while (running) {
                    try {
                        Socket client = serverSocket.accept();
                        acceptedCount.incrementAndGet();
                        client.setTcpNoDelay(true);
                        workers.submit(() -> handle(client));
                    } catch (IOException ignored) {
                        if (!running) {
                            return;
                        }
                    }
                }
            }, "lnx10-frame-server-acceptor");
            acceptor.setDaemon(true);
            acceptor.start();
        }

        int port() {
            return serverSocket.getLocalPort();
        }

        int acceptedConnections() {
            return acceptedCount.get();
        }

        private void handle(Socket client) {
            try (client;
                 DataInputStream in = new DataInputStream(new BufferedInputStream(client.getInputStream()));
                 DataOutputStream out = new DataOutputStream(new BufferedOutputStream(client.getOutputStream()))) {
                Object writeLock = new Object();
                while (running) {
                    Frame req = readFrame(in);
                    if (req == null) {
                        return;
                    }
                    int delayMs = "SLOW".equals(req.payload()) ? slowMs : fastMs;
                    scheduler.schedule(() -> {
                        synchronized (writeLock) {
                            try {
                                sendFrame(out, req.streamId(), "OK:" + req.payload());
                            } catch (IOException ignored) {
                            }
                        }
                    }, delayMs, TimeUnit.MILLISECONDS);
                }
            } catch (IOException ignored) {
            }
        }

        @Override
        public void close() throws IOException {
            running = false;
            serverSocket.close();
            scheduler.shutdownNow();
            workers.shutdownNow();
        }
    }
}
