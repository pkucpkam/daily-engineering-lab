# Lab CQ-06 · JUnit 5 & Mockito

## Mục tiêu

Thực hành toàn bộ tính năng thiết yếu của bộ ba **JUnit 5 + Mockito + AssertJ** trên bài toán [`ShippingFeeService`](src/main/java/lab/cq/testing/ShippingFeeService.java), đồng thời minh hoạ cách cấu hình `byte-buddy-agent` `-javaagent` chuẩn cho Java 21+.

## Yêu cầu môi trường

- JDK 21+ (đã verify trên OpenJDK 26.0.1 với target `--release 21`)
- Maven 3.9+ (đã có sẵn JUnit 5.10.3, AssertJ 3.25.3, Mockito 5.17.0)

## Cấu trúc thư mục

- `pom.xml` — cấu hình Maven kèm `-javaagent:...byte-buddy-agent-1.15.11.jar` cho Java 21+
- `src/main/java/lab/cq/testing/ShippingFeeService.java` — service tính phí vận chuyển + `ZoneRateCatalog` + `SurchargeCalculator` + `AuditTracker`
- `src/test/java/lab/cq/testing/ShippingFeeServiceTest.java` — dùng `@ExtendWith`, `ExecutionTimerExtension`, `@Nested`, `@ParameterizedTest`, `@Mock`, `@Spy`, `@Captor`, AssertJ
- `solutions/FakeVsMockRefactorDemo.java` — đối chiếu cách kiểm thử dựa trên Fake in-memory không cần Mockito cho L3

## Cách chạy

```powershell
# 1. Chạy full test suite (6 parameterized + 3 contract tests)
mvn -o test

# 2. Chạy demo Fake-based check L3
mvn -o -q compile
java -cp target\classes solutions\FakeVsMockRefactorDemo.java

# 3. Dọn artifact build
mvn -o -q clean
```

## Kết quả mẫu (đã chạy verify)

- `ShippingFeeServiceTest`: `Tests run: 9, Failures: 0, Errors: 0, Skipped: 0` (`3` Interaction & Captor contracts + `6` Parameterized matrix cases).
- `FakeVsMockRefactorDemo.java`:
```text
fee1=0 | fee2=65000 | recordedEvents=2
```
