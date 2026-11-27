# LNX-10 · Lab — HTTP/1.1 vs HTTP/2 vs HTTP/3

> ⚠️ **Cần kiểm chứng trên Linux:** Máy soạn là Windows 11 không có WSL/Docker nên phần cấu hình Nginx & `curl --http1.1` / `--http2` / `--http3` trong `scripts/nginx-http2-http3-compare.sh` **chưa được chạy kiểm chứng trực tiếp** (xem §5). Phần **Java 21 `HttpMultiplexingComparison` (loopback TCP thật) & `PacketLossHolSimulator`** đã chạy kiểm chứng thật 100% offline.

> **Mục tiêu:** tự đo trên loopback TCP thật hiện tượng Application HOL blocking (1 request chậm `200 ms` kìm 10 request nhanh) và cách Binary Frame Multiplexing trên **1 TCP socket** giải thoát 10 request nhanh, đồng thời định lượng TCP HOL blocking khi packet loss tăng (`0%` → `3%` → `5%`).
> **Thời lượng:** ~35 phút · **Cấp:** L2

## 0. Yêu cầu môi trường
| Công cụ | Phiên bản | Kiểm tra |
| :--- | :--- | :--- |
| JDK | 21+ | `java -version` |
| Maven | 3.9+ | `mvn -v` |
| Nginx + `curl` *(tùy chọn Linux)* | Nginx `>= 1.25` | `nginx -v` |

## 1. Cấu trúc
```text
lab/
├── README.md
├── pom.xml                                        # Maven project độc lập, Java 21
├── scripts/
│   ├── nginx-http2.conf                           # Config Nginx HTTP/2 edge + Keep-Alive tới JBoss
│   └── nginx-http2-http3-compare.sh               # Benchmark curl --http1.1 vs --http2 vs --http3 trên Linux
├── src/
│   ├── main/java/lab/linux/http/
│   │   ├── HttpMultiplexingComparison.java        # Loopback TCP: H1 (1 conn), H1 (6 conns), Binary Multiplexing (1 conn)
│   │   ├── PacketLossHolSimulator.java            # Mô phỏng TCP HOL blocking (HTTP/2) vs Independent Streams (HTTP/3 QUIC)
│   │   └── HttpVersionsRunner.java                # Runner chạy thực nghiệm
│   └── test/java/lab/linux/http/
│       └── HttpVersionsLabTest.java               # JUnit 5 kiểm chứng
└── solutions/
    └── ConcurrentHttp2ClientSolution.java         # Lời giải chuẩn Java 21 HttpClient Version.HTTP_2
```

## 2. Chạy

Từ thư mục `lab/`:
```powershell
mvn -o -B verify
mvn -o -B compile exec:java
```

## 3. Các bước thí nghiệm

### Bước 1 — Loopback TCP Multiplexing & Mô phỏng Packet Loss HOL
- **Dự đoán trước khi chạy** (ghi vào `my-notes.md`):
  1. Với `1` request chậm `200 ms` và `10` request nhanh `5 ms`, `median latency` của 10 request nhanh trong chế độ **HTTP/1.1 (1 TCP socket)** vs **Multiplexed (1 TCP socket)** khác nhau khoảng bao nhiêu ms?
  2. Khi packet loss = `3%` với `20` streams song song, kết nối **HTTP/2 (1 TCP)** chậm hơn **HTTP/3 (QUIC)** khoảng bao nhiêu?
- Làm gì: chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`.

### Bước 2 — Trên Linux với Nginx (khi có môi trường Linux/Docker)
```bash
bash scripts/nginx-http2-http3-compare.sh https://localhost:8443
```

## 4. Kết quả tham khảo khi chạy

> **Output thực tế trên máy soạn** *(Windows 11 amd64, OpenJDK 26.0.1 `--release 21`, ngày 2026-11-27)*:
> - `mvn -o -B verify`: **2/2 tests pass**, 0 failures, 0 errors.
> - `HttpMultiplexingComparison` (`1` request `200 ms` + `10` requests `5 ms` trên loopback TCP thật):
>   - `HTTP/1.1 (1 TCP conn)`: `tcpConns = 1 | totalWall = 377 ms | medianFast = 314 ms`
>   - `HTTP/1.1 (6 TCP conns)`: `tcpConns = 6 | totalWall = 241 ms | medianFast = 66 ms`
>   - `HTTP/2 Multiplexed (1 TCP conn)`: `tcpConns = 1 | totalWall = 212 ms | medianFast = 26 ms` (giảm median từ `314 ms` xuống `26 ms` chỉ trên 1 TCP socket!).
> - `PacketLossHolSimulator` (`20` streams × `10` packets, RTT `50 ms`): ở `Loss = 3.0%`, `HTTP/2 (1 TCP)` tốn `700 ms` (median `700 ms`) so với `HTTP/3 (QUIC)` `550 ms` (median `500 ms`); ở `Loss = 5.0%`, `HTTP/2` `750 ms` vs `HTTP/3` `600 ms` (median `500 ms`).

## 5. Cần kiểm chứng (chưa chạy trên Windows)
- Chạy Nginx với `scripts/nginx-http2.conf` và `bash scripts/nginx-http2-http3-compare.sh` để đo waterfall tải 20 resource qua TLS bằng `curl --http1.1` vs `curl --http2` (và `--http3` trên Nginx `>= 1.25`).
- Dùng `sudo tcpdump` / `wireshark` với SSLKEYLOGFILE quan sát frame `SETTINGS`, `HEADERS`, `DATA` và chỉ số nén HPACK trong HTTP/2 thật.

## 6. Giải thích kết quả
- Binary Frame Multiplexing loại bỏ hoàn toàn **Application HOL blocking** trên cùng 1 TCP connection; tuy nhiên ở tầng Transport, 1 TCP connection vẫn chịu **TCP HOL blocking** khi mất gói — lý do HTTP/3 chuyển sang QUIC trên UDP (xem [README §3](../README.md)).

## 7. Dọn dẹp
```powershell
mvn -o -B -q clean
```
