package lab.linux.dns;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DnsLabTest {

    @Test
    void foreverDnsCacheKeepsStaleIpAfterServerFailoverWhereasShortTtlUpdates() throws Exception {
        JvmDnsCacheDemo demo = new JvmDnsCacheDemo();
        var result = demo.runFailoverExperiment();

        assertThat(result.beforeFailover()).isEqualTo("10.10.1.10");
        assertThat(result.foreverAfterFailover()).isEqualTo("10.10.1.10");
        assertThat(result.foreverServerQueries()).isEqualTo(1);

        assertThat(result.shortTtlAfterFailover()).isEqualTo("10.10.2.20");
        assertThat(result.shortTtlServerQueries()).isEqualTo(2);
    }

    @Test
    void ndots5MultipliesUdpDnsQueriesFiveFoldComparedToNdots2OrTrailingDot() throws Exception {
        List<String> k8sSearch = List.of(
                "prod.svc.cluster.local",
                "svc.cluster.local",
                "cluster.local",
                "ap-southeast-1.compute.internal");

        try (MiniUdpDnsServer server = new MiniUdpDnsServer(60)) {
            server.putARecord("api.partner.com", "203.0.113.88");
            NdotsResolverSimulator sim = new NdotsResolverSimulator();

            var ndots5 = sim.resolveWithSearchDomains(server, "api.partner.com", k8sSearch, 5, true);
            assertThat(ndots5.resolvedIp()).isEqualTo("203.0.113.88");
            assertThat(ndots5.totalUdpQueries()).isEqualTo(10);
            assertThat(ndots5.nxdomainResponses()).isEqualTo(8);

            var ndots2 = sim.resolveWithSearchDomains(server, "api.partner.com", k8sSearch, 2, true);
            assertThat(ndots2.resolvedIp()).isEqualTo("203.0.113.88");
            assertThat(ndots2.totalUdpQueries()).isEqualTo(2);
            assertThat(ndots2.nxdomainResponses()).isZero();

            var trailing = sim.resolveWithSearchDomains(server, "api.partner.com.", k8sSearch, 5, true);
            assertThat(trailing.resolvedIp()).isEqualTo("203.0.113.88");
            assertThat(trailing.totalUdpQueries()).isEqualTo(2);
            assertThat(trailing.nxdomainResponses()).isZero();
        }
    }
}
