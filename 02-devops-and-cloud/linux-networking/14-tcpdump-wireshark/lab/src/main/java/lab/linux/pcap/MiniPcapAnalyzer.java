package lab.linux.pcap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Trình tạo và phân tích file .pcap chuẩn định dạng libpcap (Magic 0xA1B2C3D4, Ethernet/IPv4/TCP).
 * File .pcap sinh ra có thể mở trực tiếp bằng Wireshark!
 */
public final class MiniPcapAnalyzer {

    public static final int FLAG_FIN = 0x01;
    public static final int FLAG_SYN = 0x02;
    public static final int FLAG_RST = 0x04;
    public static final int FLAG_PSH = 0x08;
    public static final int FLAG_ACK = 0x10;

    public record PcapPacketSpec(
            long tsSec,
            long tsUsec,
            String srcIp,
            int srcPort,
            String dstIp,
            int dstPort,
            long seq,
            long ack,
            int flags,
            int window,
            byte[] payload) {
        public long timestampMicros() {
            return tsSec * 1_000_000L + tsUsec;
        }
    }

    public record AnalysisReport(
            int totalPackets,
            double networkRttMs,
            double serverProcessingMs,
            int synCount,
            int finCount,
            int rstCount,
            int retransmissionCount,
            int dupAckCount,
            int zeroWindowCount,
            String diagnosis) {}

    public void writePcap(Path file, List<PcapPacketSpec> packets) throws IOException {
        Files.createDirectories(file.toAbsolutePath().getParent());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // 24-byte libpcap Global Header (Little Endian, LinkType = 1 Ethernet)
        ByteBuffer gh = ByteBuffer.allocate(24).order(ByteOrder.LITTLE_ENDIAN);
        gh.putInt(0xA1B2C3D4);
        gh.putShort((short) 2); // major
        gh.putShort((short) 4); // minor
        gh.putInt(0);           // thiszone
        gh.putInt(0);           // sigfigs
        gh.putInt(65535);       // snaplen
        gh.putInt(1);           // LINKTYPE_ETHERNET
        out.write(gh.array());

        for (PcapPacketSpec spec : packets) {
            byte[] frame = buildEthernetIpv4TcpFrame(spec);
            ByteBuffer ph = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);
            ph.putInt((int) spec.tsSec());
            ph.putInt((int) spec.tsUsec());
            ph.putInt(frame.length);
            ph.putInt(frame.length);
            out.write(ph.array());
            out.write(frame);
        }

