# LNX-07 · Đáp án — select, poll, epoll & event loop

> ⚠️ **Chỉ mở sau sau khi đã tự làm.** Đọc đáp án mà chưa tự làm = mất 80% giá trị bài tập.
> Mỗi đáp án được bọc trong `<details>` để tránh lộ khi lướt.

---

### E1
<details><summary>Xem đáp án</summary>

**Đáp án:**
- Với **`poll()`:** Mỗi lần gọi, tiến trình phải copy toàn bộ mảng `50.000 struct pollfd` (~`400 KB`) từ user space xuống kernel space; kernel phải quét tuần tự cả `50.000` FD ($O(N)$) để kiểm tra cờ sự kiện rồi copy ngược lại toàn bộ mảng lên user space; cuối cùng vòng lặp user space lại quét tiếp `50.000` phần tử để nhặt ra chỉ `50` FD có dữ liệu! Nếu gọi 100 lần/giây = `5.000.000` lần kiểm tra FD vô ích/giây.
- Với **`epoll`:** `epoll_ctl` chỉ đăng ký `50.000` FD **một lần duy nhất** vào Red-Black Tree trong kernel. Khi có dữ liệu từ mạng đến, kernel callback đẩy đúng `50` FD có sự kiện vào danh sách `rdllist`. Khi gọi `epoll_wait()`, hàm chỉ trả về đúng **`50` phần tử** ($O(K)$ với $K = 50$), không copy lại 49.950 FD im lặng!

**Đáp án sai phổ biến:** Nghĩ `epoll` đọc gói tin mạng nhanh hơn `poll` → Sai; tốc độ truyền TCP và syscall `read()` cho 1 FD là như nhau, `epoll` thắng nhờ bỏ hoàn toàn chi phí copy và quét $O(N)$ cho hàng chục nghìn FD im lặng.
</details>

---

### E2
<details><summary>Xem đáp án</summary>

**Đáp án:**
- Ở chế độ **Edge-Triggered (`EPOLLET`)**, `epoll_wait()` chỉ bắn thông báo đúng **1 lần duy nhất** khi buffer chuyển từ rỗng sang có dữ liệu (cạnh lên). Khi bạn chỉ đọc `2 KB` rồi quay về gọi `epoll_wait()` mà client không gửi thêm gói mới nào nữa, **`6 KB` còn lại sẽ nằm im vĩnh viễn trong kernel buffer** và `epoll_wait()` không bao giờ đánh thức bạn nữa → kết nối bị treo cho tới timeout!
- Ngược lại, ở chế độ **Level-Triggered (mặc định của Java NIO `Selector`)**, miễn là kernel receive buffer vẫn còn `> 0` bytes (`6 KB`), lời gọi `select()` / `epoll_wait()` tiếp theo **lập tức báo lại `OP_READ` (`EPOLLIN`)** để bạn đọc tiếp phần còn lại.
</details>

---

### E3
<details><summary>Xem đáp án</summary>

**Đáp án:**
**Sai hoàn toàn.** `8` là số **EventLoop threads**, hoàn toàn **không phải** số lượng File Descriptor! Một server Netty phục vụ `50.000` client vẫn mở **`50.000` TCP socket FDs** (cộng thêm `8` epoll FDs và các FD file/pipe/JAR). Do đó trên Linux bắt buộc phải nâng `ulimit -n` / `LimitNOFILE` lên lớn hơn tổng số kết nối đồng thời + headroom (ví dụ `100.000` hoặc `1.048.576`), nếu để `1024` server sẽ nổ `Too many open files` ngay ở kết nối thứ ~1.000!
</details>

---

### E4
<details><summary>Xem đáp án</summary>

**Đáp án:**
Tập `selector.selectedKeys()` là tập hợp do chính code người dùng chịu trách nhiệm xóa phần tử sau khi xử lý. `Selector.select()` chỉ **thêm** các key sẵn sàng vào tập đó, **không bao giờ tự xóa** key cũ ra!
Nếu thiếu `iterator.remove()`, sau gói tin đầu tiên, key đó vẫn nằm mãi trong `selectedKeys()`. Các vòng lặp kế tiếp gọi `select()` thấy `selectedKeys()` vẫn chứa phần tử (hoặc `select()` trả về ngay/vòng lặp chạy liên tiếp xử lý lại key cũ), khiến CPU quay 100%!
</details>

---

### E5
<details><summary>Xem đáp án</summary>

