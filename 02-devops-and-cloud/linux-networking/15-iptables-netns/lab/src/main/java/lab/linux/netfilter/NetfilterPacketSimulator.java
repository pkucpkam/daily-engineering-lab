package lab.linux.netfilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Mô phỏng chính xác luồng gói tin qua Linux Netfilter (iptables), conntrack,
 * Docker port publish (-p 8080:80), kube-proxy 1/N probability, và Network Namespace bridge.
 */
public final class NetfilterPacketSimulator {

    public enum Action { ACCEPT, DROP, REJECT }

    public record Packet(String srcIp, int srcPort, String dstIp, int dstPort, String inIface, boolean syn) {}

    public record TraceOutcome(
            boolean delivered,
            String deliveredTarget,
            String finalSrcIp,
            String finalDstIp,
            int finalDstPort,
            Action finalAction,
            List<String> hookTrace) {}

    public record PortForwardRule(int hostPort, String containerIp, int containerPort) {}

    public record KubeService(String clusterIp, int servicePort, List<String> podEndpoints) {}

    private final String hostPublicIp;
    private final List<String> hostLocalIps = new ArrayList<>();
    private final Map<String, String> containerSubnetPrefixes = new LinkedHashMap<>();
    private final List<PortForwardRule> dockerDnatRules = new ArrayList<>();
    private final List<KubeService> kubeServices = new ArrayList<>();
    private final List<Integer> inputBlockedPorts = new ArrayList<>();
    private final List<Integer> dockerUserBlockedExternalPorts = new ArrayList<>();
    private final Map<String, String> conntrackBindings = new HashMap<>();
    private boolean ipForwardEnabled = true;
    private boolean masqueradeContainerOutbound = true;
    private final Random random;

    public NetfilterPacketSimulator(String hostPublicIp, long seed) {
        this.hostPublicIp = hostPublicIp;
        this.hostLocalIps.add(hostPublicIp);
        this.hostLocalIps.add("127.0.0.1");
        this.random = new Random(seed);
    }

    public void setIpForwardEnabled(boolean enabled) {
        this.ipForwardEnabled = enabled;
    }

    public void setMasqueradeContainerOutbound(boolean enabled) {
        this.masqueradeContainerOutbound = enabled;
    }

    public void addContainerBridgeSubnet(String prefix, String namespaceName) {
        containerSubnetPrefixes.put(prefix, namespaceName);
    }

    public void publishDockerPort(int hostPort, String containerIp, int containerPort) {
        dockerDnatRules.add(new PortForwardRule(hostPort, containerIp, containerPort));
    }

    public void addKubeService(String clusterIp, int servicePort, List<String> podEndpoints) {
        kubeServices.add(new KubeService(clusterIp, servicePort, List.copyOf(podEndpoints)));
    }

    public void blockHostInputPort(int port) {
        inputBlockedPorts.add(port);
    }

    public void blockDockerUserExternalPort(int port) {
        dockerUserBlockedExternalPorts.add(port);
    }

