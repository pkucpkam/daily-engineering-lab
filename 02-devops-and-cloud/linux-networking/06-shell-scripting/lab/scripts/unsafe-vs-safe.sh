#!/usr/bin/env bash
set -euo pipefail

# Lab LNX-06 (chạy trên Linux / WSL):
# Minh họa rõ vì sao thiếu set -e / -u / -o pipefail rất nguy hiểm.

TMP_DIR="$(mktemp -d)"
cleanup() {
  local rc=$?
  rm -rf "$TMP_DIR"
  exit "$rc"
}
trap cleanup EXIT INT TERM

echo "=== [1] Chứng minh pipefail bắt lỗi lệnh đầu trong pipe ==="
set +e
(
  set +o pipefail
  false | gzip -c > "$TMP_DIR/empty.gz"
)
echo "Thiếu pipefail: false | gzip trả về exit code = $? (nguy hiểm: tưởng thành công!)"

(
  set -o pipefail
  false | gzip -c > "$TMP_DIR/empty.gz"
)
echo "Có -o pipefail: false | gzip trả về exit code = $? (bắt được lỗi ngay!)"
set -e

echo "=== [2] Chứng minh kiểm tra grep an toàn khi bật set -e ==="
printf "INFO startup ok\n" > "$TMP_DIR/app.log"
if grep -q "OutOfMemoryError" "$TMP_DIR/app.log"; then
  echo "Found OOM"
else
  echo "Không có OOM (if grep -q giữ script sống bình thường dưới set -e)"
fi
