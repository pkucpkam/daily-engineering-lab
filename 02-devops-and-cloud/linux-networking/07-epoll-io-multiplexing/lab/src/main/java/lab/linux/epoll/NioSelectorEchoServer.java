package lab.linux.epoll;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Echo server non-blocking chạy trên ĐÚNG 1 EventLoop thread dùng java.nio.channels.Selector
 * (trên Linux chính là EPollSelectorImpl gọi epoll_create1 / epoll_ctl / epoll_wait).
 */
public final class NioSelectorEchoServer implements AutoCloseable {

    private final Selector selector;
    private final ServerSocketChannel serverChannel;
    private final Thread eventLoopThread;
    private final AtomicInteger activeConnections = new AtomicInteger();
    private volatile boolean running = true;
    private volatile long blockingDelayOnSlowPayloadMs;

    public NioSelectorEchoServer(long blockingDelayOnSlowPayloadMs) throws IOException {
        this.blockingDelayOnSlowPayloadMs = blockingDelayOnSlowPayloadMs;
        this.selector = Selector.open();
        this.serverChannel = ServerSocketChannel.open();
        this.serverChannel.configureBlocking(false);
        this.serverChannel.bind(new InetSocketAddress("127.0.0.1", 0), 1024);
        this.serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        this.eventLoopThread = new Thread(this::runEventLoop, "nio-single-event-loop");
        this.eventLoopThread.setDaemon(true);
        this.eventLoopThread.start();
    }

    public int port() throws IOException {
        return ((InetSocketAddress) serverChannel.getLocalAddress()).getPort();
    }

    public int activeConnections() {
        return activeConnections.get();
    }

    public int eventLoopThreadCount() {
        return 1;
    }

    private void runEventLoop() {
        ByteBuffer readBuffer = ByteBuffer.allocate(4096);
        try {
            while (running) {
                selector.select(50);
                if (!running) {
                    break;
                }
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    // Bắt buộc remove khỏi selectedKeys, tránh lặp vô hạn đốt 100% CPU!
                    it.remove();
                    if (!key.isValid()) {
                        continue;
                    }
                    if (key.isAcceptable()) {
                        acceptNewClient();
                    } else if (key.isReadable()) {
                        echoClient(key, readBuffer);
                    }
                }
            }
        } catch (IOException ignored) {
            if (running) {
                throw new IllegalStateException("Unexpected NIO loop error", ignored);
            }
        } finally {
            closeResourcesOnLoopThread();
        }
    }

    private void closeResourcesOnLoopThread() {
        for (SelectionKey key : selector.keys()) {
            try {
                key.channel().close();
            } catch (IOException ignored) {
            }
        }
        try {
            serverChannel.close();
        } catch (IOException ignored) {
        }
        try {
            selector.close();
        } catch (IOException ignored) {
        }
    }

    private void acceptNewClient() throws IOException {
        SocketChannel client = serverChannel.accept();
        if (client == null) {
            return;
        }
        client.configureBlocking(false);
        client.setOption(java.net.StandardSocketOptions.TCP_NODELAY, true);
        client.register(selector, SelectionKey.OP_READ);
        activeConnections.incrementAndGet();
    }

    private void echoClient(SelectionKey key, ByteBuffer buffer) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        buffer.clear();
        int bytesRead = client.read(buffer);
        if (bytesRead == -1) {
            closeClient(key, client);
            return;
        }
        if (bytesRead == 0) {
            return;
        }

        buffer.flip();
        String payload = StandardCharsets.UTF_8.decode(buffer.duplicate()).toString();
        if (blockingDelayOnSlowPayloadMs > 0 && payload.contains("SLOW")) {
            try {
                // ANTIPATTERN: cố tình block trực tiếp trên EventLoop để đo tác động lên các client khác!
                Thread.sleep(blockingDelayOnSlowPayloadMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        while (buffer.hasRemaining()) {
            client.write(buffer);
        }
    }

    private void closeClient(SelectionKey key, SocketChannel client) throws IOException {
        key.cancel();
        client.close();
        activeConnections.decrementAndGet();
    }

    @Override
    public void close() throws IOException {
        running = false;
        selector.wakeup();
        try {
            eventLoopThread.join(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
