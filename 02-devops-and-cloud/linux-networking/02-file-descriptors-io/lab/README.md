# LNX-02 · Lab — File descriptor & I/O

> ⚠️ **Cần kiểm chứng trên Linux:** Máy soạn là Windows 11 không có WSL/Docker nên các script `reproduce-emfile.sh`, `inspect-java-fds.sh` và lệnh `ulimit -n`, `/proc/<pid>/limits`, `lsof -nP` **chưa được chạy kiểm chứng trực tiếp** (xem §5). Phần **Java 21 `BoundedFdTable`, loopback `SocketChannel` & `O_NONBLOCK`** đã chạy kiểm chứng thật 100% offline.

> **Mục tiêu:** tận mắt thấy socket leak chiếm cạn bảng FD và đánh sập luôn cả thao tác mở file log không liên quan, cách `try-with-resources` giữ số FD ở mức hằng số qua 1.000+ request, và `O_NONBLOCK` trả về `0` tức thì.
> **Thời lượng:** ~35 phút · **Cấp:** L2

## 0. Yêu cầu môi trường
| Công cụ | Phiên bản | Kiểm tra |
| :--- | :--- | :--- |
| JDK | 21+ | `java -version` |
| Maven | 3.9+ | `mvn -v` |
| Linux / WSL *(tùy chọn)* | Ubuntu 22.04+ (`lsof`) | `lsof -v` |

## 1. Cấu trúc
```text
lab/
├── README.md
├── pom.xml                                  # Maven project độc lập, Java 21
├── scripts/
│   ├── reproduce-emfile.sh                  # Ép ulimit -n 64 để tái hiện EMFILE trên Linux
│   └── inspect-java-fds.sh                  # Checklist 4 bước /proc + lsof -nP trên Linux
├── src/
│   ├── main/java/lab/linux/fd/
│   │   ├── BoundedFdTable.java              # Mô hình bảng FD 0,1,2..softLimit của Linux kernel
│   │   ├── NonBlockingIoDemo.java           # SocketChannel.configureBlocking(false)
│   │   └── FdLeakLabRunner.java             # Runner demo FD leak + loopback socket + O_NONBLOCK
│   └── test/java/lab/linux/fd/
│       └── FdLeakTest.java                  # JUnit 5 kiểm chứng
└── solutions/
    └── AutoClosingHttpWrapperTest.java      # Lời giải E6
```

## 2. Chạy

Từ thư mục `lab/`:
```powershell
mvn -o -B verify
mvn -o -B compile exec:java
```

## 3. Các bước thí nghiệm

### Bước 1 — Tái hiện cạn FD & dập bằng `try-with-resources`
- **Dự đoán trước khi chạy** (ghi vào `my-notes.md`):
  1. Nếu `softLimit = 16` và `0,1,2` đã dùng cho stdio, mở được tối đa bao nhiêu socket trước khi `openSocket` ném `EMFILE`?
  2. Sau khi đóng FD bằng `try-with-resources`, số FD cấp cho vòng lặp thứ `999` là bao nhiêu?
- Làm gì: chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`.

### Bước 2 — Trên Linux / WSL (khi có môi trường Linux)
```bash
bash scripts/reproduce-emfile.sh
bash scripts/inspect-java-fds.sh <JAVA_PID>
```

## 4. Kết quả tham khảo khi chạy

> **Output thực tế trên máy soạn** *(Windows 11 amd64, OpenJDK 26.0.1 `--release 21`, ngày 2026-11-27)*:
> - `mvn -o -B verify`: **4/4 tests pass**, 0 failures, 0 errors.
> - `FdLeakLabRunner`:
>   - `[1] Socket leak chạm trần => EMFILE: Too many open files (open=16, softLimit=16) while opening TCP:10.20.9.88:8080`
>   - `Ghi log file sau khi leak socket cũng chết theo => EMFILE: ... while opening /opt/jboss/standalone/log/server.log`
>   - `[2] Loopback sockets`: 40 kết nối rò rỉ giữ mở song song vs 100 vòng `try-with-resources` sót `0` socket mở.
>   - `[3] Non-blocking SocketChannel.read()` khi buffer rỗng trả về `bytesRead=0`.

## 5. Cần kiểm chứng (chưa chạy trên Windows)
- `bash scripts/reproduce-emfile.sh` (`ulimit -n 64` tạo `Too many open files` thực tế trên Linux kernel).
- `bash scripts/inspect-java-fds.sh <PID>` (`/proc/<pid>/limits`, `/proc/<pid>/fd`, `lsof -nP -p <pid>`, `lsof +L1` xem `(deleted)`).

## 6. Giải thích kết quả
- Toàn bộ file, socket, pipe trong một process chia sẻ chung bảng FD bị giới hạn bởi `RLIMIT_NOFILE`. Đóng đúng lúc bằng `try-with-resources` (Java 7+) trả FD về kernel ngay lập tức để tái sử dụng (xem [README §3.1–3.2](../README.md)).

## 7. Dọn dẹp
```powershell
mvn -o -B -q clean
```
