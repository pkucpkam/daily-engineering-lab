# JVM-01 · Đáp án — Java 7 → 21: Bridge Cheatsheet

> ⚠️ **Chỉ mở sau khi đã tự làm.** Đọc đáp án mà chưa tự làm = mất 80% giá trị bài tập.
> Mỗi đáp án được bọc trong `<details>` để tránh lộ khi lướt.

---

### E1
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. `r1` in ra tên class ẩn được compiler sinh ra, ví dụ `AuditService$1`. `r2` in ra `AuditService` (tên class bao ngoài).
2. **Không biên dịch được** nếu `r2` đọc `retryCount`, vì ở cuối hàm có `retryCount++` khiến `retryCount` **không còn effectively final**.

**Giải thích:**
- Trong anonymous inner class (`r1`), JVM tạo một class thật sự riêng biệt nên `this` là instance của `AuditService$1`. Trong lambda (`r2`), không có class ngữ nghĩa mới ở mức ngôn ngữ — thân lambda nằm trong ngữ cảnh lexical của `AuditService`, nên `this` chính là instance của `AuditService`.
- Biến cục bộ nằm trên stack frame của thread gọi `buildTask()`. Lambda có thể được trả về và chạy sau khi `buildTask()` đã return (stack frame bị hủy). Do đó lambda chỉ có thể **copy giá trị** của biến cục bộ, không chia sẻ ô nhớ stack. Nếu Java cho phép sửa `retryCount++`, hai bản sao sẽ lệch nhau (hoặc gây race condition ngầm), nên compiler cấm.
- **Đáp án sai phổ biến:** *"Lambda chỉ là viết tắt của anonymous inner class"* → Sai, cả về `this` binding lẫn bytecode (`invokedynamic` vs file `.class` mới).
</details>

---

### E2
<details><summary>Xem đáp án</summary>

**Đáp án:**
Output trước khi ném lỗi:
```text
created
filter: an
filter: binh
map: binh
first=BINH
```
Sau đó dòng `long count = stream.count();` ném **`IllegalStateException: stream has already been operated upon or closed`**.

**Giải thích:**
1. `"created"` in ra **đầu tiên** vì `filter` và `map` là intermediate operations (lazy) — chúng chỉ nối thêm stage vào `AbstractPipeline`, chưa duyệt phần tử nào.
2. Khi `findFirst()` (terminal operation) chạy, các phần tử đi qua chuỗi `Sink` theo từng phần tử (loop fusion):
   - `"an"`: qua `filter` (false) → dừng nhánh này, **không vào `map`**.
   - `"binh"`: qua `filter` (true) → đi tiếp qua `map` (`"BINH"`) → `findFirst()` nhận đủ 1 phần tử và kích hoạt short-circuit, nên `"chi"` **không bao giờ được duyệt**.
3. `stream` đã bị terminal operation `findFirst()` tiêu thụ (linked stage đánh dấu `linkedOrConsumed = true`), nên gọi `stream.count()` lần nữa sẽ ném `IllegalStateException`.
- **Đáp án sai phổ biến:** Nghĩ `filter` chạy hết cả 3 phần tử rồi `map` mới chạy hết các phần tử còn lại → Sai, Stream xử lý theo chiều dọc (fused per element) trừ khi gặp stateful op như `sorted()`.
</details>

---

### E3
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. Với 1.000 req/s và 98% cache hit, hệ thống vẫn bắn **đủ 1.000 query/s** xuống DB (thay vì 20 query/s!). Sửa bằng:
   `UserProfile profile = cacheHit.orElseGet(() -> userRepository.loadFromDb(userId));`
2. **3 chỗ sai trong `CustomerDto`:**
   - Dùng `Optional<String>` làm **field** của class (tốn thêm object wrapper và pointer indirection).
   - Class implement `Serializable` nhưng `Optional` **không** implement `Serializable` → sẽ nổ `NotSerializableException` nếu serialize DTO (RMI, HTTP session, cache phân tán).
   - Dùng `Optional<String>` làm **tham số của setter** (`setMiddleName`) — bắt caller gói `Optional.ofNullable(...)` và vẫn có nguy cơ caller truyền `null` cho chính biến `middleName`.

