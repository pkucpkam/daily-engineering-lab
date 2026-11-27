# LNX-12 · Lab — TLS handshake & certificate chain

> ⚠️ **Cần kiểm chứng trên Linux:** Máy soạn là Windows 11 không có WSL/Docker nên phần kịch bản OpenSSL tạo CA 2 cấp (`Root CA -> Intermediate CA`) và Nginx mTLS trong `scripts/gen-ca-nginx-mtls.sh` **chưa được chạy kiểm chứng trực tiếp** (xem §5). Phần **Java 21 `TlsLabDemo` (`keytool` thật + `SSLServerSocket` TLS 1.3, tái hiện lỗi `PKIX`, SNI + Hostname SAN verification, và mTLS 2 chiều)** đã chạy kiểm chứng thật 100% offline.

> **Mục tiêu:** tận mắt chứng kiến ngoại lệ `PKIX path building failed` trên Java `SSLSocket` thật khi thiếu TrustStore, sửa đúng chuẩn bằng TrustStore + `HTTPS` Hostname Verification + `SNI`, và kiểm chứng **mTLS** (`needClientAuth = true`).
> **Thời lượng:** ~35 phút · **Cấp:** L2

## 0. Yêu cầu môi trường
| Công cụ | Phiên bản | Kiểm tra |
| :--- | :--- | :--- |
| JDK | 21+ (có sẵn `keytool`) | `java -version` |
| Maven | 3.9+ | `mvn -v` |
| OpenSSL + Nginx *(tùy chọn Linux)* | OpenSSL 1.1+ / 3.x | `openssl version` |

## 1. Cấu trúc
```text
lab/
├── README.md
├── pom.xml                                        # Maven project độc lập, Java 21
├── scripts/
│   ├── gen-ca-nginx-mtls.sh                       # Sinh Root CA -> Intermediate CA -> Server/Client cert trên Linux
│   └── nginx-mtls.conf                            # Config Nginx bật TLS 1.3 + mTLS (ssl_verify_client on)
├── src/
│   ├── main/java/lab/linux/tls/
│   │   ├── TlsLabDemo.java                        # Keytool PKCS12 + Real SSLServerSocket (TLS 1.3, PKIX, SNI, mTLS)
│   │   └── TlsLabRunner.java                      # Runner thực nghiệm
│   └── test/java/lab/linux/tls/
│       └── TlsLabTest.java                        # JUnit 5 kiểm chứng
└── solutions/
    └── CustomTrustStoreSolution.java              # Mẫu SSLContext chuẩn dùng TrustStore + HTTPS SAN check
```

## 2. Chạy

Từ thư mục `lab/`:
```powershell
mvn -o -B verify
mvn -o -B compile exec:java
```

## 3. Các bước thí nghiệm

### Bước 1 — Chạy tái hiện PKIX, TLS 1.3 + SNI & mTLS bằng JDK `SSLServerSocket`
- **Dự đoán trước khi chạy** (ghi vào `my-notes.md`):
  1. Khi client kết nối tới `SSLServerSocket` bằng `empty-truststore.p12`, Java ném lỗi gì?
  2. Khi bật `server.setNeedClientAuth(true)` (mTLS), client có `client-truststore.p12` nhưng **thiếu `client-keystore.p12`** có handshake được không?
- Làm gì: chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`.

### Bước 2 — Trên Linux với OpenSSL & Nginx (khi có môi trường Linux)
```bash
bash scripts/gen-ca-nginx-mtls.sh
```

## 4. Kết quả tham khảo khi chạy

> **Output thực tế trên máy soạn** *(Windows 11 amd64, OpenJDK 26.0.1 `--release 21`, ngày 2026-11-27)*:
> - `mvn -o -B verify`: **2/2 tests pass**, 0 failures, 0 errors.
> - `TlsLabRunner` (dùng `keytool` + `SSLServerSocket` trên loopback `127.0.0.1`):
>   - TrustStore sai (`wrongTruststore`): ném `SSLHandshakeException: (certificate_unknown) PKIX path building failed: ... unable to find valid certification path to requested target`.
>   - TrustStore hợp lệ + `SNIHostName("localhost")` + `HTTPS`: `protocol = TLSv1.3 | cipher = TLS_AES_256_GCM_SHA384 | reply = TLS_OK:PING`.
>   - mTLS (`needClientAuth = true`): client thiếu KeyStore bị từ chối handshake; client đủ KeyStore + TrustStore đàm phán `TLSv1.3` thành công (`reply = MTLS_OK:PING`).

## 5. Cần kiểm chứng (chưa chạy trên Windows)
- `bash scripts/gen-ca-nginx-mtls.sh` để sinh chuỗi 3 cấp bằng `openssl` thật và kiểm chứng `nginx-mtls.conf`.
- `openssl s_client -connect localhost:8443 -servername api.lab.local -tls1_3 -showcerts` để quan sát thứ tự `Certificate chain` gửi ra từ Nginx.

## 6. Giải thích kết quả
- TrustStore kiểm soát quyền tin cậy danh tính đối tác (thiếu CA hoặc thiếu Intermediate trên server gây `PKIX path building failed`), trong khi KeyStore chứa Private Key chứng minh danh tính bản thân cho TLS server và mTLS client (xem [README §3](../README.md)).

## 7. Dọn dẹp
```powershell
mvn -o -B -q clean
```
