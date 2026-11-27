# Đáp án · CQ-06 · JUnit 5 & Mockito

## Đáp án Bài tập

### Level 1

#### Bài 1.1 · Phân loại Stub / Fake / Mock / Spy
1. `when(rateService.getUsdRate()).thenReturn(25_000L);` → **Stub** (cung cấp giá trị giả để chạy logic tiếp theo).
2. `new InMemoryUserMapRepository();` → **Fake** (triển khai thật rút gọn in-memory có lưu/đọc nhất quán).
3. `verify(smsClient).sendOtp(...);` → **Mock** (xác minh tương tác / hàm có được gọi đúng tham số không).
4. `@Spy EmailFormatter realFormatter = new EmailFormatter();` → **Spy** (chạy code thật nhưng bọc sẵn để theo dõi/verify hoặc stub đè một hàm cụ thể).

#### Bài 1.2 · Vì sao nhiều `verify()` làm test giòn?
Vì `verify(helper).stepX(...)` kiểm tra *cách code bên trong tự gọi nhau thế nào* (**Implementation Detail**), chứ không phải *hệ thống trả về kết quả đúng gì cho caller* (**Observable Behavior**). Khi refactor gộp hoặc đổi tên helper mà đầu ra vẫn đúng 100%, bộ test `verify()` vẫn fail hàng loạt → làm đội dev ngại không dám refactor!

#### Bài 1.3 · Đặt tên & cấu trúc `@Nested` rõ ý
```java
@Nested
@DisplayName("Domestic VIP waiver & Validation rules")
class DomesticValidationTests {
    @Test
    void chargesStandardDomesticFeeWhenVipOrderIsBelow500kThreshold() { ... }

    @Test
    void throwsIllegalArgumentAndSkipsAuditWhenOrderTotalIsNegative() { ... }
}
```

#### Bài 1.4 · Ưu điểm của `@ExtendWith` so với kế thừa `BaseTest`
1. **Java chỉ cho đơn kế thừa** (`extends 1 class`), nên `BaseTest` nhanh chóng phình to thành "God BaseTest" trộn lẫn DB setup, Mockito setup, timing setup; `@ExtendWith({ExtA.class, ExtB.class})` cho phép **ghép đa Extension** sạch sẽ theo nhu cầu từng class test.
2. Extension tách rời trách nhiệm kỹ thuật (timing, container lifecycle, mock injection) khỏi dữ liệu nghiệp vụ của file test và tái sử dụng an toàn giữa nhiều module khác nhau.

---

### Level 2

#### Bài 2.1 · Đọc kết quả `ShippingFeeServiceTest`
1. `9` test gồm: `6` lần chạy của `@ParameterizedTest` trong `PricingMatrixTests` + `3` `@Test` trong `AuditAndValidationTests`.
2. `verify(surchargeSpy, never()).weightSurcharge(10.0)` chứng minh nhánh VIP được miễn giảm sớm đã không tốn công gọi tính surcharge; `ArgumentCaptor` bắt lấy object `QuoteAuditEvent` thực sự gửi cho `AuditTracker` để assert sâu từng field (`orderId`, `shippingFeeVnd = 0`, `freeWaiverApplied = true`) thay vì dùng `any()` hời hợt.
3. Trong `AuditAndValidationTests`, một số test (như `throwsWhenOrderTotalIsNegative`) ném exception trước khi kịp tra bảng giá `rateCatalog`, nên các lời stub cho `DOMESTIC`/`EXPRESS`/`INTERNATIONAL` trong `@BeforeEach` trở thành "thừa" đối với test đó → nếu không có `lenient()`, Mockito strictness sẽ bắn **`UnnecessaryStubbingException`**!

#### Bài 2.2 · `@ParameterizedTest` cho các trường hợp đầu vào lỗi
```java
@ParameterizedTest(name = "total={0}, weight={1} throws IllegalArgumentException")
@CsvSource({
        "-1, 1.0",
        "100000, 0.0",
        "100000, -2.5"
})
void throwsAndSkipsAuditOnInvalidInput(long invalidTotal, double invalidWeight) {
    assertThatThrownBy(() -> service.quoteFee(
            new ShippingFeeService.QuoteRequest("BAD", ShippingFeeService.Tier.DOMESTIC, invalidTotal, invalidWeight, false)))
            .isInstanceOf(IllegalArgumentException.class);
    verify(auditTracker, never()).record(org.mockito.ArgumentMatchers.any());
}
```

