package lab.linux.sockets;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SocketExhaustionLabTest {

    @Test
    void keepAlivePoolReusesFewConnectionsForManyHttpRequests() throws Exception {
        SocketExhaustionDemo demo = new SocketExhaustionDemo();
        var result = demo.compareNoPoolVsKeepAlive(100);

        assertThat(result.noPoolAcceptedConnections()).isEqualTo(100);
        assertThat(result.pooledAcceptedConnections()).isLessThanOrEqualTo(3);
    }

    @Test
    void unclosedSocketsAfterServerFinExhaustClientBudget() throws Exception {
        SocketExhaustionDemo demo = new SocketExhaustionDemo();
        var leak = demo.simulateCloseWaitLeak(40, 20);

        assertThat(leak.exhaustedLimit()).isTrue();
        assertThat(leak.leakedSocketCount()).isEqualTo(20);
    }

    @Test
    void sixHundredNewConnectionsPerSecondExhaustDefaultLinuxEphemeralPortsAtSecond48() {
        EphemeralPortBudgetSimulator sim = new EphemeralPortBudgetSimulator(32768, 60999, 60);
        var report = sim.simulateSingleTarget(600, 90, false, 0);

        assertThat(report.totalPortsInRange()).isEqualTo(28_232);
        assertThat(report.firstExhaustedSecond()).isEqualTo(48);
        assertThat(report.exhaustedCount()).isGreaterThan(0);
    }

    @Test
    void connectionPoolAvoidsEphemeralPortExhaustionEvenUnderHighRps() {
        EphemeralPortBudgetSimulator sim = new EphemeralPortBudgetSimulator(32768, 60999, 60);
        var steady = sim.simulateSingleTarget(400, 120, false, 0);
        var pooled = sim.simulateSingleTarget(2000, 120, true, 16);

        assertThat(steady.exhaustedCount()).isZero();
        assertThat(steady.peakTimeWaitPorts()).isEqualTo(24_000);
        assertThat(pooled.exhaustedCount()).isZero();
        assertThat(pooled.peakTimeWaitPorts()).isEqualTo(16);
    }
}
