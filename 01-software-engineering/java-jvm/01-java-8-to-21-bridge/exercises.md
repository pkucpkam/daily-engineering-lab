# JVM-01 · Bài tập — Java 7 → 21: Bridge Cheatsheet

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

### E1. `this` và biến cục bộ trong Lambda vs Anonymous Inner Class
Cho đoạn code sau trong class `AuditService`:
```java
public Runnable buildTask(String requestId) {
    int retryCount = 0;
    Runnable r1 = new Runnable() {
        @Override
        public void run() {
            System.out.println(this.getClass().getName());
        }
    };
    Runnable r2 = () -> {
        System.out.println(this.getClass().getName() + ":" + requestId);
    };
    retryCount++;
    return r2;
}
```
1. `this.getClass().getName()` trong `r1` và `r2` in ra gì khác nhau?
2. Nếu dòng `System.out.println` trong `r2` đọc thêm biến `retryCount`, code có biên dịch được không? Vì sao JVM bắt buộc quy tắc đó?

---

### E2. Stream lazy evaluation & tái sử dụng
Đoạn code dưới đây in ra gì hoặc ném exception gì? Giải thích từng bước thực thi bên trong `Stream`:
```java
List<String> names = List.of("an", "binh", "chi");
Stream<String> stream = names.stream()
        .filter(s -> {
            System.out.println("filter: " + s);
            return s.length() >= 3;
        })
        .map(s -> {
            System.out.println("map: " + s);
            return s.toUpperCase();
        });

System.out.println("created");
String first = stream.findFirst().orElse("NONE");
System.out.println("first=" + first);
long count = stream.count();
```

---

### E3. `Optional.orElse` vs `Optional.orElseGet` và ranh giới dùng `Optional`
1. Trong một API có tỷ lệ cache hit 98%, dev viết:
   `UserProfile profile = cacheHit.orElse(userRepository.loadFromDb(userId));`
   Điều gì xảy ra với số lượng query xuống DB khi có 1.000 req/s? Sửa như thế nào?
2. Chỉ ra **3 chỗ sai** trong thiết kế class dưới đây:
   ```java
   public class CustomerDto implements Serializable {
       private Optional<String> middleName;
       public void setMiddleName(Optional<String> middleName) {
           this.middleName = middleName;
       }
   }
   ```

---

### E4. Suy luận kiểu với `var`
Trong các dòng sau, dòng nào **không biên dịch**, dòng nào **biên dịch được nhưng tiềm ẩn bẫy kiểu dữ liệu**?
```java
var a = "hello";
var b = null;
var c = new ArrayList<>();
c.add(42);
c.add("text");
var d = Map.of("VN", 7, "JP", 9);
```

---

### E5. `LocalDateTime` vs `Instant` xuyên múi giờ
Một service chạy trên container đặt múi giờ `UTC` nhận yêu cầu tạo đơn lúc `2026-11-27T01:30:00+07:00` (giờ Việt Nam).
1. Nếu backend gọi `LocalDateTime.now()` trên container rồi lưu vào cột `TIMESTAMP` (without time zone), giá trị ngày và giờ lưu xuống DB là bao nhiêu?
2. Nếu nghiệp vụ cần "chốt sổ theo ngày Việt Nam" (`Asia/Ho_Chi_Minh`), nên lưu kiểu nào trong Java và chuyển đổi thế nào cho đúng bất kể server đặt ở múi giờ nào?

---

## 🟡 L2 — Apply

### E6. Refactor module Java 7 sang Java 21
**Bối cảnh:** Trong một legacy module đang tính tổng phí vận chuyển theo vùng, code hiện tại dùng Java 7 (`SimpleDateFormat`, `ArrayList` tạm, anonymous `Comparator`, `null` return, `switch` cũ và nối chuỗi JSON).

**Yêu cầu:**
Viết lại logic tương đương sang Java 21 sử dụng `java.time`, `Stream`, `Optional`, `var`, `switch` expression và Text Block:
- Giữ nguyên đầu vào `List<Order>` từ [lab/](./lab/README.md) (`Order`, `OrderStatus`).
- Lọc các đơn khác `CANCELLED`, tạo trong khoảng `[refDate - 7 ngày, refDate]`.
- Gom nhóm đếm số lượng đơn theo `OrderStatus` (`Map<OrderStatus, Long>`).
- Lấy `Optional<Order>` có `amount` lớn nhất (nếu hòa lấy `id` nhỏ hơn theo alphabet).
- Ánh xạ nhóm `customerTier` (`"GOLD"` → `0%`, `"SILVER"` → `5%`, khác → `10%`) bằng `switch` expression để tính phí vận chuyển ước tính trên tổng tiền.

