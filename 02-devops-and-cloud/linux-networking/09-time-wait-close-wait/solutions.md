# LNX-09 · Đáp án — TIME_WAIT, CLOSE_WAIT & socket exhaustion

> ⚠️ **Chỉ mở sau khi đã tự làm.** Đọc đáp án mà chưa tự làm = mất 80% giá trị bài tập.
> Mỗi đáp án được bọc trong `<details>` để tránh lộ khi lướt.

---

### E1
<details><summary>Xem đáp án</summary>

**Đáp án:**
Cả nhận định lẫn đề xuất đều **SAI**:
1. **`2.450` socket `CLOSE_WAIT` trên JBoss 100% là lỗi code ứng dụng Java trên chính máy chủ JBoss đó** (quên gọi `socket.close()`, không đóng `InputStream`/`ErrorStream`/`CloseableHttpResponse` trong khối `try-with-resources`/`finally`, hoặc giữ thread treo không nhả kết nối). Đối tác đã gửi `FIN` đóng phía họ rồi.
2. **`net.ipv4.tcp_fin_timeout` hoàn toàn không liên quan tới `CLOSE_WAIT`** (và cũng không chỉnh `TIME_WAIT` — nó chỉ là timeout cho trạng thái `FIN_WAIT_2`!). Không có tham số `sysctl` nào tự động đóng `CLOSE_WAIT` giúp bạn; chỉ có sửa code Java (hoặc tạm thời restart process JBoss để giải phóng FD) mới hết.

**Đáp án sai phổ biến:** Đổ lỗi "mạng chập chờn làm treo `CLOSE_WAIT`" hoặc chỉnh `tcp_fin_timeout = 5`. Sai hoàn toàn về cơ chế state machine TCP.
</details>

---

### E2
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. Dải `32768..60999` có `60999 - 32768 + 1 = 28.232` ephemeral ports.
   Mỗi kết nối đóng từ phía microservice nằm ở `TIME_WAIT` trong **`60 giây`** (`TCP_TIMEWAIT_LEN` trên Linux).
   → Tốc độ bền vững tối đa tới một `(dst_ip, dst_port)` duy nhất là:
   $$\frac{28.232}{60} \approx \mathbf{470.5\text{ req/s}}$$
2. **Không bị chia đôi!** Định danh TCP là **4-tuple** `(src_ip, src_port, dst_ip, dst_port)`. Vì `10.0.5.20:8080` khác hoàn toàn với `203.0.113.55:443`, nên **cùng một `src_port` `40000` có thể dùng đồng thời** để nối tới cả `203.0.113.55:443` lẫn `10.0.5.20:8080`! Mỗi đích đều có đủ `28.232` port riêng.
</details>

---

### E3
<details><summary>Xem đáp án</summary>

**Đáp án:**
Hai lý do bắt buộc của `TIME_WAIT` (`2 × MSL = 60s`):
1. **Bảo vệ gói `ACK` cuối cùng:** Nếu gói `ACK` đáp lại `FIN` bị mất, bên Passive Close ở `LAST_ACK` sẽ gửi lại `FIN`. Nhờ còn giữ state `TIME_WAIT`, Active Close gửi lại `ACK` tử tế thay vì bắn `RST` gây báo lỗi ở đối tác.
2. **Làm sạch "gói tin ma" (*wandering duplicates*):** Chờ đủ `2 × MSL` bảo đảm mọi gói tin trễ của kết nối cũ đã bị loại bỏ khỏi mạng trên cả hai chiều, tránh việc một kết nối mới mở lại đúng 4-tuple đó nhận nhầm dữ liệu cũ.
</details>

---

### E4
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. **`tcp_tw_recycle` hỏng với NAT và đã bị xóa từ Linux 4.12:** Cơ chế của nó lưu timestamp mới nhất theo **từng địa chỉ IP nguồn** chứ không theo từng kết nối. Khi 2 user A và B ngồi sau cùng một Gateway/NAT (cùng IP public), đồng hồ timestamp của A nhanh hơn B → kernel tưởng gói `SYN` của B đến trễ nên vứt bỏ thẳng tay!
2. **Bật `tcp_tw_reuse = 1` KHÔNG giúp được inbound server sockets:** `tcp_tw_reuse` chỉ hoạt động khi kernel **chọn ephemeral port để thực hiện `connect()` đi ra ngoài** (outbound client). Các kết nối đi *vào* port `443` của Nginx do client mở vào, nếu server đóng trước tạo `TIME_WAIT`, `tcp_tw_reuse` trên Nginx không tái sử dụng cho chiều nhận vào; tuy nhiên các client từ nhiều IP khác nhau vốn có 4-tuple khác nhau nên ít khi cạn slot ở inbound trừ khi từ cùng 1 NAT/proxy phía trước.
</details>

---

### E5
<details><summary>Xem đáp án</summary>