        Files.write(file, out.toByteArray());
    }

    public List<PcapPacketSpec> readPcap(Path file) throws IOException {
        byte[] bytes = Files.readAllBytes(file);
        ByteBuffer bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        int magic = bb.getInt();
        if (magic != 0xA1B2C3D4) {
            throw new IllegalArgumentException("Unsupported pcap magic: 0x" + Integer.toHexString(magic));
        }
        bb.position(24);

        List<PcapPacketSpec> parsed = new ArrayList<>();
        while (bb.remaining() >= 16) {
            long tsSec = bb.getInt() & 0xFFFFFFFFL;
            long tsUsec = bb.getInt() & 0xFFFFFFFFL;
            int inclLen = bb.getInt();
            bb.getInt(); // origLen

            byte[] frame = new byte[inclLen];
            bb.get(frame);

            // Ethernet (14 bytes) -> IPv4 (20+ bytes) -> TCP (20+ bytes)
            ByteBuffer net = ByteBuffer.wrap(frame).order(ByteOrder.BIG_ENDIAN);
            net.position(14);
            int ihl = (net.get(14) & 0x0F) * 4;
            byte[] srcAddr = new byte[4];
            byte[] dstAddr = new byte[4];
            net.position(14 + 12);
            net.get(srcAddr);
            net.get(dstAddr);

            int tcpStart = 14 + ihl;
            net.position(tcpStart);
            int srcPort = net.getShort() & 0xFFFF;
            int dstPort = net.getShort() & 0xFFFF;
            long seq = net.getInt() & 0xFFFFFFFFL;
            long ack = net.getInt() & 0xFFFFFFFFL;
            int dataOffsetBytes = ((net.get() & 0xF0) >>> 4) * 4;
            int flags = net.get() & 0xFF;
            int win = net.getShort() & 0xFFFF;

            int payloadStart = tcpStart + dataOffsetBytes;
            byte[] payload = new byte[Math.max(0, frame.length - payloadStart)];
            if (payload.length > 0) {
                System.arraycopy(frame, payloadStart, payload, 0, payload.length);
            }

            parsed.add(new PcapPacketSpec(
                    tsSec,
                    tsUsec,
                    InetAddress.getByAddress(srcAddr).getHostAddress(),
                    srcPort,
                    InetAddress.getByAddress(dstAddr).getHostAddress(),
                    dstPort,
                    seq,
                    ack,
                    flags,
                    win,
                    payload));
        }
        return parsed;
    }

    public AnalysisReport analyze(Path file) throws IOException {
        List<PcapPacketSpec> pkts = readPcap(file);
        long synTs = -1;
        long synAckTs = -1;
        long reqTs = -1;
        long respTs = -1;
        int synCount = 0;
        int finCount = 0;
        int rstCount = 0;
        int retransCount = 0;
        int dupAckCount = 0;
        int zeroWinCount = 0;

        Set<String> seenDataSegments = new HashSet<>();
        Map<String, Long> lastAckByFlow = new HashMap<>();
        String firstRstSource = null;

        for (PcapPacketSpec p : pkts) {
            boolean isSyn = (p.flags() & FLAG_SYN) != 0;
            boolean isAck = (p.flags() & FLAG_ACK) != 0;
            boolean isFin = (p.flags() & FLAG_FIN) != 0;
            boolean isRst = (p.flags() & FLAG_RST) != 0;

            if (isSyn && !isAck) {
                synCount++;
                if (synTs == -1) {
                    synTs = p.timestampMicros();
                }
            } else if (isSyn && isAck) {
                if (synAckTs == -1) {
                    synAckTs = p.timestampMicros();
                }
            }
            if (isFin) {
                finCount++;
            }
            if (isRst) {
                rstCount++;
                if (firstRstSource == null) {
                    firstRstSource = p.srcIp() + ":" + p.srcPort();
                }
            }
            if (p.window() == 0 && !isRst) {
                zeroWinCount++;
            }

            String flowKey = p.srcIp() + ":" + p.srcPort() + "->" + p.dstIp() + ":" + p.dstPort();
            if (p.payload().length > 0) {
                if (reqTs == -1) {
                    reqTs = p.timestampMicros();
                } else if (respTs == -1 && !pkts.isEmpty() && !p.srcIp().equals(pkts.get(0).srcIp())) {
                    respTs = p.timestampMicros();
                }
                String segKey = flowKey + "|seq=" + p.seq() + "|len=" + p.payload().length;
                if (!seenDataSegments.add(segKey)) {
                    retransCount++;
                }
            } else if (isAck && !isSyn && !isFin && !isRst) {
                Long prevAck = lastAckByFlow.put(flowKey, p.ack());
                if (prevAck != null && prevAck == p.ack()) {
                    dupAckCount++;
                }
            }
        }

        double rttMs = (synTs >= 0 && synAckTs >= synTs) ? (synAckTs - synTs) / 1000.0 : 0.0;
        double serverMs = (reqTs >= 0 && respTs >= reqTs) ? (respTs - reqTs) / 1000.0 : 0.0;

        String diag;
        if (rstCount > 0 && reqTs >= 0) {
            diag = "Detected RST from " + firstRstSource + " after request (SO_LINGER=0 / crash / closed socket)";
        } else if (rstCount > 0) {
            diag = "Detected immediate RST from " + firstRstSource + " (port closed or firewall reject)";
        } else if (retransCount > 0) {
            diag = "Detected " + retransCount + " TCP Retransmission(s) and " + dupAckCount + " DupACK(s)";
        } else {
            diag = "Clean TCP flow";
        }

        return new AnalysisReport(
                pkts.size(), rttMs, serverMs, synCount, finCount, rstCount, retransCount, dupAckCount, zeroWinCount, diag);
    }

    public static void generateSampleCaptures(Path dir) throws IOException {
        MiniPcapAnalyzer analyzer = new MiniPcapAnalyzer();
        String clientIp = "10.10.5.9";
        String serverIp = "10.20.1.15";
        int cPort = 51422;
        int sPort = 8080;
        long baseSec = 1_764_230_400L;

        byte[] httpGet = "GET /api/pay HTTP/1.1\r\nHost: jboss\r\n\r\n".getBytes(StandardCharsets.US_ASCII);
        byte[] httpOk = "HTTP/1.1 200 OK\r\nContent-Length: 2\r\n\r\nOK".getBytes(StandardCharsets.US_ASCII);

        // 1. Mẫu Server bắn RST giữa kết nối sau HTTP Request
        List<PcapPacketSpec> rstTrace = List.of(
                new PcapPacketSpec(baseSec, 100, clientIp, cPort, serverIp, sPort, 1000, 0, FLAG_SYN, 64240, new byte[0]),
                new PcapPacketSpec(baseSec, 500, serverIp, sPort, clientIp, cPort, 5000, 1001, FLAG_SYN | FLAG_ACK, 65160, new byte[0]),
                new PcapPacketSpec(baseSec, 650, clientIp, cPort, serverIp, sPort, 1001, 5001, FLAG_ACK, 64240, new byte[0]),
                new PcapPacketSpec(baseSec, 800, clientIp, cPort, serverIp, sPort, 1001, 5001, FLAG_PSH | FLAG_ACK, 64240, httpGet),
                new PcapPacketSpec(baseSec, 18_800, serverIp, sPort, clientIp, cPort, 5001, 1001 + httpGet.length, FLAG_RST | FLAG_ACK, 0, new byte[0])
        );
        analyzer.writePcap(dir.resolve("http-server-rst.pcap"), rstTrace);

        // 2. Mẫu TCP Retransmission + 3 DupACKs
        byte[] chunk = "DATA_SEGMENT_PAYLOAD_BLOCK".getBytes(StandardCharsets.US_ASCII);
        List<PcapPacketSpec> retransTrace = List.of(
                new PcapPacketSpec(baseSec, 100, clientIp, cPort, serverIp, sPort, 2000, 0, FLAG_SYN, 64240, new byte[0]),
                new PcapPacketSpec(baseSec, 600, serverIp, sPort, clientIp, cPort, 6000, 2001, FLAG_SYN | FLAG_ACK, 65160, new byte[0]),
                new PcapPacketSpec(baseSec, 750, clientIp, cPort, serverIp, sPort, 2001, 6001, FLAG_ACK, 64240, new byte[0]),
                new PcapPacketSpec(baseSec, 1000, clientIp, cPort, serverIp, sPort, 2001, 6001, FLAG_PSH | FLAG_ACK, 64240, chunk),
                new PcapPacketSpec(baseSec, 2000, serverIp, sPort, clientIp, cPort, 6001, 2001, FLAG_ACK, 65160, new byte[0]),
                new PcapPacketSpec(baseSec, 2500, serverIp, sPort, clientIp, cPort, 6001, 2001, FLAG_ACK, 65160, new byte[0]),
                new PcapPacketSpec(baseSec, 3000, serverIp, sPort, clientIp, cPort, 6001, 2001, FLAG_ACK, 65160, new byte[0]),
                new PcapPacketSpec(baseSec, 3500, serverIp, sPort, clientIp, cPort, 6001, 2001, FLAG_ACK, 65160, new byte[0]),
                // Retransmission cùng seq=2001 sau +205ms
                new PcapPacketSpec(baseSec, 206_000, clientIp, cPort, serverIp, sPort, 2001, 6001, FLAG_PSH | FLAG_ACK, 64240, chunk),
                new PcapPacketSpec(baseSec, 210_000, serverIp, sPort, clientIp, cPort, 6001, 2001 + chunk.length, FLAG_PSH | FLAG_ACK, 65160, httpOk)
        );
        analyzer.writePcap(dir.resolve("retransmission-burst.pcap"), retransTrace);
    }

    private static byte[] buildEthernetIpv4TcpFrame(PcapPacketSpec spec) throws IOException {
        int totalLen = 14 + 20 + 20 + spec.payload().length;
        ByteBuffer buf = ByteBuffer.allocate(totalLen).order(ByteOrder.BIG_ENDIAN);

        // Ethernet header (14 bytes)
        buf.put(new byte[]{0x02, 0x00, 0x00, 0x00, 0x00, 0x02});
        buf.put(new byte[]{0x02, 0x00, 0x00, 0x00, 0x00, 0x01});
        buf.putShort((short) 0x0800); // IPv4

        // IPv4 header (20 bytes)
        buf.put((byte) 0x45); // Version=4, IHL=5 (20 bytes)
        buf.put((byte) 0x00);
        buf.putShort((short) (20 + 20 + spec.payload().length));
        buf.putShort((short) 1);
        buf.putShort((short) 0x4000); // DF
        buf.put((byte) 64);   // TTL
        buf.put((byte) 6);    // Protocol = TCP
        buf.putShort((short) 0); // Checksum placeholder
        buf.put(InetAddress.getByName(spec.srcIp()).getAddress());
        buf.put(InetAddress.getByName(spec.dstIp()).getAddress());

        // TCP header (20 bytes)
        buf.putShort((short) spec.srcPort());
        buf.putShort((short) spec.dstPort());
        buf.putInt((int) spec.seq());
        buf.putInt((int) spec.ack());
        buf.put((byte) 0x50); // Data offset = 5 words (20 bytes)
        buf.put((byte) (spec.flags() & 0xFF));
        buf.putShort((short) (spec.window() & 0xFFFF));
        buf.putShort((short) 0); // TCP Checksum placeholder
        buf.putShort((short) 0); // Urgent pointer
        buf.put(spec.payload());
        return buf.array();
    }
}
