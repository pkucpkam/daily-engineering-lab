# READ-02 · Bài tập — Đọc source HikariCP

> Làm theo thứ tự. **Tự làm trước, chỉ mở hint khi đã thử ≥ 15 phút.** Đáp án ở [solutions.md](./solutions.md) — chỉ mở sau khi đã có câu trả lời của riêng bạn.
> Ghi thời gian làm từng bài vào `my-notes.md` để theo dõi tiến bộ.

| Cấp | Mục đích | Số lượng | Thời gian gợi ý |
| :--- | :--- | :---: | :---: |
| 🟢 L1 — Recall | Nhớ & hiểu khái niệm | 5 | 20' |
| 🟡 L2 — Apply | Viết code / phân tích tình huống cụ thể | 3 | 75' |
| 🔴 L3 — Deep | Debug, thiết kế, đo đạc, tình huống mở | 1 | 90' |

---

## 🟢 L1 — Recall

### E1. 3 tầng tìm connection trong `ConcurrentBag.borrow()`
Liệt kê đúng thứ tự 3 bước tìm `PoolEntry` trong `ConcurrentBag.borrow(long, TimeUnit)` và trạng thái `AtomicInteger` chuyển từ giá trị nào sang giá trị nào khi mượn thành công?

---

### E2. Tại sao `ThreadLocal` không làm connection bị giam chết ở 1 thread?
Giả sử Thread A mượn rồi trả `conn-1` (nên `conn-1` nằm trong `threadList` của Thread A), sau đó Thread A đi ngủ 10 phút trong khi Thread B cần connection và pool chỉ còn đúng `conn-1` rảnh. Thread B có lấy được `conn-1` không? Bằng cơ chế nào?

---

### E3. `aliveBypassWindowMs` và `isConnectionAlive`
Nếu một connection vừa được Thread A đóng cách đây `120 ms` và Thread B mượn lại ngay lúc này (với cấu hình mặc định của HikariCP), HikariCP có chạy `isValid()` / `SELECT 1` xuống database không? Vì sao? Ngược lại nếu DB vừa restart cách đây `100 ms` thì sao?

---

### E4. `ProxyLeakTask` khi tắt vs bật
1. Khi `leakDetectionThreshold = 0` (mặc định), mỗi lần `getConnection()` có tạo `new Exception()` hay đặt lịch `ScheduledFuture` không?
2. Khi đặt `leakDetectionThreshold = 2000` (2s) nhưng 1 query chạy hợp lệ mất `2500 ms` rồi mới `close()`, log của HikariCP sẽ in ra những dòng gì?

---

### E5. `FastList` vs `ArrayList`
Kể **2 tối ưu cụ thể** của `com.zaxxer.hikari.util.FastList` so với `java.util.ArrayList` khi quản lý danh sách `Statement` đang mở trên một `ProxyConnection`. Vì sao `FastList` không phù hợp làm cấu trúc dùng chung đa luồng cho code nghiệp vụ thông thường?

---

## 🟡 L2 — Apply

### E6. Debug step-through `HikariPool.getConnection` với pool size 2 và 5 thread
**Bối cảnh:** Dùng [lab/](./lab/README.md) chạy thí nghiệm `[1]` (`maximumPoolSize = 2`, 5 worker threads đồng thời).

**Yêu cầu:**
1. Chạy `mvn -q verify` và `mvn -q compile exec:java` trên máy của bạn; ghi lại số thread thành công vs số thread timeout ở `[1]`.
2. Mở IDE đặt breakpoint tại `HikariWalkthroughRunner.java` dòng gọi `ds.getConnection()`, sau đó dùng **Force Step Into** (hoặc tải source jar qua Maven) đi vào `HikariPool.getConnection(long)` và `ConcurrentBag.borrow(long, TimeUnit)`:
   - Ghi lại tên chính xác class proxy trả về (`conn.getClass().getName()`).
   - Chụp/ghi lại call stack lúc thread thứ 3 hoặc thứ 5 đang chờ ở `handoffQueue.poll(...)`.

