package lab.linux.tcp;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TcpHandshakeLabTest {

    @Test
    void keepAliveConnectionReuseIsMuchFasterThanOpeningNewTcpConnectionPerRequest() throws Exception {
        KeepAliveBenchmark benchmark = new KeepAliveBenchmark();
        KeepAliveBenchmark.BenchmarkResult result = benchmark.runComparison(200);

        assertThat(result.keepAliveReuseMs()).isLessThan(result.newConnectionPerReqMs());
        assertThat(result.speedupRatio()).isGreaterThan(2.0);
    }

    @Test
    void acceptQueueOverflowDropsFinalAckSilentlyWhenAbortOnOverflowDisabled() {
        TcpStateMachine.ServerListener listener = new TcpStateMachine.ServerListener(8, 2, false);

        var c1 = new TcpStateMachine.Endpoint(TcpStateMachine.State.CLOSED);
        var c2 = new TcpStateMachine.Endpoint(TcpStateMachine.State.CLOSED);
        var c3 = new TcpStateMachine.Endpoint(TcpStateMachine.State.CLOSED);

        listener.onSynFromClient(c1);
        assertThat(listener.onFinalAckFromClient(c1)).isTrue();
        listener.onSynFromClient(c2);
        assertThat(listener.onFinalAckFromClient(c2)).isTrue();

        // Kết nối thứ 3 gặp Accept Queue đầy (capacity=2):
        listener.onSynFromClient(c3);
        assertThat(listener.onFinalAckFromClient(c3)).isFalse();
        assertThat(listener.listenOverflows()).isEqualTo(1);
        assertThat(listener.synQueueSize()).isEqualTo(1); // vẫn kẹt ở SYN_RCVD
        assertThat(c3.state()).isEqualTo(TcpStateMachine.State.ESTABLISHED); // client tưởng đã nối xong!
    }

    @Test
    void fourWayTeardownProducesTimeWaitOnActiveSideAndCloseWaitWhenPassiveForgetsClose() {
        TcpStateMachine sm = new TcpStateMachine();
        var active = new TcpStateMachine.Endpoint(TcpStateMachine.State.ESTABLISHED);
        var leakedPassive = new TcpStateMachine.Endpoint(TcpStateMachine.State.ESTABLISHED);

        sm.performActiveClose(active, leakedPassive, false);
        assertThat(active.state()).isEqualTo(TcpStateMachine.State.TIME_WAIT);
        assertThat(leakedPassive.state()).isEqualTo(TcpStateMachine.State.CLOSE_WAIT);
    }
}
