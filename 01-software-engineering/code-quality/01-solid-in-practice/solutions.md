# Đáp án · CQ-01 · SOLID áp dụng thực tế

## Đáp án Bài tập

### Level 1

#### Bài 1.1 · Các Actor trong `LegacyOrderGodService`
1. **Kinh doanh / Marketing**: kiểm soát quy tắc chiết khấu theo hạng (`VIP`, `PARTNER`, `STANDARD`) và nội dung thông báo `[EMAIL] Order ... confirmed`.
2. **Tài chính / Logistics**: kiểm soát thuế suất VAT `0.08`, phí giao `express` (`15.0` vs `5.0`) và quy tắc làm tròn tổng tiền.
3. **DBA / Data Persistence**: kiểm soát định dạng bản ghi CSV lưu trữ (`orderId,customerTier,total`).
*(Thêm: nhóm **API/Ops** cho quy tắc validation đầu vào).*

#### Bài 1.2 · Vì sao `PaymentService` + `PaymentServiceImpl` kia là Fake DIP
- Interface truyền thẳng `java.sql.Connection` và `javax.servlet.http.HttpServletRequest`: module cấp cao vẫn phụ thuộc chặt vào JDBC và Servlet API (chi tiết hạ tầng cấp thấp!). Đổi sang REST controller mới hay JPA/HTTP client mới đều làm interface gãy theo.
- **Sửa chuẩn DIP + ISP**: tách riêng dữ liệu vào thành một domain DTO/record (ví dụ `ChargeCommand(String orderId, long amountVnd)`), interface chỉ nhận kiểu domain:

```java
public interface PaymentGateway {
    ChargeResult charge(ChargeCommand command);
}
```

#### Bài 1.3 · Vi phạm LSP với `NoOpExporter`
`NoOpExporter` trả `null` hoặc ném `UnsupportedOperationException` là vi phạm hậu điều kiện (`non-null, length > 0`) mà caller tin tưởng dựa vào interface `FileExporter`. Mọi caller gửi email đính kèm `exporter.export(data)` sẽ văng `NullPointerException` bất ngờ trên staging, ép dev khác phải viết thêm `if (!(exporter instanceof NoOpExporter))` (kéo theo vi phạm OCP!). Nếu muốn bản giả lập không xuất file thật ở staging, nên trả về một mảng byte PDF/CSV tối thiểu hợp lệ (Null Object pattern bảo toàn hợp đồng).

#### Bài 1.4 · Phản biện PR Over-abstraction
- Chỉ có 1 actor (validation đầu vào), các bước kiểm tra không cần hoán đổi đa hình độc lập lúc runtime.
- Việc băm một hàm 10 dòng dễ hiểu thành 3 interface + 1 factory làm tăng vọt tổng số file và mức độ khó đọc (**Classitis / over-abstraction**), không mang lại khả năng mở rộng thực tế nào. Nên viết 1 static/helper method gọn gàng; chỉ tách Strategy nếu tương lai phát sinh thật sự nhiều bộ luật theo từng quốc gia/tenant khác nhau.

---

### Level 2

#### Bài 2.1 · Ý nghĩa của `SolidRefactorTest`
1. Nhờ **DIP** (`OrderRepository`, `OrderNotifier`), test truyền trực tiếp lambda in-memory (`saved::add`, `r -> alerts.add(...)`) vào constructor của `OrderProcessor`, kiểm tra 100% logic tính chiết khấu, thuế, phí ship và bảo toàn hành vi cũ trong ~`109 ms` không cần DB/SMTP.
2. Hai dòng kiểm tra trong `OrderProcessor.process`:
```java
double discount = policy.calculateDiscount(request.subtotal());
if (discount < 0 || discount > request.subtotal()) {
    throw new IllegalStateException("LSP contract violated: invalid discount " + discount);
}
```
Đây là hậu điều kiện bảo vệ hợp đồng LSP: mọi `PricingPolicy` đều không được làm tổng tiền âm hoặc tăng vọt vì giảm giá âm.

#### Bài 2.2 · Thêm hạng `"STAFF"` theo OCP
- Trong `LegacyOrderGodService`: phải sửa trực tiếp khối `if-else` bên trong class cũ (vi phạm OCP, buộc build & test lại toàn bộ core god-class).
- Trong `OrderProcessor`: chỉ cần tạo map cấu hình mở rộng bên ngoài:

```java
Map<String, OrderModels.PricingPolicy> policies = new HashMap<>(OrderProcessor.defaultPolicies());
policies.put("STAFF", subtotal -> subtotal * 0.20);
OrderProcessor processor = new OrderProcessor(policies, repository, notifier);
```

