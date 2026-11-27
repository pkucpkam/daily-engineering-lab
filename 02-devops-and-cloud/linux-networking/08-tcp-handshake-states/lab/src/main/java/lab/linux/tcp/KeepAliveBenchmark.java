package lab.linux.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Đo hiệu năng thực tế qua loopback TCP:
 * 1) Mở-đóng TCP connection mới cho mỗi request vs Keep-Alive tái sử dụng 1 kết nối.
 * 2) Thực thi luồng write-write-read với TCP_NODELAY=true.
 */
public final class KeepAliveBenchmark {

    public record BenchmarkResult(long newConnectionPerReqMs, long keepAliveReuseMs, double speedupRatio) {}

    public BenchmarkResult runComparison(int requests) throws IOException {
        try (EchoServer server = new EchoServer()) {
            // Warmup ngắn để JIT compile xong socket loop
            measureKeepAlive(server.port(), 20);
            measureNewConnections(server.port(), 20);

            long newConnNs = measureNewConnections(server.port(), requests);
            long keepAliveNs = measureKeepAlive(server.port(), requests);

            long newConnMs = Math.max(1L, newConnNs / 1_000_000L);
            long keepAliveMs = Math.max(1L, keepAliveNs / 1_000_000L);
            double speedup = (double) newConnNs / Math.max(1L, keepAliveNs);
            return new BenchmarkResult(newConnMs, keepAliveMs, speedup);
        }
    }

    private long measureNewConnections(int port, int requests) throws IOException {
        long t0 = System.nanoTime();
        for (int i = 0; i < requests; i++) {
            try (Socket s = new Socket()) {
                s.setTcpNoDelay(true);
                s.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), port), 2000);
                s.setSoTimeout(2000);
                PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
                out.println("PING");
                String line = in.readLine();
                if (!"PING".equals(line)) {
                    throw new IllegalStateException("Unexpected reply: " + line);
                }
            }
        }
        return System.nanoTime() - t0;
    }

    private long measureKeepAlive(int port, int requests) throws IOException {
        long t0 = System.nanoTime();
        try (Socket s = new Socket()) {
            s.setTcpNoDelay(true);
            s.setKeepAlive(true);
            s.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), port), 2000);
            s.setSoTimeout(2000);
            PrintWriter out = new PrintWriter(
                    new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
            for (int i = 0; i < requests; i++) {
                out.println("PING");
                String line = in.readLine();
                if (!"PING".equals(line)) {
                    throw new IllegalStateException("Unexpected reply: " + line);
                }
            }
        }
        return System.nanoTime() - t0;
    }

    static final class EchoServer implements AutoCloseable {
        private final ServerSocket server;
        private final ExecutorService workers = Executors.newCachedThreadPool();
        private volatile boolean running = true;

        EchoServer() throws IOException {
            this.server = new ServerSocket(0, 512, InetAddress.getLoopbackAddress());
            Thread acceptor = new Thread(() -> {
                while (running) {
                    try {
                        Socket client = server.accept();
                        client.setTcpNoDelay(true);
                        workers.submit(() -> handle(client));
                    } catch (IOException ignored) {
                        if (!running) {
                            return;
                        }
                    }
                }
            }, "tcp-bench-acceptor");
            acceptor.setDaemon(true);
            acceptor.start();
        }

        int port() {
            return server.getLocalPort();
        }

        private void handle(Socket client) {
            try (client;
                 BufferedReader in = new BufferedReader(
                         new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
                 PrintWriter out = new PrintWriter(
                         new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true)) {
                String line;
                while ((line = in.readLine()) != null) {
                    out.println(line);
                }
            } catch (IOException ignored) {
            }
        }

        @Override
        public void close() throws IOException {
            running = false;
            server.close();
            workers.shutdownNow();
        }
    }
}
