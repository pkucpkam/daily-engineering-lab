package lab.linux.pcap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PcapLabTest {

    @Test
    void parsesAndDiagnosesServerResetAndRetransmissionPcapFiles(@TempDir Path tempDir) throws Exception {
        MiniPcapAnalyzer.generateSampleCaptures(tempDir);
        MiniPcapAnalyzer analyzer = new MiniPcapAnalyzer();

        var rstReport = analyzer.analyze(tempDir.resolve("http-server-rst.pcap"));
        assertThat(rstReport.totalPackets()).isEqualTo(5);
        assertThat(rstReport.networkRttMs()).isCloseTo(0.40, org.assertj.core.data.Offset.offset(0.05));
        assertThat(rstReport.rstCount()).isEqualTo(1);
        assertThat(rstReport.diagnosis()).contains("Detected RST");

        var retransReport = analyzer.analyze(tempDir.resolve("retransmission-burst.pcap"));
        assertThat(retransReport.totalPackets()).isEqualTo(10);
        assertThat(retransReport.dupAckCount()).isEqualTo(3);
        assertThat(retransReport.retransmissionCount()).isEqualTo(1);
        assertThat(retransReport.serverProcessingMs()).isGreaterThan(200.0);
    }

    @Test
    void loopbackTcpTriggersRefusedAndResetErrorsCleanly() throws Exception {
        LoopbackTcpScenarioGenerator gen = new LoopbackTcpScenarioGenerator();
        String refused = gen.triggerConnectionRefusedOnClosedPort();
        String reset = gen.triggerAbortiveResetWithSoLingerZero();

        assertThat(refused).isNotBlank();
        assertThat(reset).isNotBlank();
    }
}
