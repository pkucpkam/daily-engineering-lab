#!/usr/bin/env bash
set -euo pipefail

# Lab LNX-07 (chạy trên Linux / WSL):
# Soi thực tế FD epoll (anon_inode:[eventpoll]) bên trong một tiến trình Java NIO / Netty.

if [[ $# -lt 1 ]]; then
  echo "Usage: $0 <JAVA_PID>"
  exit 1
fi

PID="$1"
echo "=== [1] Tìm các epoll instance FD trong tiến trình $PID ==="
lsof -nP -p "$PID" | grep -i "eventpoll" || ls -l "/proc/$PID/fd" | grep "eventpoll" || true

echo ""
echo "=== [2] Xem các socket tfd đang được epoll theo dõi trong /proc/$PID/fdinfo ==="
for fd_link in "/proc/$PID/fd"/*; do
  target="$(readlink "$fd_link" 2>/dev/null || true)"
  if [[ "$target" == *"eventpoll"* ]]; then
    fd_num="$(basename "$fd_link")"
    echo "--- fd=$fd_num ($target) ---"
    cat "/proc/$PID/fdinfo/$fd_num" | head -n 15
  fi
done
