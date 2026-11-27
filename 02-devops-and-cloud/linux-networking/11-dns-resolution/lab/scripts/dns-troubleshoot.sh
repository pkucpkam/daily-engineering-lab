#!/usr/bin/env bash
set -euo pipefail

# Lab LNX-11 (chạy trên Linux / Kubernetes Pod):
# Kiểm tra /etc/resolv.conf, trace chuỗi phân giải DNS đầy đủ và quan sát TTL caching.

DOMAIN="${1:-api.github.com}"

echo "=== [1] Cấu hình Stub Resolver (/etc/resolv.conf & nsswitch.conf) ==="
cat /etc/resolv.conf
grep '^hosts:' /etc/nsswitch.conf || true

echo ""
echo "=== [2] Trace chuỗi Root -> TLD -> Authoritative ==="
if command -v dig >/dev/null; then
  dig +trace +nodnssec "$DOMAIN" | tail -n 25
  echo ""
  echo "=== [3] Quan sát TTL giảm dần trên Recursive Resolver ==="
  dig +noall +answer +ttlid "$DOMAIN"
else
  echo "Không tìm thấy lệnh 'dig' (cài dnsutils / bind-utils). Thử fallback getent:"
  getent ahosts "$DOMAIN" || true
fi

echo ""
echo "=== [4] Gợi ý bắt gói tin DNS (UDP/TCP 53) khi nghi ngờ bão ndots:5 ==="
echo "  sudo tcpdump -i any -nn -c 30 port 53"
