#!/usr/bin/env bash
set -euo pipefail

# Lab LNX-13 (chạy trên Linux với Nginx + 3 backend ở ports 8081, 8082, 8083):
# Bắn 20 request liên tiếp trước và sau khi kill 1 instance để quan sát passive health check.

PROXY_URL="${1:-http://localhost:8080/api/ping}"

echo "=== [1] Gửi 9 request khi cả 3 backend đều sống ==="
for i in $(seq 1 9); do
  curl -s -w "req=$i status=%{http_code} body=" "$PROXY_URL"
  echo ""
done

echo ""
echo "=== [2] Giờ hãy tắt/kill instance ở port 8082 ở terminal khác, rồi nhấn Enter để gửi tiếp 9 request ==="
read -r
for i in $(seq 10 18); do
  curl -s -w "req=$i status=%{http_code} body=" "$PROXY_URL"
  echo ""
done
