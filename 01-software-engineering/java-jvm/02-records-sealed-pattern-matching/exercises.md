# JVM-02 · Bài tập — Records, Sealed classes, Pattern matching

> Làm theo thứ tự. **Tự làm trước, chỉ mở hint khi đã thử ≥ 15 phút.** Đáp án ở [solutions.md](./solutions.md) — chỉ mở sau khi đã có câu trả lời của riêng bạn.
> Ghi thời gian làm từng bài vào `my-notes.md` để theo dõi tiến bộ.

| Cấp | Mục đích | Số lượng | Thời gian gợi ý |
| :--- | :--- | :---: | :---: |
| 🟢 L1 — Recall | Nhớ & hiểu khái niệm | 5 | 20' |
| 🟡 L2 — Apply | Viết code / phân tích tình huống cụ thể | 3 | 75' |
| 🔴 L3 — Deep | Debug, thiết kế, đo đạc, tình huống mở | 1 | 90' |
| 🏆 Challenge | Mini-project gắn với capstone | 0–1 | cuối tuần |

---

## 🟢 L1 — Recall

### E1. Shallow immutability trong `record`
Cho record sau:
```java
public record BatchTags(String batchId, List<String> tags) {}
```
1. Một caller tạo `List<String> list = new ArrayList<>(List.of("A")); var b = new BatchTags("B1", list); list.add("B");` — `b.tags()` có thay đổi không? Vì sao?
2. Viết lại compact constructor cho `BatchTags` để đảm bảo cả `list` bên ngoài lẫn gọi `b.tags().add("C")` đều không thể làm biến đổi dữ liệu bên trong record.

---

### E2. Ba modifier của `sealed` và lỗi cấu trúc
Giả sử `public sealed interface Event permits ClickEvent, ErrorEvent {}` nằm ở package `lab.events`:
1. Nếu `public class ClickEvent implements Event {}` không ghi `final`, `sealed`, hay `non-sealed` thì có biên dịch được không?
2. Nếu một team khác ở package `com.other` viết `public final class CustomEvent implements Event {}` (trong unnamed module, không dùng JPMS `module-info.java`) và thêm `CustomEvent` vào `permits`, có được phép không?

---

### E3. Pattern dominance & `default` trong `switch`
Đoạn code dưới đây gặp những vấn đề gì? (Chỉ ra 1 lỗi biên dịch và 1 lỗ hổng bảo trì):
```java
sealed interface AuthResult permits Ok, Denied {}
record Ok(String user, boolean isAdmin) implements AuthResult {}
record Denied(String reason) implements AuthResult {}

String route(AuthResult r) {
    return switch (r) {
        case Ok ok -> "USER:" + ok.user();
        case Ok(var user, var admin) when admin -> "ADMIN:" + user;
        default -> "DENIED";
    };
}
```

---

### E4. Giới hạn Record Pattern trong Java 21
Vì sao đoạn code sau **không biên dịch** được trên Java 21, và sửa lại như thế nào cho đúng chuẩn JEP 440/441?
```java
case PaymentResult.Failure(var code, var msg, true) -> "RETRY";
```

---

### E5. `record` vs Lombok `@Value` vs JPA `@Entity`
1. Vì sao không nên gắn `@Entity` lên một Java `record`?
2. Nếu một POJO dùng `@Value` và một `record` cùng có validation `if (age < 0) throw ...` trong constructor, điều gì xảy ra nếu đọc dữ liệu bị chỉnh sửa (`age = -1`) từ `ObjectInputStream`?

---

## 🟡 L2 — Apply

### E6. Tái cấu trúc Mega-DTO thành `sealed interface` + `record`
**Bối cảnh:** Service gửi thông báo đang dùng class `LegacyNotificationDto` với 7 field nullable (`status`, `providerMsgId`, `sentAt`, `retryAfterSeconds`, `bounceCode`, `bounceReason`, `headers` kiểu `List<String>`).

