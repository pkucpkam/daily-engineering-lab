package lab.linux.netfilter;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class NetfilterLabTest {

    @Test
    void dockerPublishedPortBypassesHostInputDropAndIsBlockedOnlyByDockerUser() {
        NetfilterPacketSimulator sim = new NetfilterPacketSimulator("203.0.113.10", 42L);
        sim.addContainerBridgeSubnet("172.17.0.", "ns-nginx");
        sim.publishDockerPort(8080, "172.17.0.2", 80);
        sim.blockHostInputPort(8080);
        sim.blockHostInputPort(80);

        var syn = new NetfilterPacketSimulator.Packet("198.51.100.5", 50001, "203.0.113.10", 8080, "eth0", true);
        var bypassed = sim.routePacket(syn);

        assertThat(bypassed.delivered()).isTrue();
        assertThat(bypassed.finalDstIp()).isEqualTo("172.17.0.2");
        assertThat(bypassed.finalDstPort()).isEqualTo(80);

        sim.blockDockerUserExternalPort(80);
        var syn2 = new NetfilterPacketSimulator.Packet("198.51.100.5", 50002, "203.0.113.10", 8080, "eth0", true);
        var blocked = sim.routePacket(syn2);
        assertThat(blocked.delivered()).isFalse();
        assertThat(blocked.finalAction()).isEqualTo(NetfilterPacketSimulator.Action.DROP);
    }

    @Test
    void kubeProxyIptablesDistributesEvenlyAndConntrackKeepsExistingFlowPinned() {
        NetfilterPacketSimulator sim = new NetfilterPacketSimulator("203.0.113.10", 42L);
        sim.addContainerBridgeSubnet("10.244.", "k8s-pod");
        List<String> pods = List.of("10.244.1.5:8080", "10.244.2.8:8080", "10.244.3.9:8080");
        sim.addKubeService("10.96.10.20", 80, pods);

        Map<String, Integer> counts = new LinkedHashMap<>();
        pods.forEach(p -> counts.put(p, 0));

        for (int i = 0; i < 3000; i++) {
            var res = sim.routePacket(
                    new NetfilterPacketSimulator.Packet("10.244.0.10", 30000 + i, "10.96.10.20", 80, "cni0", true));
            String ep = res.finalDstIp() + ":" + res.finalDstPort();
            counts.put(ep, counts.get(ep) + 1);
        }

        // Mỗi pod nhận xấp xỉ 1000/3000 (~33.3%)
        assertThat(counts.values()).allSatisfy(c -> assertThat(c).isBetween(880, 1120));

        // Packet tiếp theo (syn=false) của kết nối #0 phải bám conntrack về đúng Pod ban đầu
        var firstSyn = sim.routePacket(
                new NetfilterPacketSimulator.Packet("10.244.0.99", 45000, "10.96.10.20", 80, "cni0", true));
        for (int k = 0; k < 5; k++) {
            var dataPkt = sim.routePacket(
                    new NetfilterPacketSimulator.Packet("10.244.0.99", 45000, "10.96.10.20", 80, "cni0", false));
            assertThat(dataPkt.finalDstIp()).isEqualTo(firstSyn.finalDstIp());
            assertThat(dataPkt.hookTrace()).anyMatch(s -> s.startsWith("CONNTRACK_ESTABLISHED_DNAT"));
        }
    }

    @Test
    void disablingIpForwardBreaksContainerOutboundNat() {
        NetfilterPacketSimulator sim = new NetfilterPacketSimulator("203.0.113.10", 42L);
        sim.addContainerBridgeSubnet("10.200.0.", "ns1");

        var ok = sim.routePacket(new NetfilterPacketSimulator.Packet("10.200.0.2", 52000, "8.8.8.8", 53, "br-lab0", true));
        assertThat(ok.delivered()).isTrue();
        assertThat(ok.finalSrcIp()).isEqualTo("203.0.113.10");

        sim.setIpForwardEnabled(false);
        var dropped = sim.routePacket(new NetfilterPacketSimulator.Packet("10.200.0.2", 52001, "8.8.8.8", 53, "br-lab0", true));
        assertThat(dropped.delivered()).isFalse();
        assertThat(dropped.hookTrace()).contains("DROP(ip_forward=0)");
    }
}
