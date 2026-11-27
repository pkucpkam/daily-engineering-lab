#!/usr/bin/env bash
set -euo pipefail

# Lab LNX-05 (chạy trên Linux VM):
# Thực thi Brendan Gregg's 60-Second Linux Performance Checklist (chỉ đọc, an toàn cho prod).

echo "=== [1] uptime ==="
uptime

echo "=== [2] dmesg -T | tail -n 15 ==="
dmesg -T 2>/dev/null | tail -n 15 || dmesg | tail -n 15 || true

echo "=== [3] vmstat 1 5 (nhớ bỏ qua dòng đầu!) ==="
vmstat 1 5

echo "=== [4] mpstat -P ALL 1 3 ==="
command -v mpstat >/dev/null && mpstat -P ALL 1 3 || echo "Thiếu sysstat (mpstat)"

echo "=== [5] pidstat 1 3 ==="
command -v pidstat >/dev/null && pidstat 1 3 || echo "Thiếu sysstat (pidstat)"

echo "=== [6] iostat -xz 1 3 ==="
command -v iostat >/dev/null && iostat -xz 1 3 || echo "Thiếu sysstat (iostat)"

echo "=== [7] free -m ==="
free -m

echo "=== [8-9] sar -n DEV,TCP,ETCP 1 3 ==="
command -v sar >/dev/null && sar -n DEV,TCP,ETCP 1 3 || echo "Thiếu sysstat (sar)"

echo "=== [10] top batch snapshot ==="
top -b -n 1 | head -n 20
