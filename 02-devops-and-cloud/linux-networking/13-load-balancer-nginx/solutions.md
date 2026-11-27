# LNX-13 · Đáp án — Load balancer L4 vs L7 & Nginx reverse proxy

> ⚠️ **Chỉ mở sau khi đã tự làm.** Đọc đáp án mà chưa tự làm = mất 80% giá trị bài tập.
> Mỗi đáp án được bọc trong `<details>` để tránh lộ khi lướt.

---

### E1
<details><summary>Xem đáp án</summary>

**Đáp án:**
5 chức năng L7 LB làm được mà L4 LB không làm được:
1. **TLS Termination & quản lý chứng chỉ / mTLS tập trung** (L4 chỉ pass-through TLS encrypted TCP stream).
2. **Định tuyến theo nội dung HTTP (*Content-based Routing*):** bóc URL path (`/api` vs `/static`), HTTP Method, Host, Cookie hoặc Canary header.
3. **Cân bằng tải từng RPC/request cho HTTP/2 & gRPC** (L4 kẹt vào 1 backend duy nhất vì HTTP/2 dùng 1 TCP connection dài hạn).
4. **HTTP Health Check thông minh (`GET /healthz` kiểm HTTP `200 OK`) & Retry theo HTTP status** (L4 chỉ check được bắt tay TCP).
5. **Response Buffering, Nén (`gzip`/`brotli`), Rate Limiting & Header Injection** (`X-Forwarded-For`, `X-Request-ID`).
</details>

---

### E2
<details><summary>Xem đáp án</summary>

**Đáp án:**
- **Trường hợp A (`proxy_pass http://jboss_backend;` — KHÔNG có `/` cuối):** Nginx giữ nguyên toàn bộ URI gốc → JBoss nhận được **`/payments/v1/status`**.
- **Trường hợp B (`proxy_pass http://jboss_backend/;` — CÓ `/` cuối):** Nginx thay thế phần khớp với `location /payments/` bằng `/` → JBoss nhận được **`/v1/status`** (đã cắt mất `/payments`)!
</details>

---

### E3
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. **API JSON `5 MB` qua mạng 3G yếu:** BẮT BUỘC bật **`proxy_buffering on`** (mặc định). Nhờ đó JBoss chỉ mất vài chục ms đẩy hết `5 MB` vào RAM/disk của Nginx rồi **giải phóng ngay worker thread**, để Nginx tự lo phần truyền nhỏ giọt suốt 20 giây cho user 3G. Nếu tắt buffering, hàng trăm user mạng chậm sẽ giữ chết sạch `maxThreads` của JBoss!
2. **Endpoint SSE (`/api/stream-tokens`):** BẮT BUỘC tắt **`proxy_buffering off`** (kèm `chunked_transfer_encoding on` và `X-Accel-Buffering: no`). Vì nếu bật buffering, Nginx sẽ găm toàn bộ từng token/event lại chờ đầy buffer `4KB–8KB` mới đẩy xuống trình duyệt, phá hỏng tính chất real-time streaming!
</details>

---

### E4
<details><summary>Xem đáp án</summary>

**Đáp án:**
- **Thứ tự đúng (tăng dần từ trong ra ngoài):**
  `DB JDBC socketTimeout` (vd `10s`) < `JBoss Handler/Tx Timeout` (vd `25s`) < `Nginx proxy_read_timeout` (vd `30s`) < `Mobile Client Timeout` (vd `35s`).
- **Nếu đặt ngược (`Nginx = 15s < JBoss = 45s`):** Đúng giây 15, Nginx cắt kết nối trả `504 Gateway Timeout` cho client. Nhưng JBoss vẫn không hay biết, chạy nốt đến giây 20 và **`COMMIT` trừ tiền vào DB** (rồi mới gặp `Broken pipe` khi ghi response)! Khách hàng thấy lỗi `504` bấm thanh toán lại → **trừ tiền 2 lần**!
</details>

---

