#!/usr/bin/env bash
set -euo pipefail

# Lab LNX-01 (chạy trên Linux / WSL):
# Tạo một zombie process tạm thời trong 3 giây và quan sát bằng ps.

echo "Tạo process cha giữ subshell đã exit trong 3s mà không gọi wait()..."
python3 -c '
import os, time
pid = os.fork()
if pid == 0:
    os._exit(0)
print(f"Parent PID={os.getpid()}, Child PID={pid} đã exit nhưng chưa wait() -> thành Zombie Z")
time.sleep(3)
os.waitpid(pid, 0)
print("Parent vừa gọi waitpid() -> Zombie đã được reap!")
' &
PARENT_PID=$!

sleep 0.5
echo "Danh sách trạng thái Z hiện tại:"
ps -eo pid,ppid,stat,comm | awk 'NR==1 || $3 ~ /Z/'
wait "$PARENT_PID"

echo ""
echo "Lệnh lấy Thread Dump sống cho JVM/JBoss trên production (không giết app):"
echo "  kill -3 <JBOSS_OR_JAVA_PID>"
