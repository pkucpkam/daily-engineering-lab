# JVM-06 · Lab — GC cơ bản & G1

> **Mục tiêu:** quan sát trực tiếp trên GC log của JDK 21 sự khác biệt chấn động giữa cấp phát các mảng nhỏ (`4 KB`) vs **Humongous objects (`600 KB`)** khi chạy G1GC với Region `1 MB` (cùng 128 MB cấp phát nhưng số lần GC nhảy từ `4` lên `96`!).
> **Thời lượng:** ~30 phút · **Cấp:** L2

## 0. Yêu cầu môi trường
| Công cụ | Phiên bản | Kiểm tra |
| :--- | :--- | :--- |
| JDK | 21+ | `java -version` |
| Maven | 3.9+ | `mvn -v` |

> Lab chạy 100% offline, không cần Docker. Chạy được trên **Windows (PowerShell / Git Bash) và Linux/macOS**.

## 1. Cấu trúc
```text
lab/
├── README.md
├── pom.xml                                  # Maven project độc lập, Java 21
├── src/
│   ├── main/java/lab/jvm/gc/
│   │   └── GcAllocationRunner.java          # 2 chế độ: normal (4KB) vs humongous (600KB)
│   └── test/java/lab/jvm/gc/
│       └── GcLabTest.java                   # JUnit 5 kiểm chứng GC MXBean & workload
└── solutions/
    └── E8StreamingHashRefactored.java       # Lời giải E8 streaming SHA-256 buffer 16KB
```

## 2. Chạy

Từ thư mục `lab/`:
```bash
mvn -q verify
mvn -q compile
```

Chạy 2 chế độ để so sánh trực tiếp GC log:

**Windows (PowerShell):**
```powershell
java -Xms64m -Xmx64m "-XX:+UseG1GC" "-XX:G1HeapRegionSize=1m" "-Xlog:gc" -cp target\classes lab.jvm.gc.GcAllocationRunner normal
java -Xms64m -Xmx64m "-XX:+UseG1GC" "-XX:G1HeapRegionSize=1m" "-Xlog:gc" -cp target\classes lab.jvm.gc.GcAllocationRunner humongous
```

**Linux / macOS / Git Bash:**
```bash
java -Xms64m -Xmx64m -XX:+UseG1GC -XX:G1HeapRegionSize=1m -Xlog:gc -cp target/classes lab.jvm.gc.GcAllocationRunner normal
java -Xms64m -Xmx64m -XX:+UseG1GC -XX:G1HeapRegionSize=1m -Xlog:gc -cp target/classes lab.jvm.gc.GcAllocationRunner humongous
```

## 3. Các bước thí nghiệm

### Bước 1 — So sánh `normal` vs `humongous`
- **Dự đoán trước khi chạy** (ghi vào `my-notes.md`):
  1. Cùng cấp phát tổng `128 MB`, chế độ `normal` (`4 KB`) và `humongous` (`600 KB`) sẽ kích hoạt số lần GC lệch nhau bao nhiêu?
  2. Trong log của `humongous`, G1 ghi lý do kích hoạt GC là gì?
- Làm gì: chạy 2 lệnh `java` ở §2.

### Bước 2 — Nâng `G1HeapRegionSize=2m` (E6) & soi `gc+humongous=debug` (E7)
- Làm gì:
  ```bash
  java -Xms64m -Xmx64m -XX:+UseG1GC -XX:G1HeapRegionSize=2m -Xlog:gc -cp target/classes lab.jvm.gc.GcAllocationRunner humongous
  ```
  Quan sát xem hiện tượng `G1 Humongous Allocation` biến mất hoàn toàn như thế nào khi ngưỡng tăng từ `512 KB` lên `1 MB`!

## 4. Kết quả tham khảo khi chạy

> **Số đo thực tế trên máy soạn** *(Windows 11 amd64, JDK 26.0.1 `--release 21`, `-Xms64m -Xmx64m -XX:G1HeapRegionSize=1m`, ngày 2026-11-27)*:
> - `mvn verify`: **1/1 test pass**, 0 failures, 0 errors.
> - `normal` (`4 KB` × 32.768 = 128 MB): **4 lần GC** (`Pause Young (Normal)`), tổng STW GC = **2 ms**, `elapsed=21 ms`.
> - `humongous` (`600 KB` × 218 = 128 MB): **96 lần GC** (`Pause Young (Concurrent Start) (G1 Humongous Allocation)` + `Concurrent Undo Cycle`), tổng STW GC = **20 ms**, `elapsed=36 ms`, Heap sau GC kẹt ở `27M->25M(64M)` cho `24` chunk giữ lại.

## 5. Giải thích kết quả
- Vì `600 KB > 512 KB` (50% của Region `1 MB`), mỗi mảng `600 KB` trở thành Humongous Object cấp thẳng vào Old Gen và làm lãng phí ~`400 KB` cuối mỗi Region (`24` chunk giữ lại mất ~`24 MB` Region thay vì `14.4 MB`). Chi tiết xem [README §3.3](../README.md).

## 6. Biến thể để tự khám phá
- Thử tăng `retainWindow` trong `GcAllocationRunner` từ `24` lên `48` (tức `48 MB` trên Heap `64 MB`) với `G1HeapRegionSize=1m` để quan sát `To-space exhausted` hoặc `Pause Full`.

## 7. Dọn dẹp
```bash
mvn -q clean
```
