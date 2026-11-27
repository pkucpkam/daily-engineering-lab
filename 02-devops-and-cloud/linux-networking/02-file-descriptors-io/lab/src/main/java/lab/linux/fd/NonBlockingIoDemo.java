package lab.linux.fd;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Minh họa sự khác biệt giữa Blocking SocketChannel và Non-blocking SocketChannel (O_NONBLOCK).
 */
public final class NonBlockingIoDemo {

    public int readOnceNonBlockingWithoutData() throws IOException {
        try (ServerSocketChannel server = ServerSocketChannel.open()) {
            server.bind(new InetSocketAddress("127.0.0.1", 0));
            int port = ((InetSocketAddress) server.getLocalAddress()).getPort();

            try (SocketChannel client = SocketChannel.open(new InetSocketAddress("127.0.0.1", port));
                 SocketChannel accepted = server.accept()) {
                // Tương đương fcntl(fd, F_SETFL, O_NONBLOCK) trên Linux
                accepted.configureBlocking(false);
                ByteBuffer buffer = ByteBuffer.allocate(64);
                // Client chưa gửi gì -> trả về 0 ngay tức thì thay vì block thread!
                return accepted.read(buffer);
            }
        }
    }
}
