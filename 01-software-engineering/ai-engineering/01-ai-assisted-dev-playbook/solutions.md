# AI-01 · Đáp án — AI-assisted development playbook

> ⚠️ **Chỉ mở sau khi đã tự làm.** Đọc đáp án mà chưa tự làm = mất 80% giá trị bài tập.
> Mỗi đáp án được bọc trong `<details>` để tránh lộ khi lướt.

---

### E1
<details><summary>Xem đáp án</summary>

**Đáp án:**
**Sai.** Đoạn code `if (map.get(k) < 5) map.put(k, map.get(k) + 1)` dính lỗi **check-then-act race condition**.

**Giải thích:**
- `ConcurrentHashMap` chỉ đảm bảo *từng lời gọi đơn lẻ* (`get` hoặc `put`) là thread-safe (không làm hỏng cấu trúc nội bộ bảng băm). Chuỗi `get → kiểm tra → get → cộng 1 → put` là **4 thao tác tách rời không atomic**.
- Nếu 2 thread cùng vào khi giá trị đang là `4`: cả hai cùng đọc `4 < 5`, cùng cộng thành `5`, và cùng ghi `5` (mất một lần đếm — *lost update*), hoặc nếu `max = 5` thì có thể có nhiều thread vượt qua cổng cùng lúc khi đang ở sát ngưỡng!
- LLM hay sinh mẫu này vì phần lớn tutorial đơn giản dùng `HashMap`, khi được bảo "làm cho thread-safe", mô hình chỉ thay chữ `HashMap` thành `ConcurrentHashMap` mà không đổi thuật toán sang atomic CAS/`compute`.

**Đáp án sai phổ biến:** Nghĩ thêm từ khóa `volatile` cho biến `map` sẽ sửa được lỗi này → Sai, `volatile` chỉ bảo đảm visibility của tham chiếu tới `map`, không làm compound action trở nên atomic.
</details>

---

### E2
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. `Files.readString(...)` là **Version Drift (lệch phiên bản)**: đây là method có thật, nhưng được thêm từ **Java 11** (`@since 11`), không tồn tại trên **Java 7** của JBoss AS 7.1.1.
2. `HttpClient.newBuilder().retryPolicy(...)` là **Hallucinated API (bịa API)**: `java.net.http.HttpClient` có thật từ Java 11, nhưng không hề có method `.retryPolicy(...)` hay class `RetryPolicy` trong JDK tiêu chuẩn ở bất kỳ phiên bản nào!

**Cách phát hiện nhanh nhất:**
- Chạy biên dịch với đúng `--release 7` (cho dự án Java 7) hoặc chạy trên JDK mục tiêu; compiler báo ngay `cannot find symbol`.
- Trong IDE, bấm `Ctrl+B` / `Go to Declaration` trên `HttpClient.Builder` để xem danh sách method thật.
</details>

---

### E3
<details><summary>Xem đáp án</summary>

**Đáp án — 3 lỗ hổng của bài test AI sinh:**
1. **Không assert kết quả hoặc trạng thái lưu thật:** `assertDoesNotThrow` + `verify(repo, atLeastOnce()).save(any())` chỉ kiểm tra "không ném exception và có gọi `save` với một object bất kỳ". Nếu `placeOrder("item-1", 2)` tính tổng tiền bị âm, gán nhầm `quantity = 0`, hoặc lưu sai trạng thái, test **vẫn xanh**.
2. **Không dùng `ArgumentCaptor` hoặc kiểm tra return value / state:** Không kiểm tra `Order` truyền vào `save(...)` có đúng `itemId == "item-1"` và `qty == 2` không.
3. **Thiếu hoàn toàn boundary & negative cases:** Chưa test `qty <= 0`, `itemId == null`, hoặc tình huống kho hết hàng.

**Đáp án sai phổ biến:** Nghĩ chỉ cần thêm `verifyNoMoreInteractions(repo)` là test đã chặt → Sai, test vẫn không kiểm tra được tính đúng của dữ liệu tính toán và trói chặt vào implementation (xem `CQ-07`).
</details>

---

### E4
<details><summary>Xem đáp án</summary>

