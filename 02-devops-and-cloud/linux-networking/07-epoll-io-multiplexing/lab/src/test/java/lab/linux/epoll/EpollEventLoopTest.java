package lab.linux.epoll;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EpollEventLoopTest {

    @Test
    void singleEventLoopThreadHandlesTwoHundredConcurrentConnections() throws Exception {
        try (NioSelectorEchoServer nio = new NioSelectorEchoServer(0)) {
            List<Socket> clients = new ArrayList<>();
            try {
                for (int i = 0; i < 200; i++) {
                    Socket s = new Socket();
                    s.connect(new InetSocketAddress("127.0.0.1", nio.port()), 1000);
                    s.setSoTimeout(1000);
                    clients.add(s);
                }

                // Kiểm tra kết nối đầu, giữa, cuối đều phản hồi mượt qua duy nhất 1 thread EventLoop
                for (int idx : new int[]{0, 99, 199}) {
                    PrintWriter out = new PrintWriter(
                            new OutputStreamWriter(clients.get(idx).getOutputStream(), StandardCharsets.UTF_8), true);
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(clients.get(idx).getInputStream(), StandardCharsets.UTF_8));
                    out.println("msg-" + idx);
                    assertThat(in.readLine()).isEqualTo("msg-" + idx);
                }
                assertThat(nio.eventLoopThreadCount()).isEqualTo(1);
            } finally {
                for (Socket s : clients) {
                    s.close();
                }
            }
        }
    }

    @Test
    void blockingBioThreadPoolStarvesWhenAllThreadsHoldIdleConnections() throws Exception {
        try (BlockingBioEchoServer bio = new BlockingBioEchoServer(4)) {
            List<Socket> idleClients = new ArrayList<>();
            try {
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
                    out.println("blocked");
                    assertThatThrownBy(in::readLine).isInstanceOf(SocketTimeoutException.class);
                }
            } finally {
                for (Socket s : idleClients) {
                    s.close();
                }
            }
        }
    }

    @Test
    void blockingWorkInsideEventLoopStallsUnrelatedFastClients() throws Exception {
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
                long elapsedMs = (System.nanoTime() - t0) / 1_000_000L;

                assertThat(reply).isEqualTo("FAST");
                assertThat(elapsedMs).isGreaterThanOrEqualTo(180L);
            }
        }
    }
}
