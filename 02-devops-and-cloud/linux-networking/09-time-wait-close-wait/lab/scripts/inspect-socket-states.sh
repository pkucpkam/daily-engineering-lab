#!/usr/bin/env bash
set -euo pipefail

# Lab LNX-09 (chạy trên Linux / WSL):
# Soi trạng thái TIME_WAIT, CLOSE_WAIT, giới hạn ephemeral ports và FD của process Java.

echo "=== [1] Tổng quan socket TCP toàn hệ thống ==="
ss -s

echo ""
echo "=== [2] Thống kê số lượng từng trạng thái TCP ==="
ss -tan | awk 'NR>1 {print $1}' | sort | uniq -c | sort -nr || true

echo ""
echo "=== [3] Top 10 đích (dst_ip:dst_port) đang chiếm TIME_WAIT nhiều nhất ==="
ss -tan state time-wait | awk 'NR>1 {print $4}' | sort | uniq -c | sort -nr | head -n 10 || true

echo ""
echo "=== [4] Danh sách socket đang kẹt CLOSE_WAIT (lỗi app quên đóng!) ==="
ss -tanp state close-wait | head -n 20 || true

echo ""
echo "=== [5] Tham số kernel ephemeral port & TIME_WAIT ==="
sysctl net.ipv4.ip_local_port_range net.ipv4.tcp_fin_timeout net.ipv4.tcp_tw_reuse net.ipv4.tcp_timestamps

echo ""
echo "=== [6] Giới hạn File Descriptor hiện tại ==="
echo "ulimit -n = $(ulimit -n)"
