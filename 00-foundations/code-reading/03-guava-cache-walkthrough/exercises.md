# READ-03 · Bài tập — Đọc source Guava Cache / Caffeine

> Làm theo thứ tự. **Tự làm trước, chỉ mở hint khi đã thử ≥ 15 phút.** Đáp án ở [solutions.md](./solutions.md) — chỉ mở sau khi đã có câu trả lời của riêng bạn.
> Ghi thời gian làm từng bài vào `my-notes.md` để theo dõi tiến bộ.

| Cấp | Mục đích | Số lượng | Thời gian gợi ý |
| :--- | :--- | :---: | :---: |
| 🟢 L1 — Recall | Nhớ & hiểu khái niệm | 5 | 20' |
| 🟡 L2 — Apply | Viết code / phân tích tình huống cụ thể | 3 | 75' |
| 🔴 L3 — Deep | Debug, thiết kế, đo đạc, tình huống mở | 1 | 90' |

---

## 🟢 L1 — Recall

### E1. `LocalCache$Segment` trong Guava vs `BoundedLocalCache` trong Caffeine
1. `LocalCache$Segment` trong Guava kế thừa class khóa nào, và khi đọc (`get()`) có khi nào nó phải giành lock đó không?
2. Caffeine thay thế cơ chế segment lock bằng những thành phần nào trong `BoundedLocalCache`?

---

### E2. Tại sao LRU thuần thua W-TinyLFU trên workload có cold scan?
Giả sử cache chứa được 200 phần tử đang giữ top 200 sản phẩm hot nhất. Một job quét qua 300 sản phẩm lạnh mới (mỗi cái đọc đúng 1 lần). Điều gì xảy ra với tập 200 sản phẩm hot trong Guava Cache (LRU) vs Caffeine (W-TinyLFU)?

---

### E3. `expireAfterWrite` vs `refreshAfterWrite`
1. Nếu có 8 request cùng đập vào 1 key vừa quá ngưỡng `100 ms` (loader mất `120 ms`), độ trễ mà 8 caller nhận được khác nhau thế nào giữa `expireAfterWrite(100ms)` và `refreshAfterWrite(100ms)` trong Caffeine?
2. Khi nào **KHÔNG** được phép dùng `refreshAfterWrite` thay cho `expireAfterWrite`?

---

### E4. Bẫy ngầm của `refreshAfterWrite` trong Guava Cache
Nếu bạn bê nguyên cấu hình `CacheBuilder.newBuilder().refreshAfterWrite(...).build(new CacheLoader<>() { public String load(String k) { ... } })` lên Guava mà **không override `reload(K, V)`**, điều gì xảy ra với caller đầu tiên chạm ngưỡng refresh?

---

### E5. Chống Cache Stampede cục bộ
Vì sao đoạn code `if (cache.getIfPresent(k) == null) { cache.put(k, db.load(k)); }` gây stampede dưới tải 50 thread đồng thời, còn `cache.get(k, key -> db.load(key))` thì không?

---

## 🟡 L2 — Apply

### E6. Chạy benchmark Zipf + Cold Scan & phân tích `stats()`
**Bối cảnh:** Dùng [lab/](./lab/README.md) để tự đo sự khác biệt giữa Guava Cache và Caffeine.

**Yêu cầu:**
1. Chạy `mvn -q verify` và `mvn -q compile exec:java` trong `lab/`.
2. Ghi lại bảng so sánh `hitRate`, `hits`, `misses`, `time` giữa Guava và Caffeine ở thí nghiệm `[1]`, và kiểm chứng độ trễ `expireAfterWrite` vs `refreshAfterWrite` ở thí nghiệm `[2]`.
3. Thử chỉnh `generateZipfPlusScanTrace` trong 1 lượt chạy tạm để **tắt hẳn cold scan** (chỉ giữ phân phối Zipf thuần) và quan sát khoảng cách hit-rate giữa Guava vs Caffeine co lại còn bao nhiêu — giải thích tại sao.

**Input / code khởi đầu:** `lab/src/main/java/lab/codereading/cache/CacheWalkthroughRunner.java`.
**Tiêu chí hoàn thành:**
- [ ] `mvn -q verify` pass 3/3 tests.
- [ ] Giải thích được bằng dữ liệu thực tế tác động của cold scan lên LRU vs W-TinyLFU.

<details><summary>💡 Hint</summary>

Khi không có cold scan (Zipf thuần, các key hot được truy cập lặp đi lặp lại rất dày), LRU thuần vẫn giữ được phần lớn hot key ở đầu hàng đợi nên khoảng cách với W-TinyLFU thu hẹp lại (thường chỉ chênh vài % thay vì ~47% như khi có cold scan!).
</details>

---

### E7. Viết test chứng minh Guava `reload()` mặc định đồng bộ vs async override
**Bối cảnh:** Đội bạn đang bảo trì hệ thống legacy dùng Guava Cache và tưởng `refreshAfterWrite` tự chạy bất đồng bộ giống Caffeine.

**Yêu cầu:**
Viết JUnit 5 test dùng `CacheBuilder`:
1. Trường hợp A: `CacheLoader` chỉ override `load(...)` (`100ms`), bật `refreshAfterWrite(50ms)` → sau `70ms`, caller gọi `cache.get("k")` bị **block >= 80ms** mới nhận về giá trị mới.
2. Trường hợp B: `CacheLoader` override thêm `reload(String key, String oldValue)` dùng `ListeningExecutorService` (`Futures.immediateFuture` hoặc `MoreExecutors.listeningDecorator(executor).submit(...)`) → caller gọi `cache.get("k")` trả về giá trị cũ trong **< 30ms** trong khi reload chạy ngầm.

