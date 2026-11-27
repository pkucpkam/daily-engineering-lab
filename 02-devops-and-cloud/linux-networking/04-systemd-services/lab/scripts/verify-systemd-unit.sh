#!/usr/bin/env bash
set -euo pipefail

# Lab LNX-04 (chạy trên Linux có systemd PID 1):
# Kiểm tra cú pháp unit files và hướng dẫn nạp thử service giới hạn memory.

echo "=== [1] Kiểm tra cú pháp unit files bằng systemd-analyze ==="
systemd-analyze verify systemd/java-app.service || true
systemd-analyze verify systemd/jboss-legacy.service || true

echo ""
echo "=== [2] Thử kiểm tra lịch timer ==="
systemd-analyze calendar "*-*-* 02:00:00"

echo ""
echo "=== [3] Chạy nhanh tiến trình giới hạn cgroup 128M mà không cần sửa /etc/systemd (systemd-run) ==="
echo "  sudo systemd-run --unit=java-mem-test -p MemoryMax=128M -p LimitNOFILE=1024 -p SuccessExitStatus=143 /usr/bin/java -version"
echo "  journalctl -u java-mem-test --no-pager"
