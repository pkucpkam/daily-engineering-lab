# JVM-04 · Lab — JVM Memory: Heap, Stack, Metaspace, Direct memory

> **Mục tiêu:** đo từng byte kích thước thật của `Integer`, `CompactSample`, `int[1000]` vs `Integer[1000]` bằng **JOL**, và đối chiếu `Heap committed` vs tổng `Native Memory` qua **`jcmd VM.native_memory summary` (NMT)**.
> **Thời lượng:** ~40 phút · **Cấp:** L2

## 0. Yêu cầu môi trường
| Công cụ | Phiên bản | Kiểm tra |
| :--- | :--- | :--- |
| JDK | 21+ (kèm `jcmd`) | `java -version` và `jcmd -h` |
| Maven | 3.9+ | `mvn -v` |

> Lab dùng OpenJDK `jol-core 0.17`, chạy hoàn toàn offline không cần Docker. Chạy được trên **Windows (PowerShell / Git Bash) và Linux/macOS**.

## 1. Cấu trúc
```text
lab/
├── README.md
├── pom.xml                                  # jol-core 0.17 + JUnit 5 + AssertJ
├── src/
│   ├── main/java/lab/jvm/memory/
│   │   └── MemoryLayoutRunner.java          # Đo JOL layout + allocateDirect 64MB + NMT
│   └── test/java/lab/jvm/memory/
│       └── MemoryLayoutTest.java            # Test kiểm chứng 16B/24B và 5x array ratio
└── solutions/
    └── E7EscapeAnalysisDemo.java            # Lời giải E7 so sánh +-DoEscapeAnalysis
```

## 2. Chạy

Từ thư mục `lab/`:
```bash
mvn -q verify
mvn -q compile
```

Để chạy `MemoryLayoutRunner` kèm **Native Memory Tracking (NMT)** trực tiếp với `java`:

**Windows (PowerShell):**
```powershell
$jol = "$env:USERPROFILE\.m2\repository\org\openjdk\jol\jol-core\0.17\jol-core-0.17.jar"
java "-XX:NativeMemoryTracking=summary" "-Xmx256m" "-Djdk.attach.allowAttachSelf=true" -cp "target\classes;$jol" lab.jvm.memory.MemoryLayoutRunner
```

**Linux / macOS / Git Bash:**
```bash
JOL="$HOME/.m2/repository/org/openjdk/jol/jol-core/0.17/jol-core-0.17.jar"
java -XX:NativeMemoryTracking=summary -Xmx256m -Djdk.attach.allowAttachSelf=true -cp "target/classes:$JOL" lab.jvm.memory.MemoryLayoutRunner
```

## 3. Các bước thí nghiệm

### Bước 1 — Đo layout object bằng JOL
- **Dự đoán trước khi chạy** (ghi vào `my-notes.md`):
  1. `java.lang.Integer` tốn 12, 16 hay 24 bytes khi bật Compressed Oops?
  2. `Integer[1000]` lớn gấp mấy lần `int[1000]`?
- Làm gì: chạy `mvn -q verify` rồi chạy lệnh `MemoryLayoutRunner` ở §2.

### Bước 2 — Đọc snapshot NMT khi mới dùng 10 MB Heap
- Làm gì: quan sát mục `[3] jcmd <pid> VM.native_memory summary` in ra bởi `MemoryLayoutRunner` (xem mục `Total committed` vs `Java Heap committed`, `Class/Metadata`, `Thread`, `Code`).

### Bước 3 — Thử tắt Compressed Oops (E6)
- Làm gì: thêm flag `-XX:-UseCompressedOops` vào lệnh `java` ở §2 và xem lại kích thước `Integer` từ `16 bytes` nhảy lên `24 bytes`.

## 4. Kết quả tham khảo khi chạy

> **Số đo thực tế trên máy soạn** *(Windows 11 amd64, JDK 26.0.1 `--release 21`, JOL 0.17, ngày 2026-11-27)*:
> - `mvn verify`: **2/2 tests pass**, 0 failures, 0 errors.
> - `Integer`: **16 bytes** (`8B` mark + `4B` class + `4B` int, `0B` loss).
> - `CompactSample`: **32 bytes** (HotSpot reorder `int count` lên trước `long timestamp`, còn 3B gap sau `boolean flag`).
> - `int[1000]` = **4.016 bytes** vs `Integer[1000]` = **20.016 bytes** (**5,0×** lớn hơn).
> - `NMT summary` (`-Xmx256m` + 64MB DirectByteBuffer):
>   - `Total`: `reserved=1803218KB`, `committed=168222KB` (~164 MB)
>   - `Java Heap`: `reserved=262144KB`, `committed=10240KB` (~10 MB)
>   - `Thread` (#25): `reserved=25600KB`, `committed=308KB`
>   - `Code`: `reserved=248441KB`, `committed=8245KB`

## 5. Giải thích kết quả
- Dù Heap mới commit 10 MB, tổng bộ nhớ native committed đã lên ~164 MB (với ~64 MB nằm ở DirectByteBuffer ngoài Heap, Metaspace, Code Cache và Thread Stacks) — minh họa trực tiếp lý do `-Xmx` không phải giới hạn RSS toàn JVM. Chi tiết xem [README §3](../README.md).

## 6. Biến thể để tự khám phá
- Chạy `solutions/E7EscapeAnalysisDemo.java` hai lần với `-Xmx128m -Xlog:gc*` và `-Xmx128m -Xlog:gc* -XX:-DoEscapeAnalysis` để thấy tác động rõ rệt của Scalar Replacement lên số lần Young GC.

## 7. Dọn dẹp
```bash
mvn -q clean
```