**Input / code khởi đầu:** `lab/src/main/java/lab/codereading/hikari/HikariWalkthroughRunner.java`.
**Tiêu chí hoàn thành:**
- [ ] `mvn -q verify` pass 4/4 tests.
- [ ] Ghi đúng tên class `com.zaxxer.hikari.pool.HikariProxyConnection` và giải thích được vì sao với `hold = 150ms` và `connectionTimeout = 350ms` lại có kết quả timeout như output thực tế.

<details><summary>💡 Hint</summary>

Với `maximumPoolSize = 2`:
- Batch 1 (2 thread đầu) mượn lúc `t ≈ 0ms` → trả lúc `t ≈ 150–175ms`.
- Batch 2 (2 thread kế) chờ `~175ms < 350ms`, mượn được và trả ở `t ≈ 350–360ms`.
- Thread thứ 5 chờ từ `t ≈ 0ms`: qua `350ms` chưa nhận được connection từ `handoffQueue` nên hết `connectionTimeout` và ném `SQLTransientConnectionException`!
</details>

---

### E7. Viết test kích hoạt và kiểm chứng `ProxyLeakTask` + `getPoolStateCounts`
**Bối cảnh:** Bạn muốn viết 1 integration test tự động kiểm chứng `HikariPoolMXBean` báo `activeConnections = 1` khi bị quên `close()`, và sau khi trả connection thì `activeConnections` về `0`, `idleConnections` tăng lên.

**Yêu cầu:**
Viết JUnit 5 test sử dụng `HikariDataSource` (H2 in-memory, `maximumPoolSize = 2`, `leakDetectionThreshold = 250`):
1. Mượn 1 connection (không đóng ngay) → kiểm tra qua `ds.getHikariPoolMXBean()` thấy `getActiveConnections() == 1`.
2. Giữ `350ms` (> `250ms` để `ProxyLeakTask` kích hoạt), sau đó đóng connection → kiểm tra `getActiveConnections() == 0` và `getIdleConnections() >= 1`.

**Input / code khởi đầu:** `lab/src/test/java/lab/codereading/hikari/HikariWalkthroughTest.java`.
**Tiêu chí hoàn thành:**
- [ ] Test chạy xanh bằng `mvn -q verify` (xem lời giải mẫu ở `lab/solutions/E7LeakAndMxBeanTest.java`).
- [ ] Quan sát log console thấy `WARN com.zaxxer.hikari.pool.ProxyLeakTask - Connection leak detection triggered` kèm `unleaked` sau khi đóng.

<details><summary>💡 Hint</summary>

`HikariDataSource.getHikariPoolMXBean()` trả về `HikariPoolMXBean` với các method `getActiveConnections()`, `getIdleConnections()`, `getTotalConnections()`, `getThreadsAwaitingConnection()`.
</details>

---

### E8. Soi bytecode `FastList` vs `ConcurrentBag` bằng `javap`
**Bối cảnh:** Thực hành kỹ năng đọc code compiled từ jar thư viện khi chưa có source code đính kèm.

**Yêu cầu:**
Dùng `javap -c -p` lên `com.zaxxer.hikari.util.FastList` trong `.m2\repository\com\zaxxer\HikariCP\5.1.0\HikariCP-5.1.0.jar`:
1. Kiểm tra method `get(int)` và `remove(Object)` trong `FastList`: chứng minh `get(int)` không gọi bất kỳ hàm `rangeCheck` nào và `remove(Object)` duyệt ngược `i--` từ `size - 1`.
2. Kiểm tra constructor của `ConcurrentBag`: xác nhận tham số truyền vào `SynchronousQueue` là `true` (`iconst_1` → fair mode) hay `false`.

