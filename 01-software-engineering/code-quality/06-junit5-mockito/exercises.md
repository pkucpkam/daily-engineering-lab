# Bài tập · CQ-06 · JUnit 5 & Mockito

Làm bài trước khi mở [solutions.md](solutions.md). Thực hành trên [lab/](lab/README.md).

---

## Level 1 · Hiểu JUnit 5, AssertJ & Test Doubles (4 bài)

### Bài 1.1 · Phân biệt Stub, Fake, Mock, Spy
Trong 4 dòng code sau ở unit test của một service thanh toán, hãy gắn đúng nhãn (**Stub**, **Fake**, **Mock**, **Spy**):
1. `when(rateService.getUsdRate()).thenReturn(25_000L);`
2. `UserRepository repo = new InMemoryUserMapRepository();`
3. `verify(smsClient).sendOtp("0901234567", "123456");`
4. `@Spy EmailFormatter realFormatter = new EmailFormatter();`

### Bài 1.2 · Tại sao quá nhiều `verify()` làm test giòn?
Giải thích ngắn gọn điều gì xảy ra với bộ test đang có 5 dòng `verify(helper).stepX(...)` khi kỹ sư thực hiện một đợt refactor vô hại (gộp 2 helper nội bộ thành 1) không hề làm sai kết quả đầu ra.

### Bài 1.3 · Đặt tên Test rõ ý & Sắp xếp `@Nested`
Đổi tên và gợi ý cấu trúc `@Nested` cho 2 test đặt tên xấu dưới đây:
- `testCase1()`: kiểm tra khi giỏ hàng `< 500k` của VIP vẫn tính phí 25k.
- `testCase2()`: kiểm tra khi tổng tiền `< 0` ném `IllegalArgumentException` và không gọi audit.

### Bài 1.4 · Tại sao JUnit 5 ưu tiên `@ExtendWith` hơn kế thừa `BaseTest`?
Trong JUnit 4 cũ thường có `class BaseAuditAndTimerTest` bắt 40 file test `extends` theo. Nêu 2 ưu điểm của JUnit 5 Extension Model (`@ExtendWith`) so với kế thừa class cha.

---

## Level 2 · Thực hành lab JUnit 5 & Mockito (3 bài)

### Bài 2.1 · Chạy full test suite & đọc cấu trúc `ShippingFeeServiceTest`
Chạy `mvn -o test` trong `06-junit5-mockito/lab/`. Đọc [`ShippingFeeServiceTest.java`](lab/src/test/java/lab/cq/testing/ShippingFeeServiceTest.java) và trả lời:
1. Tổng số test case báo cáo là `9` — 9 test đó đến từ đâu?
2. Trong `vipFreeDomesticWaivesFeeAndRecordsWaivedEvent()`, tác dụng của **`never()`** trên `surchargeSpy` và **`ArgumentCaptor`** trên `auditTracker` là gì?
3. Vì sao trong `@BeforeEach` phải dùng `lenient().when(...)` cho cả 3 hạng `DOMESTIC`, `EXPRESS`, `INTERNATIONAL` (câu nào xảy ra nếu bỏ `lenient()` và chỉ chạy 1 test trong `AuditAndValidationTests`)?

### Bài 2.2 · Chuyển test lặp sang `@ParameterizedTest` (`@MethodSource`)
Giả sử sếp thêm yêu cầu kiểm tra mọi giá trị trọng lượng lỗi (`0.0`, `-0.5`, `-10.0`) hoặc đơn giá âm đều phải ném `IllegalArgumentException` và **không** được gọi `auditTracker.record(...)`. Viết 1 `@ParameterizedTest` gọn bằng `@CsvSource` thay vì copy-paste 4 method `@Test`.

### Bài 2.3 · Loại bỏ `Strictness LENIENT` bằng thiết kế Test tốt hơn
Trong bài lab cố ý dùng `lenient().when(...)` ở `@BeforeEach` để dùng chung bảng giá cho nhiều nested test. Nếu giữ **Mockito Strictness mặc định** (bỏ hoàn toàn `lenient()`), bạn tổ chức lại việc stub `rateCatalog` như thế nào cho sạch nhất mà không ném `UnnecessaryStubbingException`?

<details>
<summary>Gợi ý</summary>

Cách 1: Đặt helper method `givenRatesConfigured()` và chỉ gọi ở những test thực sự cần lookup giá (như `PricingMatrixTests`), hoặc stub riêng đúng `Tier` cần test trong từng test. Cách 2: Truyền **Fake map lambda** `table::get` thay vì Mockito mock!

</details>

---

## Level 3 · Thay Mock tương tác giòn bằng Fake/Stub nhẹ (1 bài)

### Bài 3.1 · Viết lại kịch bản test không phụ thuộc `verify()` nội bộ
Đọc [`lab/solutions/FakeVsMockRefactorDemo.java`](lab/solutions/FakeVsMockRefactorDemo.java). Trong tương lai, giả sử `ShippingFeeService` thay `SurchargeCalculator` bằng bảng công thức tính thẳng bên trong (bỏ luôn class `SurchargeCalculator`):
1. So sánh phản ứng của test `spyDelegatesRealWeightSurchargeForHeavyParcels()` (trong `ShippingFeeServiceTest`, vốn dùng `@Spy` + `verify(surchargeSpy)`) với cách dùng Fake/State-based assertion như trong `FakeVsMockRefactorDemo`.
2. Chạy `mvn -o -q compile` rồi `java -cp target\classes solutions\FakeVsMockRefactorDemo.java` và kiểm chứng đầu ra `fee1=0`, `fee2=65000`, và `recordedEvents=2` với **0 dòng `Mockito`**.

**Tiêu chí đạt**: Giải thích đúng vì sao Mock/Spy nội bộ gãy khi refactor còn Fake/State verification sống khoẻ, đồng thời chạy được `FakeVsMockRefactorDemo` thành công.
