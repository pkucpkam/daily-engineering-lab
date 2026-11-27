# LNX-07 · Bài tập — select, poll, epoll & event loop

> Làm theo thứ tự. **Tự làm trước, chỉ mở hint khi đã thử ≥ 15 phút.** Đáp án ở [solutions.md](./solutions.md) — chỉ mở sau khi đã có câu trả lời của riêng bạn.
> Ghi thời gian làm từng bài vào `my-notes.md` để theo dõi tiến bộ.

| Cấp | Mục đích | Số lượng | Thời gian gợi ý |
| :--- | :--- | :---: | :---: |
| 🟢 L1 — Recall | Nhớ & hiểu C10K, `epoll` vs `select`, LT vs ET, Netty EventLoop | 5 | 20' |
| 🟡 L2 — Apply | So sánh thực nghiệm NIO Selector vs BIO & tái hiện block event loop | 3 | 75' |
| 🔴 L3 — Deep | Ticket gateway WebFlux/Netty p99 vọt 4 giây dù CPU 8% | 1 | 90' |

---

## 🟢 L1 — Recall

### E1. So sánh chi phí của `poll` vs `epoll` ở 50.000 kết nối TCP (chỉ 1% active)
Giả sử một server chat giữ **50.000 kết nối TCP**, tại mỗi thời điểm `10ms` chỉ có **50 kết nối** gửi tin nhắn (49.950 kết nối còn lại im lặng). Giải thích vì sao `poll()` vẫn đốt rất nhiều CPU ở kernel và user space, trong khi `epoll_wait()` chạy gần như tức thì?

---

### E2. Tại sao Edge-Triggered (`EPOLLET`) lại bị treo mất dữ liệu nếu quên vòng lặp đọc hết?
Nếu client gửi `8 KB` dữ liệu vào socket đang gắn `EPOLLET`, và code server chỉ gọi `read(fd, buf, 2048)` đúng **1 lần** lấy ra `2 KB` rồi quay về gọi `epoll_wait()`: điều gì xảy ra với `6 KB` còn lại trong kernel receive buffer nếu client ngừng gửi thêm gì nữa? Chế độ Level-Triggered (mặc định của Java NIO) cư xử thế nào trong tình huống y hệt?

---

### E3. Vì sao vẫn phải tăng `ulimit -n` cho server Netty dù chỉ dùng 8 threads?
Một bạn nói: *"Netty chỉ tạo có `8` thread thôi nên không cần tăng `ulimit -n`, để mặc định `1024` là đủ!"*. Nhận định này **Đúng hay Sai**? Giải thích mối quan hệ giữa `EventLoop threads` và `File Descriptors` trong `epoll`.

---

### E4. Quên `iterator.remove()` trong Java NIO `Selector`
Trong vòng lặp Java NIO chuẩn:
```java
selector.select();
for (SelectionKey key : selector.selectedKeys()) {
    handle(key);
}
```
Khi chạy vòng lặp này, CPU lập tức vọt lên `100%` dù không có bất kỳ client nào gửi dữ liệu mới sau gói đầu tiên. Giải thích lý do kỹ thuật bên trong `Selector.selectedKeys()`.

---

### E5. Đọc trạng thái `EPoll.wait` trong Thread Dump
Bạn chạy `kill -3 <pid>` trên một app chạy Netty/Lettuce và thấy 16 dòng đều ghi `"lettuce-epollEventLoop-3-1" ... java.lang.Thread.State: RUNNABLE` tại `io.netty.channel.epoll.Native.epollWait(Native Method)`. Có phải cả 16 thread này đang chạy vòng lặp vô tận đốt CPU 100% không? Kiểm chứng thế nào?

---

## 🟡 L2 — Apply

