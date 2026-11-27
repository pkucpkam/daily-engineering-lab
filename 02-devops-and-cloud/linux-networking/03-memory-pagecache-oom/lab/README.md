# LNX-03 · Lab — Virtual memory, page cache, swap & OOM killer

> ⚠️ **Cần kiểm chứng trên Linux:** Máy soạn là Windows 11 không có WSL/Docker nên script `oom-pressure-demo.sh` và các lệnh kernel `/proc/<pid>/oom_score`, `free -m`, `vmstat`, `dmesg` **chưa được chạy kiểm chứng trực tiếp** (xem §5). Phần **Java 21 `MemoryFootprintAnalyzer` (`ByteBuffer.allocateDirect` + `BufferPoolMXBean`), `FreeOutputParser` & `OomScoreCalculator`** đã chạy kiểm chứng thật 100% offline.

> **Mục tiêu:** tự tay đo `64 MB` Direct Memory off-heap nằm hoàn toàn ngoài `-Xmx`, đọc đúng cột `available` vs `free` của `free -m`, và hiểu cơ chế tính `oom_score` của OOM Killer.
> **Thời lượng:** ~35 phút · **Cấp:** L2

## 0. Yêu cầu môi trường
| Công cụ | Phiên bản | Kiểm tra |
| :--- | :--- | :--- |
| JDK | 21+ | `java -version` |
| Maven | 3.9+ | `mvn -v` |
| Linux / Docker *(tùy chọn)* | Kernel 4.x+ | `free -m` |

## 1. Cấu trúc
```text
lab/
├── README.md
├── pom.xml                                  # Maven project độc lập, Java 21
├── scripts/
│   └── oom-pressure-demo.sh                 # Kiểm tra MemAvailable, oom_score, dmesg trên Linux
├── src/
│   ├── main/java/lab/linux/oom/
│   │   ├── MemoryFootprintAnalyzer.java     # Đo Heap vs Direct Buffer off-heap qua MXBean
│   │   ├── FreeOutputParser.java            # Phân tích free -m phân biệt Page Cache vs Swap Thrashing
│   │   ├── OomScoreCalculator.java          # Tính oom_score & dự đoán nạn nhân OOM Killer
│   │   └── MemoryOomLabRunner.java          # Runner chạy 3 demo
│   └── test/java/lab/linux/oom/
│       └── MemoryOomTest.java               # JUnit 5 kiểm chứng
└── solutions/
    └── OomVictimPredictionTest.java         # Lời giải E7
```

## 2. Chạy

Từ thư mục `lab/`:
```powershell
mvn -o -B verify
mvn -o -B compile exec:java
```

## 3. Các bước thí nghiệm

### Bước 1 — Đo off-heap Direct Memory & `oom_score`
- **Dự đoán trước khi chạy** (ghi vào `my-notes.md`):
  1. Khi cấp phát `64 MB` bằng `ByteBuffer.allocateDirect`, `HeapMemoryUsage.getUsed()` có tăng thêm `64 MB` không?
  2. Giữa `jboss-java` (`RSS=8000 MB, adj=0`) và `batch-exporter` (`RSS=1600 MB, adj=+500`) trên máy `16 GB`, process nào bị OOM Killer chọn giết trước?
- Làm gì: chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`.

### Bước 2 — Trên Linux / Docker (khi có môi trường Linux)
```bash
bash scripts/oom-pressure-demo.sh
```

## 4. Kết quả tham khảo khi chạy

> **Output thực tế trên máy soạn** *(Windows 11 amd64, OpenJDK 26.0.1 `--release 21`, ngày 2026-11-27)*:
> - `mvn -o -B verify`: **4/4 tests pass**, 0 failures, 0 errors.
> - `MemoryOomLabRunner`:
>   - `BufferPoolMXBean("direct")` tăng chính xác `+64.0 MB` sau `allocateDirect(64)` trong khi `heap` giữ nguyên quanh vài MB baseline.
>   - `FreeOutputParser` phân loại chính xác mẫu `free=200 MB, available=10800 MB` là `HEALTHY_PAGE_CACHE_ACTIVE`.
>   - `OomScoreCalculator`: `java-jboss` (`base=658, adj=0`) bị chọn làm nạn nhân trong khi `sshd` (`adj=-1000`) được bảo vệ ở `effective=0`.

## 5. Cần kiểm chứng (chưa chạy trên Windows)
- `bash scripts/oom-pressure-demo.sh` (`free -m`, `/proc/meminfo`, `/proc/<pid>/oom_score`, `dmesg -T | grep -i oom`).
- Chạy container `--memory=128m` tạo OOM thật để quan sát exit code `137` và log kernel `Out of memory: Killed process`.

## 6. Giải thích kết quả
- Direct Buffer cấp phát qua `malloc`/OS virtual memory ngoài Heap nên `-Xmx` không nhìn thấy nhưng OS RSS tính đủ. Trên Linux, `free` nhỏ nhờ Page Cache không đáng lo nếu `available` cao, còn OOM Killer dựa vào `oom_score + oom_score_adj` (xem [README §3](../README.md)).

## 7. Dọn dẹp
```powershell
mvn -o -B -q clean
```
