#!/usr/bin/env bash
set -euo pipefail

# Lab LNX-02 (chạy trên Linux / WSL):
# Điều tra File Descriptor của một tiến trình Java / JBoss theo PID.

if [[ $# -lt 1 ]]; then
  echo "Cách dùng: $0 <PID>"
  exit 1
fi

PID="$1"
echo "=== [1] Giới hạn FD thật của PID $PID từ /proc/$PID/limits ==="
grep -E "Limit|Max open files" "/proc/$PID/limits"

echo "=== [2] Tổng số FD đang mở ==="
ls -1 "/proc/$PID/fd" | wc -l

echo "=== [3] Phân loại theo lsof -nP (REG, IPv4, FIFO, anon_inode) ==="
lsof -nP -p "$PID" | awk 'NR>1 {print $5}' | sort | uniq -c | sort -rn

echo "=== [4] Kiểm tra file đã bị xóa nhưng còn giữ FD (deleted) ==="
lsof -nP +L1 -p "$PID" || true
