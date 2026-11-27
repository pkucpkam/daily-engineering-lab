# Bài tập · CQ-07 · Test pyramid & khi nào KHÔNG mock

Làm bài trước khi mở [solutions.md](solutions.md). Thực hành trên [lab/](lab/README.md).

---

## Level 1 · Phân loại tầng test & Mock boundary (4 bài)

### Bài 1.1 · Nhận diện "Mock hay Real?" trên 5 đối tượng
Khi viết **Unit Test** cho `OrderBookingService`, bạn gặp 5 phụ thuộc sau — quyết định cái nào dùng **Real object**, cái nào dùng **Fake/Stub/Mock tại biên**:
1. `PriceCalculator` (class nội bộ cộng trừ giảm giá và thuế VAT).
2. `StripePaymentHttpClient` (gửi HTTP POST thanh toán tới server Stripe thật).
3. `Clock` (lấy thời gian hiện tại kiểm tra hạn voucher).
4. `ObjectMapper` (Jackson chuyển DTO sang JSON string).
5. `KafkaOrderEventPublisher` (đẩy event lên Kafka cluster).

### Bài 1.2 · Phân biệt Test Behavior vs Test Implementation
Hai assertion sau cho cùng một tính năng chuyển khoản `transfer(from, to, amount)`:
- Assertion A: `verify(balanceValidator).checkSufficient(from, amount); verify(feeRule).feeFor(amount); verify(auditFormatter).format(...);`
- Assertion B: `assertThat(fromAccount.balance()).isEqualTo(890_000L); assertThat(toAccount.balance()).isEqualTo(100_000L); assertThat(fakeLedger.entries()).hasSize(1);`

Assertion nào đang trói vào *Implementation*, và chuyện gì xảy ra nếu ta gộp `BalanceValidator` vào trong domain `Account` khi refactor?

### Bài 1.3 · Khi nào Integration Test đáng tiền hơn Unit Test?
Team bạn viết một Spring Boot microservice chỉ có API `POST /v1/invoices` lưu xuống PostgreSQL (`ON CONFLICT DO UPDATE`) và trả HTTP 201. Tại sao sếp lại ưu tiên viết Integration Test qua PostgreSQL/MockMvc hơn là viết 20 Unit Test mock hết `JdbcTemplate` và `ObjectMapper`?

### Bài 1.4 · Hiểu *Ice Cream Cone Antipattern*
Tại sao mô hình "hình cây kem úp ngược" (ít Unit/Integration Test, cực nhiều E2E click browser) lại làm chậm tốc độ release của cả đội kỹ thuật sau 6 tháng phát triển?

---

## Level 2 · Thực hành phân tích Over-mocking vs Boundary-mocking (3 bài)

### Bài 2.1 · Chạy kiểm chứng lab & tìm lỗi ẩn
Chạy `mvn -o test` trong `07-test-pyramid-mocking/lab/`. Đọc [`BoundaryVsOverMockedTest.java`](lab/src/test/java/lab/cq/pyramid/BoundaryVsOverMockedTest.java):
1. Hãy thử giả định (trong suy nghĩ hoặc test nháp) rằng ai đó sửa nhầm công thức trong `DiscountRule.discountFor(...)` thành `subtotalVnd * 0.90` (giảm nhầm 90%!). Hai test nào trong `BoundaryVsOverMockedTest` sẽ bắt được bug này, và test nào vẫn PASS sai trái?
2. Giải thích vì sao `boundaryMockedBehaviorTestSurvivesV1ToV2RefactoringIdentically` kiểm tra được **cả `CheckoutServiceV1` lẫn `CheckoutServiceV2Refactored`** mà không phải đổi dòng assertion nào.

### Bài 2.2 · Loại bỏ *Mockito mock* khỏi bài test DAO JDBC thuần
Giả sử ở codebase legacy Java 7 có một hàm `findActiveCustomerEmails()` chạy 1 câu SQL thuần 15 dòng. Một đồng nghiệp viết test mock `Connection` -> `PreparedStatement` -> `ResultSet` (`when(rs.next()).thenReturn(true, false)`). Bạn đề xuất chuyển test này sang mô hình nào trong Test Pyramid / Testing Trophy?

### Bài 2.3 · Test Behavior cho `RetryHttpClient`
Bạn cần test một class `PartnerApiClient` có logic: nếu server đối tác trả HTTP `503` lần 1 và `200 OK` lần 2 thì tổng cộng hàm trả về dữ liệu thành công và tổng request gửi ra ngoài đúng `2` lần. Ở đây ta **NÊN** mock/stub ở đâu (dùng Fake HTTP server / WireMock / Stub `HttpTransport`) và assert hành vi gì?

<details>
<summary>Gợi ý</summary>

Stub/Fake ở chính cổng HTTP transport bên ngoài (trả `503` rồi `200`), còn kiểm chứng hành vi observable (`response == OK` và số lần transport nhận HTTP request = 2), không mock helper `BackoffCalculator` bên trong!

</details>

---

## Level 3 · Thiết kế bộ test sống sót qua Refactor (1 bài)

### Bài 3.1 · Chuyển từ Over-mocked sang Boundary-mocked
Hãy giả lập một cuộc refactor trên [`CheckoutDomain.java`](lab/src/main/java/lab/cq/pyramid/CheckoutDomain.java):
1. Đọc [`lab/solutions/BoundaryFakeSolution.java`](lab/solutions/BoundaryFakeSolution.java) và chạy thử: `mvn -o -q compile` → `java -cp target\classes solutions\BoundaryFakeSolution.java`.
2. Phân tích cấu trúc của `CheckoutServiceV2Refactored`: dù toàn bộ class `DiscountRule` và `TaxRule` đã biến mất khỏi constructor của `V2`, nhưng cả `BoundaryFakeSolution` lẫn `boundaryMockedBehaviorTestSurvivesV1ToV2RefactoringIdentically` vẫn kiểm chứng trọn vẹn `VIP = 1.944.000`, `STD = 2.160.000`, và `LedgerEntry` chính xác!
3. Viết ra **2 quy tắc kiểm duyệt Code Review (PR Checklist)** ngắn gọn bạn sẽ áp dụng cho team mình để ngăn đồng nghiệp lặp lại lỗi `overMockedTest`.

**Tiêu chí đạt**: Chạy được `BoundaryFakeSolution.java` in ra `VIP=1944000 | STD=2160000` và ghi đúng 2 tiêu chí PR Review phát hiện Over-mocking.
