# JVM-02 · Đáp án — Records, Sealed classes, Pattern matching

> ⚠️ **Chỉ mở sau khi đã tự làm.** Đọc đáp án mà chưa tự làm = mất 80% giá trị bài tập.
> Mỗi đáp án được bọc trong `<details>` để tránh lộ khi lướt.

---

### E1
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. **Có thay đổi!** `b.tags()` sẽ chứa `["A", "B"]` vì `record` chỉ giữ tham chiếu `private final List<String> tags` (shallow immutability), trỏ chung tới cùng object `ArrayList` mà caller đang giữ.
2. Compact constructor chuẩn:
   ```java
   public record BatchTags(String batchId, List<String> tags) {
       public BatchTags {
           tags = tags == null ? List.of() : List.copyOf(tags);
       }
   }
   ```
   Sau khi dùng `List.copyOf`, sửa `list.add("B")` bên ngoài không ảnh hưởng `b.tags()`, còn gọi `b.tags().add("C")` sẽ ném `UnsupportedOperationException`.

**Giải thích:**
- **Đáp án sai phổ biến:** Nghĩ mọi `record` tự động deep-immutable giống `String` hay primitive → Sai, `record` chỉ `final` tham chiếu field.
</details>

---

### E2
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. **Không biên dịch được.** Trình biên dịch bắt buộc mọi subtype trực tiếp của `sealed` phải khai báo rõ một trong ba modifier: `final`, `sealed`, hoặc `non-sealed`.
2. **Không được phép** trong unnamed module! Khi không dùng JPMS (`module-info.java`), JEP 409 yêu cầu `sealed` type và toàn bộ permitted direct subclasses phải nằm trong **cùng một package** (`lab.events`). Nếu nằm ở package `com.other`, compiler báo lỗi `class is not allowed to extend sealed class from another package`.

**Giải thích:**
- Lý do JVM và compiler bắt buộc cùng module (hoặc cùng package trong unnamed module) là để đảm bảo toàn bộ nhánh con được kiểm soát bởi cùng một tác giả/đơn vị phát hành, giúp exhaustiveness của `switch` luôn nhất quán.
</details>

---

### E3
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. **Lỗi biên dịch (Pattern Dominance):** `case Ok ok` phủ toàn bộ mọi giá trị `Ok`, nên nhánh `case Ok(var user, var admin) when admin` đứng phía sau **không bao giờ chạm tới được** → lỗi `this case label is dominated by a preceding case label`. Phải đảo nhánh có `when admin` lên **trước** `case Ok ok`.
2. **Lỗ hổng bảo trì (`default`):** `default -> "DENIED"` che giấu trường hợp `Denied` lẫn bất kỳ subtype mới nào được thêm vào `AuthResult` sau này (ví dụ `Locked` hoặc `Expired`), làm mất lợi ích kiểm tra đủ nhánh lúc compile. Nên thay bằng `case Denied d -> ...` và bỏ `default`.
</details>

---

### E4
<details><summary>Xem đáp án</summary>

**Đáp án:**
Java 21 (JEP 440) **không hỗ trợ constant pattern / literal pattern bên trong record pattern**, nên `true` nằm trong `Failure(..., true)` gây lỗi biên dịch `illegal start of type`.

**Cách sửa đúng trong Java 21:**
```java
case PaymentResult.Failure(var code, var msg, var retryable) when retryable -> "RETRY";
```

**Giải thích:**
- **Đáp án sai phổ biến:** Nghĩ cú pháp giải cấu trúc của Java 21 giống Scala/Rust/Elixir cho phép khớp giá trị hằng trực tiếp trong pattern → Sai; thành phần trong record pattern Java 21 chỉ nhận type pattern / `var` / nested record pattern, còn điều kiện giá trị phải đặt sau từ khóa `when`.
</details>

---

### E5
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. JPA/Hibernate yêu cầu `@Entity` là class **không `final`**, có **no-arg constructor** (để sinh proxy lazy loading) và thường có **setter / field mutable** (để dirty checking và gán ID sau persist). `record` vi phạm cả ba (`final` class, không có no-arg constructor nếu có component, mọi field `final` và không thêm được field phụ). Dùng `record` cho JPA DTO projection (`SELECT new ...`) hoặc kết quả trả về từ service, **không** dùng làm `@Entity`.
2. Với POJO kiểu `@Value` (deserialize theo cơ chế chuẩn của `ObjectInputStream`), JVM cấp phát trực tiếp object mà **không gọi constructor**, nên object có `age = -1` vẫn được tạo ra âm thầm! Ngược lại, theo JEP 395, khi deserialize `record`, `ObjectInputStream` **bắt buộc đọc các component rồi gọi canonical constructor** → `IllegalArgumentException` được kích hoạt và bọc thành `InvalidObjectException`, chặn đứng object hỏng.
</details>

---

### E6
<details><summary>Xem đáp án</summary>

**Hướng giải:**
- Khai báo `public sealed interface NotificationOutcome permits NotificationOutcome.Sent, NotificationOutcome.Throttled, NotificationOutcome.Bounced`.
- Mỗi `record` tự validate invariant và copy phòng thủ `List.copyOf(headers)` trong compact constructor.

**Code tham khảo đầy đủ:** xem [lab/solutions/E6NotificationOutcome.java](./lab/solutions/E6NotificationOutcome.java).
</details>

---

### E7
<details><summary>Xem đáp án</summary>

