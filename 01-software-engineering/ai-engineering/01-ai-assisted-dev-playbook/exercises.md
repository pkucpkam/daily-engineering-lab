# AI-01 · Bài tập — AI-assisted development playbook

> Làm theo thứ tự. **Tự làm trước, chỉ mở hint khi đã thử ≥ 15 phút.** Đáp án ở [solutions.md](./solutions.md) — chỉ mở sau khi đã có câu trả lời của riêng bạn.
> Ghi thời gian làm từng bài vào `my-notes.md` để theo dõi tiến bộ.

| Cấp | Mục đích | Số lượng | Thời gian gợi ý |
| :--- | :--- | :---: | :---: |
| 🟢 L1 — Recall | Nhớ & hiểu cơ chế lỗi AI & quy trình | 5 | 20' |
| 🟡 L2 — Apply | Soi & sửa 3 đoạn code AI lỗi tinh vi trong lab | 3 | 75' |
| 🔴 L3 — Deep | Review PR 100% do AI viết cho legacy JBoss AS 7.1.1 | 1 | 90' |

---

## 🟢 L1 — Recall

### E1. Vì sao AI hay sinh ra "check-then-act race condition" ngay cả khi dùng `ConcurrentHashMap`?
Một bạn trong team nói: *"Yên tâm, hàm này dùng `ConcurrentHashMap` rồi nên đoạn `if (map.get(k) < 5) map.put(k, map.get(k) + 1)` hoàn toàn thread-safe"*. Nhận định này **Đúng hay Sai**? Giải thích cơ chế khiến lỗi xảy ra và tại sao LLM lại dễ sinh mẫu code này.

---

### E2. Phân biệt Hallucinated API và Version Drift
Cho 2 đoạn code AI gợi ý cho dự án chạy **Java 7 / JBoss AS 7.1.1**:
1. `Files.readString(Paths.get("config.txt"), StandardCharsets.UTF_8);`
2. `HttpClient.newBuilder().retryPolicy(RetryPolicy.fixedDelay(3)).build();`
Cả hai đều không chạy được trên máy công ty, nhưng bản chất lỗi khác nhau thế nào? Cách phát hiện nhanh nhất mỗi loại là gì?

---

### E3. Bài test AI sinh ra nhưng "không bao giờ có thể đỏ"
Nhìn đoạn unit test do AI sinh bên dưới:
```java
@Test
void shouldSaveOrder() {
    OrderRepository repo = mock(OrderRepository.class);
    OrderService service = new OrderService(repo);
    assertDoesNotThrow(() -> service.placeOrder("item-1", 2));
    verify(repo, atLeastOnce()).save(any());
}
```
Chỉ ra 3 lỗ hổng nghiêm trọng của bài test này khiến bug nghiệp vụ (ví dụ trừ sai tiền, tính sai số lượng, hoặc quên validate số âm) vẫn lọt qua CI xanh 100%.

---

### E4. Thứ tự "Viết trước, hỏi sau"
Khi nhận một file legacy 400 dòng lạ trên JBoss để tìm nguyên nhân NullPointerException ở production, giữa hai cách làm:
- **Cách A:** Dán ngay 400 dòng cho AI hỏi *"Tìm NPE giúp tôi"*.
- **Cách B:** Tự đọc 10 phút theo `READ-01`, viết 3 giả thuyết và vị trí nghi ngờ rồi mới đưa cho AI phản biện.
Giải thích 2 rủi ro thực tế của Cách A trong ngắn hạn (tính đúng đắn) và dài hạn (năng lực kỹ sư).

---

### E5. Bẫy Slopsquatting khi dùng dependency AI gợi ý
AI gợi ý thêm dependency `<groupId>org.apache.commons</groupId><artifactId>commons-text-utils</artifactId>` vào `pom.xml` để escape HTML/SQL. Tại sao việc copy-paste thẳng coordinate đó từ AI lại có thể tạo ra **lỗ hổng bảo mật chuỗi cung ứng** nghiêm trọng chứ không chỉ là lỗi build?

---

## 🟡 L2 — Apply

### E6. Tái hiện & sửa Race Condition + Memory Leak trong `OrderRateLimiterBuggy`
**Bối cảnh:** Trong [lab/](./lab/README.md), class `OrderRateLimiterBuggy` được AI sinh ra để giới hạn tối đa `N` request mỗi `userId`.
**Yêu cầu:**
1. Chạy `mvn -o -B verify` trong `lab/` để xem `AiBugReproductionTest#rateLimiterAllowsMoreThanMaxUnderConcurrency` chứng minh race condition vượt quota.
2. Tự viết bản sửa hoàn chỉnh (trước khi mở `lab/solutions/OrderRateLimiterFixed.java`) đảm bảo:
   - Không bao giờ cho phép quá `maxRequests` request ngay cả khi 32 thread gọi đồng thời cùng 1 `userId`.
   - Validate `userId` (`null`/blank → `IllegalArgumentException`).
   - Có cơ chế `evictExpired(Duration ttl)` hoặc giới hạn vòng đời key để không leak RAM vô hạn.
