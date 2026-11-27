package lab.linux.pcap.solutions;

import lab.linux.pcap.MiniPcapAnalyzer;
import lab.linux.pcap.MiniPcapAnalyzer.PcapPacketSpec;

import java.util.List;

/**
 * Lời giải tham khảo E7 (LNX-14):
 * Phát hiện các gói TCP ZeroWindow (Window Size == 0 không đi kèm RST)
 * để nhận diện tình huống ứng dụng bên nhận đọc buffer không kịp (Full GC / Thread hang).
 */
public final class ZeroWindowDetectorSolution {

    public static List<PcapPacketSpec> findZeroWindowPackets(List<PcapPacketSpec> packets) {
        return packets.stream()
                .filter(p -> p.window() == 0 && (p.flags() & MiniPcapAnalyzer.FLAG_RST) == 0)
                .toList();
    }

    public static int decodeTcpWindowSize(byte[] tcpHeaderBytes) {
        if (tcpHeaderBytes.length < 16) {
            throw new IllegalArgumentException("TCP header must be at least 20 bytes");
        }
        return ((tcpHeaderBytes[14] & 0xFF) << 8) | (tcpHeaderBytes[15] & 0xFF);
    }
}