**Đáp án:**
- **Rủi ro ngắn hạn (tính đúng đắn):** NPE trên legacy JBoss thường phụ thuộc vào state giấu ở chỗ khác (JNDI lookup, session attribute, cột DB nullable, dữ liệu truyền từ caller khác không nằm trong 400 dòng đó). AI không thấy caller hay schema thật nên sẽ bịa ra giả thuyết hợp lý bề mặt (ví dụ bảo "thêm `if (x == null)`" — vô tình **che giấu** lỗi dữ liệu hỏng sớm ở tầng trên thay vì sửa tận gốc).
- **Rủi ro dài hạn (năng lực kỹ sư):** Não bộ bỏ qua giai đoạn xây dựng mô hình luồng dữ liệu (*mental model*). Khi gặp sự cố trực tiếp trên server production lúc 2 giờ sáng hoặc không được dùng AI công cụ ngoài, kỹ sư không thể tự đọc call stack và trace dữ liệu.
</details>

---

### E5
<details><summary>Xem đáp án</summary>

**Đáp án:**
Đây là nguy cơ tấn công chuỗi cung ứng gọi là **Slopsquatting** (tương tự typosquatting nhưng nhắm vào tên package mà LLM thường xuyên bịa ra một cách lặp đi lặp lại).
- Trên Apache Commons thật chỉ có `commons-text` (`org.apache.commons:commons-text`) hoặc `commons-lang3`, không có `commons-text-utils`.
- Nếu kẻ tấn công khảo sát các tên package mà LLM hay bịa và đăng ký chính xác coordinate/package đó trên kho lưu trữ công khai (ví dụ npm/PyPI, hoặc artifact giả mạo) rồi cài mã độc vào, developer copy nguyên gợi ý từ AI và build sẽ kéo trực tiếp mã độc về máy công ty / CI server!
</details>

---

### E6
<details><summary>Xem đáp án</summary>

**Hướng giải:**
1. Trong `OrderRateLimiterBuggy`, hai lời gọi `counters.getOrDefault` và `counters.put` tách rời nhau tạo ra cửa sổ race (*check-then-act*): nhiều thread cùng thấy `current < maxRequests`, cùng cho qua, rồi ghi đè lẫn nhau.
2. Để sửa đúng chuẩn: dùng 1 bucket `WindowCounter` (chứa `AtomicInteger count` + `long windowStartMillis`) hoặc dùng `counters.compute(userId, (k, bucket) -> ...)` để toàn bộ kiểm tra cửa sổ thời gian + kiểm tra ngưỡng + tăng đếm nằm trọn trong **bucket lock atomic của `ConcurrentHashMap`**.
3. Thêm `evictExpired(Duration ttl)` xóa các key đã quá hạn để không leak Heap khi có hàng triệu `userId` khác nhau đi qua hệ thống.

**Code lời giải đầy đủ:** xem [lab/solutions/OrderRateLimiterFixed.java](./lab/solutions/OrderRateLimiterFixed.java).

**Kết quả mong đợi:** Với 32 thread × 10 vòng và `maxRequests = 10`, bản buggy cho lọt `11..32` request, còn bản fixed cho lọt **chính xác `10`** request ở 100% lần chạy.
</details>

---

### E7
<details><summary>Xem đáp án</summary>

**Danh sách 6+ lỗi ẩn trong 2 file buggy:**

**File 1 — `InvoiceJdbcHelperBuggy.java`:**
1. **SQL Injection:** ghép trực tiếp `customerId` vào SQL `"SELECT ... WHERE customer_id = '" + customerId + "'"`.
2. **Resource Leak:** mở `Connection`, `Statement`, `ResultSet` không dùng `try-with-resources` (nếu `executeQuery` hoặc tính toán ném exception, cả 3 không bao giờ được đóng → cạn JDBC pool JBoss!).
3. **`BigDecimal.divide` thiếu scale & `RoundingMode`:** khi chia cho `3` hoặc số không chia hết (ví dụ `100 / 3`), `BigDecimal.divide(divisor)` ném `ArithmeticException: Non-terminating decimal expansion`.
4. **Thiếu edge case input:** không chặn `divisor <= 0` hoặc `amount == null`.

**File 2 — `PaymentClientBuggy.java`:**
5. **`SimpleDateFormat` dùng `static final` giữa nhiều thread:** `SimpleDateFormat` chứa biến `calendar` nội bộ bị mutate khi `format()`/`parse()`, dẫn đến trả ngày sai hoặc ném `NumberFormatException` / `ArrayIndexOutOfBoundsException` ngẫu nhiên khi đa luồng.
6. **Retry cả lỗi client 4xx không thể hồi phục (ví dụ `400 Bad Request`):** lãng phí tài nguyên, gây retry storm lên payment gateway.
7. **Nuốt `InterruptedException` không khôi phục interrupt flag:** trong khối `catch (InterruptedException e)` không gọi `Thread.currentThread().interrupt()`, làm thread pool không thể cancel/shutdown đúng cách.

