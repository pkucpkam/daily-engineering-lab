# LNX-13 · Lab — Load balancer L4 vs L7 & Nginx reverse proxy

> ⚠️ **Cần kiểm chứng trên Linux:** Máy soạn là Windows 11 không có WSL/Docker nên phần cấu hình Nginx thật & script `kill -9` 1 trong 3 backend trong `scripts/test-nginx-failover.sh` **chưa được chạy kiểm chứng trực tiếp** (xem §5). Phần **Java 21 `MiniL7ReverseProxy` (L7 Proxy loopback TCP thật nối tới 3 Backend HTTP servers)** đã chạy kiểm chứng thật 100% offline.

> **Mục tiêu:** tự tay chạy 1 Reverse Proxy L7 loopback cân tải **3 backend HTTP thật** để quan sát: (1) `LEAST_CONNECTIONS` né node chậm so với `ROUND_ROBIN`, (2) Passive Health Check cô lập node chết khi 1 backend bị kill, và (3) hậu quả `504 Gateway Timeout` nhưng backend vẫn `COMMIT` khi đặt sai chuỗi timeout!
> **Thời lượng:** ~35 phút · **Cấp:** L2

## 0. Yêu cầu môi trường
| Công cụ | Phiên bản | Kiểm tra |
| :--- | :--- | :--- |
| JDK | 21+ | `java -version` |
| Maven | 3.9+ | `mvn -v` |
| Nginx + `curl` *(tùy chọn Linux)* | Nginx 1.24+ | `nginx -v` |

## 1. Cấu trúc
```text
lab/
├── README.md
├── pom.xml                                        # Maven project độc lập, Java 21
├── scripts/
│   ├── nginx-lb-3backends.conf                    # Config Nginx cân tải 3 backend (least_conn, keepalive, max_fails)
│   └── test-nginx-failover.sh                     # Bắn traffic + kill 1 backend quan sát failover trên Linux
├── src/
│   ├── main/java/lab/linux/lb/
│   │   ├── MiniL7ReverseProxy.java                # L7 Proxy + 3 Loopback Backends (RR, LeastConn, Passive Health Check, Timeout)
│   │   └── LoadBalancerRunner.java                # Runner thực nghiệm
│   └── test/java/lab/linux/lb/
│       └── LoadBalancerLabTest.java               # JUnit 5 kiểm chứng
└── solutions/
    └── FailoverGracefulProxySolution.java         # Mẫu xử lý retry an toàn (GET vs POST) khi failover
```

## 2. Chạy

Từ thư mục `lab/`:
```powershell
mvn -o -B verify
mvn -o -B compile exec:java
```

## 3. Các bước thí nghiệm

### Bước 1 — Least Connections, Passive Health Check Failover & Timeout Mismatch
- **Dự đoán trước khi chạy** (ghi vào `my-notes.md`):
  1. Nếu Backend #1 chậm `80 ms`, #2 & #3 nhanh `5 ms`, `ROUND_ROBIN` vs `LEAST_CONNECTIONS` gửi bao nhiêu trong `30` requests tới Backend #1?
  2. Nếu đặt `proxyReadTimeout = 60 ms` nhưng Backend mất `180 ms` mới xong `POST /transfer`, client nhận mã lỗi gì và biến `completedCommits` ở backend bằng `0` hay `1`?
- Làm gì: chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`.

### Bước 2 — Trên Linux với Nginx (khi có môi trường Linux/Docker)
```bash
bash scripts/test-nginx-failover.sh http://localhost:8080
```

## 4. Kết quả tham khảo khi chạy

> **Output thực tế trên máy soạn** *(Windows 11 amd64, OpenJDK 26.0.1 `--release 21`, ngày 2026-11-27)*:
> - `mvn -o -B verify`: **3/3 tests pass**, 0 failures, 0 errors.
> - `LoadBalancerRunner` (`30` requests từ `6` workers × `5`, Backend #1 chậm `150 ms` vs #2 & #3 `5 ms`):
>   - `ROUND_ROBIN`: `slow-1 = 10 | fast-2 = 10 | fast-3 = 10` (`598 ms`).
>   - `LEAST_CONNECTIONS`: `slow-1 = 6 | fast-2 = 14 | fast-3 = 10` (`875 ms` — giảm `40%` tải đổ vào node chậm).
> - `Failover max_fails=2`: kill `node-2`, `15/15` GET requests trả **`200 OK`**, `node-2` chỉ bị thử đúng **`2` lần** rồi bị cách ly.
> - `Timeout Mismatch`: `proxyReadTimeout = 60 ms` vs `backend = 180 ms` → client nhận **`504`**, trong khi `backendCompletedCommits = 1`.

## 5. Cần kiểm chứng (chưa chạy trên Windows)
- Chạy Nginx với `scripts/nginx-lb-3backends.conf` nối tới 3 server (ví dụ Python `http.server` hoặc Java backend tại ports `8081..8083`), kill 1 instance và chạy `scripts/test-nginx-failover.sh` để quan sát `max_fails=2` hoạt động trên Nginx thật.

## 6. Giải thích kết quả
- `LEAST_CONNECTIONS` tránh đẩy thêm tải vào backend còn nhiều request in-flight; Passive Health Check cách ly node chết sau `max_fails`; và chuỗi timeout phải giữ `Nginx > App` để không sinh `504` giả kèm side-effect đã commit (xem [README §3](../README.md)).

## 7. Dọn dẹp
```powershell
mvn -o -B -q clean
```
