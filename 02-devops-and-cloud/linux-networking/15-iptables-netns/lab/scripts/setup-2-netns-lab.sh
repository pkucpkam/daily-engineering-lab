#!/usr/bin/env bash
set -euo pipefail

# Lab LNX-15 (chạy với sudo trên Linux):
# Tự dựng 2 "container" bằng Network Namespace (ns1, ns2) nối qua veth + Linux Bridge + NAT (MASQUERADE).

ACTION="${1:-up}"
WAN_IFACE="${2:-eth0}"

cleanup() {
  ip netns del ns1 2>/dev/null || true
  ip netns del ns2 2>/dev/null || true
  ip link del br-lab0 2>/dev/null || true
  iptables -t nat -D POSTROUTING -s 10.200.0.0/24 ! -o br-lab0 -j MASQUERADE 2>/dev/null || true
}

if [[ "$ACTION" == "down" ]]; then
  cleanup
  echo "Đã dọn sạch ns1, ns2, br-lab0 và rule NAT."
  exit 0
fi

cleanup

echo "=== [1] Tạo Bridge br-lab0 (Gateway 10.200.0.1/24) ==="
ip link add br-lab0 type bridge
ip addr add 10.200.0.1/24 dev br-lab0
ip link set br-lab0 up

echo "=== [2] Tạo 2 Network Namespace ns1 (10.200.0.2) & ns2 (10.200.0.3) + veth ==="
for idx in 1 2; do
  NS="ns${idx}"
  IP="10.200.0.$((idx + 1))/24"
  VETH_IN="veth-ns${idx}"
  VETH_BR="br-veth${idx}"

  ip netns add "$NS"
  ip link add "$VETH_IN" type veth peer name "$VETH_BR"
  ip link set "$VETH_IN" netns "$NS"

  ip link set "$VETH_BR" master br-lab0
  ip link set "$VETH_BR" up

  ip netns exec "$NS" ip link set lo up
  ip netns exec "$NS" ip addr add "$IP" dev "$VETH_IN"
  ip netns exec "$NS" ip link set "$VETH_IN" up
  ip netns exec "$NS" ip route add default via 10.200.0.1
done

echo "=== [3] Bật IP Forwarding & MASQUERADE ra ngoài ($WAN_IFACE) ==="
sysctl -w net.ipv4.ip_forward=1
iptables -t nat -A POSTROUTING -s 10.200.0.0/24 ! -o br-lab0 -j MASQUERADE

echo "=== [4] Kiểm thử ping giữa 2 namespace ==="
ip netns exec ns1 ping -c 2 10.200.0.3
echo "Hoàn tất! Chạy 'sudo bash $0 down' để dọn dẹp."