**Code lời giải đầy đủ:** xem [lab/solutions/InvoiceJdbcHelperFixed.java](./lab/solutions/InvoiceJdbcHelperFixed.java) và [lab/solutions/PaymentClientFixed.java](./lab/solutions/PaymentClientFixed.java).
</details>

---

### E8
<details><summary>Xem đáp án</summary>

**Hướng giải:**
Kết quả phân loại 5 API trên JDK:
| API | Nhóm phân loại | Giải thích |
| :--- | :--- | :--- |
| `java.lang.String#isBlank()` | **Version Drift** (`@since 11`) | Có thật ở JDK 11+, nhưng fail trên Java 7 của JBoss |
| `java.util.List#of()` | **Version Drift** (`@since 9`) | Có thật ở JDK 9+, không có ở Java 7 (`Arrays.asList` mới có) |
| `java.util.Optional#orElseThrow()` (0 args) | **Version Drift** (`@since 10`) | `Optional` mới có từ JDK 8, còn overload 0 tham số từ JDK 10 |
| `java.net.http.HttpClient.Builder#retryPolicy(...)` | **Hallucinated API** | Bịa hoàn toàn — không tồn tại trên mọi phiên bản JDK |
| `java.lang.Thread#ofVirtual()` | **Version Drift** (`@since 21`) | Có từ JDK 21 (Project Loom), hoàn toàn không có trên JDK 7/11/17 |

**Cổng CI chặn tự động:**
Trong `pom.xml` của dự án legacy, khai báo rõ:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <source>1.7</source>
        <target>1.7</target>
    </configuration>
</plugin>
```
(Hoặc dùng `maven-compiler-plugin` với `<release>8</release>` / JDK 7 bootclasspath + plugin `animal-sniffer-maven-plugin` trên CI khi build bằng JDK mới hơn để bắt 100% API của phiên bản cao hơn).
</details>

---

### E9
<details><summary>Xem đáp án</summary>

<details open><summary>Báo cáo Review PR cho DailySettlementJob (Java 7 / JBoss AS 7.1.1)</summary>

**1. Ít nhất 7 lỗi nghiêm trọng tìm thấy:**
1. **[API/Version Drift — Blocker]:** Gọi `avg.toString().isBlank()` — method `String.isBlank()` **chỉ có từ Java 11**, không tồn tại trên **Java 7** của JBoss AS 7.1.1! *(Đây cũng là lý do máy dev cài JDK 17/21 và quên `<release>7</release>` trong IDE vẫn compile được, nhưng lên JBoss Java 7 sẽ nổ `NoSuchMethodError` hoặc không compile được trên server CI Java 7.)*
2. **[Security — Blocker]:** SQL Injection ở câu query nối trực tiếp `merchantId` và `dateStr` vào `Statement` thay vì `PreparedStatement` với `?`.
3. **[Resource Leak — Critical]:** `Connection`, `Statement`, `ResultSet` đóng thủ công ở cuối hàm không nằm trong `try-with-resources` (hỗ trợ sẵn từ Java 7). Bất kỳ `SQLException` hoặc `ArithmeticException` nào xảy ra đều làm rò rỉ JDBC connection khỏi pool của JBoss!
4. **[Concurrency & Memory Leak — Critical]:** `CACHE` là `HashMap` không đồng bộ (trong Java 7, ghi đồng thời vào `HashMap` có thể tạo **vòng lặp vô hạn trong bucket linked-list gây CPU 100%** khi resize!), đồng thời không có giới hạn kích thước / TTL nên tăng vô hạn tới OOM Heap.
5. **[Concurrency — Major]:** `SimpleDateFormat` khai báo `static final` (nếu sử dụng để parse/format `dateStr` giữa nhiều thread JBoss sẽ gây hỏng dữ liệu ngày hoặc crash ngẫu nhiên).
6. **[Edge Case — Critical]:** Nếu merchant chưa có giao dịch trong ngày (`count == 0`), `sum.divide(BigDecimal.valueOf(0))` nổ **`ArithmeticException: / by zero`**; nếu chia không hết (ví dụ `100 / 3`), `divide` không truyền `RoundingMode` nổ **`ArithmeticException: Non-terminating decimal expansion`**.
7. **[Edge Case / Null Safety — Major]:** Cột `amount` trong DB có giá trị `NULL` → `rs.getBigDecimal("amount")` trả `null` → `sum.add(null)` ném `NullPointerException` (và cũng làm tăng biến `count++` sai nếu đặt sau).
8. **[Logic/Input Validation]:** Không validate `merchantId`, `dateStr` trước khi query (dù `avg.toString()` không bao giờ blank nên lệnh `if (!avg.toString().isBlank())` thừa thãi vô nghĩa — dấu hiệu code AI sinh cho có).

**2. Bản sửa chuẩn Java 7 / JBoss AS 7.1.1 / JDBC:**
```java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.sql.DataSource;

