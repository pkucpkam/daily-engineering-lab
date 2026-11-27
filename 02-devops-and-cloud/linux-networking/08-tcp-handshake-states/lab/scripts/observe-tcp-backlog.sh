#!/usr/bin/env bash
set -euo pipefail

# Lab LNX-08 (chạy trên Linux / WSL):
# Kiểm tra tham số kernel TCP backlog, quan sát Recv-Q/Send-Q và bắt gói tin 3-way handshake bằng tcpdump.

PORT="${1:-8080}"

echo "=== [1] Thông số kernel SYN Queue & Accept Queue ==="
sysctl net.core.somaxconn net.ipv4.tcp_max_syn_backlog net.ipv4.tcp_abort_on_overflow net.ipv4.tcp_syncookies

echo ""
echo "=== [2] Tình trạng Accept Queue trên các cổng LISTEN ==="
ss -lnt

echo ""
echo "=== [3] Bộ đếm tràn Accept Queue của kernel ==="
command -v nstat >/dev/null && nstat -az TcpExtListenOverflows TcpExtListenDrops || true

echo ""
echo "=== [4] Gợi ý bắt 3-way & 4-way handshake bằng tcpdump ==="
echo "  sudo tcpdump -i lo -nn -S -c 20 port $PORT"
echo "  (Đọc cờ: [S]=SYN, [S.]=SYN-ACK, [.]=ACK, [P.]=PSH+ACK, [F.]=FIN+ACK, [R.]=RST+ACK)"
