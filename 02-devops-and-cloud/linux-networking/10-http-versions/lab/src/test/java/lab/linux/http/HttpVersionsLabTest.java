package lab.linux.http;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HttpVersionsLabTest {

    @Test
    void multiplexedSingleTcpSocketUnblocksFastRequestsBehindSlowRequest() throws Exception {
        HttpMultiplexingComparison cmp = new HttpMultiplexingComparison();
        var results = cmp.runAll(10, 200, 5);

        var h1Single = results.get(0);
        var h1Pool = results.get(1);
        var h2Mux = results.get(2);

        assertThat(h1Single.tcpConnectionsOpened()).isEqualTo(1);
        assertThat(h1Single.medianFastRequestMs()).isGreaterThanOrEqualTo(180);

        assertThat(h1Pool.tcpConnectionsOpened()).isEqualTo(6);
        assertThat(h1Pool.medianFastRequestMs()).isLessThan(100);

        // Điểm mấu chốt của HTTP/2: vẫn chỉ 1 TCP connection nhưng medianFast thoát hoàn toàn khỏi request 200ms!
        assertThat(h2Mux.tcpConnectionsOpened()).isEqualTo(1);
        assertThat(h2Mux.medianFastRequestMs()).isLessThan(100);
        assertThat(h2Mux.medianFastRequestMs()).isLessThan(h1Single.medianFastRequestMs());
    }

    @Test
    void http3OutperformsHttp2OnPacketLossBecauseTcpHolBlocksAllStreams() {
        PacketLossHolSimulator sim = new PacketLossHolSimulator();
        var zeroLoss = sim.simulate(0.0, 20, 10, 50, 42L);
        var threePctLoss = sim.simulate(0.03, 20, 10, 50, 42L);

        assertThat(zeroLoss.http2CompletionMs()).isEqualTo(500);
        assertThat(zeroLoss.http3CompletionMs()).isEqualTo(500);

        assertThat(threePctLoss.http3MedianStreamMs()).isLessThan(threePctLoss.http2MedianStreamMs());
        assertThat(threePctLoss.http3CompletionMs()).isLessThan(threePctLoss.http2CompletionMs());
    }
}
