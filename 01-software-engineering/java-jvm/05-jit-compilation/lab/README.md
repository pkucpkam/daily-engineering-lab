# JVM-05 · Lab — JIT: Interpreter, C1, C2, tiered compilation

> **Mục tiêu:** tận mắt thấy bằng `-XX:+PrintCompilation` quá trình JVM chuyển từ Tier 3 → Tier 4, OSR (`%`), Dead Code Elimination xóa vòng lặp, và khoảnh khắc `made not entrant: uncommon trap` xảy ra khi nạp thêm implementation thứ 2 phá vỡ monomorphic inline.
> **Thời lượng:** ~35 phút · **Cấp:** L2

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
│   ├── main/java/lab/jvm/jit/
│   │   └── JitWarmupRunner.java             # 3 thí nghiệm: Cold/Warm, DCE trap, Deoptimization
│   └── test/java/lab/jvm/jit/
│       └── JitTest.java                     # JUnit 5 kiểm chứng correctness trước/sau warmup
└── solutions/
    └── E7MegamorphicBenchmark.java          # Lời giải E7 so sánh 1 impl vs 4 impl
```

## 2. Chạy

Từ thư mục `lab/`:
```bash
mvn -q verify
mvn -q compile
```

Để thấy log `-XX:+PrintCompilation` (khuyên lọc qua tên class để dễ đọc):

**Windows (PowerShell):**
```powershell
java "-XX:+PrintCompilation" -cp target\classes lab.jvm.jit.JitWarmupRunner | Select-String "JitWarmupRunner|\[1\]|\[2\]|\[3\]"
```

**Linux / macOS / Git Bash:**
```bash
java -XX:+PrintCompilation -cp target/classes lab.jvm.jit.JitWarmupRunner | grep -E "JitWarmupRunner|\[1\]|\[2\]|\[3\]"
```

## 3. Các bước thí nghiệm

### Bước 1 — Quan sát Tier 3, Tier 4 và OSR (`%`)
- **Dự đoán trước khi chạy** (ghi vào `my-notes.md`):
  1. Trong thí nghiệm `[2]`, vòng lặp 10 triệu lần bỏ qua giá trị trả về (`discarded`) nhanh hơn vòng lặp cộng dồn (`consumed`) khoảng mấy lần?
  2. Khi `VipTax` được truyền vào `applyTax`, bản Tier 4 trước đó của `applyTax` có bị đánh dấu `made not entrant` không?
- Làm gì: chạy lệnh `-XX:+PrintCompilation` ở §2.

### Bước 2 — Kiểm tra `PrintInlining` (E8)
- Làm gì:
  ```bash
  java -XX:+UnlockDiagnosticVMOptions -XX:+PrintInlining -cp target/classes lab.jvm.jit.JitWarmupRunner
  ```

## 4. Kết quả tham khảo khi chạy

> **Output thực tế trên máy soạn** *(Windows 11 amd64, JDK 26.0.1 `--release 21`, ngày 2026-11-27)*:
> - `mvn verify`: **1/1 test pass**, 0 failures, 0 errors.
> - `-XX:+PrintCompilation`:
>   - `58 ms`: `pureWork (22 bytes)` lên Tier 3 rồi Tier 4; `hotSum @ 4 (24 bytes)` xuất hiện OSR (`%`).
>   - `98 ms`: `applyTax (8 bytes)` lên Tier 4 (`id 119`).
>   - `103 ms`: sau khi `VipTax` xuất hiện, `id 119` bị **`made not entrant: uncommon trap`** rồi compile lại `id 123` ở Tier 4 tại `105 ms`.
> - `[2] Dead Code Elimination trap (10M calls)`: `discarded=2 ms` vs `consumed=9 ms`.

## 5. Giải thích kết quả
- Khi chưa dùng giá trị trả về, C2 loại bỏ thân vòng lặp qua Dead Code Elimination.
- Khi `VipTax` phá vỡ giả định monomorphic của `applyTax`, HotSpot deoptimize `id 119` (`uncommon trap`) quay về Interpreter cập nhật profile rồi compile lại bản mới `id 123`. Chi tiết xem [README §3](../README.md).

## 6. Biến thể để tự khám phá
- Chạy `solutions/E7MegamorphicBenchmark.java` để so sánh khi vượt quá 2 implementation (megamorphic = 4 implementation).

## 7. Dọn dẹp
```bash
mvn -q clean
```
