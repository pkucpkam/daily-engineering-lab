# Bài tập · CQ-01 · SOLID áp dụng thực tế

Làm bài trước khi mở [solutions.md](solutions.md). Thực hành trên [lab/](lab/README.md).

---

## Level 1 · Hiểu bản chất & phân biệt (4 bài)

### Bài 1.1 · Đếm số "Actor" trong `LegacyOrderGodService`
Mở [`LegacyOrderGodService.java`](lab/src/main/java/lab/cq/solid/before/LegacyOrderGodService.java). Liệt kê ít nhất **3 Actor (bên liên quan)** khác nhau có lý do thay đổi độc lập trong hàm `processOrder`, chỉ rõ từng dòng nghiệp vụ thuộc về actor nào.

### Bài 1.2 · Bóc trần "Fake DIP"
Một dự án có package `com.acme.service` chứa `PaymentService` (interface) và `PaymentServiceImpl` (duy nhất 1 class triển khai suốt 4 năm), trong đó method viết:

```java
ResultSet chargeCustomer(Connection jdbcConn, HttpServletRequest req);
```

Giải thích tại sao cặp interface/impl này **không phải DIP** mà chỉ là boilerplate gây rối. Sửa lại chữ ký `interface` theo đúng tinh thần DIP + ISP.

### Bài 1.3 · Nhận diện vi phạm LSP ngoài đời thật
Giả sử có `interface FileExporter { byte[] export(ReportData data); }` cam kết luôn trả về mảng byte PDF/CSV hợp lệ (không `null`, độ dài > 0). Một developer thêm `NoOpExporter` trả về `null` (hoặc ném `UnsupportedOperationException`) khi chạy môi trường staging. Vì sao điều này vi phạm LSP và gây hậu quả gì cho caller?

### Bài 1.4 · Nhận diện Over-abstraction (lạm dụng SOLID)
Trong một PR review, đồng nghiệp đề xuất tách một hàm `validateEmailSyntax(String email)` thành 3 interface (`NullCheckStrategy`, `RegexMatchStrategy`, `DomainWhitelistStrategy`) kèm `EmailValidatorFactory`. Bạn phản biện PR này thế nào theo tiêu chí thực dụng?

<details>
<summary>Gợi ý</summary>

Hỏi 2 câu: có bao nhiêu biến thể thực sự cần hoán đổi ở runtime, và các rule này có thay đổi bởi các Actor khác nhau không hay cùng một logic validation đơn giản?

</details>

---

## Level 2 · Thực hành lab & đánh giá refactor (3 bài)

### Bài 2.1 · Chạy lab & chứng minh tính đẳng trị trước/sau refactor
Chạy `mvn -o test` trong `01-solid-in-practice/lab/`. Đọc [`SolidRefactorTest.java`](lab/src/test/java/lab/cq/solid/SolidRefactorTest.java) và giải thích:
1. Tại sao bản test `OrderProcessor` chạy không cần database hay mail server thật nhưng vẫn tin cậy hơn việc chạy tay `LegacyOrderGodService`?
2. Dòng `check` nào trong `OrderProcessor.process(...)` đang thực thi bảo vệ hậu điều kiện của LSP?

### Bài 2.2 · So sánh điểm khác biệt giữa Refactor OCP và Sửa nhánh `if-else`
Trong `LegacyOrderGodService`, muốn thêm tier `"STAFF"` giảm 20%, bạn phải làm gì? Trong `OrderProcessor`, muốn thêm `"STAFF"` giảm 20% mà **không chạm vào mã nguồn đã biên dịch của `OrderProcessor`**, bạn viết thế nào? Viết đoạn code ngắn (3–5 dòng) chứng minh.

### Bài 2.3 · Chuyển ngữ cảnh Java 7 / JDBC thuần trên JBoss
Bạn đang bảo trì class Java 7 sau trong một EJB/Servlet:

```java
public class DailyBillingJob {
    public void runBilling(String date) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@prod:1521:ORCL", "user", "pass");
        // 150 dòng query + tính tiền phạt trễ hạn + update DB + gọi HttpURLConnection gửi webhook
    }
}
```

Áp dụng **DIP + SRP ở mức tối giản** trên Java 7 (không Spring, không framework ngoài) để có thể unit test logic tính tiền phạt trong JUnit 4/5 mà không chạm Oracle DB hay HTTP webhook.

---

## Level 3 · Mở rộng chính sách mới giữ vững hợp đồng SOLID (1 bài)

### Bài 3.1 · Thiết kế `PricingPolicy` mới có trần giảm giá (Capped Tier)
Yêu cầu nghiệp vụ mới từ Kinh doanh:
- Thêm hạng `"ENTERPRISE"`: giảm **25%** trên `subtotal`, nhưng **tối đa không quá `100.0`** (capped discount), đảm bảo luôn thoả bất biến LSP (`0 <= discount <= subtotal`).
- Phải cắm vào `OrderProcessor` theo đúng nguyên tắc **OCP** (không sửa file `OrderProcessor.java`).

**Yêu cầu**:
1. Viết giải pháp (hoặc đối chiếu [`lab/solutions/CappedTierSolution.java`](lab/solutions/CappedTierSolution.java)) định nghĩa policy cho `"ENTERPRISE"`.
2. Chạy `java -cp target\classes solutions\CappedTierSolution.java` với đơn `subtotal = 1000.0`, kiểm tra `discount = 100.0` (không phải `250.0`), `afterDiscount = 900.0`, `tax = 72.0`, `shipping = 5.0` → in ra đúng **`total=977.0`**.
3. Kiểm tra thêm trường hợp biên `subtotal = 200.0` (`25% = 50.0 < 100.0`) và `subtotal = 0.0` xem hợp đồng LSP có còn giữ nguyên không.

**Tiêu chí đạt**: Không sửa `OrderProcessor.java`, giữ đúng invariant `0 <= discount <= subtotal`, và chạy `CappedTierSolution.java` ra `Saved ENT-1 total=977.0`.