#### Bài 2.3 · Bỏ `lenient()` để test sạch nhất
Cách tốt nhất là **tránh dùng Mockito cho bảng dữ liệu tĩnh như `ZoneRateCatalog`**: thay vì `@Mock ZoneRateCatalog` kèm `lenient().when(...)`, truyền thẳng Fake map lambda (`Map.of(DOMESTIC, 25_000L, ...)::get`) trong `@BeforeEach`. Test vừa chạy nhanh hơn, vừa không bao giờ dính `UnnecessaryStubbingException` hay lệ thuộc vào chuyện test nào có tra hay không tra map!

---

### Level 3

#### Bài 3.1 · Phân tích `FakeVsMockRefactorDemo`
1. Nếu `ShippingFeeService` loại bỏ `SurchargeCalculator` và gộp công thức tính vào hàm nội bộ, test `spyDelegatesRealWeightSurchargeForHeavyParcels()` buộc phải **xóa/sửa lại** vì `@Spy SurchargeCalculator` không còn tồn tại trong constructor nữa (`Implementation Coupling`). Ngược lại, cách dùng Fake như [`lab/solutions/FakeVsMockRefactorDemo.java`](lab/solutions/FakeVsMockRefactorDemo.java) chỉ kiểm tra `fee1`, `fee2` và danh sách `recorded` nhận được ở cổng ra → **sống sót nguyên vẹn** qua đợt refactor!
2. Kết quả chạy thực tế `java -cp target\classes solutions\FakeVsMockRefactorDemo.java`:
```text
fee1=0 | fee2=65000 | recordedEvents=2
```

---

## Đáp án Câu hỏi tự kiểm tra

<details>
<summary>1. Tại sao JUnit 5 mặc định tạo một instance test class mới cho mỗi `@Test`?</summary>

Để đảm bảo **Test Isolation**: các field thông thường (`private List<...>`, service instance) được cấp mới sạch sẽ trước mỗi test, tránh việc test trước để lại state rác làm test sau pass/fail sai lệch (*test pollution / order-dependent tests*).

</details>

<details>
<summary>2. So sánh Stub, Fake, Mock, Spy — cái nào dễ bảo trì nhất?</summary>

**Fake** (cùng dùng real internal objects) dễ bảo trì nhất khi refactor vì nó mô phỏng hành vi thật gọn nhẹ không trói vào cách gọi hàm. **Stub** trả dữ liệu giả giúp đi qua nhánh logic; **Mock** assert lời gọi hàm (nên giới hạn cho side-effect ra biên); **Spy** bọc instance thật.

</details>

<details>
<summary>3. Khi nào `verify()` + `ArgumentCaptor` đáng dùng vs khi nào phản tác dụng?</summary>

- **Đáng dùng**: khi kiểm tra **output đi ra I/O biên bên ngoài** (ví dụ service thực sự bắn đúng 1 audit event / message Kafka với payload chính xác, và không bắn khi validation fail).
- **Phản tác dụng**: khi `verify()` từng lời gọi hàm giữa các **helper class nội bộ** cùng module — gây khoá cứng cấu trúc và dễ gãy khi refactor.

</details>

<details>
<summary>4. Tại sao ưu tiên `@ExtendWith` hơn kế thừa `BaseTest`?</summary>

Vì Java chỉ cho đơn kế thừa nên `BaseTest` dễ phình to, ép mọi test phải kéo theo cấu hình thừa. Extension (`@ExtendWith`) theo nguyên tắc **Composition** — có thể ghép nhiều Extension độc lập (Mockito + Timer + TempDir + Container) linh hoạt và gọn nhẹ.

</details>

<details>
<summary>5. Tính năng JUnit 5 kiểm tra 10 biên giá không lặp code?</summary>

**`@ParameterizedTest`** kết hợp **`@CsvSource`** (hoặc `@MethodSource` / `@EnumSource`): mỗi hàng dữ liệu chạy thành một invocation độc lập với tên báo cáo tùy biến qua `name = "..."`, fail hàng nào chỉ rõ đúng bộ tham số của hàng đó.

</details>
