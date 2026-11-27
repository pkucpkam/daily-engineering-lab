package lab.linux.http;

/**
 * Runner chạy thực nghiệm cho LNX-10.
 */
public final class HttpVersionsRunner {

    public static void main(String[] args) throws Exception {
        HttpMultiplexingComparison cmp = new HttpMultiplexingComparison();
        System.out.println("=== [1] Loopback TCP: 1 request SLOW (200 ms) + 10 requests FAST (5 ms) ===");
        for (var r : cmp.runAll(10, 200, 5)) {
            System.out.printf("%-32s | tcpConns=%2d | totalWall=%4d ms | medianFast=%4d ms | slow=%4d ms%n",
                    r.modeName(), r.tcpConnectionsOpened(), r.totalWallTimeMs(), r.medianFastRequestMs(), r.slowRequestMs());
        }

        PacketLossHolSimulator sim = new PacketLossHolSimulator();
        System.out.println("\n=== [2] Mô phỏng TCP HOL Blocking: 20 streams x 10 packets (RTT = 50 ms) ===");
        for (double loss : new double[]{0.0, 0.03, 0.05}) {
            var rep = sim.simulate(loss, 20, 10, 50, 42L);
            System.out.printf("Loss=%4.1f%% | HTTP/2 (1 TCP): total=%4d ms, median=%4d ms | HTTP/3 (QUIC): total=%4d ms, median=%4d ms%n",
                    loss * 100.0,
                    rep.http2CompletionMs(),
                    rep.http2MedianStreamMs(),
                    rep.http3CompletionMs(),
                    rep.http3MedianStreamMs());
        }
    }
}
