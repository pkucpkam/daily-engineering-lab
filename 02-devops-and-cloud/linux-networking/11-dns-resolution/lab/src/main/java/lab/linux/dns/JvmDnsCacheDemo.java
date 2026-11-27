package lab.linux.dns;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Minh họa cơ chế sun.net.InetAddressCachePolicy của JVM khi gọi truy vấn UDP thật qua MiniUdpDnsServer:
 * - ttlSeconds = -1 (FOREVER, mặc định khi bật SecurityManager): đổi IP trên DNS server nhưng client giữ IP cũ mãi!
 * - ttlSeconds > 0 (vd 1s): tự hết hạn và gửi UDP query lấy IP mới sau khi failover.
 */
public final class JvmDnsCacheDemo {

    public record CacheEntry(String ip, long expiresAtEpochMs) {}

    public static final class CachingDnsClient {
        private final int dnsPort;
        private final int positiveTtlSeconds;
        private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

        public CachingDnsClient(int dnsPort, int positiveTtlSeconds) {
            this.dnsPort = dnsPort;
            this.positiveTtlSeconds = positiveTtlSeconds;
        }

        public String resolveA(String host, long nowEpochMs) throws IOException {
            CacheEntry entry = cache.get(host);
            if (entry != null && (positiveTtlSeconds < 0 || nowEpochMs < entry.expiresAtEpochMs())) {
                return entry.ip();
            }
            var resp = MiniUdpDnsServer.sendUdpQuery(dnsPort, host, MiniUdpDnsServer.TYPE_A);
            if (resp.resolvedIp() == null) {
                throw new IOException("NXDOMAIN for " + host);
            }
            long expiresAt = positiveTtlSeconds < 0
                    ? Long.MAX_VALUE
                    : nowEpochMs + positiveTtlSeconds * 1000L;
            cache.put(host, new CacheEntry(resp.resolvedIp(), expiresAt));
            return resp.resolvedIp();
        }
    }

    public record CacheExperimentResult(
            String beforeFailover,
            String foreverAfterFailover,
            int foreverServerQueries,
            String shortTtlAfterFailover,
            int shortTtlServerQueries) {}

    public CacheExperimentResult runFailoverExperiment() throws Exception {
        String host = "db.prod.internal";
        long t0 = Instant.now().toEpochMilli();

        try (MiniUdpDnsServer server = new MiniUdpDnsServer(2)) {
            server.putARecord(host, "10.10.1.10");

            CachingDnsClient foreverClient = new CachingDnsClient(server.port(), -1);
            String ipBefore = foreverClient.resolveA(host, t0);
            // Failover DNS record sang IP Standby mới!
            server.putARecord(host, "10.10.2.20");
            // 120 giây sau vẫn tra lại từ foreverClient
            String foreverIpAfter = foreverClient.resolveA(host, t0 + 120_000L);
            int foreverQueries = server.queryCount();

            server.resetStats();
            server.putARecord(host, "10.10.1.10");
            CachingDnsClient shortTtlClient = new CachingDnsClient(server.port(), 1);
            shortTtlClient.resolveA(host, t0);
            server.putARecord(host, "10.10.2.20");
            // 2 giây sau failover (> ttl 1s)
            String shortTtlAfter = shortTtlClient.resolveA(host, t0 + 2_000L);
            int shortTtlQueries = server.queryCount();

            return new CacheExperimentResult(ipBefore, foreverIpAfter, foreverQueries, shortTtlAfter, shortTtlQueries);
        }
    }
}