3. Viết JUnit 5 test kiểm chứng với `CountDownLatch` + `ExecutorService`.

**Tiêu chí hoàn thành:**
- [ ] Giải thích chính xác race condition giữa `getOrDefault` và `put`.
- [ ] Test chạy 32 thread × 10 vòng với `maxRequests = 10` luôn cho đúng `10` request thành công (chạy lặp 20 lần không flaky).

<details><summary>💡 Hint</summary>

Dùng `AtomicInteger` (hoặc immutable record/counter có timestamp) trong `ConcurrentHashMap.computeIfAbsent`, cập nhật bằng vòng lặp `compareAndSet` hoặc dùng `ConcurrentHashMap.compute(userId, ...)` cho toàn bộ thao tác kiểm tra + tăng biến đếm trong 1 bucket lock duy nhất!
</details>

---

### E7. Săn 3 lỗi ẩn trong `InvoiceJdbcHelperBuggy` & `PaymentClientBuggy`
**Bối cảnh:** Mở 2 file trong `lab/src/main/java/lab/ai/playbook/buggy/`:
- `InvoiceJdbcHelperBuggy.java`
- `PaymentClientBuggy.java`

**Yêu cầu:**
Áp dụng **Checklist 7 điểm** (README §3.5) đọc tay *trước khi chạy test*, liệt kê đầy đủ toàn bộ lỗi ẩn trong 2 file trên (tối thiểu 6 lỗi tổng cộng, phân rõ nhóm: Concurrency, Edge case, Resource/Exception, Security). Sau đó đối chiếu với các test chứng minh lỗi trong `AiBugReproductionTest.java` và tự viết bản sửa `Fixed`.

**Tiêu chí hoàn thành:**
- [ ] Tìm đủ ít nhất 6 lỗi thực tế (SQL injection/resource leak, `BigDecimal` rounding, off-by-one/negative qty, `SimpleDateFormat` thread-unsafe, retry nhầm HTTP 4xx, nuốt `InterruptedException`).
- [ ] Bản sửa của bạn pass mọi assertion tương ứng với `lab/solutions/` (không dùng `SimpleDateFormat` dùng chung, đóng resource an toàn, khôi phục interrupt flag).

<details><summary>💡 Hint</summary>

Soi kỹ từng dòng:
1. Câu SQL trong `InvoiceJdbcHelperBuggy` ghép chuỗi gì? `Connection`/`Statement` đóng ở đâu nếu ném exception? `BigDecimal.divide` thiếu gì khi chia cho `3`?
2. Trong `PaymentClientBuggy`: biến `DATE_FORMAT` là `static final`, điều gì xảy ra khi 8 thread cùng `parse`/`format`? Khi HTTP trả `400 Bad Request` vòng `retry` có dừng không? Khối `catch (InterruptedException)` thiếu lệnh gì?
</details>

---

### E8. Xây dựng bộ kiểm chứng "API Hallucination Detector" cho Java target
**Bối cảnh:** Trong team chạy production **Java 7**, developer hay vô tình copy code AI dùng API của Java 11/17/21 hoặc tên method bịa.
**Yêu cầu:**
1. Mở `lab/src/main/java/lab/ai/playbook/ApiVerifier.java` và `PlaybookLabRunner.java`.
2. Thêm danh sách kiểm tra cho 5 snippet nghi vấn (ví dụ: `String#isBlank`, `List#of`, `Optional#orElseThrow`, `java.net.http.HttpClient#retryPolicy`, `Thread#ofVirtual`) và chạy đối chiếu xem method nào tồn tại ở runtime hiện tại, method nào không tồn tại ở Java 7 (`@since` > 1.7), và method nào bịa hoàn toàn.
3. Trình bày cách dùng `javac --release 7` (hoặc Maven Animal Sniffer / `maven-compiler-plugin <release>7</release>`) làm cổng CI tự động chặn AI API drift.

**Tiêu chí hoàn thành:**
- [ ] Phân loại đúng 3 nhóm: (a) Có ở JDK 7, (b) Có từ JDK 8+ (Version Drift), (c) Không tồn tại ở bất kỳ JDK nào (Hallucination).
- [ ] Chạy `mvn -o -B compile exec:java` in ra bảng phân loại rõ ràng.

