package lab.linux.epoll;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Echo server theo mô hình cổ điển Thread-per-request (BIO, giống Tomcat/JBoss BIO connector):
 * 1 kết nối TCP chiếm trọn 1 thread trong pool suốt thời gian kết nối mở.
 */
public final class BlockingBioEchoServer implements AutoCloseable {

    private final ServerSocket serverSocket;
    private final ExecutorService workerPool;
    private volatile boolean running = true;
    private final Thread acceptThread;

    public BlockingBioEchoServer(int maxThreads) throws IOException {
        this.serverSocket = new ServerSocket(0, 128, InetAddress.getLoopbackAddress());
        this.workerPool = Executors.newFixedThreadPool(maxThreads);
        this.acceptThread = new Thread(this::acceptLoop, "bio-acceptor");
        this.acceptThread.setDaemon(true);
        this.acceptThread.start();
    }

    public int port() {
        return serverSocket.getLocalPort();
    }

    private void acceptLoop() {
        while (running) {
            try {
                Socket client = serverSocket.accept();
                workerPool.submit(() -> handleClient(client));
            } catch (IOException ignored) {
                if (!running) {
                    return;
                }
            }
        }
    }

    private void handleClient(Socket client) {
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
            // Client đóng kết nối
        }
    }

    @Override
    public void close() throws IOException {
        running = false;
        serverSocket.close();
        workerPool.shutdownNow();
    }
}
