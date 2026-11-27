# LNX-09 · Lab — TIME_WAIT, CLOSE_WAIT & socket exhaustion

> ⚠️ **Cần kiểm chứng trên Linux:** Máy soạn là Windows 11 không có WSL/Docker nên các lệnh Linux kernel (`ss -s`, `ss -tanp state close-wait`, `lsof`, `sysctl ip_local_port_range`) trong `scripts/inspect-socket-states.sh` **chưa được chạy kiểm chứng trực tiếp** (xem §5). Phần **Java 21 `SocketExhaustionDemo` (loopback TCP thật) & `EphemeralPortBudgetSimulator`** đã chạy kiểm chứng thật 100% offline.

> **Mục tiêu:** quan sát tận mắt chênh lệch số kết nối TCP giữa `No-Pool` vs `HTTP Keep-Alive Pool` trên loopback thật, tái hiện rò rỉ socket giữ trạng thái mở (`CLOSE_WAIT` leak), và mô phỏng thời điểm cạn `28.232` ephemeral ports (`EADDRNOTAVAIL`).
> **Thời lượng:** ~35 phút · **Cấp:** L2

## 0. Yêu cầu môi trường
| Công cụ | Phiên bản | Kiểm tra |
| :--- | :--- | :--- |
| JDK | 21+ | `java -version` |
| Maven | 3.9+ | `mvn -v` |
| Linux (`iproute2`, `lsof`) *(tùy chọn)* | Kernel 4.x+ | `ss -v` |

## 1. Cấu trúc
```text
lab/
├── README.md
├── pom.xml                                        # Maven project độc lập, Java 21
├── scripts/
│   └── inspect-socket-states.sh                   # Soi TIME_WAIT, CLOSE_WAIT, ip_local_port_range trên Linux
├── src/
│   ├── main/java/lab/linux/sockets/
│   │   ├── SocketExhaustionDemo.java              # Loopback TCP server/client: No-Pool vs Keep-Alive & Leak
│   │   ├── EphemeralPortBudgetSimulator.java      # Mô phỏng bảng 4-tuple & TIME_WAIT 60s
│   │   └── SocketLabRunner.java                   # Runner thực nghiệm
│   └── test/java/lab/linux/sockets/
│       └── SocketExhaustionLabTest.java           # JUnit 5 kiểm chứng
└── solutions/
    └── HttpClientPoolSolution.java                # Lời giải E7: tái sử dụng connection + đóng sạch kể cả HTTP 500
```

## 2. Chạy

Từ thư mục `lab/`:
```powershell
mvn -o -B verify
mvn -o -B compile exec:java
```

## 3. Các bước thí nghiệm

### Bước 1 — Chạy loopback demo & mô phỏng Ephemeral Port Exhaustion
- **Dự đoán trước khi chạy** (ghi vào `my-notes.md`):
  1. Gửi `200` HTTP requests trên loopback `127.0.0.1` bằng `HttpClient` Keep-Alive pool thì server gọi `accept()` bao nhiêu lần so với mở mới từng kết nối?
  2. Với `28.232` ephemeral ports (`32768..60999`) và `TIME_WAIT = 60s`, ở tốc độ `600 req/s` đóng mỗi request, lỗi `EADDRNOTAVAIL` xuất hiện ở giây thứ mấy?
- Làm gì: chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`.

### Bước 2 — Trên Linux (khi có môi trường Linux)
```bash
bash scripts/inspect-socket-states.sh
```

## 4. Kết quả tham khảo khi chạy

> **Output thực tế trên máy soạn** *(Windows 11 amd64, OpenJDK 26.0.1 `--release 21`, ngày 2026-11-27)*:
> - `mvn -o -B verify`: **4/4 tests pass**, 0 failures, 0 errors.
> - `SocketLabRunner` (`200` HTTP requests trên loopback `127.0.0.1`): `No-Pool = 200 acceptedConns | 804 ms` vs `Keep-Alive Pool (HttpClient đã warmup 1 kết nối) = 0 acceptedConns mới | 670 ms`.
> - `LeakyClient`: `Leaked sockets = 32 / budget = 32 | exhausted = true` (mô phỏng cạn FD khi quên `close()` sau `FIN`).
> - `EphemeralPortBudgetSimulator` (`32768..60999` = `28.232` ports, `TIME_WAIT = 60s`): `600 req/s` chạm **`EADDRNOTAVAIL` ở giây thứ `48`** (`failedReqs = 7768`), còn `Pool-16` giữ ổn định `16` ports (`failedReqs = 0`).
> - `HttpClientPoolSolution`: hoàn tất `100` requests (`200 = 80, 500 = 20`) chỉ qua **`1` TCP connection**.

## 5. Cần kiểm chứng (chưa chạy trên Windows)
- `bash scripts/inspect-socket-states.sh` (`ss -s`, `ss -tan state time-wait`, `ss -tanp state close-wait`, `sysctl net.ipv4.ip_local_port_range net.ipv4.tcp_tw_reuse net.ipv4.tcp_fin_timeout`).
- Quan sát socket `CLOSE_WAIT` bằng `lsof -p <PID> | grep CLOSE_WAIT` trên Linux thật khi chạy Java process giữ socket chưa đóng.

## 6. Giải thích kết quả
- Giữ TCP socket qua HTTP Connection Pool giúp `200` requests chỉ tốn `1–2` TCP handshakes (không tạo `TIME_WAIT` churn làm cạn dải `28.232` ephemeral ports), và đóng đầy đủ cả stream thành công lẫn `ErrorStream` ngăn triệt để socket rò rỉ ở `CLOSE_WAIT` (xem [README §3](../README.md)).

## 7. Dọn dẹp
```powershell
mvn -o -B -q clean
```
