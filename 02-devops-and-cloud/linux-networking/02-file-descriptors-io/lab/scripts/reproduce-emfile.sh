#!/usr/bin/env bash
set -euo pipefail

# Lab LNX-02 (chạy trên Linux / WSL):
# Đặt ulimit -n cực thấp (64) trong subshell và mở file hàng loạt để nhìn tận mắt lỗi "Too many open files" (EMFILE).

TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT

(
  ulimit -n 64
  echo "Soft limit hiện tại trong subshell: $(ulimit -Sn) | Hard limit: $(ulimit -Hn)"
  python3 -c '
import os, sys
opened = []
try:
    for i in range(100):
        f = open(f"'"$TMP_DIR"'/f_{i}.tmp", "w")
        opened.append(f)
except OSError as e:
    print(f"Chạm trần FD tại file thứ {len(opened)}: errno={e.errno} ({e.strerror})")
finally:
    for f in opened:
        f.close()
'
)
