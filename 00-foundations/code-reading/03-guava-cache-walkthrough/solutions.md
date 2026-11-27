# READ-03 · Đáp án — Đọc source Guava Cache / Caffeine

> ⚠️ **Chỉ mở sau khi đã tự làm.** Đọc đáp án mà chưa tự làm = mất 80% giá trị bài tập.
> Mỗi đáp án được bọc trong `<details>` để tránh lộ khi lướt.

---

### E1
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. `LocalCache$Segment` kế thừa **`java.util.concurrent.locks.ReentrantLock`**. Khi đọc (`get`), nó **vẫn có thể phải giành lock** của segment trong trường hợp `readCount` chạm ngưỡng (cần `drainRecencyQueue()` để cập nhật thứ tự LRU `accessQueue`), hoặc khi có miss/expired cần `lockedGetOrLoad`.
2. Caffeine thay segment lock bằng:
   - **`ConcurrentHashMap<Object, Node<K, V>> data`** để tra cứu lock-free.
   - **`readBuffer`** (striped ring buffer) và **`writeBuffer`** (`MpscGrowableArrayQueue`) để ghi nhận thao tác đọc/ghi không cần giành lock.
   - **`evictionLock`** (`ReentrantLock`, dùng `tryLock()`) chỉ để một thread chạy lô bảo trì (`PerformCleanupTask`) tại một thời điểm.
</details>

---

### E2
<details><summary>Xem đáp án</summary>

**Đáp án:**
- **Guava Cache (LRU thuần):** 200 phần tử mới đầu tiên trong đợt quét sẽ lần lượt chèn vào đầu danh sách LRU và **đuổi sạch toàn bộ 200 sản phẩm hot cũ ra khỏi cache**. Sau đợt quét, các request sản phẩm hot phải miss và query lại DB toàn bộ.
- **Caffeine (W-TinyLFU):** 300 sản phẩm lạnh chỉ đi qua **Window** (~1% dung lượng = ~2 slot). Khi bị đẩy khỏi Window để xin vào **Main Probation**, `FrequencySketch` thấy tần suất của chúng (`1`) **nhỏ hơn** tần suất của các sản phẩm hot đang nằm trong Main (`> 1`), nên W-TinyLFU **từ chối cho vào Main**. Khoảng ~99% dung lượng cache vẫn giữ nguyên tập sản phẩm hot!
</details>

---

### E3
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. Với `expireAfterWrite(100ms)`: cả 8 thread đều coi entry đã hết hạn và **đều phải chờ ~120–135ms** để loader tải xong giá trị mới (đo thực tế trong lab: `min=133ms, max=134ms`).
   Với `refreshAfterWrite(100ms)` trên Caffeine: Caffeine kích hoạt reload ngầm qua executor và **trả về giá trị cũ ngay lập tức cho cả 8 thread** (`min=0ms, max=4ms` trong lab).
2. **Không được dùng `refreshAfterWrite` thay `expireAfterWrite`** cho dữ liệu đòi hỏi tính chính xác tức thời hoặc bảo mật (ví dụ: token xác thực đã bị thu hồi, quyền user vừa bị khóa, hoặc số dư tài khoản không được phép trả dữ liệu cũ quá hạn).
</details>

---

### E4
<details><summary>Xem đáp án</summary>

**Đáp án:**
Caller đầu tiên gọi `get(key)` sau ngưỡng `refreshAfterWrite` sẽ bị **block đồng bộ ngay trên thread gọi** để chạy `CacheLoader.reload(key, oldValue)` (vì mặc định `CacheLoader.reload` trong Guava gọi thẳng `Futures.immediateFuture(load(key))` đồng bộ!). Điều này khiến Guava không còn ưu điểm "trả stale ngay" trừ khi bạn tự override `reload()` trả về `ListenableFuture` chạy trên một `ListeningExecutorService` riêng.
</details>

---

### E5
<details><summary>Xem đáp án</summary>

**Đáp án:**
- Trong `if (cache.getIfPresent(k) == null) { cache.put(k, db.load(k)); }`, hàm `getIfPresent` không đặt bất kỳ khóa/placeholder nào cho key `k`. Cả 50 thread đến cùng lúc đều nhận về `null` và **tất cả 50 thread cùng gọi `db.load(k)`** (gấp 50× tải DB — trong lab 16 thread gây đúng 16 lần load).
- Ngược lại, `cache.get(k, fn)` / `LoadingCache.get(k)` sử dụng per-key synchronization (`LoadingValueReference` trong Guava `Segment` hoặc `ConcurrentHashMap.computeIfAbsent` trong Caffeine) để **chỉ 1 thread chạy `load(k)`**, 49 thread còn lại đợi kết quả chung đó (chỉ 1 lần load).
</details>

