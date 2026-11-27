package lab.linux.sockets;

/**
 * Runner thực nghiệm cho LNX-09: chạy HTTP Keep-Alive vs No-Pool trên loopback TCP
 * và mô phỏng ephemeral port exhaustion với dải 32768..60999.
 */
public final class SocketLabRunner {

    public static void main(String[] args) throws Exception {
        SocketExhaustionDemo demo = new SocketExhaustionDemo();

        var cmp = demo.compareNoPoolVsKeepAlive(200);
        System.out.println("=== [1] Loopback TCP thật: 200 HTTP requests (No-Pool vs Keep-Alive Pool) ===");
        System.out.printf("No-Pool (Connection: close): acceptedConns=%d | time=%d ms%n",
                cmp.noPoolAcceptedConnections(), cmp.noPoolDurationMs());
        System.out.printf("Keep-Alive Pool (HttpClient): acceptedConns=%d | time=%d ms%n",
                cmp.pooledAcceptedConnections(), cmp.pooledDurationMs());

        var leak = demo.simulateCloseWaitLeak(50, 32);
        System.out.println("\n=== [2] Tái hiện rò rỉ socket khi quên close() sau khi nhận FIN (CLOSE_WAIT leak) ===");
        System.out.printf("Leaked sockets=%d / budget=%d | exhausted=%s%n",
                leak.leakedSocketCount(), leak.poolLimit(), leak.exhaustedLimit());

        EphemeralPortBudgetSimulator sim = new EphemeralPortBudgetSimulator(32768, 60999, 60);
        var burst600 = sim.simulateSingleTarget(600, 90, false, 0);
        var steady400 = sim.simulateSingleTarget(400, 120, false, 0);
        var pooled = sim.simulateSingleTarget(600, 120, true, 16);

        System.out.println("\n=== [3] Mô phỏng Ephemeral Port (32768..60999 = 28,232 ports, TIME_WAIT = 60s) ===");
        System.out.printf("No-Pool 600 req/s (90s) : peakPorts=%d | firstEADDRNOTAVAIL=sec %d | failedReqs=%d%n",
                burst600.peakTimeWaitPorts(), burst600.firstExhaustedSecond(), burst600.exhaustedCount());
        System.out.printf("No-Pool 400 req/s (120s): peakPorts=%d | firstEADDRNOTAVAIL=%d | failedReqs=%d%n",
                steady400.peakTimeWaitPorts(), steady400.firstExhaustedSecond(), steady400.exhaustedCount());
        System.out.printf("Pool-16 600 req/s (120s): activePorts=%d | failedReqs=%d%n",
                pooled.peakTimeWaitPorts(), pooled.exhaustedCount());
    }
}
