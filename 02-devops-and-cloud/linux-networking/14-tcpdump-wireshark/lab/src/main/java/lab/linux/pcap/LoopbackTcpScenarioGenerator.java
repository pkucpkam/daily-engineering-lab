package lab.linux.pcap;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Tái hiện gói TCP RST thật trên Windows/Linux loopback socket:
 * 1) Gọi tới port đang đóng -> kernel bắn RST ngay sau SYN (ConnectException: Connection refused).
 * 2) Server gọi socket.setSoLinger(true, 0) + close() -> bắn TCP RST thật (SocketException: Connection reset).
 */
public final class LoopbackTcpScenarioGenerator {

    public String triggerConnectionRefusedOnClosedPort() throws IOException {
        int unusedPort;
        try (ServerSocket tmp = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            unusedPort = tmp.getLocalPort();
        }
        try (Socket client = new Socket()) {
            client.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), unusedPort), 1000);
            return "UNEXPECTED_CONNECTED";
        } catch (ConnectException expected) {
            return expected.getMessage();
        }
    }

    public String triggerAbortiveResetWithSoLingerZero() throws Exception {
        CountDownLatch serverReady = new CountDownLatch(1);
        CountDownLatch clientConnected = new CountDownLatch(1);
        try (ServerSocket server = new ServerSocket(0, 16, InetAddress.getLoopbackAddress())) {
            Thread serverThread = new Thread(() -> {
                serverReady.countDown();
                try (Socket accepted = server.accept()) {
                    clientConnected.await(2, TimeUnit.SECONDS);
                    // Bật SO_LINGER = 0 để khi close() kernel gửi thẳng TCP RST thay vì 4-way FIN!
                    accepted.setSoLinger(true, 0);
                } catch (Exception ignored) {
                }
            }, "lnx14-rst-server");
            serverThread.setDaemon(true);
            serverThread.start();
            serverReady.await(2, TimeUnit.SECONDS);

            try (Socket client = new Socket()) {
                client.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), server.getLocalPort()), 1000);
                client.setSoTimeout(2000);
                client.getOutputStream().write("HELLO\n".getBytes(StandardCharsets.US_ASCII));
                client.getOutputStream().flush();
                clientConnected.countDown();
                serverThread.join(2000);

                // Đọc từ socket đã nhận RST -> OS ném Connection reset (hoặc EOF tùy thời điểm OS)
                int b = client.getInputStream().read();
                return b == -1 ? "EOF_OR_RESET" : "READ_BYTE=" + b;
            } catch (SocketException expectedReset) {
                return expectedReset.getClass().getSimpleName() + ": " + expectedReset.getMessage();
            }
        }
    }
}