---

### E6
<details><summary>Xem đáp án</summary>

**Số đo tham khảo** *(Windows 11, JDK 26.0.1 `--release 21`, Guava `33.3.1-jre`, Caffeine `3.1.8`)*:
- `mvn -q verify`: **3/3 tests pass**.
- `[1]` Zipf + periodic cold scan (`maximumSize = 200`, 60.000 accesses):
  - Guava (Segmented LRU): **49,18% hit-rate** (`29.509` hits / `30.491` misses, `331 ms`).
  - Caffeine (W-TinyLFU): **96,24% hit-rate** (`57.745` hits / `2.255` misses, `220 ms`).
- `[2]` `expireAfterWrite(100ms)` vs `refreshAfterWrite(100ms)`:
  - `expireAfterWrite`: 8 callers waited `133–134 ms`, `loads=2`.
  - `refreshAfterWrite`: 8 callers returned stale in `0–4 ms`, sau 150ms đọc lại được `v-2`.
- Khi bỏ cold scan (chỉ giữ Zipf thuần), Guava LRU không còn bị 40 key lạnh chèn xóa hot key liên tục nên hit-rate của Guava tăng vọt lên tiệm cận Caffeine (chỉ còn chênh vài phần trăm).
</details>

---

### E7
<details><summary>Xem đáp án</summary>

**Hướng giải:**
- Trường hợp A: `CacheLoader` chỉ implement `load(...)` mất `100ms` → sau khi quá hạn `refreshAfterWrite(50ms)`, lời gọi `cache.get("k")` mất `>= 80ms` và trả về luôn `"v2"` vì Guava chạy `reload` đồng bộ.
- Trường hợp B: Override `reload(String key, String oldValue)` gọi `asyncExecutor.submit(() -> load(key))` → `cache.get("k")` trả về ngay `"v1"` trong `< 30ms`, và sau ~130ms đọc lại được `"v2"`.

**Code tham khảo đầy đủ:** xem [lab/solutions/E7GuavaAsyncReloadTest.java](./lab/solutions/E7GuavaAsyncReloadTest.java).
</details>

---

### E8
<details><summary>Xem đáp án</summary>

**Kết quả thực tế soi từ jar bằng `javap -p`:**
- Guava (`guava-33.3.1-jre.jar`): `class com.google.common.cache.LocalCache$Segment<K, V> extends java.util.concurrent.locks.ReentrantLock` với các field `table`, `recencyQueue`, `readCount`, `writeQueue`, `accessQueue` và các method `lockedGetOrLoad`, `waitForLoadingValue`, `drainRecencyQueue`.
- Caffeine (`caffeine-3.1.8.jar`): `abstract class com.github.benmanes.caffeine.cache.BoundedLocalCache<K, V>` với các field `data` (`ConcurrentHashMap`), `readBuffer` (`Buffer`), `writeBuffer` (`MpscGrowableArrayQueue`), `evictionLock` (`ReentrantLock`), `refreshes` (`ConcurrentMap<Object, CompletableFuture<?>>`), và các deque `accessOrderWindowDeque()`, `accessOrderProbationDeque()`, `accessOrderProtectedDeque()`.
</details>

---

### E9
<details><summary>Xem đáp án</summary>

<details open><summary>Phân tích nhân quả & phương án khắc phục</summary>

1. **Tại sao hit-rate vẫn rớt xuống 18% dù dùng W-TinyLFU?**
   - **Thứ nhất (tổng lượng miss cộng dồn & reset sketch):** 20.000 `userId` lạnh chỉ đọc 1 lần đã bơm trực tiếp 20.000 lượt **miss** vào phép tính `hitRate = hits / (hits + misses)`! Hơn nữa, với `maximumSize = 500`, `FrequencySketch` trong Caffeine có chu kỳ `sampleSize = 10 × maximumSize = 5.000`: cứ mỗi 5.000 lần ghi/truy cập, toàn bộ bộ đếm tần suất lại bị **chia đôi (halving)** để quên bớt quá khứ. Nếu trong vài giây script quét qua 20.000 key lạ (4 lần halving liên tiếp!), bộ đếm của các hot key bị chia nhỏ tới 16×, khiến `FrequencySketch` không còn giữ được lợi thế lịch sử trước các key có từ 2 lần truy cập ngẫu nhiên trở lên (hoặc nếu working set thực sự lớn hơn 500, cache bị thrashing).
   - **Bài học:** W-TinyLFU chống ô nhiễm *bộ nhớ cache* rất tốt, nhưng **batch scan không nên đi xuyên qua online cache** nếu ta biết trước dữ liệu đó là cold scan một lần!

