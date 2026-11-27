package lab.linux.dns;

import java.security.Security;
import java.util.List;

/**
 * Runner thực nghiệm cho LNX-11 trên loopback UDP DNS server thật.
 */
public final class DnsLabRunner {

    public static void main(String[] args) throws Exception {
        String configuredTtl = Security.getProperty("networkaddress.cache.ttl");
        String negativeTtl = Security.getProperty("networkaddress.cache.negative.ttl");
        System.out.println("=== [1] Giá trị Security Property DNS hiện tại trên JDK đang chạy ===");
        System.out.printf("networkaddress.cache.ttl=%s | networkaddress.cache.negative.ttl=%s%n",
                configuredTtl, negativeTtl);

        JvmDnsCacheDemo cacheDemo = new JvmDnsCacheDemo();
        var cacheRes = cacheDemo.runFailoverExperiment();
        System.out.println("\n=== [2] Thí nghiệm Failover DB qua UDP DNS Server RFC 1035 thật ===");
        System.out.printf("Trước failover        : %s%n", cacheRes.beforeFailover());
        System.out.printf("TTL=-1 (Forever) +120s: %s (STALE! totalUdpQueries=%d)%n",
                cacheRes.foreverAfterFailover(), cacheRes.foreverServerQueries());
        System.out.printf("TTL=1s           +2s  : %s (UPDATED! totalUdpQueries=%d)%n",
                cacheRes.shortTtlAfterFailover(), cacheRes.shortTtlServerQueries());

        List<String> k8sSearch = List.of(
                "prod.svc.cluster.local",
                "svc.cluster.local",
                "cluster.local",
                "ap-southeast-1.compute.internal");

        try (MiniUdpDnsServer server = new MiniUdpDnsServer(60)) {
            server.putARecord("api.partner.com", "203.0.113.88");
            NdotsResolverSimulator sim = new NdotsResolverSimulator();

            var ndots5 = sim.resolveWithSearchDomains(server, "api.partner.com", k8sSearch, 5, true);
            var ndots2 = sim.resolveWithSearchDomains(server, "api.partner.com", k8sSearch, 2, true);
            var trailingDot = sim.resolveWithSearchDomains(server, "api.partner.com.", k8sSearch, 5, true);

            System.out.println("\n=== [3] Thí nghiệm K8s ndots trên UDP DNS Server thật (A + AAAA) ===");
            System.out.printf("ndots:5 (api.partner.com)  : udpQueries=%2d | nxdomain=%d | ip=%s%n",
                    ndots5.totalUdpQueries(), ndots5.nxdomainResponses(), ndots5.resolvedIp());
            System.out.printf("ndots:2 (api.partner.com)  : udpQueries=%2d | nxdomain=%d | ip=%s%n",
                    ndots2.totalUdpQueries(), ndots2.nxdomainResponses(), ndots2.resolvedIp());
            System.out.printf("ndots:5 (api.partner.com.) : udpQueries=%2d | nxdomain=%d | ip=%s%n",
                    trailingDot.totalUdpQueries(), trailingDot.nxdomainResponses(), trailingDot.resolvedIp());
        }
    }
}
