# Lab CQ-07 · Test pyramid & khi nào KHÔNG mock

## Mục tiêu

Đối chiếu hai phong cách viết test khi tiến hành refactor từ `CheckoutServiceV1` (ủy quyền cho `DiscountRule` + `TaxRule`) sang `CheckoutServiceV2Refactored` (gộp tính toán gọn hơn bên trong mà giữ nguyên hành vi ra ngoài):
- **Over-mocked test**: mock `DiscountRule` & `TaxRule` + assert `verify(...)` → vừa lọt bug thật nếu công thức tính sai, vừa gãy khi refactor sang V2.
- **Boundary-mocked test**: chạy `DiscountRule` & `TaxRule` **thật**, chỉ truyền **Fake in-memory** cho cổng biên `PaymentLedgerPort` → bắt được bug toán học thật và chạy xanh trên cả V1 lẫn V2.

## Yêu cầu môi trường

- JDK 21+ (đã verify trên OpenJDK 26.0.1 với target `--release 21`)
- Maven 3.9+

## Cách chạy

```powershell
# 1. Chạy bài test đối chiếu over-mocked vs boundary-mocked
mvn -o test

# 2. Chạy lời giải L3 kiểm chứng V2 bằng boundary fake thuần
mvn -o -q compile
java -cp target\classes solutions\BoundaryFakeSolution.java

# 3. Dọn artifact build
mvn -o -q clean
```

## Kết quả mẫu (đã chạy verify)

- `BoundaryVsOverMockedTest`: `Tests run: 2, Failures: 0, Errors: 0, Skipped: 0`
- `BoundaryFakeSolution.java`:
```text
VIP=1944000 | STD=2160000 | recordedCharges=[LedgerEntry[orderId=B-1, netPayableVnd=1944000], LedgerEntry[orderId=B-2, netPayableVnd=2160000]]
```
