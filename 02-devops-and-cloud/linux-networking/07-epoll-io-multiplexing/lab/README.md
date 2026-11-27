# LNX-07 · Lab — select, poll, epoll & event loop

> ⚠️ **Cần kiểm chứng trên Linux:** Máy soạn là Windows 11 không có WSL/Docker nên `SelectorProvider` trên máy soạn chạy `WEPollSelectorProvider` của Windows thay vì Linux `EPollSelectorProvider`, và script `inspect-epoll-fd.sh` (`anon_inode:[eventpoll]`, `/proc/<pid>/fdinfo/`) **chưa chạy kiểm chứng trên Linux** (xem §5). Phần **Java 21 NIO `Selector` EventLoop vs Blocking BIO Server & EventLoop Blocking benchmark** đã chạy thật 100% qua loopback TCP.

> **Mục tiêu:** tận mắt thấy 1 EventLoop thread phục vụ 200 kết nối song song, BIO pool 4 threads chết đói ở kết nối thứ 5, và 1 task blocking 250ms trên EventLoop làm đóng băng toàn bộ client vô tội.
> **Thời lượng:** ~35 phút · **Cấp:** L2

## 0. Yêu cầu môi trường
| Công cụ | Phiên bản | Kiểm tra |
| :--- | :--- | :--- |
| JDK | 21+ | `java -version` |
| Maven | 3.9+ | `mvn -v` |
| Linux *(tùy chọn)* | Kernel 2.6+ | `lsof -v` |

## 1. Cấu trúc
```text
lab/
├── README.md
├── pom.xml                                  # Maven project độc lập, Java 21
├── scripts/
│   └── inspect-epoll-fd.sh                  # Soi anon_inode:[eventpoll] & fdinfo trên Linux
├── src/
│   ├── main/java/lab/linux/epoll/
│   │   ├── BlockingBioEchoServer.java       # BIO Thread-per-request (pool nhỏ dễ bị starvation)
│   │   ├── NioSelectorEchoServer.java       # NIO Selector 1 EventLoop thread duy nhất
│   │   └── EpollLabRunner.java              # Runner đo 200 connections + BIO vs NIO + Block EventLoop
│   └── test/java/lab/linux/epoll/
│       └── EpollEventLoopTest.java          # JUnit 5 kiểm chứng
└── solutions/
    └── OffloadedEventLoopTest.java          # Lời giải E7 (offload blocking work + selector.wakeup)
```

## 2. Chạy

Từ thư mục `lab/`:
```powershell
mvn -o -B verify
mvn -o -B compile exec:java
```

## 3. Các bước thí nghiệm

### Bước 1 — Quan sát NIO (1 thread) vs BIO (4 threads) & EventLoop Stall
- **Dự đoán trước khi chạy** (ghi vào `my-notes.md`):
  1. Khi `4` client giữ kết nối mở im lặng vào `BlockingBioEchoServer(4)`, client thứ `5` gửi `"hello"` mất bao lâu hay timeout?
  2. Khi client `"SLOW"` khiến EventLoop gọi `Thread.sleep(250)`, client `"FAST"` kết nối ngay sau đó phải chờ tối thiểu bao nhiêu mili-giây?
- Làm gì: chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`.

### Bước 2 — Trên Linux (khi có môi trường Linux)
```bash
bash scripts/inspect-epoll-fd.sh <JAVA_PID>
```

## 4. Kết quả tham khảo khi chạy

> **Output thực tế trên máy soạn** *(Windows 11 amd64, OpenJDK 26.0.1 `--release 21`, ngày 2026-11-27)*:
> - `mvn -o -B verify`: **3/3 tests pass**, 0 failures, 0 errors.
> - `EpollLabRunner`:
>   - `[1] NIO Selector (threads=1)`: giữ mở 200 concurrent connections và echo `#200` thành công chỉ với `1` EventLoop thread trong `344 ms`.
>   - `[2] BIO pool=4`: client thứ 5 ném `SocketTimeoutException` khi 4 client đầu im lặng.
>   - `[3] Block EventLoop 250ms`: client `'FAST'` phải chờ `231 ms` vì nằm chung EventLoop.

## 5. Cần kiểm chứng (chưa chạy trên Windows)
- `bash scripts/inspect-epoll-fd.sh <JAVA_PID>` (soi `anon_inode:[eventpoll]` và `tfd:` trong `/proc/<pid>/fdinfo/`).
- Xác nhận `SelectorProvider` trên Linux là `sun.nio.ch.EPollSelectorProvider` và thử tải `10.000` kết nối sau khi nâng `ulimit -n 20000`.

## 6. Giải thích kết quả
- Non-blocking I/O kết hợp `Selector`/`epoll` cho phép tách số thread khỏi số lượng kết nối đồng thời, nhưng đòi hỏi giữ EventLoop thread hoàn toàn không bị block (xem [README §3.3–3.4](../README.md)).

## 7. Dọn dẹp
```powershell
mvn -o -B -q clean
```
