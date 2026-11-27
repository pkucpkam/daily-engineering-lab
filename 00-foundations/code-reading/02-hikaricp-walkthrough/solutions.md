# READ-02 · Đáp án — Đọc source HikariCP

> ⚠️ **Chỉ mở sau khi đã tự làm.** Đọc đáp án mà chưa tự làm = mất 80% giá trị bài tập.
> Mỗi đáp án được bọc trong `<details>` để tránh lộ khi lướt.

---

### E1
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. **Bước 1 (`threadList` — `ThreadLocal`):** quét ngược danh sách các `PoolEntry` mà chính thread hiện tại đã trả trước đó; thử CAS `STATE_NOT_IN_USE (0)` → `STATE_IN_USE (1)`.
2. **Bước 2 (`sharedList` — `CopyOnWriteArrayList`):** nếu `threadList` không có entry rảnh, tăng `waiters`, duyệt `sharedList` tìm entry có thể CAS `0 → 1`. Nếu không có, gọi `listener.addBagItem(waiting)` báo `HikariPool` tạo thêm connection (nếu chưa đạt `maximumPoolSize`).
3. **Bước 3 (`handoffQueue` — `SynchronousQueue`):** gọi `handoffQueue.poll(timeout, NANOSECONDS)` chờ nhận trực tiếp entry từ một thread khác gọi `requite()` (hoặc `add()`). Hết `timeout` trả về `null`.
</details>

---

### E2
<details><summary>Xem đáp án</summary>

**Đáp án:**
**Có, Thread B lấy được `conn-1`.**

**Giải thích:**
`threadList` của Thread A chỉ giữ *tham chiếu* tới `conn-1`, **không phải sở hữu độc quyền**. Khi Thread A gọi `close()` (`requite`), trạng thái CAS của `conn-1` đã chuyển về `STATE_NOT_IN_USE (0)`. Khi Thread B đi vào Bước 2 (`sharedList`), nó thấy `conn-1` đang ở trạng thái `0` nên thực hiện `compareAndSet(0, 1)` thành công (stealing). Sau này Thread A thức dậy và quét `threadList` của mình, nó gọi `compareAndSet(0, 1)` trên `conn-1` sẽ thấy đang là `1` (thất bại) nên bỏ qua an toàn.
</details>

---

### E3
<details><summary>Xem đáp án</summary>

**Đáp án:**
- Với `120 ms < aliveBypassWindowMs` (mặc định `500 ms`), HikariCP **KHÔNG** ping `isValid()` / `SELECT 1` xuống database, mà giao luôn connection cho Thread B để tiết kiệm 1 round-trip mạng.
- **Đánh đổi:** Nếu DB vừa restart/ngắt kết nối trong cửa sổ `< 500 ms` đó, `getConnection()` vẫn trả về connection (còn sót socket chết) và câu SQL đầu tiên của Thread B sẽ gặp lỗi từ driver (sau đó HikariCP mới phát hiện lỗi kết nối và evict connection đó).
</details>

---

### E4
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. **Không.** Khi `leakDetectionThreshold == 0`, `leakTaskFactory.schedule(poolEntry)` trả về singleton `ProxyLeakTask.NO_LEAK` — không chụp stack trace `new Exception()` và không tạo task trên `houseKeepingExecutorService`.
2. Sau `2000 ms`, `housekeeper` thread in log `WARN com.zaxxer.hikari.pool.ProxyLeakTask - Connection leak detection triggered ...` kèm stack trace điểm gọi `getConnection()`. Đến `2500 ms` khi `conn.close()` chạy, `ProxyLeakTask` in thêm `INFO ... Previously reported leaked connection ... was returned to the pool (unleaked)`.
</details>

---

### E5
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. **Hai tối ưu của `FastList`:** (a) Bỏ kiểm tra biên (`rangeCheck`) trong `get(int)` và bỏ biến đếm `modCount` (`ConcurrentModificationException`); (b) `remove(Object)` duyệt ngược từ đuôi (`size - 1` về `0`), khớp với mẫu LIFO đóng `Statement`/`ResultSet`, giúp khớp ngay ở phần tử cuối mà không phải dịch chuyển toàn bộ mảng.
2. **Vì sao không dùng cho code nghiệp vụ đa luồng:** `FastList` hoàn toàn **không thread-safe** (không có synchronization) và bỏ qua cả kiểm tra `IndexOutOfBounds`/`ConcurrentModificationException` — nếu gọi sai index hoặc truy cập đồng thời sẽ gây `ArrayIndexOutOfBoundsException` hoặc hỏng dữ liệu thầm lặng.
</details>

