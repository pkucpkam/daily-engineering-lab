package lab.linux.pcap;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Runner thực nghiệm LNX-14: tái hiện RST thật trên loopback + sinh & phân tích .pcap chuẩn Wireshark.
 */
public final class PcapLabRunner {

    public static void main(String[] args) throws Exception {
        LoopbackTcpScenarioGenerator loopback = new LoopbackTcpScenarioGenerator();
        System.out.println("=== [1] Loopback TCP thật: Tái hiện gói RST ===");
        System.out.println("Port đóng (SYN -> RST+ACK): " + loopback.triggerConnectionRefusedOnClosedPort());
        System.out.println("Server SO_LINGER=0 (RST)  : " + loopback.triggerAbortiveResetWithSoLingerZero());

        Path pcapDir = Path.of("target", "generated-pcaps");
        Files.createDirectories(pcapDir);
        MiniPcapAnalyzer.generateSampleCaptures(pcapDir);

        MiniPcapAnalyzer analyzer = new MiniPcapAnalyzer();
        System.out.println("\n=== [2] Phân tích file .pcap chuẩn libpcap (mở được bằng Wireshark) ===");
        for (String name : new String[]{"http-server-rst.pcap", "retransmission-burst.pcap"}) {
            var rep = analyzer.analyze(pcapDir.resolve(name));
            System.out.printf("%s -> pkts=%d | rtt=%.2f ms | serverTime=%.2f ms | syn=%d | rst=%d | dupAck=%d | retrans=%d%n  => Diagnosis: %s%n",
                    name, rep.totalPackets(), rep.networkRttMs(), rep.serverProcessingMs(),
                    rep.synCount(), rep.rstCount(), rep.dupAckCount(), rep.retransmissionCount(), rep.diagnosis());
        }
    }
}
