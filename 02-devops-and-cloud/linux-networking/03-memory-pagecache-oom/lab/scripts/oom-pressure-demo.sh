#!/usr/bin/env bash
set -euo pipefail

# Lab LNX-03 (chạy trên Linux VM hoặc container Docker có giới hạn RAM):
# Quan sát free -m, vmstat, /proc/<pid>/oom_score và dmesg khi tạo áp lực bộ nhớ.

echo "=== [1] free -m và /proc/meminfo ==="
free -m
grep -E "MemTotal|MemFree|MemAvailable|Cached|Dirty|Committed_AS" /proc/meminfo

echo ""
echo "=== [2] Top 5 tiến trình có oom_score cao nhất ==="
for p in /proc/[0-9]*; do
  pid="${p##*/}"
  if [[ -r "$p/oom_score" && -r "$p/comm" ]]; then
    printf "%6s  score=%4s  adj=%5s  %s\n" \
      "$pid" "$(cat "$p/oom_score" 2>/dev/null || echo 0)" \
      "$(cat "$p/oom_score_adj" 2>/dev/null || echo 0)" \
      "$(cat "$p/comm" 2>/dev/null || echo '?')"
  fi
done | sort -t= -k2 -rn | head -n 5

echo ""
echo "=== [3] Kiểm tra lịch sử OOM Killer trong dmesg ==="
dmesg -T | grep -i -E "oom|killed process|out of memory" | tail -n 10 || echo "Chưa có OOM gần đây."

echo ""
echo "=== [4] Gợi ý chạy thử OOM Killer an toàn trong container Docker 128MB ==="
echo '  docker run --rm --memory=128m --memory-swap=128m eclipse-temurin:21-jre \'
echo '    java -Xmx64m -XX:MaxDirectMemorySize=128m -e ... (hoặc python cấp phát bytearray 150MB)'
