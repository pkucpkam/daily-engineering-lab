package lab.linux.dns;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * DNS Server UDP thật nói chuẩn wire-format RFC 1035 chạy trên loopback 127.0.0.1.
 * Hỗ trợ record A (type=1), trả NXDOMAIN cho tên không tồn tại, và cho phép đổi IP lúc đang chạy.
 */
public final class MiniUdpDnsServer implements AutoCloseable {

    public static final int TYPE_A = 1;
    public static final int TYPE_AAAA = 28;
    public static final int RCODE_NOERROR = 0;
    public static final int RCODE_NXDOMAIN = 3;

    public record QueryResponse(String qname, int qtype, int rcode, String resolvedIp) {}

    private final DatagramSocket socket;
    private final Map<String, byte[]> aRecords = new ConcurrentHashMap<>();
    private final List<String> queriedNames = new CopyOnWriteArrayList<>();
    private final AtomicInteger queryCount = new AtomicInteger();
    private final int responseTtlSeconds;
    private volatile boolean running = true;

    public MiniUdpDnsServer(int responseTtlSeconds) throws IOException {
        this.responseTtlSeconds = responseTtlSeconds;
        this.socket = new DatagramSocket(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
        Thread worker = new Thread(this::serveLoop, "lnx11-udp-dns-server");
        worker.setDaemon(true);
        worker.start();
    }

    public int port() {
        return socket.getLocalPort();
    }

    public int queryCount() {
        return queryCount.get();
    }

    public void resetStats() {
        queryCount.set(0);
        queriedNames.clear();
    }

    public List<String> queriedNames() {
        return List.copyOf(queriedNames);
    }

    public void putARecord(String domain, String ipv4) throws IOException {
        String normalized = normalize(domain);
        aRecords.put(normalized, InetAddress.getByName(ipv4).getAddress());
    }

    private void serveLoop() {
        byte[] buf = new byte[512];
        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                byte[] reply = handleWireQuery(packet.getData(), packet.getLength());
                socket.send(new DatagramPacket(reply, reply.length, packet.getAddress(), packet.getPort()));
            } catch (IOException ignored) {
                if (!running) {
                    return;
                }
            }
        }
    }

    private byte[] handleWireQuery(byte[] req, int length) throws IOException {
        ByteBuffer bb = ByteBuffer.wrap(req, 0, length);
        int txId = bb.getShort() & 0xFFFF;
        bb.getShort(); // flags
        bb.getShort(); // qdcount
        bb.getShort(); // ancount
        bb.getShort(); // nscount
        bb.getShort(); // arcount

        String qname = readQName(bb);
        int qtype = bb.getShort() & 0xFFFF;
        int qclass = bb.getShort() & 0xFFFF;
        int questionEndOffset = bb.position();

        queryCount.incrementAndGet();
        queriedNames.add(qname + ":" + (qtype == TYPE_AAAA ? "AAAA" : "A"));

        byte[] ipv4 = (qtype == TYPE_A) ? aRecords.get(normalize(qname)) : null;
        boolean knownDomain = aRecords.containsKey(normalize(qname));

        ByteArrayOutputStream out = new ByteArrayOutputStream(128);
        // Header (12 bytes)
        writeShort(out, txId);
        int rcode = knownDomain ? RCODE_NOERROR : RCODE_NXDOMAIN;
        int flags = 0x8180 | (rcode & 0x0F); // QR=1 (response), AA=0, RD=1, RA=1
        writeShort(out, flags);
        writeShort(out, 1); // QDCOUNT = 1
        writeShort(out, ipv4 != null ? 1 : 0); // ANCOUNT
        writeShort(out, 0);
        writeShort(out, 0);

        // Copy nguyên phần Question từ request
        out.write(req, 12, questionEndOffset - 12);

        if (ipv4 != null) {
            writeShort(out, 0xC00C); // Pointer trỏ về offset 12 (QNAME)
            writeShort(out, TYPE_A);
            writeShort(out, qclass);
            writeInt(out, responseTtlSeconds);
            writeShort(out, 4); // RDLENGTH = 4 byte IPv4
            out.write(ipv4);
        }
        return out.toByteArray();
    }

    public static QueryResponse sendUdpQuery(int port, String domain, int qtype) throws IOException {
        String cleanDomain = domain.endsWith(".") ? domain.substring(0, domain.length() - 1) : domain;
        ByteArrayOutputStream req = new ByteArrayOutputStream(64);
        int txId = 0x1234;
        writeShort(req, txId);
        writeShort(req, 0x0100); // RD=1
        writeShort(req, 1); // QDCOUNT
        writeShort(req, 0);
        writeShort(req, 0);
        writeShort(req, 0);

        for (String label : cleanDomain.split("\\.")) {
            byte[] bytes = label.getBytes(StandardCharsets.US_ASCII);
            req.write(bytes.length);
            req.write(bytes);
        }
        req.write(0);
        writeShort(req, qtype);
        writeShort(req, 1); // IN

        byte[] payload = req.toByteArray();
        try (DatagramSocket client = new DatagramSocket()) {
            client.setSoTimeout(2000);
            client.send(new DatagramPacket(payload, payload.length, InetAddress.getLoopbackAddress(), port));
            byte[] respBuf = new byte[512];
            DatagramPacket respPacket = new DatagramPacket(respBuf, respBuf.length);
            client.receive(respPacket);

            ByteBuffer bb = ByteBuffer.wrap(respPacket.getData(), 0, respPacket.getLength());
            bb.getShort(); // txId
            int flags = bb.getShort() & 0xFFFF;
            int rcode = flags & 0x0F;
            bb.getShort(); // qd
            int ancount = bb.getShort() & 0xFFFF;
            bb.getShort();
            bb.getShort();

            String qname = readQName(bb);
            bb.getShort(); // qtype
            bb.getShort(); // qclass

            String ip = null;
            if (ancount > 0) {
                bb.getShort(); // pointer 0xC00C
                int rtype = bb.getShort() & 0xFFFF;
                bb.getShort(); // rclass
                bb.getInt();   // ttl
                int rdlen = bb.getShort() & 0xFFFF;
                if (rtype == TYPE_A && rdlen == 4) {
                    byte[] addr = new byte[4];
                    bb.get(addr);
                    ip = InetAddress.getByAddress(addr).getHostAddress();
                }
            }
            return new QueryResponse(qname, qtype, rcode, ip);
        }
    }

    private static String readQName(ByteBuffer bb) {
        List<String> labels = new ArrayList<>();
        while (bb.hasRemaining()) {
            int len = bb.get() & 0xFF;
            if (len == 0) {
                break;
            }
            byte[] labelBytes = new byte[len];
            bb.get(labelBytes);
            labels.add(new String(labelBytes, StandardCharsets.US_ASCII));
        }
        return String.join(".", labels);
    }

    private static String normalize(String domain) {
        String d = domain.toLowerCase();
        return d.endsWith(".") ? d.substring(0, d.length() - 1) : d;
    }

    private static void writeShort(ByteArrayOutputStream out, int v) {
        out.write((v >>> 8) & 0xFF);
        out.write(v & 0xFF);
    }

    private static void writeInt(ByteArrayOutputStream out, int v) {
        out.write((v >>> 24) & 0xFF);
        out.write((v >>> 16) & 0xFF);
        out.write((v >>> 8) & 0xFF);
        out.write(v & 0xFF);
    }

    @Override
    public void close() {
        running = false;
        socket.close();
    }
}