public class DailySettlementJob {
    private static final int MAX_CACHE_ENTRIES = 1000;
    private static final String SQL =
        "SELECT amount FROM settlements WHERE merchant_id = ? AND tx_date = ?";

    // Bounded LRU cache thread-safe tương thích Java 7 (hoặc dùng Guava/JBoss Infinispan)
    private final Map<String, BigDecimal> cache = Collections.synchronizedMap(
        new LinkedHashMap<String, BigDecimal>(128, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, BigDecimal> eldest) {
                return size() > MAX_CACHE_ENTRIES;
            }
        }
    );

    public BigDecimal calculateAverageTicket(DataSource ds, String merchantId, String dateStr)
            throws SQLException {
        if (merchantId == null || merchantId.trim().isEmpty()
                || dateStr == null || dateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("merchantId and dateStr must not be blank");
        }
        String key = merchantId + ":" + dateStr;
        BigDecimal cached = cache.get(key);
        if (cached != null) {
            return cached;
        }

        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;

        // Java 7 try-with-resources đảm bảo đóng ResultSet -> PreparedStatement -> Connection
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1, merchantId);
            ps.setString(2, dateStr);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal amount = rs.getBigDecimal("amount");
                    if (amount != null) {
                        sum = sum.add(amount);
                        count++;
                    }
                }
            }
        }

        BigDecimal avg = (count == 0)
                ? BigDecimal.ZERO
                : sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
        cache.put(key, avg);
        return avg;
    }
}
```
*(Lưu ý thêm trong thực tế: cân nhắc tính thẳng `COALESCE(AVG(amount), 0)` trong SQL để tránh kéo hàng trăm nghìn dòng qua mạng lên JVM!)*

</details>
</details>

---

## Self-check (từ README §9) — đáp án gợi ý
<details><summary>Xem</summary>

1. **Tại sao LLM bịa API:** LLM dự đoán từng token theo xác suất thống kê từ dữ liệu trộn nhiều phiên bản và không tự chạy compiler khi trả lời; khi gặp khoảng trống ngữ cảnh, nó ghép tên method nghe hợp lý theo quy luật đặt tên phổ biến thay vì từ chối trả lời.
2. **4 bước phát hiện API bịa:** (1) Biên dịch bằng đúng `--release`/JDK target của server; (2) Kiểm tra coordinate trong `pom.xml`/kho Maven chính thức (chống slopsquatting); (3) `Ctrl+B` mở source/Javadoc thật kiểm tra chữ ký & hành vi; (4) Khóa phiên bản rõ trong prompt nhưng luôn ưu tiên bước 1–3.
3. **Quy trình "Viết trước, hỏi sau":** Tự đọc và viết bản nháp thiết kế/giả thuyết 10–15' trước → mới prompt AI phản biện lỗ hổng → tự kiểm chứng bằng compiler + test thật → explain-back 5' không nhìn AI. Giúp não giữ quyền kiểm soát mô hình tư duy, không bị "ảo giác hiểu" khi đọc code trôi chảy của AI.
4. **Checklist 7 điểm:** (1) API & Runtime match; (2) Edge case & Data invariants; (3) Concurrency & State; (4) Resource & Transaction lifecycle; (5) Security & Validation; (6) Test Integrity (test có khả năng fail không?); (7) Explain-back Gate.
5. **3 lỗi đặc thù Java 7 / JBoss AS 7.1.1:** (1) Sinh API Java 8/11/21 (`Stream`, `Optional`, `java.time`, `isBlank`) không biên dịch được trên JDK 7; (2) Dùng `static SimpleDateFormat` gây race condition đa luồng trên worker thread JBoss; (3) Quên đóng JDBC `Connection`/`Statement`/`ResultSet` an toàn làm cạn connection pool, hoặc dùng `HashMap` không đồng bộ/không giới hạn gây CPU 100% / OOM.
</details>
