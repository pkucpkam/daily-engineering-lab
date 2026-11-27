# LNX-05 · Lab — Công cụ hiệu năng Linux

> ⚠️ **Cần kiểm chứng trên Linux:** Máy soạn là Windows 11 không có WSL/Docker nên script `perf-60s-checklist.sh` và các lệnh `vmstat`, `iostat`, `mpstat`, `pidstat`, `sar`, `top -H`, `strace`, `perf` **chưa được chạy kiểm chứng trực tiếp** (xem §5). Phần **Java 21 `CpuBurnWorkload` (đo ThreadMXBean CPU time thật) & `PerfTriageAnalyzer`** đã chạy kiểm chứng thật 100% offline.

> **Mục tiêu:** đo chênh lệch CPU time giữa thread burner (`R`) và sleeping thread (`S`), phân biệt 2 nguyên nhân hoàn toàn khác nhau của cùng `Load Average = 20`, và ánh xạ `top -H` TID sang hex `nid=0x...` trong thread dump.
> **Thời lượng:** ~35 phút · **Cấp:** L2

## 0. Yêu cầu môi trường
| Công cụ | Phiên bản | Kiểm tra |
| :--- | :--- | :--- |
| JDK | 21+ | `java -version` |
| Maven | 3.9+ | `mvn -v` |
| Linux + `sysstat` *(tùy chọn)* | Ubuntu / RHEL (`apt install sysstat`) | `vmstat -V` |

## 1. Cấu trúc
```text
lab/
├── README.md
├── pom.xml                                  # Maven project độc lập, Java 21
├── scripts/
│   └── perf-60s-checklist.sh                # Script chạy đủ 10 lệnh checklist 60 giây trên Linux
├── src/
│   ├── main/java/lab/linux/perf/
│   │   ├── CpuBurnWorkload.java             # Tạo CPU load thật & đo qua ThreadMXBean
│   │   ├── PerfTriageAnalyzer.java          # Chẩn đoán vmstat/mpstat & ánh xạ TID -> hex nid
│   │   └── PerfTriageLabRunner.java         # Runner chạy benchmark & phân tích
│   └── test/java/lab/linux/perf/
│       └── PerfToolsTest.java               # JUnit 5 kiểm chứng
└── solutions/
    └── StealTimeTriageTest.java             # Lời giải E7
```

## 2. Chạy

Từ thư mục `lab/`:
```powershell
mvn -o -B verify
mvn -o -B compile exec:java
```

## 3. Các bước thí nghiệm

### Bước 1 — Đo CPU time thật & phân tích `Load 20` / `TID -> nid`
- **Dự đoán trước khi chạy** (ghi vào `my-notes.md`):
  1. Trong `300ms` chạy song song, thread `cpu-burner-worker` và `idle-io-waiter` khác nhau thế nào về `ThreadMXBean.getCurrentThreadCpuTime()`?
  2. Nếu `vmstat` trên máy 8 core báo `r=1, b=19, wa=66`, `PerfTriageAnalyzer` trả về `CPU_SATURATION` hay `DISK_IO_BOTTLENECK`?
  3. Linux `TID = 4839` đổi sang `nid=0x...` bằng bao nhiêu?
- Làm gì: chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`.

### Bước 2 — Trên Linux VM (khi có môi trường Linux)
```bash
bash scripts/perf-60s-checklist.sh
```

## 4. Kết quả tham khảo khi chạy

> **Output thực tế trên máy soạn** *(Windows 11 amd64, OpenJDK 26.0.1 `--release 21`, ngày 2026-11-27)*:
> - `mvn -o -B verify`: **3/3 tests pass**, 0 failures, 0 errors.
> - `PerfTriageLabRunner`:
>   - `CpuBurnWorkload (300ms)`: `cpu-burner = 296 ms CPU | idle-io-waiter = 0 ms CPU`.
>   - `vmstat`: `(r=21, b=0, wa=1)` → `CPU_SATURATION`; `(r=1, b=19, wa=66)` → `DISK_IO_BOTTLENECK`.
>   - `mpstat per-core [1,0,2,99,1,0,1,0]` → `SINGLE_CORE_HOTSPOT`.
>   - `TID 4839` → `0x12e7` → khớp `"http-executor-17"`.

## 5. Cần kiểm chứng (chưa chạy trên Windows)
- `bash scripts/perf-60s-checklist.sh` (`uptime`, `dmesg -T`, `vmstat 1`, `mpstat -P ALL 1`, `pidstat 1`, `iostat -xz 1`, `sar -n DEV,TCP,ETCP 1`, `top`).
- `top -H -p <pid>` kết hợp `kill -3 <pid>` và `perf top -p <pid>` trên tiến trình Java chạy trên Linux.

## 6. Giải thích kết quả
- Một con số Load Average đơn lẻ không phân biệt được CPU bound hay I/O bound; phải kết hợp cột `r` vs `b` của `vmstat`, `await`/`%util` của `iostat`, và `TID -> nid hex` để khoanh đúng dòng code Java (xem [README §3.2–3.3](../README.md)).

## 7. Dọn dẹp
```powershell
mvn -o -B -q clean
```