**Yêu cầu:**
Thiết kế lại bằng `sealed interface NotificationOutcome` gồm 3 `record` (`Sent`, `Throttled`, `Bounced`):
- `Sent`: `providerMsgId` (non-blank), `sentAt` (`Instant` non-null), `headers` (`List<String>` immutable defensive copy).
- `Throttled`: `retryAfterSeconds` (`> 0`).
- `Bounced`: `bounceCode` (non-blank), `reason` (non-null, null chuyển thành `""`), `permanent` (`boolean`).

**Input / code khởi đầu:** Tạo/đối chiếu với cấu trúc package `lab.jvm.dop`.
**Tiêu chí hoàn thành:**
- [ ] Biên dịch sạch với `--release 21`.
- [ ] Thử truyền `new ArrayList<>(List.of("X"))` vào `Sent` rồi sửa list gốc → `sent.headers()` không đổi.
- [ ] Thử truyền `retryAfterSeconds <= 0` → ném `IllegalArgumentException`.

<details><summary>💡 Hint</summary>

Dùng nested `record` bên trong `sealed interface NotificationOutcome permits ...` và đặt validate + `List.copyOf(...)` trong compact constructor của từng `record`.
</details>

---

### E7. Viết Router xử lý `NotificationOutcome` không dùng `default`
**Bối cảnh:** Từ mô hình ở E6, hệ thống cần quyết định hành động tiếp theo dựa trên outcome và nội dung cảnh báo.

**Yêu cầu:**
Viết method `decideNextStep(NotificationOutcome outcome)` sử dụng Java 21 `switch` expression + record pattern + `when` guard + `case null`:
- `null` → `"INVALID_NULL"`
- `Sent` có `headers.contains("PRIORITY")` → `"LOG_PRIORITY:" + providerMsgId`
- `Sent` khác → `"LOG_OK:" + providerMsgId`
- `Throttled` với `retryAfterSeconds <= 60` → `"FAST_RETRY:" + retryAfterSeconds`
- `Throttled` khác → `"DELAYED_RETRY:" + retryAfterSeconds`
- `Bounced` với `permanent == true` → `"DISABLE_RECIPIENT:" + bounceCode`
- `Bounced` với `permanent == false` → `"SOFT_BOUNCE:" + bounceCode`

**Input / code khởi đầu:** Dùng `NotificationOutcome` từ E6.
**Tiêu chí hoàn thành:**
- [ ] Không dùng từ khóa `default`.
- [ ] Thử thêm tạm 1 subtype thứ tư `record Cancelled() implements NotificationOutcome` → compiler bắt buộc báo lỗi thiếu nhánh ở `decideNextStep`.

<details><summary>💡 Hint</summary>

Đặt các nhánh có `when` lên trước nhánh không `when` của cùng `record`. Nhánh `Bounced` dùng `case NotificationOutcome.Bounced(var code, var reason, var permanent) when permanent -> ...`.
</details>

---

### E8. Kiểm chứng bảo vệ bất biến khi Deserialize & Soi `PermittedSubclasses`
**Bối cảnh:** Bạn muốn chứng minh cho team thấy giá trị thực tế ở tầng JVM của `record` và `sealed` so với POJO cũ.

**Yêu cầu:**
1. Chạy `mvn -q verify` và `mvn -q compile exec:java` trong [lab/](./lab/README.md).
2. Chạy `javap -v -p` lên `PaymentResult.class` và `PaymentResult$Success.class`, ghi lại đúng tên 2 JVM classfile attribute mới (`PermittedSubclasses` và `Record`) cùng bootstrap method của `equals/hashCode/toString` (`ObjectMethods.bootstrap`).
3. Thêm 1 `record` mới trong test (ví dụ `PositivePrice(long vnd)` bắt buộc `vnd >= 1000`) và kiểm chứng `ObjectInputStream` từ chối stream đã bị sửa thành `500`.

**Input / code khởi đầu:** `lab/src/test/java/lab/jvm/dop/DopComparisonTest.java`.
**Tiêu chí hoàn thành:**
- [ ] `mvn -q verify` pass 100% sau khi thêm test của bạn.
- [ ] Trích được dòng `ObjectMethods.bootstrap` từ output `javap -v`.

