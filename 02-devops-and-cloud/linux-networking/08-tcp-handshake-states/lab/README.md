# LNX-08 · Lab — TCP: handshake, state machine, backlog

> ⚠️ **Cần kiểm chứng trên Linux:** Máy soạn là Windows 11 không có WSL/Docker nên các thông số kernel Linux (`somaxconn`, `tcp_abort_on_overflow`, Linux `40 ms` Delayed ACK), `ss -lnt`, `nstat`, và `tcpdump` trong `scripts/observe-tcp-backlog.sh` **chưa được chạy kiểm chứng trực tiếp** (xem §5). Phần **Java 21 `KeepAliveBenchmark` (loopback TCP thật) & `TcpStateMachine`** đã chạy kiểm chứng thật 100% offline.

> **Mục tiêu:** tự tay đo chênh lệch tốc độ giữa Keep-Alive reuse và mở kết nối TCP mới cho 200 requests trên loopback thật, và mô phỏng chính xác 2 chế độ tràn Accept Queue (`tcp_abort_on_overflow = 0` vs `= 1`).
> **Thời lượng:** ~35 phút · **Cấp:** L2

## 0. Yêu cầu môi trường
| Công cụ | Phiên bản | Kiểm tra |
| :--- | :--- | :--- |
| JDK | 21+ | `java -version` |
| Maven | 3.9+ | `mvn -v` |
| Linux (`iproute2`, `tcpdump`) *(tùy chọn)* | Kernel 4.x+ | `ss -v` |

## 1. Cấu trúc
```text
lab/
├── README.md
├── pom.xml                                  # Maven project độc lập, Java 21
├── scripts/
│   └── observe-tcp-backlog.sh               # Kiểm tra somaxconn, ss -lnt, nstat & tcpdump trên Linux
├── src/
│   ├── main/java/lab/linux/tcp/
│   │   ├── KeepAliveBenchmark.java          # Benchmark loopback 200 reqs: NewConn vs KeepAlive
│   │   ├── TcpStateMachine.java             # Mô phỏng SYN/Accept Queue & 3-way/4-way state
│   │   └── TcpLabRunner.java                # Runner chạy benchmark & mô phỏng
│   └── test/java/lab/linux/tcp/
│       └── TcpHandshakeLabTest.java         # JUnit 5 kiểm chứng
└── solutions/
    └── TcpDropAndRetransmitTest.java        # Lời giải E7 (tcp_abort_on_overflow = 1)
```

## 2. Chạy

Từ thư mục `lab/`:
```powershell
mvn -o -B verify
mvn -o -B compile exec:java
```

## 3. Các bước thí nghiệm

### Bước 1 — Benchmark Keep-Alive vs New TCP Connection & State Machine
- **Dự đoán trước khi chạy** (ghi vào `my-notes.md`):
  1. Trên loopback `127.0.0.1` với `200` requests, mở mới 200 TCP connections chậm hơn Keep-Alive khoảng bao nhiêu lần?
  2. Khi Accept Queue (`cap = 2`) đầy và `tcp_abort_on_overflow = 0`, socket client #3 ở trạng thái gì và socket trên server nằm ở queue nào?
- Làm gì: chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`.

### Bước 2 — Trên Linux (khi có môi trường Linux)
```bash
bash scripts/observe-tcp-backlog.sh 8080
```

## 4. Kết quả tham khảo khi chạy

> **Output thực tế trên máy soạn** *(Windows 11 amd64, OpenJDK 26.0.1 `--release 21`, ngày 2026-11-27)*:
> - `mvn -o -B verify`: **3/3 tests pass**, 0 failures, 0 errors.
> - `KeepAliveBenchmark` (200 requests trên loopback `127.0.0.1`): `NewConn = 363 ms | KeepAlive = 29 ms` (**nhanh hơn ~`12.2×`**).
> - `TcpStateMachine`: tái hiện chính xác `TcpExtListenOverflows` khi Accept Queue tràn, `TIME_WAIT` trên active close và `CLOSE_WAIT` khi passive quên đóng.

## 5. Cần kiểm chứng (chưa chạy trên Windows)
- `bash scripts/observe-tcp-backlog.sh 8080` (`sysctl net.core.somaxconn`, `ss -lnt` đọc `Recv-Q`/`Send-Q`, `nstat -az TcpExtListenOverflows TcpExtListenDrops`).
- `sudo tcpdump -i lo -nn -S port 8080` (bắt gói tin `[S]`, `[S.]`, `[.]`, `[P.]`, `[F.]`, `[R.]` thật).
- Đo thực nghiệm hiệu ứng trễ `40 ms` của Nagle + Delayed ACK trên Linux loopback.

## 6. Giải thích kết quả
- Giữ TCP connection qua Keep-Alive loại bỏ chi phí 3-way handshake, cấp phát FD và teardown lặp lại; còn kiểm soát `min(backlog, somaxconn)` giúp ngăn lỗi drop `ACK` âm thầm khi burst traffic (xem [README §3.2–3.3](../README.md)).

## 7. Dọn dẹp
```powershell
mvn -o -B -q clean
```
