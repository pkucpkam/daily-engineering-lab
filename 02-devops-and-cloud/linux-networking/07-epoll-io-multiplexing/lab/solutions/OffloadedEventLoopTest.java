package lab.linux.epoll.solutions;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lời giải E7: Offload tác vụ chậm ra Worker Pool + selector.wakeup(),
 * giúp EventLoop giữ latency cho các client nhanh luôn < 50ms dù có client chậm 250ms!
 */
class OffloadedEventLoopTest {

    static final class OffloadingNioServer implements AutoCloseable {
        private record PendingWrite(SocketChannel channel, ByteBuffer buffer) {}

        private final Selector selector;
        private final ServerSocketChannel server;
        private final ExecutorService blockingWorkers = Executors.newFixedThreadPool(4);
        private final Queue<PendingWrite> writeQueue = new ConcurrentLinkedQueue<>();
        private final Thread loopThread;
        private volatile boolean running = true;

        OffloadingNioServer() throws IOException {
            this.selector = Selector.open();
            this.server = ServerSocketChannel.open();
            this.server.configureBlocking(false);
            this.server.bind(new InetSocketAddress("127.0.0.1", 0), 128);
            this.server.register(selector, SelectionKey.OP_ACCEPT);

            this.loopThread = new Thread(this::loop, "offloaded-event-loop");
            this.loopThread.setDaemon(true);
            this.loopThread.start();
        }

        int port() throws IOException {
            return ((InetSocketAddress) server.getLocalAddress()).getPort();
        }

        private void loop() {
            ByteBuffer buf = ByteBuffer.allocate(1024);
            try {
                while (running) {
                    selector.select(50);
                    if (!running) {
                        break;
                    }
                    PendingWrite pw;
                    while ((pw = writeQueue.poll()) != null) {
                        if (pw.channel().isOpen()) {
                            while (pw.buffer().hasRemaining()) {
                                pw.channel().write(pw.buffer());
                            }
                        }
                    }
                    Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                    while (it.hasNext()) {
                        SelectionKey key = it.next();
                        it.remove();
                        if (!key.isValid()) continue;
                        if (key.isAcceptable()) {
                            SocketChannel ch = server.accept();
                            if (ch != null) {
                                ch.configureBlocking(false);
                                ch.register(selector, SelectionKey.OP_READ);
                            }
                        } else if (key.isReadable()) {
                            SocketChannel ch = (SocketChannel) key.channel();
                            buf.clear();
                            int n = ch.read(buf);
                            if (n <= 0) {
                                if (n == -1) ch.close();
                                continue;
                            }
                            buf.flip();
                            byte[] copy = new byte[buf.remaining()];
                            buf.get(copy);
                            String msg = new String(copy, StandardCharsets.UTF_8);
                            if (msg.contains("SLOW")) {
                                blockingWorkers.submit(() -> {
                                    try {
                                        Thread.sleep(250);
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                    }
                                    writeQueue.add(new PendingWrite(ch, ByteBuffer.wrap(copy)));
                                    selector.wakeup();
                                });
                            } else {
                                writeQueue.add(new PendingWrite(ch, ByteBuffer.wrap(copy)));
                                selector.wakeup();
                            }
                        }
                    }
                }
            } catch (IOException ignored) {
            } finally {
                for (SelectionKey key : selector.keys()) {
                    try {
                        key.channel().close();
                    } catch (IOException ignored) {
                    }
                }
                try {
                    server.close();
                } catch (IOException ignored) {
                }
                try {
                    selector.close();
                } catch (IOException ignored) {
                }
            }
        }

        @Override
        public void close() throws IOException {
            running = false;
            selector.wakeup();
            try {
                loopThread.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            blockingWorkers.shutdownNow();
        }
    }

    @Test
    void offloadingSlowTaskKeepsFastClientUnblocked() throws Exception {
        try (OffloadingNioServer srv = new OffloadingNioServer();
             Socket slow = new Socket("127.0.0.1", srv.port());
             Socket fast = new Socket("127.0.0.1", srv.port())) {
            slow.setSoTimeout(2000);
            fast.setSoTimeout(2000);

            PrintWriter slowOut = new PrintWriter(
                    new OutputStreamWriter(slow.getOutputStream(), StandardCharsets.UTF_8), true);
            BufferedReader slowIn = new BufferedReader(
                    new InputStreamReader(slow.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter fastOut = new PrintWriter(
                    new OutputStreamWriter(fast.getOutputStream(), StandardCharsets.UTF_8), true);
            BufferedReader fastIn = new BufferedReader(
                    new InputStreamReader(fast.getInputStream(), StandardCharsets.UTF_8));

            slowOut.println("SLOW");
            Thread.sleep(20);
            long t0 = System.nanoTime();
            fastOut.println("FAST");
            assertThat(fastIn.readLine()).isEqualTo("FAST");
            long fastLatencyMs = (System.nanoTime() - t0) / 1_000_000L;

            assertThat(fastLatencyMs).isLessThan(100L);
            assertThat(slowIn.readLine()).isEqualTo("SLOW");
        }
    }
}