### E5
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. `max_fails=2 fail_timeout=15s`: Đây là **Passive Health Check** của Nginx. Nếu trong cửa sổ `15 giây`, có `2` lần gửi request tới `10.0.1.12:8080` gặp lỗi kết nối, timeout hoặc HTTP status nằm trong `proxy_next_upstream`, Nginx đánh dấu node này **không khả dụng suốt `15s`** (chuyển hết tải sang node khác), hết 15s mới thử gửi lại 1 request để kiểm tra xem node đã sống lại chưa.
2. **Bẫy Deep Health Check:** Nếu `/healthz` chạy query vào DB và trả `500` khi DB lag nhẹ `1–2 giây`, LB sẽ tưởng **toàn bộ 100% node Web đều hỏng** và đánh dấu `DOWN` toàn bộ cluster (mất `15–30s` phục hồi sau khi DB hết lag, thậm chí tạo *thundering herd* khi mở lại!) — gây sập toàn bộ site kể cả các route không cần DB.
</details>

---

### E6
<details><summary>Xem đáp án</summary>

**Hướng giải:**
Khi chạy `mvn -o -B verify` và `mvn -o -B compile exec:java` với `30` concurrent requests (`Backend #1 = 80 ms`, `Backend #2 & #3 = 5 ms`):
- **`ROUND_ROBIN`:** Phân phối đều đặn **`10 / 10 / 10`** requests cho cả 3 backend. Vì Backend #1 chậm `80 ms`, `10` request dính vào nó tạo hàng đợi kéo dài tổng thời gian thực thi.
- **`LEAST_CONNECTIONS`:** Theo dõi bộ đếm `inFlight` theo thời gian thực trên từng backend; trong lúc Backend #1 còn kẹt xử lý `80 ms`, #2 và #3 liên tục rảnh `inFlight = 0` nên hứng phần lớn request (chỉ `~2–4` request rơi vào #1, còn `#2 + #3` xử lý phần còn lại!) → tổng thời gian giảm mạnh.
</details>

---

### E7
<details><summary>Xem đáp án</summary>

**Hướng giải:**
Trong `MiniL7ReverseProxy`, khi Backend #2 bị đóng nóng giữa traffic:
- 2 request đầu tiên chạm Backend #2 gặp `ConnectException`, Passive Health Check tăng `consecutiveFailures` lên `2 == maxFails` → chuyển trạng thái Backend #2 thành `UNHEALTHY` trong `failTimeoutMs`, đồng thời retry idempotent `GET` sang backend khỏe nên client vẫn nhận đủ **`100% HTTP 200 OK`**!
- Trong suốt thời gian `failTimeoutMs` tiếp theo, proxy bỏ qua hoàn toàn Backend #2 và chỉ gửi tải vào Backend #1 & #3.

**Code lời giải tham khảo:** xem [lab/solutions/FailoverGracefulProxySolution.java](./lab/solutions/FailoverGracefulProxySolution.java).
</details>

---

### E8
<details><summary>Xem đáp án</summary>

**Hướng giải:**
Trong `LoadBalancerLabTest`:
- Proxy gọi backend `POST /transfer` mất `180 ms` với `proxyReadTimeoutMs = 60 ms`.
- Đúng ~`60 ms`, client nhận **`504 Gateway Timeout`** từ Proxy; nhưng đợi `250 ms` kiểm tra `backend.completedCommits()` thấy kết quả bằng **`1`**! Chứng minh việc cắt kết nối tại Proxy không hề ngăn được backend đã nhận payload thực hiện side-effect vào DB!
</details>

---

### E9
<details><summary>Xem đáp án</summary>

<details open><summary>Phân tích Ticket Production: Trừ tiền 2 lần vì 504 + non_idempotent & cạn thread JBoss</summary>

**1. Chuỗi nhân quả trừ tiền 2 lần (`14:02:10` → `14:03:11`):**
1. Lúc `14:02:10`, Nginx đẩy `POST /api/v1/pay` sang **JBoss-1**. JBoss-1 gọi Core Banking bị chậm (ví dụ `65s`, vẫn nhỏ hơn `90s` JTA transaction timeout và `70s` downstream timeout).
2. Lúc `14:03:10` (sau đúng **`60s`**), **`proxy_read_timeout 60s`** của Nginx nổ! JBoss-1 lúc này vẫn đang chạy nốt 5 giây cuối và **`COMMIT` trừ tiền lần 1**.
3. Vì config `proxy_next_upstream` chứa từ khóa **`non_idempotent`** cùng `timeout`, Nginx **tự động gửi lại chính request `POST /api/v1/pay` đó sang JBoss-2**! JBoss-2 chạy tiếp, trừ tiền **lần 2**, và nếu cũng vượt `60s` thì Nginx trả **`504 Gateway Timeout`** về mobile!

