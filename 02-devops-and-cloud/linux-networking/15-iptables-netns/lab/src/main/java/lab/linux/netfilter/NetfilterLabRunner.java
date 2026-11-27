package lab.linux.netfilter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Runner thực nghiệm cho LNX-15.
 */
public final class NetfilterLabRunner {

    public static void main(String[] args) {
        NetfilterPacketSimulator sim = new NetfilterPacketSimulator("203.0.113.10", 42L);
        sim.addContainerBridgeSubnet("172.17.0.", "ns-docker-nginx");
        sim.addContainerBridgeSubnet("10.200.0.", "ns-manual");
        sim.addContainerBridgeSubnet("10.244.", "k8s-pod");

        sim.publishDockerPort(8080, "172.17.0.2", 80);
        // Bẫy kinh điển: Admin chặn 8080 & 80 trên INPUT nhưng quên DOCKER-USER!
        sim.blockHostInputPort(8080);
        sim.blockHostInputPort(80);

        var dockerPkt = new NetfilterPacketSimulator.Packet("198.51.100.9", 54000, "203.0.113.10", 8080, "eth0", true);
        var out1 = sim.routePacket(dockerPkt);
        System.out.println("=== [1] Docker -p 8080:80 khi đã chặn ở INPUT (Bypass!) ===");
        System.out.printf("Delivered=%s | Target=%s (%s:%d) | Trace=%s%n",
                out1.delivered(), out1.deliveredTarget(), out1.finalDstIp(), out1.finalDstPort(), out1.hookTrace());

        sim.blockDockerUserExternalPort(80);
        var out2 = sim.routePacket(new NetfilterPacketSimulator.Packet("198.51.100.9", 54001, "203.0.113.10", 8080, "eth0", true));
        System.out.println("\n=== [2] Chặn đúng cách tại DOCKER-USER (nằm trong FORWARD) ===");
        System.out.printf("Delivered=%s | Action=%s | Trace=%s%n",
                out2.delivered(), out2.finalAction(), out2.hookTrace());

        // Kiểm chứng kube-proxy 1/N trên 3000 kết nối mới
        List<String> pods = List.of("10.244.1.5:8080", "10.244.2.8:8080", "10.244.3.9:8080");
        sim.addKubeService("10.96.10.20", 80, pods);
        Map<String, Integer> counts = new LinkedHashMap<>();
        pods.forEach(p -> counts.put(p, 0));
        for (int i = 0; i < 3000; i++) {
            var res = sim.routePacket(new NetfilterPacketSimulator.Packet("10.244.0.10", 40000 + i, "10.96.10.20", 80, "cni0", true));
            String key = res.finalDstIp() + ":" + res.finalDstPort();
            counts.put(key, counts.getOrDefault(key, 0) + 1);
        }
        System.out.println("\n=== [3] Phân phối xác suất kube-proxy iptables (1/3 -> 1/2 -> 1.0) cho 3.000 SYN ===");
        System.out.println("Distribution: " + counts);

        // Kiểm chứng netns ns1 ra Internet qua MASQUERADE
        var wanOut = sim.routePacket(new NetfilterPacketSimulator.Packet("10.200.0.2", 51000, "8.8.8.8", 53, "br-lab0", true));
        System.out.println("\n=== [4] Gói từ ns1 (10.200.0.2) ra Internet qua POSTROUTING MASQUERADE ===");
        System.out.printf("Delivered=%s | SrcIP sau NAT=%s | Trace=%s%n",
                wanOut.delivered(), wanOut.finalSrcIp(), wanOut.hookTrace());
    }
}
