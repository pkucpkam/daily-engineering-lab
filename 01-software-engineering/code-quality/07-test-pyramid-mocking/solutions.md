# Đáp án · CQ-07 · Test pyramid & khi nào KHÔNG mock

## Đáp án Bài tập

### Level 1

#### Bài 1.1 · Mock hay Real cho 5 phụ thuộc?
1. `PriceCalculator` → **Real object** (thuần nội bộ domain, chạy tức thì).
2. `StripePaymentHttpClient` → **Fake / Stub / Mock tại biên I/O** (gọi mạng/tiền thật ra ngoài).
3. `Clock` → **Fixed Clock (`Clock.fixed(...)` — Fake/Stub chuẩn JDK)** để tránh test bị flaky theo thời gian thực.
4. `ObjectMapper` → **Real object** (phát hiện luôn bug annotation JSON/`LocalDate` thay vì giả vờ nó hoạt động).
5. `KafkaOrderEventPublisher` → **Fake in-memory list (`events::add`)** hoặc Mock ở Unit Test, và Kafka Testcontainers thật ở Integration Test.

#### Bài 1.2 · Assertion A vs Assertion B
- **Assertion A** đang trói vào *Implementation* (verify đúng 3 lời gọi class nội bộ). Khi gộp `BalanceValidator` vào `Account`, test A nổ đỏ toàn tập dù nghiệp vụ chuyển khoản vẫn chuẩn 100%.
- **Assertion B** kiểm tra *Behavior* observable (số dư đích/nguồn sau giao dịch + bản ghi ledger ở cổng ra) — sống khoẻ qua mọi đợt tái cấu trúc bên trong!

#### Bài 1.3 · Khi nào Integration Test giá trị hơn Unit Test?
Vì trọng tâm rủi ro của một API CRUD SQL-heavy không nằm ở vài phép cộng Java, mà nằm ở **nối dây HTTP JSON + Validation + SQL dialect PostgreSQL (`ON CONFLICT`) + Database constraints + Transaction**. Mock hết `JdbcTemplate` và `ObjectMapper` khiến unit test bỏ lọt 90% nguy cơ lỗi thật khi lên production!

#### Bài 1.4 · Tại sao Ice Cream Cone làm chậm đội kỹ thuật?
Vì E2E test tốn hàng chục phút chạy, đòi hỏi dựng toàn bộ cụm UI + DB + Auth, hay bị flaky bởi timeout/mạng/rendering, và khi fail không chỉ rõ hỏng ở tầng service nào. Sau 6 tháng, team mất niềm tin vào test đỏ và mất hàng giờ điều tra sai cảnh báo.

---

### Level 2

#### Bài 2.1 · Phân tích `BoundaryVsOverMockedTest`
1. Nếu `DiscountRule` thật bị sửa nhầm thành `subtotalVnd * 0.90`:
   - `overMockedTestPassesEvenIfRealDiscountFormulaWereBroken()` **VẪN PASS SAI TRÁI**, vì nó đang mock `mockedDiscount` trả về cứng `100_000L` mà không chạm code thật!
   - `boundaryMockedBehaviorTestSurvivesV1ToV2RefactoringIdentically()` **BẮT ĐƯỢC BUG NGAY LẬP TỨC**, vì `v1` dùng `new CheckoutDomain.DiscountRule()` thật (tính ra `900_000` giảm giá thay vì `100_000`) và lệch ngay với `972_000L`!
2. Vì `boundaryMockedBehaviorTest...` chỉ kiểm tra trạng thái trả về (`total`) và dữ liệu ghi sang cổng biên (`v1Ledger` vs `v2Ledger`), nên dù `V2Refactored` thay đổi cấu trúc tính bên trong thế nào, hợp đồng hành vi ngoài vẫn khớp 100%.

#### Bài 2.2 · Test cho SQL `findActiveCustomerEmails()`
Chuyển sang **Integration Test trên Database thật** (ví dụ Testcontainers PostgreSQL hoặc schema test thật với dữ liệu seed), kiểm chứng chính câu SQL `WHERE ... JOIN ...` chạy với engine thật. Tuyệt đối tránh mock chuỗi `Connection -> PreparedStatement -> ResultSet` vừa dài dòng vừa không kiểm tra được lỗi cú pháp SQL hay sai cột!