**Đáp án:**
Trong `HttpURLConnection`, khi server trả HTTP status `>= 400` (như `500`), lời gọi `conn.getInputStream()` sẽ **ném `IOException` ngay lập tức**, khiến luồng code không bao giờ đi vào thân `try` để đọc hết dữ liệu trả về và stream lỗi thực chất nằm ở **`conn.getErrorStream()`**.
- Nếu không đọc hết và gọi `.close()` trên `conn.getErrorStream()` trong khối `catch`/`finally`, socket underlying **không thể được trả về `KeepAliveCache`** (hoặc giữ nguyên không giải phóng kịp cho đến tận kỳ GC `Cleaner`/`Finalizer`), gây rò rỉ socket / đóng mở lại liên tục và tăng `CLOSE_WAIT` khi đối tác gửi `FIN` trước.
</details>

---

### E6
<details><summary>Xem đáp án</summary>

**Hướng giải:**
Khi chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`:
- Với `200` requests ở chế độ **mở mới không reuse (`No-Pool`)**: server phải `accept()` đủ **`200` kết nối TCP**, tạo hàng loạt socket vòng đời ngắn.
- Với `200` requests qua **`Keep-Alive Pool`**: server chỉ `accept()` đúng **`1`–`2` kết nối TCP** duy nhất và xử lý xong toàn bộ `200` requests nhanh hơn nhiều lần!
- Với kịch bản **`LeakyClient`** (không gọi `close()` sau khi server gửi `FIN`): các socket phía client đều giữ trạng thái mở (trên OS Linux/Windows nằm ở **`CLOSE_WAIT`**) và làm chạm trần giới hạn socket/FD ngay lập tức.
</details>

---

### E7
<details><summary>Xem đáp án</summary>

**Hướng giải:**
- Nguyên tắc cốt lõi:
  1. **Dùng chung 1 instance `HttpClient`** (hoặc connection pool singleton trên toàn app, không tạo mới theo từng request!).
  2. Luôn cấu hình cả `connectTimeout` và `requestTimeout` để tránh thread bị treo kẹt vĩnh viễn khi peer mất mạng.
  3. Tiêu thụ hết body (`BodyHandlers.ofString()` / `discarding()`) ngay cả khi HTTP status là `4xx`/`5xx`, để TCP socket luôn sạch và quay lại pool thay vì bị leak!

**Code lời giải đầy đủ:** xem [lab/solutions/HttpClientPoolSolution.java](./lab/solutions/HttpClientPoolSolution.java).
</details>

---

### E8
<details><summary>Xem đáp án</summary>

**Đáp án:**
- **Trường hợp A (`600 req/s`, `TIME_WAIT = 60s`, `28.232` ports):**
  - Mỗi giây mở và đóng 600 kết nối → 600 port bị giữ trong `TIME_WAIT`.
  - Thời điểm cạn sạch `28.232` ports: `28.232 / 600 = 47.053s` → lỗi **`EADDRNOTAVAIL`** đầu tiên xuất hiện ngay tại **giây thứ `48`** (sau `28.232` requests)!
- **Trường hợp B (`400 req/s` hoặc `Connection Pool 16 sockets`):**
  - Ở `400 req/s`: sau `60s`, tổng số port nằm trong `TIME_WAIT` ổn định ở mức `400 × 60 = 24.000 < 28.232` (vì từ giây thứ 61, mỗi giây có 400 port cũ hết hạn `TIME_WAIT` nhả ra bù cho 400 port mới!) → `exhaustedCount = 0`. *(Tuy nhiên vẫn rất nguy hiểm vì dư địa còn quá ít nếu có spike!)*
  - Với **Connection Pool `16` sockets**: dù chạy ở `5.000 req/s`, số ephemeral port sử dụng **luôn cố định là `16`**, `exhaustedCount = 0`!
</details>

---

### E9
<details><summary>Xem đáp án</summary>

<details open><summary>Phân tích Ticket Production Gateway JBoss: TIME_WAIT + CLOSE_WAIT kép</summary>

**1. Phân tích 2 nguyên nhân tách biệt từ output `ss`:**
- **Chiều outbound (`28.104 TIME-WAIT`):** JBoss (`10.50.1.5`) gọi Core Banking (`10.50.1.10:8443`) nhưng **không dùng HTTP Keep-Alive / Connection Pool** (hoặc gửi `Connection: close`). Vì mỗi request đóng xong giữ 1 ephemeral port trong `TIME_WAIT` 60s, toàn bộ `28.232` ports trong `ip_local_port_range` (`32768..60999`) bị dùng cạn → phát sinh **`BindException: Cannot assign requested address`** lúc `10:15:02`!
- **Chiều inbound (`1.942 CLOSE-WAIT` tại `10.50.1.5:8080`):** Hãy nhìn kỹ cột `Local Address:Port = 10.50.1.5:8080` và `Peer = 10.50.1.2` (Nginx)! Khi JBoss gọi Core Banking bị lỗi/treo chậm, các worker thread JBoss kẹt rất lâu mới xử lý xong. Phía **Nginx (`10.50.1.2`) hết `proxy_read_timeout` nên chủ động đóng kết nối gửi `FIN` về JBoss `:8080`**. Kernel JBoss trả `ACK` đưa socket `:8080` vào `CLOSE_WAIT` để chờ JBoss xử lý xong và đóng response; hoặc do code servlet/JBoss dính exception không nhả socket kịp, làm **`1.942` kết nối inbound treo ở `CLOSE_WAIT`**!

**2. Vì sao `28.104 TIME-WAIT` không làm hết `ulimit -n = 2048`, còn `1.942 CLOSE-WAIT` lại làm nổ `Too many open files`?**
- **`TIME_WAIT` không tốn File Descriptor (FD)** của process vì `close()` đã được gọi rồi — kernel chỉ giữ cấu trúc `inet_timewait_sock` nhỏ gọn trong mạng TCP stack (~200 bytes) để bảo vệ 4-tuple.
- **`CLOSE_WAIT` giữ chết 1 FD thật** trong bảng FD của PID `4120` (`fd=512, 513...`). `1.942` socket `CLOSE_WAIT` + `180` socket `ESTAB` + các file `.jar`/`.log` của JBoss vượt quá trần **`ulimit -n = 2048`** → lúc `10:22:10` bùng lỗi `Too many open files`!

**3. Kế hoạch khắc phục triệt để:**
1. **Sửa chiều gọi Core Banking (triệt tiêu `TIME_WAIT` exhaustion):** Bật **HTTP Connection Pool** (ví dụ Apache `PoolingHttpClientConnectionManager` trên Java 7, tái sử dụng `Keep-Alive`, `maxPerRoute = 50–100`), thiết lập chặt `connectTimeout` (vd `2s`) và `socketTimeout` (vd `5s`) để worker JBoss không bị treo dây chuyền khi Core Banking chậm!
2. **Sửa chiều inbound JBoss + Circuit Breaker:** Đặt `timeout` cho cuộc gọi downstream ngắn hơn `proxy_read_timeout` của Nginx; bảo đảm servlet/filter luôn đóng stream an toàn trong `finally`; cân nhắc bulkhead/circuit breaker ([PAT-01](../../../01-software-engineering/patterns/circuit-breaker.md)) khi Core Banking lỗi.
3. **Tuning Linux OS:** Nâng `LimitNOFILE=65536` cho service JBoss (xem [LNX-02](../02-file-descriptors-io/README.md) & [LNX-04](../04-systemd-services/README.md)); mở rộng `net.ipv4.ip_local_port_range="1024 65000"` và bật Nginx upstream `keepalive`. Không dùng `tcp_tw_recycle`.

</details>
</details>

---

## Self-check (từ README §9) — đáp án gợi ý
<details><summary>Xem</summary>

1. **2 lý do của `TIME_WAIT` (`60s` trên Linux):** (a) Bảo đảm gói `ACK` cuối cùng tới được bên Passive Close (nếu mất, còn nhận lại `FIN` retransmit để trả `ACK` thay vì bắn `RST`); (b) Chờ đủ `2 × MSL` cho mọi gói trễ của kết nối cũ tan biến khỏi mạng trước khi tái sử dụng 4-tuple. Nằm ở **bên Active Close** (bên gọi `close()` trước).
2. **Công thức `~470 req/s`:** Dải ephemeral port mặc định `32768..60999` = `28.232` ports. Gọi tới 1 `(dst_ip, dst_port)` cố định thì 3 thành phần còn lại của 4-tuple không đổi. Nếu đóng sau mỗi request, mỗi `src_port` kẹt `60s` ở `TIME_WAIT` → trần `28.232 / 60 = 470.5 req/s`. Vượt ngưỡng này sẽ dính `EADDRNOTAVAIL`.
3. **3.000 `CLOSE_WAIT` là lỗi của ai?** 100% lỗi code ứng dụng tại chính host đang hiện `CLOSE_WAIT` (quên gọi `close()` / không đóng stream lỗi). TCP không có timeout tự dọn `CLOSE_WAIT` vì vẫn chờ app đóng chiều ghi còn lại.
4. **`tcp_fin_timeout` vs `tcp_tw_recycle`:** `tcp_fin_timeout` chỉ chỉnh timeout cho **`FIN_WAIT_2`**, không tác dụng với `TIME_WAIT` (`60s` hardcode). `tcp_tw_recycle` bị xóa từ Linux `4.12` vì phá vỡ kết nối từ các client đứng sau cùng một NAT IP do kiểm tra timestamp theo per-IP.
5. **Cấu hình HTTP Connection Pool chuẩn:** Dùng singleton pool tái sử dụng socket (`Keep-Alive`), đặt `connectTimeout` + `socketTimeout`, giới hạn `maxPerRoute` / `maxTotal`, và đặt `idleTimeout` (vd `30s–45s`) **ngắn hơn** idle timeout của ALB/Nginx (`65s`) để tránh bốc phải stale socket.
</details>
