#!/usr/bin/env bash
set -euo pipefail

# Lab LNX-14 (tham khảo chạy trên Linux / Kubernetes Node):
# Tổng hợp câu lệnh tcpdump an toàn cho production, BPF filters và bắt gói trong K8s Pod.

cat <<'EOF'
=== [1] Bắt traffic JBoss an toàn (tắt DNS -nn, xoay vòng 5x100MB) ===
sudo tcpdump -i any -nn -S -s 0 -C 100 -W 5 -w /tmp/jboss.pcap 'host 10.20.1.15 and port 8080'

=== [2] Chỉ bắt SYN, FIN, RST trên port DB (cực nhẹ) ===
sudo tcpdump -i any -nn -S 'tcp[tcpflags] & (tcp-syn|tcp-fin|tcp-rst) != 0 and port 5432'

=== [3] Bắt 50 gói DNS port 53 đầu tiên ===
sudo tcpdump -i any -nn -c 50 'port 53'

=== [4] Bắt gói trong Kubernetes Pod không có tcpdump ===
# Cách A (Ephemeral Debug Container):
kubectl debug -it <pod-name> -n <namespace> --image=nicolaka/netshoot --target=<container> -- \
  tcpdump -i eth0 -nn -s 0 -w /tmp/pod.pcap 'port 8080'

# Cách B (SSH vào Worker Node -> dùng nsenter mượn NetNS của container PID):
sudo nsenter -t <CONTAINER_PID> -n tcpdump -i eth0 -nn -s 0 -w /tmp/pod.pcap 'port 8080'
EOF
