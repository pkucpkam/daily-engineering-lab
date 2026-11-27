package lab.linux.dns;

import java.io.IOException;
import java.util.List;

/**
 * Gửi UDP DNS queries thật tới MiniUdpDnsServer mô phỏng chính xác thuật toán search domain + ndots
 * trong glibc/musl của Linux (/etc/resolv.conf).
 */
public final class NdotsResolverSimulator {

    public record NdotsReport(
            String inputDomain,
            int ndots,
            int totalUdpQueries,
            int nxdomainResponses,
            String resolvedIp,
            List<String> queryTrace) {}

    public NdotsReport resolveWithSearchDomains(
            MiniUdpDnsServer server,
            String rawDomain,
            List<String> searchDomains,
            int ndots,
            boolean queryDualStackAAndAAAA) throws IOException {
        server.resetStats();

        boolean isExplicitFqdn = rawDomain.endsWith(".");
        String stripped = isExplicitFqdn ? rawDomain.substring(0, rawDomain.length() - 1) : rawDomain;
        int dotCount = countDots(stripped);

        int nxdomainCount = 0;
        String resolvedIp = null;

        // Theo resolv.conf(5): nếu không kết thúc bằng '.' và số dấu '.' < ndots -> thử toàn bộ search list trước!
        boolean trySearchFirst = !isExplicitFqdn && dotCount < ndots;

        if (trySearchFirst) {
            for (String suffix : searchDomains) {
                String candidate = stripped + "." + suffix;
                var res = queryOneCandidate(server.port(), candidate, queryDualStackAAndAAAA);
                nxdomainCount += res.nxCount();
                if (res.ip() != null) {
                    resolvedIp = res.ip();
                    return new NdotsReport(rawDomain, ndots, server.queryCount(), nxdomainCount, resolvedIp, server.queriedNames());
                }
            }
        }

        // Hỏi tên gốc như một FQDN tuyệt đối
        var direct = queryOneCandidate(server.port(), stripped, queryDualStackAAndAAAA);
        nxdomainCount += direct.nxCount();
        resolvedIp = direct.ip();

        return new NdotsReport(rawDomain, ndots, server.queryCount(), nxdomainCount, resolvedIp, server.queriedNames());
    }

    private record CandidateResult(String ip, int nxCount) {}

    private static CandidateResult queryOneCandidate(int port, String fqdn, boolean dualStack) throws IOException {
        int nx = 0;
        var aResp = MiniUdpDnsServer.sendUdpQuery(port, fqdn, MiniUdpDnsServer.TYPE_A);
        if (aResp.rcode() == MiniUdpDnsServer.RCODE_NXDOMAIN) {
            nx++;
        }
        if (dualStack) {
            var aaaaResp = MiniUdpDnsServer.sendUdpQuery(port, fqdn, MiniUdpDnsServer.TYPE_AAAA);
            if (aaaaResp.rcode() == MiniUdpDnsServer.RCODE_NXDOMAIN) {
                nx++;
            }
        }
        return new CandidateResult(aResp.resolvedIp(), nx);
    }

    private static int countDots(String s) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '.') {
                count++;
            }
        }
        return count;
    }
}
