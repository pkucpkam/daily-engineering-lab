package lab.linux.tcp.solutions;

import lab.linux.tcp.TcpStateMachine;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lời giải E7: So sánh tcp_abort_on_overflow = 0 (drop ACK âm thầm) vs = 1 (trả RST fail-fast).
 */
class TcpDropAndRetransmitTest {

    @Test
    void abortOnOverflowSendsRstToClientWhenAcceptQueueIsFull() {
        TcpStateMachine.ServerListener listener = new TcpStateMachine.ServerListener(8, 1, true);
        var c1 = new TcpStateMachine.Endpoint(TcpStateMachine.State.CLOSED);
        var c2 = new TcpStateMachine.Endpoint(TcpStateMachine.State.CLOSED);

        listener.onSynFromClient(c1);
        assertThat(listener.onFinalAckFromClient(c1)).isTrue();

        listener.onSynFromClient(c2);
        assertThat(listener.onFinalAckFromClient(c2)).isFalse();
        assertThat(c2.state()).isEqualTo(TcpStateMachine.State.RESET);
        assertThat(listener.listenOverflows()).isEqualTo(1);
    }
}
