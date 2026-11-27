package lab.linux.http;

import java.util.Arrays;
import java.util.Random;

/**
 * Mô phỏng định lượng TCP-level Head-of-Line Blocking còn sót lại ở HTTP/2
 * so với Independent Streams của HTTP/3 (QUIC trên UDP) khi có packet loss.
 */
public final class PacketLossHolSimulator {

    public record PacketLossReport(
            double lossRate,
            int streams,
            int packetsPerStream,
            int rttMs,
            long http2CompletionMs,
            long http2MedianStreamMs,
            long http3CompletionMs,
            long http3MedianStreamMs) {}

    public PacketLossReport simulate(double lossRate, int streams, int packetsPerStream, int rttMs, long seed) {
        Random h2Random = new Random(seed);
        Random h3Random = new Random(seed);

        // HTTP/2: tất cả streams đi chung 1 TCP connection.
        // Ở mỗi lượt truyền RTT, bất kỳ 1 stream nào bị mất gói -> TẤT CẢ các stream bị TCP kernel giữ chờ 1 RTT retransmit!
        long[] h2StreamFinish = new long[streams];
        long h2TimeMs = 0;
        for (int pkt = 1; pkt <= packetsPerStream; pkt++) {
            boolean anyPacketLostInTcp = false;
            for (int s = 0; s < streams; s++) {
                if (h2Random.nextDouble() < lossRate) {
                    anyPacketLostInTcp = true;
                }
            }
            h2TimeMs += rttMs;
            if (anyPacketLostInTcp) {
                // Retransmit + khóa cửa sổ nhận TCP cho toàn bộ streams
                h2TimeMs += rttMs;
            }
            if (pkt == packetsPerStream) {
                Arrays.fill(h2StreamFinish, h2TimeMs);
            }
        }

        // HTTP/3 (QUIC trên UDP): mỗi stream phục hồi mất gói độc lập, không chặn stream anh em!
        long[] h3StreamFinish = new long[streams];
        long h3MaxTimeMs = 0;
        for (int s = 0; s < streams; s++) {
            long streamTime = 0;
            for (int pkt = 1; pkt <= packetsPerStream; pkt++) {
                streamTime += rttMs;
                while (h3Random.nextDouble() < lossRate) {
                    // Chỉ riêng stream s chờ retransmit 1 RTT
                    streamTime += rttMs;
                }
            }
            h3StreamFinish[s] = streamTime;
            if (streamTime > h3MaxTimeMs) {
                h3MaxTimeMs = streamTime;
            }
        }

        Arrays.sort(h3StreamFinish);
        return new PacketLossReport(
                lossRate,
                streams,
                packetsPerStream,
                rttMs,
                h2TimeMs,
                h2StreamFinish[streams / 2],
                h3MaxTimeMs,
                h3StreamFinish[streams / 2]);
    }
}
