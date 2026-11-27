# LNX-01 · Lab — Process, thread & signal

> ⚠️ **Cần kiểm chứng trên Linux:** Máy soạn là Windows 11 không có WSL/Docker nên các script bash Linux (`pid1-shell-vs-exec.sh`, `observe-zombie-and-signals.sh`) và lệnh `ps`, `kill -3`, `docker stop` **chưa được chạy kiểm chứng trực tiếp** (xem §5). Phần **Java 21 `ProcessBuilder` & Simulator** đã chạy kiểm chứng thật 100% offline.

> **Mục tiêu:** tận mắt thấy JVM shutdown hook giữ cho in-flight requests hoàn tất trước khi thoát (khác hẳn với `kill -9` cắt ngang giữa chừng), và mô phỏng cơ chế Zombie / D-state / PID 1 reaper.
> **Thời lượng:** ~35 phút · **Cấp:** L2

## 0. Yêu cầu môi trường
| Công cụ | Phiên bản | Kiểm tra |
| :--- | :--- | :--- |
| JDK | 21+ | `java -version` |
| Maven | 3.9+ | `mvn -v` |
| Linux / WSL / Docker *(tùy chọn)* | Ubuntu 22.04+ | `uname -a` |

## 1. Cấu trúc
```text
lab/
├── README.md
├── pom.xml                                  # Maven project độc lập, Java 21
├── scripts/
│   ├── pid1-shell-vs-exec.sh                # So sánh sh không exec vs exec trên Linux
│   └── observe-zombie-and-signals.sh        # Quan sát Z-state & kill -3 trên Linux
├── src/
│   ├── main/java/lab/linux/processes/
│   │   ├── GracefulShutdownApp.java         # Child JVM xử lý in-flight + ShutdownHook
│   │   ├── ProcessLifecycleSimulator.java   # Mô phỏng state R/D/Z & PID 1 reaper
│   │   └── ProcessLifecycleLabRunner.java   # Runner chạy thật ProcessBuilder
│   └── test/java/lab/linux/processes/
│       └── ProcessLifecycleTest.java        # JUnit 5 kiểm chứng drain vs kill -9
└── solutions/
    ├── ShutdownTimeoutTest.java             # Lời giải E6
    └── ZombieReaperSimulationTest.java      # Lời giải E8
```

## 2. Chạy

### Phần chạy thật trên Windows & Linux (Java 21)
Từ thư mục `lab/`:
```powershell
mvn -o -B verify
mvn -o -B compile exec:java
```

## 3. Các bước thí nghiệm

### Bước 1 — So sánh Graceful Shutdown vs `destroyForcibly()`
- **Dự đoán trước khi chạy** (ghi vào `my-notes.md`):
  1. Khi có `3` request đang chạy (`250ms`/request) và app nhận tín hiệu dừng graceful với timeout `3000ms`, có bao nhiêu request hoàn thành trước khi exit?
  2. Nếu bị cắt ngang bằng `destroyForcibly()` (tương đương `SIGKILL`), exit code bằng `0` hay khác `0`?
- Làm gì: chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`.

### Bước 2 — Thực hành trên máy Linux / Docker (khi có môi trường Linux)
```bash
bash scripts/pid1-shell-vs-exec.sh
bash scripts/observe-zombie-and-signals.sh
```

## 4. Kết quả tham khảo khi chạy

> **Output thực tế trên máy soạn** *(Windows 11 amd64, OpenJDK 26.0.1 `--release 21`, ngày 2026-11-27)*:
> - `mvn -o -B verify`: **3/3 tests pass**, 0 failures, 0 errors.
> - `ProcessLifecycleLabRunner`:
>   - `[1] Graceful shutdown`: `exitCode=0, completedTasks=3` (toàn bộ 3 in-flight tasks hoàn tất gọn trước khi exit).
>   - `[2] Forcible kill`: `exitCode=1, completedTasks=0` (bị cắt ngang ngay lập tức).
>   - `[3] Simulator`: quên `wait()` tạo `5` zombie; cha chết với PID 1 không reaper giữ nguyên `5` zombie; cha chết với PID 1 `tini/systemd` giảm còn `0` zombie.

## 5. Cần kiểm chứng (chưa chạy trên Windows)
- `bash scripts/pid1-shell-vs-exec.sh` và `docker stop -t 10` (kiểm chứng `sh -c` giữ PID 1 và bỏ qua `SIGTERM` so với `exec`).
- `bash scripts/observe-zombie-and-signals.sh` và `ps -eo pid,ppid,stat,comm` (quan sát trạng thái `Z <defunct>` trong kernel process table thật).
- `kill -3 <pid>` gửi `SIGQUIT` tới tiến trình JVM thật để kiểm tra HotSpot xuất `Full thread dump` ra `stdout`.

## 6. Giải thích kết quả
- Graceful shutdown yêu cầu tiến trình nhận được `SIGTERM`, ngừng nhận việc mới, và gọi `executor.awaitTermination(...)` trong khoảng thời gian nhỏ hơn giới hạn kill của Docker/K8s/systemd (xem [README §3.5–3.6](../README.md)).

## 7. Dọn dẹp
```powershell
mvn -o -B -q clean
```