#### Bài 2.3 · Áp dụng DIP + SRP trên Java 7 / JDBC thuần
Tách thành 3 thành phần rõ ràng (chỉ dùng Java 7 thuần, chưa cần framework DI):
1. `interface BillingStore` (`List<Invoice> findUnpaidInvoices(String date); void savePenalty(String id, long fee);`) -> `JdbcBillingStore` giữ `DriverManager` / `PreparedStatement`.
2. `interface BillingWebhook` (`void sendNotice(String id, long fee);`) -> `HttpBillingWebhook` giữ `HttpURLConnection`.
3. `PenaltyPolicy` (POJO/method thuần không I/O) + `DailyBillingService(BillingStore store, BillingWebhook webhook)`: nhận 2 port qua constructor nên unit test chỉ cần fake `InMemoryBillingStore` và `FakeWebhook` chạy vài mili-giây!

---

### Level 3

#### Bài 3.1 · Capped Tier `"ENTERPRISE"`
Xem code hoàn chỉnh ở [`lab/solutions/CappedTierSolution.java`](lab/solutions/CappedTierSolution.java):

```java
public static OrderModels.PricingPolicy cappedTierPolicy(double rate, double maxDiscount) {
    return subtotal -> Math.min(maxDiscount, Math.max(0.0, subtotal * rate));
}
```

- Với `subtotal = 1000.0`: `1000 * 0.25 = 250.0` bị chặn về `100.0` → `afterDiscount = 900.0`, `tax = 72.0`, `shipping = 5.0` → kết quả chạy thực tế:
```text
Saved ENT-1 total=977.0
Notified tier=ENTERPRISE
```
- Kiểm tra biên: với `subtotal = 200.0` → `min(100.0, 50.0) = 50.0` (`<= 200`); với `subtotal = 0.0` → `0.0`. Công thức luôn thoả `0 <= discount <= subtotal` nên giữ trọn hợp đồng LSP.

---

## Đáp án Câu hỏi tự kiểm tra

<details>
<summary>1. `printReport()` và `saveReport()` trong 1 class vi phạm nguyên tắc nào?</summary>

Vi phạm **SRP (Single Responsibility Principle)**. Hai method phục vụ 2 **Actor** hoàn toàn khác nhau: Kế toán (muốn thay đổi layout PDF/báo cáo) và DBA/DevOps (muốn thay đổi schema bảng hoặc nơi lưu trữ). Đặt chung khiến sửa phần này có nguy cơ làm hỏng phần kia.

</details>

<details>
<summary>2. Vì sao `CustomerService` + `CustomerServiceImpl` dính `ResultSet` / `@Entity` không phải DIP?</summary>

Vì abstraction (`CustomerService`) đang phụ thuộc ngược vào chi tiết hạ tầng (`ResultSet`, JPA entity) thay vì dùng kiểu dữ liệu thuần của domain. Đây chỉ là lớp bọc 1-1 dư thừa; khi đổi JDBC sang REST API, cả interface cũng phải đổi theo.

</details>

<details>
<summary>3. Điều kiện đảm bảo một implementation mới không vi phạm LSP?</summary>

Nó phải chấp nhận được ít nhất toàn bộ đầu vào mà interface cha hứa hỗ trợ (**không siết chặt precondition**), luôn trả về đầu ra thoả mọi ràng buộc/bất biến mà interface cha cam kết (**không nới lỏng postcondition**, không trả `null` bất ngờ hay ném ngoại lệ mới nằm ngoài hợp đồng).

</details>

<details>
<summary>4. Tại sao `tax = afterDiscount * 0.08` không cần bọc trong `interface TaxCalculator`?</summary>

Vì phép tính nội bộ này không có I/O, không phụ thuộc đa hình tại runtime và hiện tại chưa có trục biến đổi thực tế nào. Bọc interface lúc này tạo *over-abstraction* vô ích (khi nào có rule thuế đa quốc gia hãy dùng Refactor -> Extract Interface/Strategy).

</details>

<details>
<summary>5. Áp dụng DIP trên Java 7 + JDBC thuần không có Spring bằng cách nào?</summary>

Định nghĩa một interface port nhỏ trong package domain (ví dụ `OrderRepository`), chuyển hết code `Connection`/`PreparedStatement` vào `JdbcOrderRepository implements OrderRepository`, rồi truyền `OrderRepository` qua constructor của service (*Constructor Injection thủ công*). Khi viết unit test chỉ cần truyền `FakeOrderRepository` bằng `HashMap`.

</details>
