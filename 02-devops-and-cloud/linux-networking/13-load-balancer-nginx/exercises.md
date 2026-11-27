# LNX-13 · Bài tập — Load balancer L4 vs L7 & Nginx reverse proxy

> Làm theo thứ tự. **Tự làm trước, chỉ mở hint khi đã thử ≥ 15 phút.** Đáp án ở [solutions.md](./solutions.md) — chỉ mở sau khi đã có câu trả lời của riêng bạn.
> Ghi thời gian làm từng bài vào `my-notes.md` để theo dõi tiến bộ.

| Cấp | Mục đích | Số lượng | Thời gian gợi ý |
| :--- | :--- | :---: | :---: |
| 🟢 L1 — Recall | Nắm chắc L4 vs L7, thuật toán, dấu `/`, `proxy_buffering`, Timeout Chain | 5 | 20' |
| 🟡 L2 — Apply | Chạy Mini L7 Reverse Proxy 3 backends: Least-Conn, Failover & Timeout `504` | 3 | 75' |
| 🔴 L3 — Deep | Ticket Production: `POST /pay` bị `504` gây trừ tiền 2 lần & JBoss hết thread khi mạng 3G yếu | 1 | 90' |

---

## 🟢 L1 — Recall

### E1. Liệt kê 5 tính năng L7 LB làm được mà L4 LB không thể làm
Một kiến trúc sư đề xuất thay Nginx L7 bằng một Load Balancer L4 thuần túy trước cụm web JBoss để giảm latency vài trăm microsecond. Hãy nêu **5 chức năng** bị mất đi nếu bỏ L7 LB ở tầng giữa.

---

### E2. Giải mã URL thật tới JBoss khi thêm hoặc bớt dấu `/` ở `proxy_pass`
Cho cấu hình Nginx:
```nginx
location /payments/ {
    # Trường hợp A:
    proxy_pass http://jboss_backend;
    # Trường hợp B:
    # proxy_pass http://jboss_backend/;
}
```
Khi client gọi `GET https://api.example.com/payments/v1/status`, JBoss sẽ nhận được URI là gì ở Trường hợp A và Trường hợp B?

---

### E3. Vì sao tắt `proxy_buffering` trên API thường làm sập JBoss, nhưng lại BẮT BUỘC tắt cho SSE?
Giải thích tác động trái ngược của chỉ thị `proxy_buffering on` vs `off` trong 2 tình huống:
1. API xuất báo cáo JSON `5 MB` phục vụ hàng trăm user dùng mạng 3G/4G yếu.
2. Endpoint Server-Sent Events (`/api/stream-tokens`) truyền từng sự kiện real-time từ AI/notification xuống trình duyệt.

---

### E4. Chuỗi Timeout & Rủi ro khi `proxy_read_timeout < JBoss timeout`
Sắp xếp thứ tự giá trị timeout tăng dần chuẩn cho 4 tầng: `DB JDBC socketTimeout`, `JBoss HTTP/Tx Timeout`, `Nginx proxy_read_timeout`, `Mobile Client Timeout`. Giải thích chuyện gì xảy ra với giao dịch chuyển tiền nếu bạn lỡ đặt `Nginx proxy_read_timeout = 15s` trong khi `JBoss Timeout = 45s`.

---

### E5. Passive Health Check trong Nginx (`max_fails` / `fail_timeout`) vs Deep Health Check
1. Giải thích hành vi của dòng `server 10.0.1.12:8080 max_fails=2 fail_timeout=15s;`.
2. Tại sao đặt endpoint `/healthz` kiểm tra cứng `SELECT 1` tới Database có thể biến 1 giây lag của DB thành sự cố sập toàn bộ 100% node Web?

---

## 🟡 L2 — Apply

