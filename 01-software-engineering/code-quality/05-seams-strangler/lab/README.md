# Lab CQ-05 · Seams & Strangler Fig

## Mục tiêu

Thực hành 3 mẫu kỹ thuật giải quyết legacy an toàn không cần đập đi viết lại:
- **Sprout Class** ([`TaxExemptionSproutPolicy`](src/main/java/lab/cq/seams/TaxExemptionSproutPolicy.java)) — thêm luật miễn giảm thuế mới có unit test 100%, chỉ gọi 1 dòng từ [`LegacyInvoiceProcessor`](src/main/java/lab/cq/seams/LegacyInvoiceProcessor.java).
- **Wrap Method + Object Seam** (`calculateAndAudit` + `AuditSink`) — bọc thêm audit quanh method cũ không sửa logic tính tiền gốc.
- **Module Strangler Router & Auto-Fallback** (`InvoiceStranglerRouter` + [`StranglerFallbackSwitch`](solutions/StranglerFallbackSwitch.java)) — chuyển dần cohort tenant từ legacy sang modern kèm fallback an toàn.

## Yêu cầu môi trường

- JDK 21+ (đã verify trên OpenJDK 26.0.1 với target `--release 21`)
- Maven 3.9+

## Cách chạy

```powershell
# 1. Chạy unit tests
mvn -o test

# 2. Chạy minh họa Strangler fallback L3
mvn -o -q compile
java -cp target\classes solutions\StranglerFallbackSwitch.java

# 3. Dọn artifact build
mvn -o -q clean
```

## Kết quả mẫu (đã chạy verify)

- `SeamsStranglerTest`: `Tests run: 3, Failures: 0, Errors: 0, Skipped: 0`
- `StranglerFallbackSwitch.java`:
```text
TENANT-STD   -> 1100000
TENANT-PILOT -> 1075000
[FALLBACK] Reverting TENANT-ERR to legacy due to: New microservice timeout
TENANT-ERR   -> 1100000
```
