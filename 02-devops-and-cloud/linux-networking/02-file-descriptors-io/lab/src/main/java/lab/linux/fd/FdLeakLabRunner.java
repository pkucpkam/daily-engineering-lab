package lab.linux.fd;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Runner chạy kiểm chứng FD leak (BoundedFdTable + loopback TCP sockets thật) và O_NONBLOCK read.
 */
public final class FdLeakLabRunner {

    public static void main(String[] args) throws Exception {
        System.out.println("=== LNX-02 File Descriptor & I/O Lab ===");

        runBoundedFdExhaustionDemo();
        runRealLoopbackSocketLeakComparison();
        runNonBlockingReadDemo();
    }

    private static void runBoundedFdExhaustionDemo() {
        BoundedFdTable table = new BoundedFdTable(16);
        List<BoundedFdTable.FdHandle> leaked = new ArrayList<>();
        try {
            for (int i = 0; i < 20; i++) {
                leaked.add(table.openSocket("10.20.9.88:8080"));
            }
        } catch (BoundedFdTable.TooManyOpenFilesException ex) {
            System.out.println("[1] Socket leak chạm trần => " + ex.getMessage());
        }
        try {
            table.openFile("/opt/jboss/standalone/log/server.log");
        } catch (BoundedFdTable.TooManyOpenFilesException ex) {
            System.out.println("    Ghi log file sau khi leak socket cũng chết theo => " + ex.getMessage());
        }
        leaked.forEach(BoundedFdTable.FdHandle::close);
        System.out.println("    Sau khi close() tất cả leaked sockets, openCount=" + table.openCount());
    }

    private static void runRealLoopbackSocketLeakComparison() throws IOException {
        try (ServerSocketChannel server = ServerSocketChannel.open()) {
            server.bind(new InetSocketAddress("127.0.0.1", 0));
            int port = ((InetSocketAddress) server.getLocalAddress()).getPort();

            List<SocketChannel> leakedClientSockets = new ArrayList<>();
            List<SocketChannel> leakedServerSockets = new ArrayList<>();
            for (int i = 0; i < 40; i++) {
                SocketChannel c = SocketChannel.open(new InetSocketAddress("127.0.0.1", port));
                SocketChannel s = server.accept();
                leakedClientSockets.add(c);
                leakedServerSockets.add(s);
            }
            System.out.println("[2] Loopback sockets không đóng đang mở giữ nguyên: "
                    + leakedClientSockets.size() + " client + " + leakedServerSockets.size() + " server");

            for (SocketChannel c : leakedClientSockets) c.close();
            for (SocketChannel s : leakedServerSockets) s.close();

            int openAfterLoop = 0;
            for (int i = 0; i < 100; i++) {
                try (SocketChannel c = SocketChannel.open(new InetSocketAddress("127.0.0.1", port));
                     SocketChannel s = server.accept()) {
                    if (c.isOpen() && s.isOpen()) {
                        // hoạt động bình thường
                    }
                }
            }
            System.out.println("    Sau 100 vòng try-with-resources: số socket còn mở sót = " + openAfterLoop);
        }
    }

    private static void runNonBlockingReadDemo() throws IOException {
        NonBlockingIoDemo demo = new NonBlockingIoDemo();
        long t0 = System.nanoTime();
        int bytesRead = demo.readOnceNonBlockingWithoutData();
        long elapsedMicros = (System.nanoTime() - t0) / 1000;
        System.out.printf("[3] Non-blocking SocketChannel.read() khi buffer rỗng => bytesRead=%d trong %d µs%n",
                bytesRead, elapsedMicros);
    }
}
