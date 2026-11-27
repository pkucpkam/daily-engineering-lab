# Lab CQ-01 · SOLID áp dụng thực tế

## Mục tiêu

Đối chiếu **before** ([`LegacyOrderGodService`](src/main/java/lab/cq/solid/before/LegacyOrderGodService.java) — God Class trộn 5 trách nhiệm) và **after** ([`OrderProcessor`](src/main/java/lab/cq/solid/after/OrderProcessor.java) — tách đúng ranh giới Actor/DIP/OCP/ISP/LSP mà không sinh cặp `Service/ServiceImpl` thừa).

## Yêu cầu môi trường

- JDK 21+ (đã verify trên OpenJDK 26.0.1 với target `--release 21`)
- Maven 3.9+

## Cấu trúc thư mục

- `pom.xml` — cấu hình Maven (`--release 21`, JUnit 5, AssertJ)
- `src/main/java/lab/cq/solid/before/LegacyOrderGodService.java` — bản cũ vi phạm SRP, OCP, DIP
- `src/main/java/lab/cq/solid/after/OrderModels.java` — value records + interface port gọn (`PricingPolicy`, `OrderRepository`, `OrderNotifier`)
- `src/main/java/lab/cq/solid/after/OrderProcessor.java` — service điều phối áp dụng DIP & OCP hợp lý
- `src/test/java/lab/cq/solid/SolidRefactorTest.java` — kiểm chứng tương đương hành vi + mở rộng OCP + chặn vi phạm LSP
- `solutions/CappedTierSolution.java` — lời giải L3 thêm tier `"ENTERPRISE"` có trần giảm giá

## Cách chạy

```powershell
# 1. Chạy unit tests
mvn -o test

# 2. Chạy lời giải tham khảo L3
mvn -o -q compile
java -cp target\classes solutions\CappedTierSolution.java

# 3. Dọn artifact build
mvn -o -q clean
```

## Kết quả mẫu (đã chạy verify)

- `SolidRefactorTest`: `Tests run: 3, Failures: 0, Errors: 0, Skipped: 0`
- `CappedTierSolution.java`:
```text
Saved ENT-1 total=977.0
Notified tier=ENTERPRISE
```