**Tiêu chí hoàn thành:**
- [ ] Trích xuất được đoạn bytecode xác nhận `iconst_1` cho `SynchronousQueue(Z)` và vòng lặp `iinc ..., -1` trong `FastList.remove(Object)`.

<details><summary>💡 Hint</summary>

```bash
javap -c -p -cp <path-to-HikariCP-5.1.0.jar> com.zaxxer.hikari.util.FastList
```
Tìm method `public boolean remove(java.lang.Object)` và để ý `aload_0; getfield size; iconst_1; isub` rồi `iinc ..., -1`.
</details>

---

## 🔴 L3 — Deep

### E9. Sự cố Production: Bão timeout + cảnh báo Leak giả sau giờ cao điểm
**Tình huống:**
> Service đặt hàng dùng HikariCP (`maximumPoolSize = 10`, `connectionTimeout = 3000ms`, `leakDetectionThreshold = 2000ms`, `maxLifetime = 1800000ms`). Lúc 20:00 flash sale:
> 1. Hệ thống xuất hiện hàng loạt log:
>    - `WARN ProxyLeakTask - Connection leak detection triggered for conn7...` ở hàm `OrderService.createOrder()`, nhưng 800ms sau lại thấy `INFO ProxyLeakTask - Previously reported leaked connection conn7 ... was returned to the pool (unleaked)`.
>    - Cùng lúc đó, nhiều request khác ném: `SQLTransientConnectionException: Connection is not available, request timed out after 3002ms (total=10, active=10, idle=0, waiting=34)`.
> 2. Kiểm tra code `OrderService.createOrder()`:
>    ```java
>    try (Connection conn = ds.getConnection()) {
>        insertOrder(conn, order);
>        boolean ok = httpClient.reserveInventory(order); // HTTP call sang service khác, lúc cao điểm mất ~2.8s!
>        if (!ok) throw new RuntimeException("out of stock");
>    }
>    ```

**Yêu cầu:**
1. Dựa vào cơ chế của `HikariPool`, `ConcurrentBag` và `ProxyLeakTask`, giải thích **chuỗi nhân quả đầy đủ** từ 1 đoạn code tưởng chừng "đã dùng `try-with-resources` nên không thể leak" dẫn tới `waiting=34` và timeout hàng loạt.
2. Tại sao `ProxyLeakTask` vừa báo `leak` rồi lại báo `unleaked`? Đây có phải bug của HikariCP không?
3. Đề xuất **cách sửa triệt để** (về thứ tự I/O & Transaction boundary) + thông số `leakDetectionThreshold` hợp lý, và đối chiếu với cách xử lý tương tự trên **JBoss AS 7.1.1 / JCA pool**.

**Tiêu chí hoàn thành:**
- [ ] Tính được throughput tối đa của pool 10 connection khi mỗi connection bị giữ `2.8s` (Little's Law sơ bộ: `10 / 2.8 ≈ 3.57 req/s`).
- [ ] Chỉ ra chính xác vì sao gọi HTTP bên ngoài trong `try (Connection conn = ...)` là nguyên nhân gốc, và nêu giải pháp tách DB connection khỏi HTTP I/O.

<details><summary>💡 Hint</summary>

- Khi 10 connection đều bị giữ `2.8s` để chờ HTTP response, thông lượng tối đa chỉ còn `10 / 2.8 ≈ 3.57 req/s`! Nếu tải vào là `20 req/s`, hàng đợi `handoffQueue` phình rất nhanh → `waiting=34` và mọi thread chờ quá `3.0s` đều timeout.
- `ProxyLeakTask` không phải lỗi thật ở trường hợp này: nó chỉ đếm đồng hồ từ lúc `borrow()` (`t=0`) tới lúc `close()` (`t=2.8s > 2.0s`), nên kích hoạt ở `t=2.0s` rồi nhận `close()` ở `t=2.8s` và báo `unleaked`.
</details>
