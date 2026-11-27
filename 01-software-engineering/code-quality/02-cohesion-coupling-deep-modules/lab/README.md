# Lab CQ-02 · Cohesion, Coupling, Deep modules

## Mục tiêu

So sánh trực tiếp 2 thiết kế thực hiện cùng một nghiệp vụ parse & validate danh sách hóa đơn CSV:
- **Shallow design ([`ShallowCsvPipeline`](src/main/java/lab/cq/modules/shallow/ShallowCsvPipeline.java))**: chia nhỏ quá mức thành 5 class truyền `String[]` trung gian (*Classitis* + *Information Leakage* + *Temporal Coupling*).
- **Deep design ([`InvoiceBatchImporter`](src/main/java/lab/cq/modules/deep/InvoiceBatchImporter.java))**: che giấu toàn bộ parsing/trimming/defaulting/validation sau 1 interface duy nhất `importCsv(String rawCsv)`.

## Yêu cầu môi trường

- JDK 21+ (đã verify trên OpenJDK 26.0.1 với target `--release 21`)
- Maven 3.9+

## Cấu trúc thư mục

- `pom.xml` — cấu hình Maven (`--release 21`, JUnit 5, AssertJ)
- `src/main/java/lab/cq/modules/shallow/ShallowCsvPipeline.java` — thiết kế Shallow với 7 nested types & pass-through coordinator
- `src/main/java/lab/cq/modules/deep/InvoiceBatchImporter.java` — thiết kế Deep Module cô đọng
- `src/test/java/lab/cq/modules/DeepVsShallowTest.java` — kiểm chứng tương đương kết quả + đo public surface area (`7` vs `3`)
- `solutions/DeepConfigLoader.java` — bài L3 thiết kế Deep Module cho config số nguyên dương

## Cách chạy

```powershell
# 1. Chạy unit test kiểm chứng parity & surface metrics
mvn -o test

# 2. Chạy lời giải mẫu L3
java solutions\DeepConfigLoader.java

# 3. Dọn artifact build
mvn -o -q clean
```

## Kết quả mẫu (đã chạy verify)

- `DeepVsShallowTest`: `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0` (khẳng định kết quả khớp 100%, public types giảm từ `7` xuống `3`).
- `DeepConfigLoader.java`:
```text
pool.max=16
timeout.ms=3000
```