### E6. Thực nghiệm NIO `Selector` (1 Thread) vs BIO Thread-per-request (Pool 4 Threads)
**Bối cảnh:** Dùng [lab/](./lab/README.md) chạy `NioSelectorEchoServer` và `BlockingBioEchoServer`.
**Yêu cầu:**
1. Chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`.
2. Quan sát kết quả thực tế:
   - Với `BlockingBioEchoServer` có `4` worker threads: mở `4` client giữ kết nối idle (chưa gửi dòng nào) → thử kết nối client thứ `5` gửi `"ping"` → điều gì xảy ra?
   - Với `NioSelectorEchoServer` chỉ chạy **đúng `1` EventLoop thread duy nhất**: giữ mở đồng thời `200` kết nối (và chạy client gửi/nhận echo xen kẽ) → đo thời gian phản hồi và số server thread.

**Tiêu chí hoàn thành:**
- [ ] `mvn -o -B verify` pass 100%.
- [ ] Giải thích chính xác vì sao `BlockingBioEchoServer` chết đói thread từ kết nối thứ `5` còn `NioSelectorEchoServer` phục vụ trơn tru `200` kết nối bằng `1` thread.

<details><summary>💡 Hint</summary>

Ở `BlockingBioEchoServer`, 4 worker thread kẹt trong `BufferedReader.readLine()` chờ 4 client đầu gửi dữ liệu, nên dù accept queue nhận được client thứ 5 nhưng không còn thread nào trong pool để xử lý nó!
</details>

---

### E7. Tái hiện & Sửa thảm họa "Block the Event Loop"
**Bối cảnh:** Mở `lab/src/main/java/lab/linux/epoll/NioSelectorEchoServer.java` và `EpollEventLoopTest.java`.
**Yêu cầu:**
1. Chạy test `blockingWorkInsideEventLoopStallsUnrelatedFastClients`: khi có 1 client gửi payload đặc biệt khiến EventLoop gọi `Thread.sleep(250)` trực tiếp trên EventLoop thread, đo latency của 10 client gửi `"fast"` ngay sau đó (thấy bị stall `>= 200ms`).
2. Viết (hoặc đối chiếu với `lab/solutions/OffloadedEventLoopTest.java`) cơ chế đẩy tác vụ chậm `250ms` sang `ExecutorService` worker pool riêng rồi gọi `selector.wakeup()` để ghi trả kết quả — chứng minh các client `"fast"` vẫn phản hồi dưới `< 25ms` ngay trong lúc tác vụ chậm đang chạy!

**Tiêu chí hoàn thành:**
- [ ] Pass test tái hiện stall và test offload giữ latency client `"fast"` `< 50 ms` trong khi 1 client khác tốn `250 ms`.

<details><summary>💡 Hint</summary>

Để worker thread khác ghi lại response an toàn lên `SocketChannel` của EventLoop, hãy đẩy response vào `ConcurrentLinkedQueue<PendingWrite>` (hoặc đổi `key.interestOps(OP_READ | OP_WRITE)`) rồi gọi `selector.wakeup()` để đánh thức `selector.select()` xử lý write ở đúng EventLoop thread!
</details>

---

### E8. Kiểm tra lỗi `selectedKeys()` & `OP_WRITE` spinning
**Bối cảnh:** Đọc đoạn Java NIO client/server lỗi kinh điển bên dưới:
```java
key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
```
Nếu giữ bit `OP_WRITE` bật liên tục ngay cả khi **không còn byte nào trong hàng đợi cần ghi**, chuyện gì xảy ra với `selector.select()` và `%CPU` của EventLoop thread?

**Tiêu chí hoàn thành:**
- [ ] Giải thích đúng lý do kernel buffer gửi (`SO_SNDBUF`) hầu như lúc nào cũng còn chỗ trống nên `OP_WRITE` luôn sẵn sàng (`EPOLLOUT`), làm `select()` trả về ngay lập tức ở tốc độ vòng lặp CPU 100%!

<details><summary>💡 Hint</summary>

Trái ngược với `OP_READ` (chỉ ready khi có dữ liệu từ mạng đến), `OP_WRITE` ready bất cứ khi nào bộ đệm gửi của kernel còn chỗ trống — tức là **99.99% thời gian**! Vì vậy chỉ bật `OP_WRITE` khi `write()` trước đó không ghi hết được vào buffer (`remaining > 0`), và tắt ngay `OP_WRITE` khi đã ghi sạch hàng đợi!
</details>

---

## 🔴 L3 — Deep

### E9. Ticket Production: API Gateway Netty/WebFlux bị timeout hàng loạt khi gọi về JBoss legacy mỗi lúc 11:00
**Tình huống:**
> Team xây dựng một API Gateway mới bằng Java 21 + Netty (`8` EventLoop threads) đứng trước hệ thống **JBoss AS 7.1.1** cũ. Bình thường Gateway chịu tải `8.000 req/s`, p99 = `18 ms`, CPU `22%`.
> Lúc `11:00` mỗi ngày, khi một nhóm người dùng gọi endpoint `/api/v1/export-pdf` (chuyển tiếp sang JBoss xử lý mất `1.5 giây`), bỗng nhiên **cả các endpoint siêu nhẹ như `/health` và `/api/v1/products` (cache Redis 1ms) cũng bị p99 vọt lên `1.600 ms` và timeout hàng loạt**, mặc dù CPU Gateway vẫn chỉ `18%`!
> Đọc code handler mới deploy trong Gateway thấy đoạn sau:
> ```java
> @Override
> public void channelRead(ChannelHandlerContext ctx, Object msg) {
>     HttpRequest req = (HttpRequest) msg;
>     if (req.uri().startsWith("/api/v1/export-pdf")) {
>         // Gọi sang JBoss legacy bằng Apache HttpClient đồng bộ cũ
>         byte[] pdf = legacyBlockingClient.fetchPdfSync(req);
>         ctx.writeAndFlush(pdfResponse(pdf));
>         return;
>     }
>     // ... đọc Redis async
> }
> ```
> Và khi chạy `kill -3 <gateway_pid>`, các thread `nioEventLoopGroup-3-*` hiện trạng thái:
> `java.lang.Thread.State: RUNNABLE at java.net.SocketInputStream.socketRead0(Native Method)` (hoặc `sun.nio.ch.Net.poll`).

**Yêu cầu:**
1. Giải thích **cơ chế đầy đủ** tại sao chỉ vài chục request `/export-pdf` (1.5s) lại đánh sập latency của cả `/health` và `/products` trên cùng Gateway dù CPU vẫn rảnh `82%`.
2. Phân biệt trạng thái `RUNNABLE at EPoll.wait` (rảnh) vs `RUNNABLE at socketRead0 / LegacyBlockingClient` (bị block trên EventLoop) trong thread dump vừa thu được.
3. Đề xuất **kiến trúc sửa đúng** trên Netty/Java (kèm đoạn code sửa sử dụng Worker Executor pool + Bulkhead tách riêng cho `legacyBlockingClient`, đồng thời nhớ bảo vệ JBoss bằng timeout & giới hạn concurrency!).

**Tiêu chí hoàn thành:**
- [ ] Giải thích chính xác `8` EventLoop threads bị chiếm giữ bởi `fetchPdfSync` (`socketRead0`), làm tê liệt `epoll_wait` cho toàn bộ 8.000 req/s còn lại.
- [ ] Viết code offload sang Worker `ExecutorService` (kèm `Semaphore` hoặc bounded queue bulkhead không để JBoss legacy bị ngập) và trả response qua `ctx.executor().execute(...)` hoặc `ctx.writeAndFlush(...)` an toàn.

<details><summary>💡 Hint</summary>

1. Chỉ cần `8` request `/export-pdf` đồng thời đi vào `8` EventLoop threads khác nhau là **100% các EventLoop threads của Netty đều đang kẹt trong `socketRead0` chờ JBoss trả PDF suốt 1.5 giây**! Không còn thread nào quay về gọi `epoll_wait()` cho hàng nghìn kết nối `/health` và `/products`.
2. Khắc phục: đưa `legacyBlockingClient.fetchPdfSync` sang `blockingPool` riêng có `maxPoolSize` giới hạn (bảo vệ JBoss BIO threads phía sau!), đặt timeout chặt, rồi gọi `ctx.writeAndFlush(...)` (Netty tự dispatch việc write về lại đúng EventLoop thread sở hữu channel đó!).
</details>