**Input / code khởi đầu:** `lab/src/test/java/lab/codereading/cache/CacheWalkthroughTest.java`.
**Tiêu chí hoàn thành:**
- [ ] Test chạy pass 100% (xem lời giải mẫu tại `lab/solutions/E7GuavaAsyncReloadTest.java`).
- [ ] Nhớ shutdown executor sau khi chạy xong test.

<details><summary>💡 Hint</summary>

Dùng `MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor())` của Guava và `executor.submit(() -> load(key))` để trả về `ListenableFuture<String>` trong `reload(...)`.
</details>

---

### E8. Soi cấu trúc `BoundedLocalCache` & `LocalCache$Segment` bằng `javap`
**Bối cảnh:** Kiểm chứng bằng công cụ đọc bytecode thay vì tin trí nhớ.

**Yêu cầu:**
Dùng `javap -p` trên 2 file jar trong `~/.m2/repository`:
1. Xác nhận `LocalCache$Segment` trong `guava-33.3.1-jre.jar` kế thừa `ReentrantLock` và chứa `recencyQueue`, `writeQueue`, `accessQueue`.
2. Xác nhận `BoundedLocalCache` trong `caffeine-3.1.8.jar` chứa 3 hàng đợi W-TinyLFU (`accessOrderWindowDeque`, `accessOrderProbationDeque`, `accessOrderProtectedDeque`), `readBuffer`, `writeBuffer`, `evictionLock`, và `ConcurrentHashMap data`.

**Tiêu chí hoàn thành:**
- [ ] Liệt kê được đúng các tên field/method đã thấy từ `javap`.

<details><summary>💡 Hint</summary>

Trên PowerShell nhớ escape dấu `$` trong `LocalCache$Segment`:
`javap -p -cp <path-to-guava.jar> 'com.google.common.cache.LocalCache$Segment'`
</details>

---

## 🔴 L3 — Deep

### E9. Sự cố Production: Caffeine làm treo `CompletableFuture` và rớt hit-rate về 18%
**Tình huống:**
> Một service Spring Boot (Java 21, container 4 vCPU) dùng Caffeine làm cache tra cứu quyền & cấu hình khách hàng:
> ```java
> LoadingCache<String, Config> configCache = Caffeine.newBuilder()
>         .maximumSize(500)
>         .refreshAfterWrite(Duration.ofSeconds(30))
>         .build(key -> loadConfigFromSlowHttp(key)); // mất ~200ms khi upstream chậm
> ```
> Vào buổi trưa:
> 1. Có một script quét toàn bộ 20.000 `userId` (`key = "user:" + id`) chạy qua cache này → hit-rate từ 94% tụt xuống **18%**, dù đây là Caffeine (W-TinyLFU)!
> 2. Đồng thời, các API khác dùng `CompletableFuture.supplyAsync(...)` đột ngột bị chậm thêm 400–600ms dù không gọi `configCache`.

**Yêu cầu:**
1. Tại sao dùng **W-TinyLFU** mà hit-rate vẫn rớt xuống **18%** khi script quét 20.000 `userId` với `maximumSize = 500`? *(Gợi ý: xem cách hoạt động của `FrequencySketch` và kích thước tập dữ liệu vs dung lượng `500`.)*
2. Tại sao `configCache` dùng `refreshAfterWrite` lại làm chậm các `CompletableFuture.supplyAsync(...)` không liên quan trên container 4 vCPU?
3. Đề xuất bản sửa toàn diện cho cấu hình cache và luồng truy cập của script quét batch.

**Tiêu chí hoàn thành:**
- [ ] Giải thích chính xác giới hạn của `FrequencySketch` khi tần suất key nóng bị pha loãng hoặc khi `maximumSize = 500` quá nhỏ so với working set thực (`> 500` hot keys) + tần suất reset counter của TinyLFU (`sampleSize = 10 * maximumSize` = 5.000).
- [ ] Chỉ ra `Caffeine` mặc định chạy `refreshAfterWrite` trên `ForkJoinPool.commonPool()` (3 threads trên container 4 vCPU) và tách executor riêng + kết hợp `expireAfterWrite`.

<details><summary>💡 Hint</summary>

1. Nếu trong lúc script chạy, 20.000 lần truy cập đều là miss trên key lạ nhưng `FrequencySketch` chỉ lưu lịch sử `sampleSize = 10 * maximumSize = 5.000` (sau mỗi 5.000 lượt ghi/truy cập lại chia đôi toàn bộ bộ đếm!), đồng thời dung lượng `500` có thể cũng nhỏ hơn tập hot key thật hoặc chính các key mới được lặp > 1 lần, hit-rate tổng sẽ bị kéo tụt mạnh vì 20.000 miss cộng dồn trực tiếp vào mẫu số `misses`! Ngoài ra, nếu script quét từng user chỉ 1 lần, ta **không nên đi qua cache phục vụ online** (hoặc tách endpoint batch không ghi vào cache online).
2. `Caffeine.newBuilder()` không truyền `.executor(...)` thì `refreshAfterWrite` chạy `loadConfigFromSlowHttp` (blocking 200ms) trên chính `ForkJoinPool.commonPool()` (chỉ có `4 - 1 = 3` thread), làm nghẽn toàn bộ `CompletableFuture.supplyAsync` dùng chung pool đó!
</details>