<details><summary>💡 Hint</summary>

Dùng `Class.forName(...)` + `Class.getMethod(...)` kết hợp bảng metadata `@since` để phân biệt API không tồn tại vs API lệch version; trong CI thực tế, `<maven.compiler.release>7</maven.compiler.release>` (hoặc `-source 1.7 -target 1.7` với bootclasspath JDK 7) sẽ khiến `mvn compile` fail ngay tức khắc khi AI dùng `String.isBlank()`.
</details>

---

## 🔴 L3 — Deep

### E9. Ticket Review PR: Module báo cáo thanh toán viết 100% bằng AI cho JBoss AS 7.1.1
**Tình huống:**
> Một đồng nghiệp nộp PR sau cho hệ thống **Java 7 / JBoss AS 7.1.1 / Oracle JDBC** và nói *"Em dùng AI viết nhanh cho kịp sprint, chạy thử trên máy em thấy compile được"*:
> ```java
> public class DailySettlementJob {
>     private static final SimpleDateFormat FMT = new SimpleDateFormat("yyyy-MM-dd");
>     private static final Map<String, BigDecimal> CACHE = new HashMap<>();
>
>     public BigDecimal calculateAverageTicket(DataSource ds, String merchantId, String dateStr) throws Exception {
>         String key = merchantId + ":" + dateStr;
>         if (CACHE.containsKey(key)) {
>             return CACHE.get(key);
>         }
>         Connection conn = ds.getConnection();
>         Statement st = conn.createStatement();
>         ResultSet rs = st.executeQuery("SELECT amount FROM settlements WHERE merchant_id = '"
>                 + merchantId + "' AND tx_date = '" + dateStr + "'");
>         BigDecimal sum = BigDecimal.ZERO;
>         int count = 0;
>         while (rs.next()) {
>             sum = sum.add(rs.getBigDecimal("amount"));
>             count++;
>         }
>         BigDecimal avg = sum.divide(BigDecimal.valueOf(count));
>         if (!avg.toString().isBlank()) {
>             CACHE.put(key, avg);
>         }
>         rs.close();
>         st.close();
>         conn.close();
>         return avg;
>     }
> }
> ```

**Yêu cầu:**
1. Dùng **Checklist 7 điểm** chỉ ra toàn bộ lỗi nghiêm trọng trong 28 dòng code trên (có ít nhất **7 lỗi thuộc ít nhất 5 nhóm khác nhau**!).
2. Giải thích vì sao câu nói *"chạy thử trên máy em thấy compile được"* của đồng nghiệp có thể xảy ra (liên hệ JDK trên máy dev vs JDK 7 trên server JBoss).
3. Viết lại class `DailySettlementJob` đúng chuẩn **Java 7 + JDBC + JBoss AS 7.1.1** (không dùng API Java 8+, không SQL injection, không leak connection khi exception, không NPE/chia 0/`ArithmeticException`, không race condition/leak `CACHE` vô hạn).

**Tiêu chí hoàn thành:**
- [ ] Liệt kê đủ ≥ 7 lỗi thực tế và giải thích hậu quả từng lỗi trên JBoss production.
- [ ] Bản sửa viết lại tương thích Java 7 (chạy được với `try-with-resources` + `PreparedStatement` + `RoundingMode.HALF_UP` + xử lý `count == 0` & `null` amount + bound/thread-safe cache).

<details><summary>💡 Hint</summary>

Đếm kỹ từng dòng:
1. `SimpleDateFormat` static (thread-unsafe, thậm chí chưa dùng).
2. `HashMap` static không đồng bộ + không giới hạn kích thước (vừa race vừa OOM Heap trên JBoss!).
3. `Connection` / `Statement` / `ResultSet` không đóng trong `try-with-resources` (có từ Java 7) -> leak JDBC pool khi SQL nổ lỗi!
4. SQL Injection (`merchantId` + `dateStr` nối chuỗi trực tiếp).
5. `rs.getBigDecimal("amount")` có thể trả `null` -> `sum.add(null)` ném `NullPointerException`.
6. Nếu không có dòng nào (`count == 0`) hoặc chia không hết (`100 / 3`) -> `sum.divide(...)` ném `ArithmeticException: Non-terminating decimal expansion` hoặc chia cho 0!
7. `.isBlank()` mới có từ **Java 11** -> máy dev dùng JDK 17/21 compile xanh, lên JBoss Java 7 nổ ngay `NoSuchMethodError` hoặc fail compile!
</details>
