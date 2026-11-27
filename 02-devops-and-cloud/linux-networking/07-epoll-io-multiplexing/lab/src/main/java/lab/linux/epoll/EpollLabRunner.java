package lab.linux.epoll;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Runner chạy thực tế trên loopback TCP:
 * 1) 200 concurrent connections trên NIO Selector dùng ĐÚNG 1 EventLoop thread.
 * 2) So sánh BIO pool=4 khi 4 client giữ idle (client #5 bị treo) vs NIO Selector.
 * 3) Minh họa hậu quả block EventLoop thread lên latency các client vô tội.
 */
public final class EpollLabRunner {

    public static void main(String[] args) throws Exception {
        System.out.println("=== LNX-07 select, poll, epoll & Event Loop Lab ===");
        System.out.println("JDK SelectorProvider class: "
                + java.nio.channels.spi.SelectorProvider.provider().getClass().getName());

        runNio200ConnectionsDemo();
        runBioStarvationVsNioDemo();
        runEventLoopBlockingDemo();
    }

    private static void runNio200ConnectionsDemo() throws Exception {
        try (NioSelectorEchoServer nio = new NioSelectorEchoServer(0)) {
            List<Socket> clients = new ArrayList<>();
            long t0 = System.nanoTime();
            for (int i = 0; i < 200; i++) {
                Socket s = new Socket();
                s.connect(new InetSocketAddress("127.0.0.1", nio.port()), 1000);
                s.setSoTimeout(1000);
                clients.add(s);
            }
            PrintWriter out = new PrintWriter(
                    new OutputStreamWriter(clients.get(199).getOutputStream(), StandardCharsets.UTF_8), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(clients.get(199).getInputStream(), StandardCharsets.UTF_8));
            out.println("ping-from-client-200");
            String reply = in.readLine();
            long elapsedMs = (System.nanoTime() - t0) / 1_000_000L;
            System.out.printf("[1] NIO Selector (threads=%d): 200 concurrent open, #200 echoed '%s' in %d ms%n",
                    nio.eventLoopThreadCount(), reply, elapsedMs);
            for (Socket s : clients) {
                s.close();
            }
        }
    }

    private static void runBioStarvationVsNioDemo() throws Exception {
        try (BlockingBioEchoServer bio = new BlockingBioEchoServer(4)) {
            List<Socket> idleClients = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                Socket s = new Socket("127.0.0.1", bio.port());
                idleClients.add(s);
            }
            try (Socket fifth = new Socket()) {
                fifth.connect(new InetSocketAddress("127.0.0.1", bio.port()), 500);
                fifth.setSoTimeout(200);
                PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(fifth.getOutputStream(), StandardCharsets.UTF_8), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(fifth.getInputStream(), StandardCharsets.UTF_8));
                out.println("hello-5th");
                try {
                    in.readLine();
                    System.out.println("[2] BIO client #5 unexpected success");
                } catch (Exception timeout) {
                    System.out.println("[2] BIO pool=4 khi 4 client idle -> client #5 bị treo: "
                            + timeout.getClass().getSimpleName());
                }
            }
            for (Socket s : idleClients) {
                s.close();
            }
        }
    }

    private static void runEventLoopBlockingDemo() throws Exception {
        try (NioSelectorEchoServer nio = new NioSelectorEchoServer(250)) {
            try (Socket slow = new Socket("127.0.0.1", nio.port());
                 Socket fast = new Socket("127.0.0.1", nio.port())) {
                slow.setSoTimeout(2000);
                fast.setSoTimeout(2000);

                PrintWriter slowOut = new PrintWriter(
                        new OutputStreamWriter(slow.getOutputStream(), StandardCharsets.UTF_8), true);
                PrintWriter fastOut = new PrintWriter(
                        new OutputStreamWriter(fast.getOutputStream(), StandardCharsets.UTF_8), true);
                BufferedReader fastIn = new BufferedReader(
                        new InputStreamReader(fast.getInputStream(), StandardCharsets.UTF_8));

                slowOut.println("SLOW");
                Thread.sleep(20);
                long t0 = System.nanoTime();
                fastOut.println("FAST");
                String reply = fastIn.readLine();
                long latencyMs = (System.nanoTime() - t0) / 1_000_000L;
                System.out.printf("[3] Block EventLoop 250ms -> client 'FAST' vô tội phải chờ %d ms (reply=%s)%n",
                        latencyMs, reply);
            }
        }
    }
}