**Đáp án:**
**Không phải đang đốt CPU.** Java chỉ có 6 `Thread.State` (`NEW`, `RUNNABLE`, `BLOCKED`, `WAITING`, `TIMED_WAITING`, `TERMINATED`), nên bất kỳ thread nào đang chạy hoặc đang **ngủ trong native OS blocking syscall** (như `epollWait` chờ packet mạng hoặc `socketRead0` chờ socket) đều được JVM gắn nhãn **`RUNNABLE`**!
Cách kiểm chứng nhanh nhất: chạy **`top -H -p <pid>`** (xem [LNX-05](../05-linux-perf-tools/README.md)) — nếu các `lettuce-epollEventLoop` thread có `%CPU ≈ 0.0%` thì chúng đang ngủ nhàn rỗi chờ sự kiện mạng; chỉ khi `%CPU` vọt `90–100%` mới là spinning loop.
</details>

---

### E6
<details><summary>Xem đáp án</summary>

**Hướng giải:**
- Với `BlockingBioEchoServer` dùng pool `4` threads: 4 kết nối đầu giữ mở làm cả 4 thread kẹt trong `readLine()` chờ dữ liệu (`S`-state). Kết nối thứ `5` dù hoàn tất TCP handshake ở Accept Queue nhưng **không có thread rảnh nào xử lý** → client thứ 5 bị timeout `Read timed out`!
- Với `NioSelectorEchoServer` chỉ chạy **đúng `1` EventLoop thread duy nhất**: tất cả `SocketChannel` đều ở `configureBlocking(false)`. Khi 4 (hay 200) client đầu im lặng, EventLoop không bị kẹt ở bất kỳ client nào nên phục vụ ngay client kế tiếp trong vài mili giây.
</details>

---

### E7
<details><summary>Xem đáp án</summary>

**Hướng giải:**
- Khi bật `blockingDelayMs = 250` trực tiếp trên EventLoop thread, EventLoop ngừng quay lại `selector.select()` trong suốt 250 ms, khiến mọi client `"fast"` gửi trong thời gian đó bị kẹt chờ `>= 250 ms`.
- Bản sửa chuẩn: đưa công việc chậm vào `ExecutorService workerPool`, sau khi worker tính xong thì đặt dữ liệu phản hồi vào queue của channel, bật `OP_WRITE` (hoặc queue đăng ký `OP_WRITE`) và gọi **`selector.wakeup()`** để EventLoop gửi kết quả về mà không block các client `"fast"` (`< 25ms`).

**Code lời giải đầy đủ cho E7:** xem [lab/solutions/OffloadedEventLoopTest.java](./lab/solutions/OffloadedEventLoopTest.java).
</details>

---

### E8
<details><summary>Xem đáp án</summary>

**Đáp án:**
`OP_WRITE` (`EPOLLOUT`) mang ý nghĩa **"kernel send buffer hiện đang còn chỗ trống để ghi thêm byte vào"**. Vì phần lớn thời gian bộ đệm gửi trên socket rảnh rỗi còn trống chỗ, nếu bạn giữ bit `OP_WRITE` bật thường trực trên key mà không tắt đi sau khi ghi xong hàng đợi: mỗi vòng lặp `selector.select()` lập tức trả về ngay lập tức (`selectedKeys` có key đó với `isWritable() == true`) **không chờ đợi 1 micro giây nào** → EventLoop quay vòng vô tận đốt **100% CPU của 1 core** (*busy-spin*) dù không có byte nào để gửi!
**Quy tắc đúng:** Bình thường chỉ đăng ký `OP_READ`. Chỉ đăng ký `OP_WRITE` khi `channel.write(buf)` trả về còn `buf.hasRemaining()`, và ngay khi ghi cạn hàng đợi phải gọi lại `key.interestOps(OP_READ)` để bỏ `OP_WRITE`.
</details>

---

### E9
<details><summary>Xem đáp án</summary>

<details open><summary>Phân tích & Kiến trúc sửa lỗi Block EventLoop trên Netty Gateway</summary>

**1. Chuỗi nhân quả đánh sập Gateway lúc 11:00:**
- Netty Gateway chỉ có **`8` EventLoop threads** gánh toàn bộ `8.000 req/s` (`~1.000 req/s` trên mỗi EventLoop thread).
- Handler gọi trực tiếp `legacyBlockingClient.fetchPdfSync(req)` (mất `1.500 ms`) ngay trên EventLoop thread. Chỉ cần `8` request `/export-pdf` rơi vào 8 EventLoop threads là **100% EventLoop threads kẹt cứng trong `SocketInputStream.socketRead0()` suốt 1.5 giây**.
- Trong suốt 1.5 giây đó, không có thread nào gọi `epoll_wait()` để đọc dữ liệu Redis hay phục vụ `/health` và `/products`! CPU Gateway vẫn nhàn rỗi (`18%`) vì các thread đang nằm ngủ chờ mạng từ JBoss (`S`-state).