    public TraceOutcome routePacket(Packet pkt) {
        List<String> trace = new ArrayList<>();
        String srcIp = pkt.srcIp();
        int srcPort = pkt.srcPort();
        String dstIp = pkt.dstIp();
        int dstPort = pkt.dstPort();
        String connKey = srcIp + ":" + srcPort + "->" + dstIp + ":" + dstPort;

        // 1. PREROUTING (nếu gói đến từ interface vào)
        trace.add("PREROUTING(raw->mangle->nat)");
        if (conntrackBindings.containsKey(connKey) && !pkt.syn()) {
            String[] target = conntrackBindings.get(connKey).split(":");
            dstIp = target[0];
            dstPort = Integer.parseInt(target[1]);
            trace.add("CONNTRACK_ESTABLISHED_DNAT(" + dstIp + ":" + dstPort + ")");
        } else {
            // Kiểm tra kube-proxy KUBE-SERVICES trước
            for (KubeService svc : kubeServices) {
                if (dstIp.equals(svc.clusterIp()) && dstPort == svc.servicePort()) {
                    String chosen = selectKubeEndpoint(svc.podEndpoints());
                    String[] ep = chosen.split(":");
                    dstIp = ep[0];
                    dstPort = Integer.parseInt(ep[1]);
                    conntrackBindings.put(connKey, dstIp + ":" + dstPort);
                    trace.add("KUBE-SERVICES->KUBE-SVC->DNAT(" + chosen + ")");
                    break;
                }
            }
            // Kiểm tra Docker -p hostPort:containerPort
            if (hostLocalIps.contains(dstIp)) {
                for (PortForwardRule r : dockerDnatRules) {
                    if (dstPort == r.hostPort()) {
                        dstIp = r.containerIp();
                        dstPort = r.containerPort();
                        conntrackBindings.put(connKey, dstIp + ":" + dstPort);
                        trace.add("DOCKER-DNAT(" + dstIp + ":" + dstPort + ")");
                        break;
                    }
                }
            }
        }

        // 2. Routing Decision: đích hiện tại thuộc Host hay phải FORWARD sang Bridge/WAN?
        if (hostLocalIps.contains(dstIp)) {
            trace.add("INPUT(filter)");
            if (inputBlockedPorts.contains(dstPort)) {
                trace.add("INPUT_DROP(port=" + dstPort + ")");
                return new TraceOutcome(false, "NONE", srcIp, dstIp, dstPort, Action.DROP, trace);
            }
            trace.add("LOCAL_DELIVERY(HOST)");
            return new TraceOutcome(true, "HOST_PROCESS", srcIp, dstIp, dstPort, Action.ACCEPT, trace);
        }

        // 3. Nếu cùng bridge giữa 2 netns (vd 10.200.0.2 -> 10.200.0.3)
        if ("br-lab0".equals(pkt.inIface()) && isContainerIp(dstIp)) {
            trace.add("BRIDGE_L2_SWITCH(br-lab0)");
            trace.add("VETH_DELIVERY(" + resolveNamespace(dstIp) + ")");
            return new TraceOutcome(true, resolveNamespace(dstIp), srcIp, dstIp, dstPort, Action.ACCEPT, trace);
        }

        // 4. Đường FORWARD xuyên qua host (từ WAN vào container hoặc từ container ra WAN)
        trace.add("FORWARD(filter)");
        if (!ipForwardEnabled) {
            trace.add("DROP(ip_forward=0)");
            return new TraceOutcome(false, "NONE", srcIp, dstIp, dstPort, Action.DROP, trace);
        }

        // Kiểm tra DOCKER-USER chain khi đi từ eth0 vào container
        if ("eth0".equals(pkt.inIface()) && isContainerIp(dstIp)) {
            if (dockerUserBlockedExternalPorts.contains(dstPort)) {
                trace.add("DOCKER-USER_DROP(dport=" + dstPort + ")");
                return new TraceOutcome(false, "NONE", srcIp, dstIp, dstPort, Action.DROP, trace);
            }
        }

        // 5. POSTROUTING (SNAT / MASQUERADE khi container đi ra ngoài Internet)
        trace.add("POSTROUTING(nat)");
        if (isContainerIp(srcIp) && !isContainerIp(dstIp) && masqueradeContainerOutbound) {
            srcIp = hostPublicIp;
            trace.add("MASQUERADE(srcIp->" + hostPublicIp + ")");
        }

        String target = isContainerIp(dstIp) ? resolveNamespace(dstIp) : "EXTERNAL_WAN";
        trace.add("DELIVERED(" + target + ")");
        return new TraceOutcome(true, target, srcIp, dstIp, dstPort, Action.ACCEPT, trace);
    }

    /**
     * Thuật toán xác suất điều kiện 1/(N - i) của kube-proxy iptables.
     */
    private String selectKubeEndpoint(List<String> endpoints) {
        int n = endpoints.size();
        for (int i = 0; i < n - 1; i++) {
            double prob = 1.0 / (n - i);
            if (random.nextDouble() < prob) {
                return endpoints.get(i);
            }
        }
        return endpoints.get(n - 1);
    }

    private boolean isContainerIp(String ip) {
        return containerSubnetPrefixes.keySet().stream().anyMatch(ip::startsWith);
    }

    private String resolveNamespace(String ip) {
        return containerSubnetPrefixes.entrySet().stream()
                .filter(e -> ip.startsWith(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse("CONTAINER_NS");
    }
}