**Giải thích:**
- `orElse(T other)` là một method Java bình thường nên mọi đối số truyền vào đều được evaluate *eagerly* trước khi bước vào thân `orElse`. `orElseGet(Supplier<? extends T>)` chỉ gọi `.get()` của `Supplier` khi giá trị bên trong `Optional` là `null`.
- **Đáp án sai phổ biến:** Cho rằng `Optional` thay thế hoàn toàn `null` ở mọi nơi (kể cả field DTO và parameter) → Sai với hướng dẫn của JSR-335 và *Effective Java* Item 55.
</details>

---

### E4
<details><summary>Xem đáp án</summary>

**Đáp án:**
- `var a = "hello";` → Biên dịch OK (`String`).
- `var b = null;` → **KHÔNG biên dịch** (`cannot infer type for local variable b: variable initializer is 'null'`).
- `var c = new ArrayList<>();` → **Biên dịch được nhưng là bẫy!** Compiler suy luận `c` có kiểu `ArrayList<Object>`, nên `c.add(42)` và `c.add("text")` cùng hợp lệ, mất hoàn toàn kiểm tra kiểu generic. Phải viết `var c = new ArrayList<Integer>();`.
- `var d = Map.of("VN", 7, "JP", 9);` → Biên dịch OK (`Map<String, Integer>`).

**Giải thích:**
`var` thực hiện suy luận kiểu tĩnh tại **compile time** dựa trên biểu thức vế phải. `null` không có kiểu cụ thể nên compiler từ chối; còn `new ArrayList<>()` không có đối số kiểu ở cả hai vế nên diamond operator rơi về cận trên là `Object`.
- **Đáp án sai phổ biến:** Nghĩ `var b = null;` suy ra `Object` hoặc `var` làm Java thành dynamic-typed như JS → Sai; `var` vẫn là static typing 100%.
</details>

---

### E5
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. Nếu container chạy múi giờ `UTC`, lúc `2026-11-27T01:30:00+07:00` đồng hồ `UTC` đang là **`2026-11-26T18:30:00`**. Gọi `LocalDateTime.now()` sẽ lưu `2026-11-26 18:30:00` (không kèm offset) → mất thông tin ngày giao dịch thật ở VN và làm lệch ngày chốt sổ từ `27` thành `26`!
2. Nên lưu thời điểm phát sinh bằng **`Instant`** (tương ứng `TIMESTAMP WITH TIME ZONE` / `TIMESTAMPTZ` trong DB). Khi cần tính ngày nghiệp vụ tại Việt Nam, chuyển tường minh:
   ```java
   LocalDate vnBusinessDate = eventInstant.atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDate();
   ```

**Giải thích:**
`LocalDateTime` chỉ là cặp (ngày, giờ) trên tờ lịch, không gắn với mốc vật lý toàn cầu nếu thiếu `ZoneId`/`ZoneOffset`. Kết quả của `LocalDateTime.now()` phụ thuộc vào timezone hệ điều hành/JVM (`-Duser.timezone`), rất nguy hiểm khi dev chạy Windows `+07:00` còn container production chạy `UTC`.
</details>

---

### E6
<details><summary>Xem đáp án</summary>

**Hướng giải:**
1. Chuẩn hóa đầu vào `orders` bằng `Optional.ofNullable(orders).orElseGet(List::of)` để an toàn trước list `null`.
2. Dùng `LocalDate.parse(order.getCreatedDateIso(), DateTimeFormatter.ISO_LOCAL_DATE)` và `refDate.minusDays(7)` để lọc cửa sổ ngày.
3. Dùng `Collectors.groupingBy(Order::getStatus, Collectors.counting())` để đếm theo status.
4. Tìm đơn lớn nhất (ưu tiên `amount` lớn nhất, hòa thì `id` nhỏ hơn) bằng `min(Comparator.comparing(Order::getAmount).reversed().thenComparing(Order::getId))` — trả trực tiếp `Optional<Order>`.
5. Dùng `switch` expression cho `tier` (tránh `NullPointerException` khi `tier` là `null`) và Text Block `.formatted(...)` để xuất JSON.

