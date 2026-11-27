#!/usr/bin/env bash
set -euo pipefail

# Lab LNX-12 (chạy trên Linux với openssl & keytool):
# Dựng Root CA -> Intermediate CA -> cấp cert cho Nginx (fullchain) & Client (mTLS) + import vào Java TrustStore.

OUT_DIR="${1:-./certs-out}"
mkdir -p "$OUT_DIR"
cd "$OUT_DIR"

echo "=== [1] Tạo Root CA ==="
openssl genrsa -out rootCA.key 2048
openssl req -x509 -new -nodes -key rootCA.key -sha256 -days 365 \
  -subj "/CN=Lab Root CA" -out rootCA.crt

echo "=== [2] Tạo Intermediate CA (có basicConstraints CA:TRUE) ==="
cat > inter-ext.cnf <<'EOF'
basicConstraints = critical, CA:TRUE, pathlen:0
keyUsage = critical, digitalSignature, cRLSign, keyCertSign
EOF
openssl genrsa -out intermediateCA.key 2048
openssl req -new -key intermediateCA.key -subj "/CN=Lab Intermediate CA" -out intermediateCA.csr
openssl x509 -req -in intermediateCA.csr -CA rootCA.crt -CAkey rootCA.key -CAcreateserial \
  -out intermediateCA.crt -days 180 -sha256 -extfile inter-ext.cnf

echo "=== [3] Cấp Server Cert có SAN và ghép server-fullchain.pem ==="
cat > server-ext.cnf <<'EOF'
subjectAltName = DNS:api.lab.local,DNS:localhost,IP:127.0.0.1
extendedKeyUsage = serverAuth
EOF
openssl genrsa -out server.key 2048
openssl req -new -key server.key -subj "/CN=api.lab.local" -out server.csr
openssl x509 -req -in server.csr -CA intermediateCA.crt -CAkey intermediateCA.key -CAcreateserial \
  -out server.crt -days 90 -sha256 -extfile server-ext.cnf
cat server.crt intermediateCA.crt > server-fullchain.pem

echo "=== [4] Cấp Client Cert cho mTLS ==="
cat > client-ext.cnf <<'EOF'
extendedKeyUsage = clientAuth
EOF
openssl genrsa -out client.key 2048
openssl req -new -key client.key -subj "/CN=jboss-client-01" -out client.csr
openssl x509 -req -in client.csr -CA intermediateCA.crt -CAkey intermediateCA.key -CAcreateserial \
  -out client.crt -days 90 -sha256 -extfile client-ext.cnf

echo "=== [5] Kiểm tra lệnh Nginx & s_client ==="
echo "Xem chain: openssl verify -CAfile rootCA.crt -untrusted intermediateCA.crt server.crt"
openssl verify -CAfile rootCA.crt -untrusted intermediateCA.crt server.crt