**Input / code khởi đầu:** Dùng class `Order` và `OrderStatus` trong `lab/src/main/java/lab/jvm/bridge/`.
**Tiêu chí hoàn thành:**
- [ ] Class biên dịch sạch với `--release 21`, không dùng `SimpleDateFormat` hay trả về `null`.
- [ ] Pass test kiểm chứng đối chiếu với bản Java 7 (đặt test vào `lab/src/test/java/...` hoặc so sánh với đáp án mẫu trong `lab/solutions/E6ShippingSummary.java`).
- [ ] Không dùng `Optional.get()` trần hoặc `var` với diamond `<>` không rõ kiểu.

<details><summary>💡 Hint</summary>

1. Đếm số lượng theo key dùng `Collectors.groupingBy(Order::getStatus, Collectors.counting())`.
2. Tìm max với tie-breaker dùng `Comparator.comparing(Order::getAmount).thenComparing(Order::getId, Comparator.reverseOrder())` + `.max(...)` (lưu ý `max` cần `id` nhỏ hơn đứng *lớn hơn* khi so sánh max, hoặc dùng `.min(Comparator.comparing(Order::getAmount).reversed().thenComparing(Order::getId))`).
3. Trong `switch (tier)` trên `String`, nhớ xử lý `null` hoặc chuẩn hóa bằng `Optional.ofNullable(tier).orElse("")` trước khi vào `switch`.
</details>

---

### E7. Đo và giải thích ngưỡng `for-loop` vs `Stream` vs `parallelStream`
**Bối cảnh:** Đội bạn tranh luận nên đổi toàn bộ vòng `for` trong service báo cáo sang `.parallelStream()` để "tận dụng hết CPU".

**Yêu cầu:**
1. Chạy `BridgeLabRunner` trong `lab/` và ghi lại bảng số đo cho 3 kịch bản (small list `n=100`, CPU-heavy `n=1M`, blocking I/O `24 × 20ms`).
2. Viết thêm 1 thí nghiệm nhỏ (hoặc sửa copy tạm) chạy **đồng thời** một `parallelStream()` đang `Thread.sleep(500)` với một `CompletableFuture.supplyAsync(() -> "OK")` (không truyền custom executor). Đo thời gian `CompletableFuture` phải chờ để hoàn thành.

**Input / code khởi đầu:** `lab/src/main/java/lab/jvm/bridge/BridgeLabRunner.java`.
**Tiêu chí hoàn thành:**
- [ ] Bảng số đo thực tế trên máy của bạn cho cả 3 kịch bản + thí nghiệm `CompletableFuture` bị ảnh hưởng.
- [ ] Giải thích được bằng con số tại sao list `n=100` dùng `parallelStream()` chậm hơn hàng chục/trăm lần, và tại sao `CompletableFuture.supplyAsync` bị kéo chậm theo.

<details><summary>💡 Hint</summary>

- `CompletableFuture.supplyAsync(supplier)` (bản 1 tham số) mặc định dùng chính `ForkJoinPool.commonPool()`.
- Nếu kích hoạt số task blocking nhiều hơn số worker (ví dụ `ForkJoinPool.commonPool().getParallelism() * 2` task `sleep(500)` — vì bản thân thread gọi `parallelStream` cũng tham gia chạy 1 phần), toàn bộ worker pool lẫn calling thread sẽ kẹt 500ms, nên `CompletableFuture` đơn giản chỉ trả về `"OK"` cũng bị chờ ~450–500ms!
</details>

---

### E8. Xây dựng Async HTTP Client tổng hợp dữ liệu (Java 21 `HttpClient`)
**Bối cảnh:** Một endpoint báo cáo cần gọi song song 2 dịch vụ nội bộ (`/tax-rate` và `/fx-rate`) rồi kết hợp kết quả. Nếu bất kỳ dịch vụ nào trả về khác `200` hoặc quá `1500ms` không phản hồi, phải fail rõ ràng thay vì treo thread.

**Yêu cầu:**
Viết class dùng `java.net.http.HttpClient`:
- Khởi tạo **1 `HttpClient` tái sử dụng** có `connectTimeout(Duration.ofMillis(1500))`.
- Dùng `sendAsync` gọi song song 2 URL với `request.timeout(Duration.ofMillis(1500))`.
- Gộp kết quả bằng `CompletableFuture`, kiểm tra `statusCode() == 200`, nhân `taxRate` và `fxRate` trả về `BigDecimal`.
- Dùng `AutoCloseable` (implements `AutoCloseable` và đóng `httpClient.close()` ở `close()`).

**Input / code khởi đầu:** Có thể khởi động `com.sun.net.httpserver.HttpServer` nội bộ như trong `BridgeComparisonTest.java`.
**Tiêu chí hoàn thành:**
- [ ] Hai request chạy song song: nếu mỗi endpoint giả lập trễ `200ms`, tổng thời gian chờ của client phải `< 350ms` (không phải `>= 400ms` như gọi tuần tự).
- [ ] Endpoint trả `503` hoặc trễ `2000ms` phải ném exception rõ ràng.

<details><summary>💡 Hint</summary>