---

### E6
<details><summary>Xem đáp án</summary>

**Hướng giải & số đo tham khảo** *(Windows 11, JDK 26.0.1 `--release 21`, HikariCP 5.1.0, H2 2.2.224)*:
- `mvn -q verify`: **4/4 tests pass**.
- `mvn -q compile exec:java`: với `maximumPoolSize = 2`, hold time `150 ms`, `connectionTimeout = 350 ms`:
  - 2 thread đầu mượn và trả ở `~176 ms`.
  - 2 thread kế tiếp nhận connection ngay sau đó và trả ở `~360–361 ms`.
  - Thread thứ 5 chờ quá `350 ms` → ném `SQLTransientConnectionException: lab-pool-2 - Connection is not available, request timed out after 361ms (total=2, active=2, idle=0, waiting=0)`.
- Tên class thực tế trả về từ `ds.getConnection()` là `com.zaxxer.hikari.pool.HikariProxyConnection`.
</details>

---

### E7
<details><summary>Xem đáp án</summary>

**Hướng giải:**
- Khởi tạo `HikariConfig` với `maximumPoolSize = 2`, `leakDetectionThreshold = 250`.
- Gọi `ds.getHikariPoolMXBean()` để assert `getActiveConnections() == 1` khi đang giữ connection, `Thread.sleep(350)` để `ProxyLeakTask` chạy, rồi `conn.close()` và assert `getActiveConnections() == 0` và `getIdleConnections() >= 1`.

**Code tham khảo đầy đủ:** xem [lab/solutions/E7LeakAndMxBeanTest.java](./lab/solutions/E7LeakAndMxBeanTest.java).
</details>

---

### E8
<details><summary>Xem đáp án</summary>

**Kết quả thực tế soi từ `HikariCP-5.1.0.jar`:**
1. Trong `FastList.get(int)` chỉ có `aload_0; getfield elementData; iload_1; aaload; areturn` — hoàn toàn không có lệnh `rangeCheck`. Trong `FastList.remove(Object)`: bắt đầu bằng `aload_0; getfield size; iconst_1; isub; istore_2` (tức `int i = size - 1`) và cuối vòng lặp là `iinc 2, -1` (duyệt ngược về `0`).
2. Trong constructor `ConcurrentBag(IBagStateListener)` (dòng bytecode 17–26):
   ```text
   17: new  #6  // class java/util/concurrent/SynchronousQueue
   20: dup
   21: iconst_1 // true -> fair = true!
   22: invokespecial #7 // Method java/util/concurrent/SynchronousQueue."<init>":(Z)V
   ```
   Xác nhận `handoffQueue` dùng `new SynchronousQueue<>(true)` (chế độ fair FIFO để tránh thread bị đói khi tải cao).
</details>

---

### E9
<details><summary>Xem đáp án</summary>

<details open><summary>Phân tích nhân quả & phương án sửa</summary>

1. **Chuỗi nhân quả (vì sao có `try-with-resources` vẫn cạn pool):**
   - `OrderService.createOrder()` mở `Connection` từ đầu rồi giữ nguyên nó trong suốt `httpClient.reserveInventory()` (mất ~2.800 ms lúc cao điểm).
   - Với `maximumPoolSize = 10` và thời gian giữ `2.8s`, thông lượng phục vụ tối đa của pool là `10 / 2.8 ≈ 3,57 req/s`. Khi flash sale đổ vào cao hơn ngưỡng đó (ví dụ 15–20 req/s), toàn bộ 10 `PoolEntry` trong `ConcurrentBag` đều ở trạng thái `STATE_IN_USE` (`active=10, idle=0`).
   - Các thread tiếp theo đi vào `handoffQueue.poll(3000ms)` (khiến `waiting=34`) và lần lượt vượt `connectionTimeout = 3000ms`, ném `SQLTransientConnectionException`.