2. **Tại sao `CompletableFuture.supplyAsync(...)` bị chậm theo 400–600ms?**
   - `configCache` không cấu hình `.executor(...)`, nên mặc định Caffeine chạy `refreshAfterWrite` (`loadConfigFromSlowHttp` — blocking I/O 200ms) trên **`ForkJoinPool.commonPool()`**.
   - Trên container 4 vCPU, `commonPool` chỉ có `4 - 1 = 3` worker threads. Khi hàng loạt key cùng kích hoạt refresh hoặc script quét kích load/refresh làm nghẽn cả 3 thread, mọi `CompletableFuture.supplyAsync` khác dùng chung `commonPool` đều bị xếp hàng chờ 400–600ms!

3. **Cách sửa toàn diện:**
   - **Tách Executor riêng cho Caffeine:** `.executor(Executors.newFixedThreadPool(16))` (hoặc `Executors.newVirtualThreadPerTaskExecutor()` trên Java 21) để `refreshAfterWrite` không bao giờ chạm vào `ForkJoinPool.commonPool()`.
   - **Kết hợp `expireAfterWrite`:** thêm `.expireAfterWrite(Duration.ofMinutes(10))` bên cạnh `.refreshAfterWrite(Duration.ofSeconds(30))`.
   - **Cách ly batch scan khỏi online cache:** đọc trực tiếp từ replica/API batch hoặc hàm `loadConfigBypassCache()` khi script quét 20.000 user, không làm loãng `FrequencySketch` và `stats()` của luồng phục vụ traffic thật; đồng thời đo lại working set thực để tăng `maximumSize` (ví dụ `5.000–10.000` nếu RAM cho phép).

</details>
</details>

---

## Self-check (từ README §9) — đáp án gợi ý
<details><summary>Xem</summary>

1. **3 lý do Caffeine thay thế Guava Cache:** (1) Bỏ `Segment` (`ReentrantLock`) chuyển sang `ConcurrentHashMap` + `readBuffer`/`writeBuffer` lock-free + `evictionLock.tryLock()`; (2) Thuật toán W-TinyLFU cho hit-rate vượt trội trước workload có cold scan so với LRU thuần; (3) API hiện đại hỗ trợ `CompletableFuture`/`AsyncCache`, `refreshAfterWrite` chạy async mặc định và không kéo dependency Guava nặng.
2. **W-TinyLFU chống cold scan:** Entry mới chỉ vào **Window** (~1%). Khi bị đẩy ra Window, nó phải thắng phép so sánh tần suất trong **FrequencySketch** (`freq(candidate) > freq(victim)`) mới được vào **Main Probation**; key lạnh xuất hiện 1 lần thua tần suất sẽ bị từ chối, giữ nguyên Main cache cho hot key.
3. **`expireAfterWrite` vs `refreshAfterWrite`:** `expireAfterWrite` chặn caller khi quá hạn để chờ tải giá trị mới (đảm bảo dữ liệu mới nhất, nhưng gây latency spike). `refreshAfterWrite` trên Caffeine trả ngay giá trị cũ (`0–4 ms`) và refresh ngầm bằng executor. Lưu ý trên Guava, `refreshAfterWrite` mặc định **đồng bộ** trừ khi override `CacheLoader.reload()` với `ListenableFuture`.
4. **Chống stampede cục bộ:** `LoadingCache.get(K)` khóa/gom theo từng key (`LoadingValueReference` trong Guava, `ConcurrentHashMap.computeIfAbsent` trong Caffeine) nên dù nhiều thread cùng miss một key lạnh, loader chỉ chạy 1 lần và các thread khác đợi chung kết quả.
5. **Tách `.executor(...)` và kết hợp `expireAfterWrite`:** Caffeine mặc định dùng `ForkJoinPool.commonPool()` cho refresh bất đồng bộ; nếu loader là blocking I/O sẽ làm nghẽn toàn bộ các tác vụ dùng chung `commonPool` (như `CompletableFuture.supplyAsync`). Kết hợp `expireAfterWrite` giúp key không bị giữ mãi giá trị quá cũ nếu không ai truy cập hoặc upstream lỗi kéo dài.
</details>
