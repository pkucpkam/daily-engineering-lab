#!/usr/bin/env bash
set -euo pipefail

# Lab LNX-10 (chạy trên Linux với Nginx >= 1.25 + curl hỗ trợ HTTP/2 / HTTP/3):
# So sánh thời gian tải 20 resource song song qua HTTP/1.1 vs HTTP/2 vs HTTP/3.

BASE_URL="${1:-https://localhost:8443}"

echo "=== [1] Kiểm tra phiên bản curl và giao thức hỗ trợ ==="
curl -V | head -n 4

echo ""
echo "=== [2] Thử 1 request qua HTTP/1.1 ==="
curl -sk --http1.1 -o /dev/null -w "http_version=%{http_version} time_connect=%{time_connect}s time_appconnect=%{time_appconnect}s time_total=%{time_total}s\n" "$BASE_URL/fast"

echo ""
echo "=== [3] Thử 1 request qua HTTP/2 ==="
curl -sk --http2 -o /dev/null -w "http_version=%{http_version} time_connect=%{time_connect}s time_appconnect=%{time_appconnect}s time_total=%{time_total}s\n" "$BASE_URL/fast"

echo ""
echo "=== [4] Thử 1 request qua HTTP/3 (nếu curl & Nginx hỗ trợ QUIC) ==="
if curl -V | grep -qi "HTTP3"; then
  curl -sk --http3-only -o /dev/null -w "http_version=%{http_version} time_connect=%{time_connect}s time_appconnect=%{time_appconnect}s time_total=%{time_total}s\n" "$BASE_URL/fast" || true
else
  echo "curl hiện tại chưa biên dịch kèm nghttp3/HTTP3."
fi
