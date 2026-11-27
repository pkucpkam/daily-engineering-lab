#!/usr/bin/env bash
set -euo pipefail

# Lab LNX-01 (chạy trên Linux / WSL / Docker):
# Minh họa shell-form wrapper KHÔNG forward SIGTERM so với dùng exec.

echo "=== Test 1: Shell wrapper KHÔNG dùng exec ==="
sh -c 'sleep 30' &
WRAPPER_PID=$!
sleep 0.3
echo "Process tree khi thiếu exec:"
ps -o pid,ppid,stat,comm -g "$(ps -o pgid= -p "$WRAPPER_PID" | tr -d ' ')" || true

echo "Gửi SIGTERM tới wrapper PID=$WRAPPER_PID ..."
kill -TERM "$WRAPPER_PID" 2>/dev/null || true
sleep 0.3
ps -ef | grep "[s]leep 30" || echo "Không còn sleep 30"

echo ""
echo "=== Test 2: Shell wrapper CÓ dùng exec ==="
sh -c 'exec sleep 30' &
EXEC_PID=$!
sleep 0.3
echo "Process với exec (sleep thay thế thẳng PID của sh):"
ps -o pid,ppid,stat,comm -p "$EXEC_PID" || true
kill -TERM "$EXEC_PID" 2>/dev/null || true
wait "$EXEC_PID" 2>/dev/null || true
echo "Exit status sau SIGTERM thường là 143 (128 + 15)."