**Hướng giải:**
- Dùng `switch (outcome)` không có `default`, đặt `case null -> "INVALID_NULL"` ở đầu, và đặt các nhánh có `when` (`headers.contains("PRIORITY")`, `retryAfterSeconds <= 60`, `permanent`) đứng ngay trước nhánh mặc định của từng subtype.

**Code tham khảo đầy đủ:** xem [lab/solutions/E7NotificationRouter.java](./lab/solutions/E7NotificationRouter.java).
</details>

---

### E8
<details><summary>Xem đáp án</summary>

**Hướng giải:**
1. Chạy `mvn -q verify` (5/5 tests trong `DopComparisonTest` pass) và `mvn -q compile exec:java`.
2. Trong `javap -v -p target/classes/lab/jvm/dop/PaymentResult.class` thấy attribute `PermittedSubclasses`; trong `PaymentResult$Success.class` thấy attribute `Record:` và phần `BootstrapMethods:` gọi `java/lang/runtime/ObjectMethods.bootstrap` cho `equals`, `hashCode`, `toString`.
3. Kiểm chứng deserialization tương tự method `recordDeserializationInvokesCanonicalConstructorWhereasPojoBypassesIt()` trong `DopComparisonTest.java`: thay `int 1000` thành `500` trong mảng byte serialize → `ObjectInputStream.readObject()` ném `InvalidObjectException`.
</details>

---

### E9
<details><summary>Xem đáp án</summary>

<details open><summary>Phân tích & thiết kế sửa triệt để</summary>

1. **Hai nguyên nhân gốc rễ:**
   - **Stringly-typed + `default` nuốt trạng thái mới:** Dùng `String status` và `default: return Action.REFUND_IMMEDIATELY` khiến compiler không có thông tin gì về tập trạng thái hợp lệ. Khi ngân hàng trả `"TIMED_OUT"`, code lặng lẽ rơi vào nhánh `default` và hoàn tiền nhầm.
   - **Deserialization bỏ qua invariant trên POJO:** POJO đọc từ stream/cache không chạy lại constructor, nên bản ghi thiếu `txId` dù `status == "SUCCESS"` vẫn tồn tại trong bộ nhớ.

2. **Kiến trúc thay thế (code đầy đủ tại [lab/solutions/E9SafePaymentRouter.java](./lab/solutions/E9SafePaymentRouter.java)):**
   - Mô hình hóa bằng `sealed interface GatewayOutcome permits Captured, Pending, TimedOut, Declined`.
   - Khi sau này thêm `record Disputed(...) implements GatewayOutcome`, mọi `switch (outcome)` không dùng `default` sẽ **báo lỗi biên dịch ngay ở bước `mvn compile`** trên máy dev và CI, không thể lọt ra production.
   - Do mọi biến thể là `record`, dữ liệu đọc qua `ObjectInputStream` luôn chạy qua canonical constructor nên `Captured` có `txId == null` sẽ bị từ chối ngay khi deserialize.

3. **Phương án trên Java 7 / JBoss AS 7.1.1:**
   - Đổi `String status` thành `enum GatewayStatus` (giá trị đóng) và tách DTO bất biến có validate trong constructor + `readObject`.
   - **Cấm** viết `default: return Action.REFUND_IMMEDIATELY;` trong `switch (status)` — đặt `default: throw new AssertionError("Unhandled status: " + status);` và viết unit test duyệt qua toàn bộ `GatewayStatus.values()` để đảm bảo thêm enum mới mà quên sửa `switch` thì unit test fail ngay.

</details>
</details>

---

## Self-check (từ README §9) — đáp án gợi ý
<details><summary>Xem</summary>

1. **Java `record` vs Lombok `@Value`:** (a) Deserialization của `record` bắt buộc đi qua canonical constructor (giữ nguyên invariant), còn POJO `@Value` bỏ qua constructor; (b) `record` dùng `invokedynamic` → `ObjectMethods.bootstrap` thay vì sinh bytecode `equals/hashCode/toString` dài dòng; (c) `record` kế thừa `java.lang.Record` và cấm thêm non-static instance field (không dựa annotation processor nội bộ `javac`).
2. **Shallow immutability:** `record` chỉ giữ các reference `private final`, không tự deep-copy các đối tượng mutable bên trong như `List` hoặc `Date`. Khắc phục bằng compact constructor gọi `List.copyOf(...)` / `Map.copyOf(...)` (tạo bản sao bất biến).
3. **Không dùng `record` làm JPA `@Entity`:** Vì `record` là `final`, không có no-arg constructor và field bất biến, làm hỏng cơ chế lazy proxy và dirty tracking của Hibernate/JPA. Chỉ nên dùng `record` cho DTO projection (`SELECT new ...`) hoặc tầng API/domain value object.
4. **3 modifier & classfile attribute:** Subtype trực tiếp của `sealed` bắt buộc khai báo `final`, `sealed`, hoặc `non-sealed` (và cùng module/package). Danh sách `permits` được lưu trong attribute `PermittedSubclasses` của file `.class` để JVM kiểm chứng khi load class.
5. **Lỗi compiler bắt được với `sealed` + `switch`:** (a) Thiếu nhánh subtype (non-exhaustive) khi không có `default`; (b) Dominance error khi pattern rộng đặt trước pattern hẹp có `when`; (c) Không khớp kiểu giải cấu trúc trong record pattern; (d) `Illegal start of type` nếu dùng literal trực tiếp trong record pattern Java 21 thay vì `when`.
</details>