**Code tham khảo đầy đủ:** xem [lab/solutions/E6ShippingSummary.java](./lab/solutions/E6ShippingSummary.java).

**Đáp án sai phổ biến:**
- Dùng `switch (order.getCustomerTier())` trực tiếp không kiểm tra `null` → ném `NullPointerException` nếu `tier == null` (trước Java 21 `case null ->` không tồn tại, và ngay cả trên Java 21 cũng phải viết rõ `case null, default ->`).
- Quên `reversed()` hoặc đảo nhầm chiều `thenComparing` khi lấy tie-breaker `id` nhỏ nhất.
</details>

---

### E7
<details><summary>Xem đáp án</summary>

**Hướng giải & số đo tham khảo** *(trên Windows 11, JDK 26 `--release 21`, 16 logical CPUs, `ForkJoinPool.commonPool` parallelism = 15)*:

| Kịch bản | `for-loop` | `stream()` | `parallelStream()` |
| :--- | ---: | ---: | ---: |
| List nhỏ (`n = 100`, 100k lần) | 6 ms | 10 ms | 1.026 ms |
| List lớn CPU (`n = 1.000.000`) | 8 ms | 10 ms | 2 ms |
| Blocking I/O (`24 × 20ms`) | — | 496 ms | 59 ms (chiếm 15 thread pool chung) |

**Thí nghiệm `CompletableFuture.supplyAsync` bị kéo chậm:**
- Xem code tái hiện tại [lab/solutions/E7CommonPoolStarvationDemo.java](./lab/solutions/E7CommonPoolStarvationDemo.java).
- Khi `parallelStream()` chạy `parallelism * 2` (= 30 trên máy 16 CPU) tác vụ `Thread.sleep(500)`, một `CompletableFuture.supplyAsync(() -> "OK")` được bắn ngay sau đó phải chờ đến khi có worker thread trong `commonPool()` được giải phóng (đo thực tế trên máy soạn: **454 ms** sau bước chờ 50ms ban đầu, thay vì `< 1 ms`), mặc dù công việc của `CompletableFuture` không tốn chút CPU nào!

**Giải thích:**
- Với `n = 100`, `parallelStream()` chậm hơn ~171× vì chi phí chia `Spliterator`, đẩy `ForkJoinTask` vào queue của pool, đánh thức worker threads và gom kết quả lớn hơn rất nhiều so với vài phép cộng trên CPU.
- Máy bạn có số core khác nhau nên giá trị tuyệt đối (ms) sẽ khác, nhưng **xu hướng** (small list `parallelStream` tệ nhất, blocking I/O làm nghẽn `CompletableFuture` chung) là giống nhau.
</details>

---

### E8
<details><summary>Xem đáp án</summary>

**Hướng giải:**
1. Tạo 1 instance `HttpClient` dùng chung trong class với `.connectTimeout(Duration.ofMillis(1500))`.
2. Xây dựng helper `fetchAsync(String url)` tạo `HttpRequest` với `.timeout(Duration.ofMillis(1500))`, gọi `client.sendAsync(..., BodyHandlers.ofString())`, và check `statusCode() == 200` trong `.thenApply(...)`.
3. Dùng `taxFuture.thenCombine(fxFuture, BigDecimal::multiply)` để chạy 2 request song song và nhận kết quả gộp trong ~200ms thay vì ~400ms.
4. Implement `AutoCloseable` gọi `client.close()` (API mới của Java 21) để giải phóng tài nguyên khi xong.

