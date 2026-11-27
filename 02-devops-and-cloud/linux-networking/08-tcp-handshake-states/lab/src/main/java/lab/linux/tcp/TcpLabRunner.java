package lab.linux.tcp;

/**
 * Runner chạy benchmark Keep-Alive vs New TCP Connection + kiểm chứng State Machine & Accept Queue overflow.
 */
public final class TcpLabRunner {

    public static void main(String[] args) throws Exception {
        System.out.println("=== LNX-08 TCP Handshake, State Machine & Backlog Lab ===");

        KeepAliveBenchmark benchmark = new KeepAliveBenchmark();
        KeepAliveBenchmark.BenchmarkResult result = benchmark.runComparison(200);
        System.out.printf("[1] 200 requests loopback TCP: NewConn=%d ms | KeepAlive=%d ms (speedup ~%.1fx)%n",
                result.newConnectionPerReqMs(), result.keepAliveReuseMs(), result.speedupRatio());

        TcpStateMachine sm = new TcpStateMachine();
        TcpStateMachine.ServerListener listener = new TcpStateMachine.ServerListener(8, 2, false);
        for (int i = 0; i < 3; i++) {
            var client = new TcpStateMachine.Endpoint(TcpStateMachine.State.CLOSED);
            listener.onSynFromClient(client);
            boolean queued = listener.onFinalAckFromClient(client);
            System.out.printf("[2] Client #%d -> clientState=%s, enteredAcceptQueue=%s, overflows=%d%n",
                    i + 1, client.state(), queued, listener.listenOverflows());
        }

        var cActive = new TcpStateMachine.Endpoint(TcpStateMachine.State.ESTABLISHED);
        var sPassive = new TcpStateMachine.Endpoint(TcpStateMachine.State.ESTABLISHED);
        sm.performActiveClose(cActive, sPassive, false);
        System.out.printf("[3] 4-way teardown khi server quên close(): active=%s, passive=%s%n",
                cActive.state(), sPassive.state());
    }
}