### E6. Thử nghiệm Round-Robin vs Least Connections khi có 1 Backend chậm (`80 ms`)
**Bối cảnh:** Dùng [lab/](./lab/README.md) chạy `LoadBalancerLabTest` và `LoadBalancerRunner`.
**Yêu cầu:**
1. Chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`.
2. Trong `MiniL7ReverseProxy`, cho 3 backend loopback thật: Backend #1 xử lý chậm (`80 ms`), Backend #2 & #3 nhanh (`5 ms`), bắn đồng thời `30` requests:
   - Ghi lại số request đổ vào từng node dưới chiến lược `ROUND_ROBIN` vs `LEAST_CONNECTIONS` và tổng thời gian hoàn thành.
3. Giải thích vì sao `LEAST_CONNECTIONS` giảm tải rõ rệt cho node chậm.

**Tiêu chí hoàn thành:**
- [ ] `mvn -o -B verify` pass 100%.
- [ ] Chứng minh node #1 chậm nhận ít request hơn hẳn node #2/#3 khi dùng `LEAST_CONNECTIONS`.

<details><summary>💡 Hint</summary>

Với `ROUND_ROBIN`, proxy chia máy móc `1/3` request (`10` cái) vào node #1 bất chấp node #1 đang còn hàng đợi `inFlight > 0`! Với `LEAST_CONNECTIONS`, trong lúc node #1 còn bận xử lý `80 ms`, các request kế tiếp thấy `inFlight` của #2 và #3 bằng `0` nên né hoàn toàn sang #2 và #3!
</details>

---

### E7. Kill 1 Backend trong 3 node & quan sát Passive Health Check Failover
**Bối cảnh:** Tiếp tục chạy kịch bản Failover trong `MiniL7ReverseProxy` (và đọc `lab/solutions/FailoverGracefulProxySolution.java`).
**Yêu cầu:**
1. Khởi chạy proxy cân tải 3 backend với `maxFails = 2`, `failTimeoutMs = 2000`.
2. Đóng đột ngột (`close()`) Backend #2 giữa lúc đang có traffic (`30` requests):
   - Quan sát proxy bắt connect failure, chuyển sang retry an toàn (`GET` idempotent) sang node sống và đánh dấu Backend #2 là **`UNHEALTHY`** sau 2 lần lỗi.
   - Đếm số request thành công (`200 OK`) nhận được tại client (`100%` không rớt request sau khi failover!) và số request gửi nhầm sang Backend #2 sau khi đã bị đánh dấu `UNHEALTHY`.

**Tiêu chí hoàn thành:**
- [ ] Client nhận đủ `200 OK` sau khi failover và proxy không tiếp tục đâm đầu vào node chết trong cửa sổ `failTimeout`.

<details><summary>💡 Hint</summary>

Lưu ý quy tắc vàng ở tầng HTTP: chỉ retry sang `next_upstream` với method idempotent (`GET`/`HEAD`) hoặc trước khi gửi dữ liệu (`connect error`) — với `POST` đã gửi xong mà timeout đọc response thì **KHÔNG được retry**!
</details>

---

### E8. Tái hiện Timeout Mismatch (`504 Gateway Timeout` nhưng Backend vẫn Commit!)
**Bối cảnh:** Trong `LoadBalancerLabTest`, thiết lập backend xử lý mất `180 ms` (và ghi nhận `completedCommits++` ở cuối hàm), nhưng proxy đặt `readTimeoutMs = 60 ms`.
**Yêu cầu:**
1. Gửi request `POST /transfer` qua proxy.
2. Quan sát: Client nhận HTTP status gì (`504`)? Trong khi đó giá trị `backend.completedCommits()` sau 250 ms là `0` hay `1`?
3. Viết ra quy tắc chỉnh timeout đúng để hiện tượng này không bao giờ tái diễn.

**Tiêu chí hoàn thành:**
- [ ] Chứng minh thực nghiệm: Client nhận `504` nhưng `backend.completedCommits() == 1`!

<details><summary>💡 Hint</summary>

Khi socket của Proxy đóng (hoặc hết `SO_TIMEOUT`) sau `60 ms`, thread trên Backend vẫn đang thực thi `Thread.sleep(180)` / query DB chứ không hề nhận tín hiệu hủy cho tới khi nó chạy xong và thử `out.write(...)` (lúc đó mới ném `Broken pipe` sau khi đã lỡ `COMMIT` DB!).
</details>

---

## 🔴 L3 — Deep

### E9. Ticket Production: `POST /api/v1/pay` trả `504` cho khách nhưng trừ tiền **2 lần** trên 2 node JBoss khác nhau
**Tình huống:**
> Hệ thống thanh toán chạy **Nginx (1 node) → 3 node JBoss AS 7.1.1**.
> Một ngày cao điểm, khách hàng phản ánh bấm thanh toán `1` lần, app mobile nhận lỗi **`504 Gateway Timeout`**, nhưng kiểm tra sổ cái thấy bị **trừ tiền đúng `2` lần** (1 bản ghi ở JBoss-1 lúc `14:02:10`, 1 bản ghi ở JBoss-2 lúc `14:03:11`)!
> Ngoài ra vào `14:05`, dù CPU JBoss chỉ `40%`, thread pool JBoss chạm trần `200/200` busy threads khi một nhóm user mạng 3G tải danh sách lịch sử lớn.
>
> Trích đoạn cấu hình `nginx.conf` lấy được từ server:
> ```nginx
> upstream jboss_pay {
>     server 10.20.1.11:8080;
>     server 10.20.1.12:8080;
>     server 10.20.1.13:8080;
>     keepalive 32;
> }
> location /api/ {
>     proxy_pass http://jboss_pay;
>     proxy_read_timeout 60s;
>     proxy_next_upstream error timeout http_502 http_504 non_idempotent;
>     proxy_buffering off;
> }
> ```
> Còn trên JBoss: JTA transaction timeout là `90s`, downstream Core Banking timeout là `70s`.

**Yêu cầu:**
1. Truy vết chính xác **chuỗi nhân quả** làm 1 request `POST /api/v1/pay` lúc `14:02:10` bị trừ tiền ở JBoss-1 lẫn JBoss-2 (`14:03:11`) và trả `504` về mobile!
2. Chỉ ra **4 lỗi sai nghiêm trọng** trong đoạn config Nginx + JBoss trên (để ý từng dòng: `keepalive`, `proxy_read_timeout`, `proxy_next_upstream`, `proxy_buffering`).
3. Viết lại cấu hình Nginx + quy tắc timeout JBoss + biện pháp chống trùng lặp chuẩn production.

**Tiêu chí hoàn thành:**
- [ ] Giải thích chính xác thời điểm `14:02:10 -> 14:03:10` (`60s` timeout!) -> retry `non_idempotent` sang JBoss-2 -> `60s` nữa trả `504` trong khi cả 2 node đều commit lúc `~65s`!
- [ ] Phát hiện đủ 4 lỗi (`proxy_http_version 1.1` + `Connection ""`, `proxy_read_timeout 60s < 90s`, `non_idempotent` retry `POST`, `proxy_buffering off`).

<details><summary>💡 Hint</summary>

Hãy nhìn mốc thời gian: `14:02:10` đến `14:03:11` chênh nhau **đúng `61 giây`**!
1. JBoss-1 gọi Core Banking chậm mất `65 giây` (< `90s` JBoss timeout).
2. Lúc `14:03:10` (tròn `60s`), `proxy_read_timeout 60s` nổ! Vì config có chữ **`non_idempotent`** trên `proxy_next_upstream ... timeout`, Nginx tự động gửi nguyên bản `POST /api/v1/pay` sang **JBoss-2**!
3. Trong lúc JBoss-2 chạy, JBoss-1 chạy nốt `5s` cuối và commit trừ tiền lần 1; sau đó JBoss-2 cũng timeout/commit trừ tiền lần 2 rồi Nginx trả `504` về mobile!
</details>