**Code tham khảo đầy đủ:** xem [lab/solutions/E8AsyncRateClient.java](./lab/solutions/E8AsyncRateClient.java).

**Lỗi hay gặp:**
- Gọi `.join()` ngay sau `sendAsync` thứ nhất rồi mới gọi `sendAsync` thứ hai → thực chất chạy **tuần tự**, tổng thời gian thành 400ms+!
- Quên `request.timeout(...)`, chỉ set `connectTimeout(...)` → nếu kết nối TCP thành công nhưng server treo không trả body, request bị treo vĩnh viễn.
</details>

---

### E9
<details><summary>Xem đáp án</summary>

<details open><summary>Phân tích sự cố & hướng giải</summary>

1. **Các giả thuyết hợp lý & cách kiểm chứng:**
   - **Giả thuyết 1 (Date format race condition):** `SimpleDateFormat` khai báo `static final` và bị nhiều thread trong `parallelStream` gọi đồng thời → corrupt `Calendar` nội bộ, sinh `NumberFormatException` hoặc sai ngày. *Kiểm chứng:* kiểm tra stack trace trỏ vào `DigitList` / `SimpleDateFormat.subFormat`, hoặc chạy unit test 8 thread gọi `FORMAT.format/parse` song song.
   - **Giả thuyết 2 (`commonPool` starvation do blocking I/O):** `parallelStream()` thực hiện blocking HTTP ~350ms trên `ForkJoinPool.commonPool()` (chỉ có `4 - 1 = 3` thread trên container 4 vCPU). Các `CompletableFuture.supplyAsync` của `/api/checkout` dùng chung `commonPool` bị xếp hàng chờ. *Kiểm chứng:* lấy thread dump (`jcmd <pid> Thread.print`) thấy các thread `ForkJoinPool.commonPool-worker-*` đều đang `RUNNABLE`/`TIMED_WAITING` ở socket read của webhook, còn CPU process rất thấp (12%).
   - **Giả thuyết 3 (Timezone mismatch):** `23:55 UTC` tương ứng `06:55 +07:00` ngày hôm sau; nếu nơi khác sinh `rawDate` từ `LocalDate.now()` trên container UTC sẽ lệch 1 ngày so với hệ thống giờ VN. *Kiểm chứng:* đối chiếu `TZ` / `-Duser.timezone` của container với log timestamp.
   - *(Giả thuyết phụ để loại trừ)* DB lock / GC pause dài → có thể kiểm tra GC log và connection pool metrics, nhưng CPU 12% + triệu chứng trùng khớp chính xác với lock/thread starvation ở app pool.

2. **Kết luận Root Cause:**
   - **Lỗi sai ngày / exception ngẫu nhiên:** Dùng chung `SimpleDateFormat` (mutable, non-thread-safe) qua các thread của `parallelStream()`.
   - **p99 latency 4.200ms khi CPU 12%:** Container 4 vCPU → `commonPool` có **3 worker thread**. Một batch 36 `txIds` × 350ms blocking I/O chạy trên 3 thread mất `36 / 3 × 350ms = 4.200ms`, chiếm trọn `commonPool` khiến mọi `CompletableFuture.supplyAsync` của `/api/checkout` bị đói thread (CPU thấp vì thread đang ngủ chờ I/O).

3. **Fix:**
   - **Trên Java 21:** (a) Thay `SimpleDateFormat` bằng `DateTimeFormatter.ISO_LOCAL_DATE` + `LocalDate` / `Instant.atZone(ZoneId.of("Asia/Ho_Chi_Minh"))`; (b) Thay `parallelStream()` + blocking HTTP bằng `ExecutorService` riêng (hoặc `Executors.newVirtualThreadPerTaskExecutor()` trong Java 21) + `HttpClient` tái sử dụng có `connectTimeout` và `request.timeout`. Code mẫu đã sửa nằm ở [lab/solutions/E9FixedSettlementJob.java](./lab/solutions/E9FixedSettlementJob.java).
   - **Vá tạm trên Java 7 / JBoss AS 7.1.1:** (a) Đổi `static final SimpleDateFormat` thành `ThreadLocal<SimpleDateFormat>` (nhớ `.remove()` nếu dùng theo request) hoặc `FastDateFormat`; (b) Tách batch webhook ra một `ExecutorService` giới hạn số thread riêng (hoặc JMS queue bất đồng bộ), tuyệt đối không chạy trên worker thread phục vụ request online; (c) Set `setConnectTimeout` và `setReadTimeout` cho `HttpURLConnection`.