- Dùng `client.sendAsync(req1, HttpResponse.BodyHandlers.ofString())` và `.thenCombine(...)` giữa 2 `CompletableFuture`.
- Nhớ bọc `httpClient` trong class implement `AutoCloseable` để người gọi dùng được `try (var agg = new ...)`, vừa tái sử dụng pool cho nhiều lần gọi, vừa đóng gọn khi tắt app.
</details>

---

## 🔴 L3 — Deep

### E9. Sự cố Production: Sai ngày ngẫu nhiên & p99 vọt lên 4,2 giây
**Tình huống:**
Bạn nhận ticket P1 từ hệ thống thanh toán (đã nâng cấp lên Java 21 chạy trên container 4 vCPU, `ForkJoinPool.commonPool` parallelism = 3):
> *Log lúc 23:55 UTC (06:55 giờ Việt Nam):*
> - Một số batch job báo `NumberFormatException: multiple points` khi parse ngày thanh toán, và tệ hơn có 12 giao dịch ngày `2026-11-27` bị ghi thành ngày khác khi đối chiếu.
> - Đồng thời từ 23:50, API `/api/checkout` (dùng `CompletableFuture.supplyAsync` gọi check tồn kho nhẹ ~15ms) bị vọt **p99 latency từ 25ms lên 4.200ms**, dù CPU container chỉ ở mức 12%!
>
> *Đoạn code nghi vấn trong `SettlementJob.java`:*
> ```java
> public class SettlementJob {
>     private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");
>
>     public List<String> settleBatch(List<String> txIds, String rawDate) {
>         Date date = parseUnsafe(rawDate);
>         return txIds.parallelStream()
>                 .map(id -> {
>                     String d = FORMAT.format(date); // format ngày để gửi webhook
>                     return callPartnerWebhookBlocking(id, d); // mỗi call mất ~350ms I/O
>                 })
>                 .toList();
>     }
> }
> ```

**Yêu cầu:**
1. Đưa ra **≥ 3 giả thuyết** cho 2 triệu chứng trên (sai/lỗi parse ngày và p99 spike 4.2s khi CPU chỉ 12%) và cách kiểm chứng từng giả thuyết.
2. Kết luận **root cause** chính xác cho cả hai triệu chứng.
3. Đề xuất **bản fix đầy đủ trên Java 21** (kèm xử lý lỗi ngày giờ theo múi giờ) **VÀ** phương án vá tạm nếu code này vẫn phải chạy trên **Java 7 / JBoss AS 7.1.1**.
4. Đề xuất **guardrail phòng ngừa** (code review checklist / static analysis / tách executor) để lỗi không tái diễn.

**Tiêu chí hoàn thành:**
- [ ] Giải thích đúng vì sao CPU chỉ 12% mà p99 lại lên 4.200ms (kèm tính toán số lượng thread `commonPool` = 3).
- [ ] Bản fix Java 21 loại bỏ hoàn toàn `SimpleDateFormat` và giải phóng `commonPool` khỏi blocking I/O.

<details><summary>💡 Hint</summary>

1. Kiểm tra `SimpleDateFormat` giữ field `Calendar` nội bộ thế nào khi `parallelStream` gồm 3 worker + calling thread cùng gọi `FORMAT.format(...)` / `FORMAT.parse(...)`.
2. Trên container 4 vCPU, `commonPool` chỉ có `4 - 1 = 3` thread. Nếu 1 batch `txIds` có 36 item, mỗi item blocking I/O `350ms`: `36 / 3 * 350ms = 4.200ms`! Trong suốt 4,2s đó CPU gần như rảnh (`sleep/wait` socket) nhưng `commonPool` kẹt cứng nên mọi `CompletableFuture.supplyAsync` của `/api/checkout` đều phải xếp hàng chờ!
3. Còn `rawDate` lúc `23:55 UTC` vs `06:55 +07:00`: kiểm tra xem job truyền ngày theo múi giờ máy container hay múi giờ nghiệp vụ VN.
</details>

---

## 🏆 Challenge (tùy chọn)
**Gắn với:** Project 1 — [Milestone 1.1](../../../04-projects/01-distributed-backend-system/MILESTONES.md)
**Mô tả:** Thiết kế package domain `order` bằng Java 21 thuần (chưa dùng Lombok) cho hệ thống đặt hàng: dùng `Instant` cho mọi timestamp sự kiện, `ZoneId.of("Asia/Ho_Chi_Minh")` cho rule cắt ngày khuyến mãi, `Optional` đúng chuẩn ở repository/lookup interface, `switch` expression cho chuyển trạng thái đơn hàng (`OrderStatus`), và viết bộ JUnit 5 test chứng minh không lệch ngày ở ranh giới `23:59:59+07:00` ↔ `16:59:59Z`.
**Definition of Done:** Bộ test pass với `mvn -q verify`, không có field nào kiểu `Optional` hay `LocalDateTime` cho timestamp giao dịch.