**2. 4 lỗi sai nghiêm trọng trong cấu hình:**
1. **`proxy_next_upstream ... timeout ... non_idempotent`:** Cho phép Nginx retry mù quáng method `POST` khi bị timeout đọc response!
2. **Đặt ngược Timeout (`Nginx proxy_read_timeout = 60s` < `JBoss JTA = 90s` & `Core Banking = 70s`):** Nginx cắt trước khi JBoss kịp timeout & rollback.
3. **`proxy_buffering off;`:** Buộc worker thread JBoss phải giữ kết nối sống chờ từng byte truyền xuống mobile 3G chậm → cạn sạch `200/200` JBoss threads dù CPU thấp!
4. **Thiếu `proxy_http_version 1.1;` và `proxy_set_header Connection "";`:** Làm dòng `keepalive 32;` trong `upstream jboss_pay` hoàn toàn vô tác dụng (vẫn dùng HTTP/1.0 `Connection: close`).

**3. Cấu hình & giải pháp khắc phục chuẩn production:**
- **Nginx:**
  - Đặt `least_conn;` + giữ `keepalive 32;` kèm `proxy_http_version 1.1;` và `proxy_set_header Connection "";`.
  - Xóa `non_idempotent` khỏi `proxy_next_upstream` (chỉ giữ `error timeout` cho idempotent `GET`, hoặc tắt hẳn retry với endpoint thanh toán).
  - Bật `proxy_buffering on;` cho API thông thường.
  - Sửa timeout chain: Core Banking socket timeout `20s` < JBoss Handler/JTA timeout `30s` < Nginx `proxy_read_timeout 35s` < Mobile timeout `40s`.
- **Ứng dụng Java / DB:** Bắt buộc yêu cầu client gửi **`Idempotency-Key`** (unique constraint trong DB) cho mọi API `POST /pay` để dù retry ở tầng nào cũng chỉ trừ tiền tối đa 1 lần ([PAT-18](../../../01-software-engineering/patterns/idempotent-consumer.md)).

</details>
</details>

---

## Self-check (từ README §9) — đáp án gợi ý
<details><summary>Xem</summary>

1. **5 điều L7 LB làm được mà L4 không:** (1) TLS termination & mTLS tập trung; (2) Route theo URL/Host/Header/Cookie; (3) Cân bằng tải từng RPC của HTTP/2 & gRPC; (4) Active/Passive HTTP health check (`/healthz` status `200`) & retry theo HTTP status; (5) Response buffering, gzip/brotli, rate limit, inject `X-Forwarded-*`.
2. **Round-Robin vs Least-Conn vs IP Hash:** Round-Robin chia đều theo lượt (kém khi request nặng/nhẹ lệch nhau); `least_conn` ưu tiên node có ít kết nối in-flight nhất nên tự né node chậm; `ip_hash` gắn cố định client IP vào 1 node để tận dụng cache local nhưng dễ lệch tải qua NAT.
3. **`proxy_buffering on` vs `off`:** Khi `on`, Nginx hút nhanh toàn bộ response của JBoss vào buffer rồi từ từ phát cho user mạng 3G chậm, cứu worker thread JBoss khỏi bị giữ lâu. Phải tắt (`off`) khi làm **SSE / LLM streaming / large file stream** để không kẹt byte đầu tiên.
4. **Timeout Chain chuẩn:** `Mobile/Client > Nginx proxy_read_timeout > App/JBoss timeout > Downstream/DB timeout`. Nếu `Nginx < JBoss`, Nginx trả `504` sớm trong khi JBoss vẫn tiếp tục commit DB (và nếu dính `non_idempotent` retry sẽ gây thực hiện 2 lần!).
5. **Passive vs Active Health Check & Deep Health Check:** Passive (`max_fails`/`fail_timeout`) quan sát lỗi từ request thật của user; Active chủ động ping `/healthz` định kỳ. Bẫy Deep Health Check: trả `500` ở `/healthz` khi DB nháy `1–2s` khiến LB đánh sập toàn bộ 100% app pods cùng lúc.
</details>