<details><summary>💡 Hint</summary>

Dùng `javap -v -p target/classes/lab/jvm/dop/PaymentResult$Success.class` và tìm phần `BootstrapMethods:` ở cuối output.
</details>

---

## 🔴 L3 — Deep

### E9. Sự cố Production: Hoàn tiền nhầm sau khi thêm trạng thái mới
**Tình huống:**
> Hệ thống thanh toán vừa thêm trạng thái `TIMED_OUT` từ cổng ngân hàng mới. Toàn bộ DTO đang dùng `LegacyPaymentResponse` (`String status`, `String txId`, `String errorCode`, `boolean retryable`) và hàm xử lý:
> ```java
> switch (resp.getStatus()) {
>     case "SUCCESS": return Action.CAPTURE;
>     case "PENDING": return Action.POLL;
>     default:        return Action.REFUND_IMMEDIATELY;
> }
> ```
> Kết quả: trong 2 giờ đầu tiên, **4% giao dịch đang chờ xác nhận từ ngân hàng (`TIMED_OUT`) bị chuyển sang `REFUND_IMMEDIATELY`**, gây hoàn tiền nhầm trước khi ngân hàng báo thành công. Ngoài ra, một cache Redis dùng Java serialization phục hồi nhầm một bản ghi `SUCCESS` nhưng `txId == null` (do lỗi deserialize bản cũ thiếu field).

**Yêu cầu:**
1. Phân tích 2 nguyên nhân gốc rễ trong mô hình dữ liệu và luồng xử lý trên.
2. Thiết kế kiến trúc thay thế hoàn toàn bằng `sealed interface` + `record` + `switch` pattern matching (thêm `TimedOut` vào hierarchy), chỉ rõ **tại bước nào** quá trình build sẽ chặn được thảm họa hoàn tiền nhầm nếu sau này thêm trạng thái thứ năm (ví dụ `DISPUTED`).
3. Nếu hệ thống công ty vẫn đang chạy **Java 7 / JBoss AS 7.1.1** chưa lên được Java 21, đề xuất cách phòng ngừa tương đương trên Java 7 để không rơi vào bẫy `default`.

**Tiêu chí hoàn thành:**
- [ ] Có code hoàn chỉnh cho mô hình mới + router xử lý `TimedOut` (tham chiếu mẫu tại `lab/solutions/E9SafePaymentRouter.java`).
- [ ] Giải thích rõ vì sao `record` chặn được lỗi `txId == null` khi deserialize còn POJO không chặn được.

<details><summary>💡 Hint</summary>

1. Nguyên nhân gốc 1: dùng `String status` + `default: REFUND_IMMEDIATELY` khiến compiler không có cách nào cảnh báo khi xuất hiện giá trị `"TIMED_OUT"` mới.
2. Nguyên nhân gốc 2: POJO khi deserialize không chạy constructor (nếu thiếu field hoặc dữ liệu cũ, field `txId` nhận giá trị `null`). Với `record`, `ObjectInputStream` luôn gọi canonical constructor → nổ `InvalidObjectException` ngay nếu `txId == null`.
3. Trên Java 7: dùng `enum` thay `String`, **cấm** dùng `default` mang ý nghĩa nghiệp vụ (chỉ để `default: throw new IllegalStateException(...)`), hoặc dùng `EnumMap` + unit test kiểm tra phủ đủ `Enum.values()`.
</details>

---

## 🏆 Challenge (tùy chọn)
**Gắn với:** Project 1 — [Milestone 1.1](../../../04-projects/01-distributed-backend-system/MILESTONES.md)
**Mô tả:** Trong module `order` của Project 1, thiết kế toàn bộ kết quả xử lý đặt hàng (`ReserveInventoryResult`, `PaymentOutcome`) bằng `sealed interface` + `record` + compact constructor validation; viết unit test xác nhận mọi trạng thái đều có xử lý tường minh không cần `default`.
**Definition of Done:** Không có `OrderResult` mega-DTO nào có field nullable mâu thuẫn nhau; `mvn -q verify` pass.