#### Bài 2.3 · Test Behavior cho `RetryHttpClient`
Tạo **Stub/Fake `HttpTransport` (hoặc WireMock local server)** cấu hình trả HTTP `503` cho request thứ nhất và HTTP `200` cho request thứ hai. Sau đó assert đúng 2 kết quả hành vi: (1) `PartnerApiClient` trả về dữ liệu thành công, và (2) Fake/Stub `HttpTransport` ghi nhận tổng số lượt gửi ra ngoài là `2` — **không mock** bộ tính thời gian chờ hoặc helper nội bộ bên trong client.

---

### Level 3

#### Bài 3.1 · Kiểm chứng `BoundaryFakeSolution` & PR Checklist
Kết quả chạy thực tế `java -cp target\classes solutions\BoundaryFakeSolution.java`:
```text
VIP=1944000 | STD=2160000 | recordedCharges=[LedgerEntry[orderId=B-1, netPayableVnd=1944000], LedgerEntry[orderId=B-2, netPayableVnd=2160000]]
```

**2 Quy tắc PR Code Review chống Over-mocking:**
1. **Không mock class nội bộ không làm I/O**: Trong review, thấy `@Mock` / `mock(...)` trỏ vào domain helper, mapper, formatter, hoặc `ObjectMapper` → yêu cầu đổi sang **dùng instance thật**.
2. **Cảnh giác với nhiều hơn 1–2 `verify(...)` trong 1 test**: Nếu test toàn `verify(internalCollaborator).step(...)` mà không assert giá trị trả về hay trạng thái cuối tại biên I/O, yêu cầu đổi sang kiểm tra state/Fake thay vì tương tác nội bộ.

---

## Đáp án Câu hỏi tự kiểm tra

<details>
<summary>1. Điểm nhấn của Testing Trophy khác Test Pyramid truyền thống?</summary>

Test Pyramid truyền thống đặt Unit Test là tầng to nhất; **Testing Trophy** đẩy **Integration Test** thành tầng trọng tâm lớn nhất (nhất là ở backend microservices kết nối DB/HTTP) vì tỷ lệ phát hiện bug thật/công viết test cao hơn unit test mock chằng chịt, kèm nền tảng là Static Analysis.

</details>

<details>
<summary>2. Ba trường hợp Integration Test giá trị vượt trội Unit Test có mock?</summary>

(1) Câu SQL / ORM mapping / Flyway migration trên DB thật; (2) Transaction rollback, deadlock, constraint DB (`UNIQUE`, `CHECK`); (3) Wiring Spring Context / HTTP serialization / Security filter chain thực tế.

</details>

<details>
<summary>3. Vì sao nên fake/mock PaymentLedgerPort nhưng không mock DiscountRule/TaxRule?</summary>

`PaymentLedgerPort` là **ranh giới I/O bên ngoài** (DB/HTTP), cần fake/stub để cô lập test chạy nhanh không chạm mạng/DB thật. `DiscountRule` và `TaxRule` là **logic nghiệp vụ nội bộ thuần** không I/O — mock chúng vừa bỏ lọt bug toán học thật vừa phá vỡ test mỗi khi refactor.

</details>

<details>
<summary>4. Ý nghĩa "Test behavior, not implementation" và hậu quả nếu vi phạm?</summary>

Test chỉ nên kiểm tra *kết quả đầu vào → đầu ra và side-effect ở ranh giới hệ thống*, không kiểm tra *hàm nào gọi helper nào bên trong*. Vi phạm khiến bộ test gãy vụn mỗi lần tái cấu trúc code (*brittle tests*) dù nghiệp vụ đúng 100%.

</details>

<details>
<summary>5. DAO chứa 1 câu SQL JOIN 4 bảng: Unit Test mock ResultSet hay Integration Test trên DB thật?</summary>

**Integration Test trên DB thật**, vì toàn bộ rủi ro nằm ở câu SQL JOIN, tên cột, alias, NULL handling và dialect của Database — mock `ResultSet` không kiểm chứng được bất kỳ thứ nào trong số đó mà lại tốn công bảo trì mock.

</details>