4. **Guardrail phòng ngừa:**
   - Bật rule static analysis (SpotBugs / ErrorProne / SonarQube) cấm `static SimpleDateFormat`, cấm `Optional.get()` không check, cảnh báo `parallelStream()` trong service layer.
   - Quy ước code review: mọi `CompletableFuture.supplyAsync` hoặc tác vụ I/O phải truyền **Executor đặt tên rõ ràng**, không dùng `commonPool()`.
   - Thêm metric theo dõi queue depth / active threads của các custom executor (và alert khi latency webhook tăng).

</details>
</details>

---

## Self-check (từ README §9) — đáp án gợi ý
<details><summary>Xem</summary>

1. **Lambda vs Anonymous Inner Class:** Anonymous class sinh file `.class` mới (tốn load/verify/Metaspace), cấp phát object mỗi lần `new`, và `this` trỏ vào instance ẩn. Lambda biên dịch thân hàm thành method tĩnh/instance trong class gốc, gọi tại call site qua `invokedynamic` + `LambdaMetafactory`; non-capturing lambda thường dùng 1 singleton instance, biến cục bộ bắt buộc *effectively final* (vì copy giá trị từ stack), và `this` trỏ vào class bao ngoài.
2. **Stream `Pipeline`/`Sink` & hiệu năng:** `Stream` là danh sách liên kết các stage (`Head`, `StatelessOp`, `StatefulOp`) dựng chuỗi `Sink` để *loop fusion* (duyệt 1 vòng cho `filter/map/sum`) và hỗ trợ short-circuit. Với list nhỏ hoặc tight loop primitive, `for-loop` thường nhanh hơn vì không tốn cấp phát stage/`Sink` hay boxing. `parallelStream()` gây hại khi tập nhỏ (overhead chia/gom lớn hơn việc làm), cấu trúc khó tách (`LinkedList`), hoặc chứa blocking I/O làm nghẽn `ForkJoinPool.commonPool()` dùng chung toàn JVM.
3. **4 chỗ không nên dùng `Optional` & `orElse` vs `orElseGet`:** Không dùng làm (1) field class/DTO (tốn memory + không `Serializable`), (2) method/constructor parameter, (3) phần tử trong Collection/Map, (4) bọc rồi gọi `.isPresent() + .get()`. `orElse(x)` luôn evaluate `x` bất kể `Optional` có giá trị hay không; `orElseGet(() -> x)` chỉ chạy `Supplier` khi `Optional` rỗng.
4. **`SimpleDateFormat` vs `DateTimeFormatter`, `Instant` vs `LocalDateTime`:** `SimpleDateFormat` giữ field `Calendar` mutable nên race condition khi đa luồng; `DateTimeFormatter` bất biến nên thread-safe 100%. `Instant` là mốc thời gian tuyệt đối trên trục UTC; `LocalDateTime` chỉ là ngày-giờ không gắn múi giờ (phụ thuộc timezone nơi chạy).
5. **`HttpClient` trong production Java 21:** Tái sử dụng 1 instance `HttpClient` (hoặc quản lý vòng đời qua `AutoCloseable` khi shutdown app) để giữ connection pool & selector thread; luôn cấu hình đủ 2 lớp timeout (`HttpClient.Builder.connectTimeout` và `HttpRequest.Builder.timeout`); luôn kiểm tra `statusCode()` và đọc/xử lý lỗi HTTP rõ ràng.
</details>