2. **Hiện tượng `leak` rồi `unleaked`:**
   - **Không phải bug của HikariCP.** `ProxyLeakTask` chỉ đếm thời gian từ lúc `borrow()` (`0s`) tới lúc `close()`. Vì `leakDetectionThreshold = 2000ms < 2800ms`, timer bắn cảnh báo ở mốc `2.0s`. Đến `2.8s`, khi `try-with-resources` đóng connection (`requite()`), `ProxyLeakTask` ghi thêm dòng `unleaked` để báo connection đã được trả chứ không rò rỉ vĩnh viễn.

3. **Cách sửa triệt để:**
   - **Tách DB Connection khỏi HTTP I/O:** Gọi HTTP (hoặc đọc dữ liệu chuẩn bị) *trước* hoặc *sau* khối `try (Connection conn = ds.getConnection())`, hoặc áp dụng mô hình Outbox / Saga để DB transaction chỉ giữ connection trong **5–20 ms** (lúc đó `10` connection phục vụ được `500–2.000 req/s`).
   - Đặt `httpClient` timeout nhỏ hơn `connectionTimeout` nếu bất khả kháng, và chỉnh `leakDetectionThreshold` cao hơn thời gian xử lý DB hợp lệ dài nhất (ví dụ `10.000–30.000 ms`) — nhưng mấu chốt là **không giữ DB connection khi gọi mạng ra ngoài**.
   - **Trên JBoss AS 7.1.1 / JCA:** Nguyên tắc y hệt — không đặt lời gọi EJB/HTTP/SOAP bên trong một JTA `@TransactionAttribute(REQUIRED)` đang giữ JDBC connection từ JBoss DataSource (`max-pool-size`), và dùng `TransactionAttributeType.NOT_SUPPORTED` ở tầng điều phối gọi HTTP trước khi mở transaction ghi DB.

</details>
</details>

---

## Self-check (từ README §9) — đáp án gợi ý
<details><summary>Xem</summary>

1. **Luồng `HikariPool.getConnection(timeout)`:** Qua `suspendResumeLock` → vòng `do/while` gọi `connectionBag.borrow(timeout)` (1: quét ngược `threadList`, 2: quét `sharedList` + báo `addBagItem` nếu thiếu, 3: chờ `handoffQueue.poll`) → nếu `null` ném timeout exception → nếu có entry, check `isMarkedEvicted` và `isConnectionAlive` (bỏ qua nếu mới dùng `< aliveBypassWindowMs = 500ms`) → trả `HikariProxyConnection` kèm lịch `ProxyLeakTask`.
2. **`ConcurrentBag` tránh contention & stealing:** Dùng `ThreadLocal<List>` (fast-path không lock), CAS trên `AtomicInteger` từng `PoolEntry`, `CopyOnWriteArrayList sharedList` (duyệt không lock), và `SynchronousQueue` + `AtomicInteger waiters` (chỉ chạm queue khi thật sự có waiter). `ThreadLocal` không giam chết connection vì thread khác duyệt `sharedList` có thể steal bất kỳ entry nào đang `STATE_NOT_IN_USE` qua CAS `0 → 1`.
3. **`aliveBypassWindowMs` (500ms):** Giảm chi phí ping kiểm tra (`isValid`/`SELECT 1`) nếu connection vừa mới trả về trong vòng 500ms, đánh đổi là rủi ro rất nhỏ nếu DB chết ngay trong cửa sổ 500ms đó.
4. **`ProxyLeakTask`:** `threshold == 0` dùng singleton `NO_LEAK` (0 overhead). `threshold > 0` chụp `new Exception` ngay tại điểm gọi `getConnection()` để lưu stack trace, hẹn chạy sau `threshold` ms; nếu `close()` gọi kịp thì `cancel()`, nếu quá hạn thì in log `WARN` kèm stack trace thủ phạm.
5. **`FastList` nhanh hơn `ArrayList`:** Bỏ `rangeCheck` trong `get(int)`, bỏ theo dõi `modCount`, `remove(Object)` quét ngược từ đuôi (khớp LIFO đóng `Statement`), và `removeLast()` $O(1)$.
</details>
