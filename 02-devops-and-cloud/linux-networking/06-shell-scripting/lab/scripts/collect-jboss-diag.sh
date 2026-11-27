#!/usr/bin/env bash
set -euo pipefail

# Lab LNX-06 (chạy trên Linux / WSL):
# Script thu thập gói chẩn đoán sự cố Java / JBoss an toàn & idempotent.

usage() {
  echo "Usage: $0 [-p <PID>] -l <log_file> -o <out_dir> [-s <HH:MM:SS>] [-e <HH:MM:SS>]" >&2
  exit 1
}

PID=""
LOG_FILE=""
OUT_DIR=""
START_TIME="00:00:00"
END_TIME="23:59:59"

while getopts "p:l:o:s:e:h" opt; do
  case "$opt" in
    p) PID="$OPTARG" ;;
    l) LOG_FILE="$OPTARG" ;;
    o) OUT_DIR="$OPTARG" ;;
    s) START_TIME="$OPTARG" ;;
    e) END_TIME="$OPTARG" ;;
    *) usage ;;
  esac
done

[[ -n "$LOG_FILE" && -n "$OUT_DIR" ]] || usage
[[ -f "$LOG_FILE" ]] || { echo "Không tìm thấy log file: $LOG_FILE" >&2; exit 1; }

TMP_DIR="$(mktemp -d)"
cleanup() {
  local rc=$?
  rm -rf "$TMP_DIR"
  exit "$rc"
}
trap cleanup EXIT INT TERM

mkdir -p "$OUT_DIR"

# 1. Nếu có truyền PID sống trên Linux -> lấy thông tin OS + signal thread dump
if [[ -n "$PID" && -d "/proc/$PID" ]]; then
  cat "/proc/$PID/limits" > "$TMP_DIR/proc-limits.txt"
  ls -1 "/proc/$PID/fd" | wc -l > "$TMP_DIR/fd-count.txt"
  if command -v jcmd >/dev/null 2>&1; then
    jcmd "$PID" Thread.print > "$TMP_DIR/thread-dump.txt" 2>&1 || true
    jcmd "$PID" GC.heap_info > "$TMP_DIR/heap-info.txt" 2>&1 || true
  else
    # Fallback cho Java 7 / JBoss không có jcmd: gửi SIGQUIT (kill -3) in thread dump ra stdout
    kill -3 "$PID" || true
  fi
fi

# 2. Lọc lỗi theo khung giờ bằng awk (an toàn với set -e)
awk -v s="$START_TIME" -v e="$END_TIME" '
  {
    ts = substr($2, 1, 8)
    if (ts >= s && ts <= e && ($0 ~ /ERROR/ || $0 ~ /OutOfMemoryError/)) {
      print
    }
  }
' "$LOG_FILE" > "$TMP_DIR/errors-in-window.log"

ERROR_COUNT="$(wc -l < "$TMP_DIR/errors-in-window.log" | tr -d ' ')"
printf 'window=%s..%s\nmatched_errors=%s\n' "$START_TIME" "$END_TIME" "$ERROR_COUNT" > "$TMP_DIR/summary.txt"

# 3. Atomic move sang thư mục đích để đảm bảo tính idempotent khi chạy lại
for f in "$TMP_DIR"/*; do
  [[ -e "$f" ]] || continue
  mv -f "$f" "$OUT_DIR/$(basename "$f")"
done

echo "Thu thập thành công vào $OUT_DIR (matched_errors=$ERROR_COUNT)"