**2. Phân biệt 2 dòng `RUNNABLE` trong Thread Dump:**
- `RUNNABLE at EPoll.wait`: trạng thái rảnh bình thường của EventLoop đang chờ sự kiện kernel.
- `RUNNABLE at SocketInputStream.socketRead0` (bên trong `fetchPdfSync`): **báo động đỏ!** Chứng tỏ EventLoop thread đang bị chiếm giữ bởi một lời gọi I/O đồng bộ (*blocking network read*), vi phạm nguyên tắc tối thượng của EventLoop!

**3. Kiến trúc sửa chuẩn (Worker Pool + Bulkhead + Timeout):**
```java
// Khởi tạo pool riêng giới hạn đồng thời (Bulkhead) để bảo vệ JBoss legacy phía sau
private final ExecutorService jbossPdfExecutor = new ThreadPoolExecutor(
        16, 32, 60L, TimeUnit.SECONDS,
        new ArrayBlockingQueue<>(100),
        new ThreadPoolExecutor.AbortPolicy() // Reject nhanh (HTTP 429/503) nếu JBoss quá tải!
);

@Override
public void channelRead(ChannelHandlerContext ctx, Object msg) {
    HttpRequest req = (HttpRequest) msg;
    if (req.uri().startsWith("/api/v1/export-pdf")) {
        // 1. Đẩy công việc blocking ra khỏi EventLoop trong < 0.1ms!
        try {
            jbossPdfExecutor.execute(() -> {
                try {
                    byte[] pdf = legacyBlockingClient.fetchPdfSync(req); // có connect/read timeout!
                    // 2. ctx.writeAndFlush() an toàn đa luồng: Netty tự chuyển việc flush về đúng EventLoop thread
                    ctx.writeAndFlush(pdfResponse(pdf));
                } catch (Exception ex) {
                    ctx.writeAndFlush(errorResponse(502, "Upstream JBoss error"));
                }
            });
        } catch (RejectedExecutionException full) {
            ctx.writeAndFlush(errorResponse(503, "PDF exporter busy"));
        }
        return;
    }
    // /health và /products (Redis async) vẫn chạy < 1ms mượt mà trên EventLoop!
}
```
*(Gợi ý nâng cao trong Netty: có thể gắn một `EventExecutorGroup` riêng trực tiếp vào pipeline cho handler blocking, hoặc chuyển hẳn sang `AsyncHttpClient` non-blocking để bỏ luôn blocking thread pool.)*

</details>
</details>

---

## Self-check (từ README §9) — đáp án gợi ý
<details><summary>Xem</summary>

1. **C10K & Thất bại của BIO:** C10K là bài toán phục vụ 10.000 kết nối đồng thời trên 1 server. BIO (1 thread = 1 connection) tốn ~`10 GB` cho riêng thread stack (`10.000 × 1 MB`), plus overhead context-switch lớn và dễ bị slowloris chiếm hết pool.
2. **Vì sao `epoll` thắng `select`/`poll`:** `select` giới hạn `FD_SETSIZE=1024` và quét $O(N)$; `poll` vẫn copy toàn bộ mảng `pollfd` user↔kernel và quét $O(N)$. `epoll` dùng `epoll_ctl` đăng ký 1 lần vào RB-Tree ở kernel, NIC callback đẩy FD ready vào `rdllist`, nên `epoll_wait` chỉ trả về đúng $K$ FD sẵn sàng ($O(K)$).
3. **Level-Triggered vs Edge-Triggered:** LT nhắc lại chừng nào buffer còn dữ liệu (an toàn, mặc định của Java NIO); ET (`EPOLLET`) chỉ nhắc đúng 1 lần khi buffer chuyển từ rỗng sang có dữ liệu — nếu không đọc vòng lặp cho tới `EAGAIN` sẽ bỏ sót dữ liệu còn lại và treo kết nối.
4. **Netty phục vụ 100k connection:** Dùng ít EventLoop threads (`2×cores`), mỗi thread sở hữu 1 `Selector`/`epoll` quản lý hàng nghìn `SocketChannel` non-blocking; mỗi channel gắn cố định 1 EventLoop nên không cần khóa lock, và idle connection chỉ tốn vài KB object thay vì 1 MB stack.
5. **Block EventLoop gây ra gì:** Vì 1 EventLoop phục vụ hàng nghìn kết nối, blocking call (JDBC/sleep/HTTP sync) khiến EventLoop ngừng gọi `epoll_wait`, làm đóng băng toàn bộ các kết nối nhanh vô tội khác nằm chung EventLoop đó và gây timeout dây chuyền.
</details>
