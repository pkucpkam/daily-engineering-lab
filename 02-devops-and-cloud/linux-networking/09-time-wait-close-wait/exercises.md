# LNX-09 · Bài tập — TIME_WAIT, CLOSE_WAIT & socket exhaustion

> Làm theo thứ tự. **Tự làm trước, chỉ mở hint khi đã thử ≥ 15 phút.** Đáp án ở [solutions.md](./solutions.md) — chỉ mở sau khi đã có câu trả lời của riêng bạn.
> Ghi thời gian làm từng bài vào `my-notes.md` để theo dõi tiến bộ.

| Cấp | Mục đích | Số lượng | Thời gian gợi ý |
| :--- | :--- | :---: | :---: |
| 🟢 L1 — Recall | Phân biệt `TIME_WAIT` vs `CLOSE_WAIT`, tính toán ephemeral port & 4-tuple | 5 | 20' |
| 🟡 L2 — Apply | Tái hiện rò rỉ socket / Keep-Alive pool trên loopback & tính cạn port | 3 | 75' |
| 🔴 L3 — Deep | Điều tra ticket `Cannot assign requested address` + `CLOSE_WAIT` trên JBoss | 1 | 90' |

---

## 🟢 L1 — Recall

### E1. Hàng nghìn `CLOSE_WAIT` trên JBoss là lỗi của ai?
Đội vận hành báo trên máy chủ JBoss đang có **`2.450` socket ở trạng thái `CLOSE_WAIT`**, còn phía API đối tác báo không thấy bất thường gì. Có người đề xuất tăng `net.ipv4.tcp_fin_timeout = 5` để kernel dọn bớt `CLOSE_WAIT` nhanh hơn. Nhận định và đề xuất này đúng hay sai? Giải thích cơ chế kernel.

---

### E2. Tính toán giới hạn RPS trước khi hết ephemeral port
Một microservice trên Linux giữ cấu hình mặc định `net.ipv4.ip_local_port_range = 32768 60999` và gọi sang 1 cổng thanh toán duy nhất tại `203.0.113.55:443`.
1. Mỗi request gửi xong, microservice đóng kết nối ngay (`Connection: close`). Tốc độ tối đa bền vững là bao nhiêu `req/s` trước khi xảy ra `EADDRNOTAVAIL`?
2. Nếu microservice này đồng thời gọi thêm 1 service thứ 2 ở `10.0.5.20:8080`, số ephemeral port cho mỗi đích có bị chia đôi thành `14.116` không? Tại sao?

---

### E3. Phân biệt 2 mục đích của `TIME_WAIT` (`2 × MSL`)
Vì sao RFC 9293 không cho phép bên Active Close chuyển thẳng từ `FIN_WAIT_2` về `CLOSED` sau khi gửi `ACK` cuối cùng, mà bắt buộc đứng chờ **`60 giây`** (`2 × MSL` trên Linux) ở `TIME_WAIT`?

---

### E4. Vì sao `tcp_tw_recycle` bị xóa còn `tcp_tw_reuse` chỉ dùng cho client?
Giải thích ngắn gọn:
1. Tại sao cờ `net.ipv4.tcp_tw_recycle` từng gây rớt mạng hàng loạt cho user sau NAT và bị xóa từ Linux kernel `4.12`?
2. Nếu một server Nginx chỉ *nhận* kết nối từ Internet vào port `443` (server là bên passive hay active tùy rule) và có `30.000 TIME_WAIT` với nhiều client IP khác nhau, bật `tcp_tw_reuse = 1` có giúp được inbound traffic không?

---

### E5. Bẫy `HttpURLConnection` khi server trả HTTP 500
Một đoạn code Java 7 gọi API nội bộ bằng `HttpURLConnection` trong khối `try (InputStream is = conn.getInputStream()) { ... }`. Tại sao khi API đối tác bình thường (`200 OK`) hệ thống chạy ổn định, nhưng khi đối tác lỗi `500 Internal Server Error` liên tục 2 giờ thì JBoss bị treo / rò rỉ kết nối hoặc không tái sử dụng được `KeepAliveCache`?

---

## 🟡 L2 — Apply

### E6. Tái hiện rò rỉ socket vs HTTP Keep-Alive Pooling trên loopback TCP
**Bối cảnh:** Dùng [lab/](./lab/README.md) chạy `SocketExhaustionLabTest` và `SocketLabRunner`.
**Yêu cầu:**
1. Chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`.
2. Quan sát sự khác biệt số lượng kết nối TCP mà server phải `accept()` khi chạy `200` requests ở 2 chế độ:
   - Mở mới / không tái sử dụng kết nối (`Connection: close`).
   - Tái sử dụng kết nối qua HTTP Connection Pool (`Keep-Alive`).
3. Quan sát kịch bản client quên `close()` sau khi server đã gửi `FIN` (`LeakyClient`): số lượng open sockets phía client tích tụ bao nhiêu và gây lỗi gì khi vượt ngưỡng pool/FD?

**Tiêu chí hoàn thành:**
- [ ] `mvn -o -B verify` pass 100%.
- [ ] Có bảng đối chiếu số `serverAcceptedConnections` và thời gian chạy giữa `No-Pool` vs `Keep-Alive Pool`.

<details><summary>💡 Hint</summary>

Xem `SocketExhaustionDemo.java`: server đếm bằng `AtomicInteger acceptedCount` mỗi lần `serverSocket.accept()` trả về một kết nối mới! Khi client dùng HTTP Keep-Alive pool `1` hoặc `2` connections, `200` HTTP requests chỉ tốn `1–2` lần `accept()`.
</details>

---

### E7. Viết Wrapper / Connection Pool an toàn đóng sạch Stream & tái sử dụng Socket
**Bối cảnh:** Mở `lab/src/main/java/lab/linux/sockets/SocketExhaustionDemo.java` và đối chiếu với `lab/solutions/HttpClientPoolSolution.java`.
**Yêu cầu:**
1. Hãy phân tích lỗ hổng rò rỉ trong `LeakyClient` (giữ kết nối mở sau khi nhận `FIN` từ server → nằm ở `CLOSE_WAIT` trên OS) và viết/chạy hàm gửi HTTP request tái sử dụng kết nối (dùng `java.net.http.HttpClient` hoặc `try-with-resources` + bounded pool), bảo đảm:
   - Không rò rỉ bất kỳ socket nào sau khi hoàn thành `100` request thành công **lẫn** request trả HTTP `500`.
   - Tổng số kết nối TCP server chấp nhận cho `100` request tuần tự `<= 4`.

**Tiêu chí hoàn thành:**
- [ ] Code không rò rỉ FD/socket cả khi server trả HTTP `200` lẫn `500`.
- [ ] Pass bài kiểm chứng tương đương `HttpClientPoolSolution.java`.

<details><summary>💡 Hint</summary>

Với `java.net.http.HttpClient`, sử dụng một instance singleton dùng chung (đừng tạo `HttpClient.newHttpClient()` mới trong mỗi vòng lặp!), luôn set `connectTimeout` và `request.timeout()`, và dùng `BodyHandlers.ofString()` hoặc `discarding()` để JDK tự quản lý vòng đời connection pool và trả socket sạch về pool.
</details>

---

### E8. Tính toán Ephemeral Port Exhaustion với `EphemeralPortBudgetSimulator`
**Bối cảnh:** Mở `lab/src/main/java/lab/linux/sockets/EphemeralPortBudgetSimulator.java`.
**Yêu cầu:**
1. Chạy mô phỏng với `portRange = 32768..60999` (`28.232` ports), `timeWaitSeconds = 60`:
   - Trường hợp A: Mở kết nối mới tới cùng `10.20.1.50:443` ở tốc độ **`600 req/s`**. Hỏi sau đúng bao nhiêu giây thì xảy ra **`EADDRNOTAVAIL`** đầu tiên?
   - Trường hợp B: Hạ tốc độ xuống **`400 req/s`** hoặc chuyển sang **Connection Pool 16 sockets**. Có bị cạn port sau `120 giây` không?
2. Giải thích kết quả dựa trên định nghĩa 4-tuple trong Linux kernel.

**Tiêu chí hoàn thành:**
- [ ] Ghi lại chính xác thời điểm (giây thứ mấy) lỗi `EADDRNOTAVAIL` xuất hiện ở Trường hợp A.
- [ ] Chứng minh được Trường hợp B có `exhaustedCount == 0`.

<details><summary>💡 Hint</summary>

Ở tốc độ `600 req/s`, mỗi giây tiêu tốn `600` cổng mới và trong `60` giây đầu chưa có cổng nào hết hạn `TIME_WAIT`! Vậy chạm `28.232` ports ở khoảng giây `28232 / 600 ≈ 47.05` (giây thứ `48`).
</details>

---

## 🔴 L3 — Deep

### E9. Ticket Production: Gateway JBoss báo song song `Cannot assign requested address` VÀ `Too many open files`
**Tình huống:**
> Một service trung gian **JBoss AS 7.1.1** (trên RHEL, `ulimit -n = 2048`, `ip_local_port_range = 32768 60999`) vừa gọi sang **Core Banking HTTP API** (`10.50.1.10:8443`), vừa nhận request từ Nginx (`:8080`).
> Lúc `10:15`, hệ thống bùng phát 2 nhóm lỗi nối tiếp nhau trong log:
> - `10:15:02`: `java.net.NoRouteToHostException / BindException: Cannot assign requested address` khi gọi `10.50.1.10:8443`.
> - `10:22:10`: `java.io.IOException: Too many open files` trên port `8080` làm JBoss chết hẳn phần nhận request từ Nginx!
>
> Snapshot lệnh `ss` lấy lúc `10:21` trước khi restart JBoss:
> ```text
> $ ss -s
> TCP: 31420 (estab 1890, closed 28110, orphaned 0, timewait 28104)
>
> $ ss -tan | awk 'NR>1 {print $1}' | sort | uniq -c | sort -nr
>   28104 TIME-WAIT
>    1942 CLOSE-WAIT
>     180 ESTAB
>
> $ ss -tanp state close-wait | head -n 4
> Recv-Q Send-Q Local Address:Port  Peer Address:Port  Process
> 0      0      10.50.1.5:8080      10.50.1.2:49102    users:(("java",pid=4120,fd=512))
> 0      0      10.50.1.5:8080      10.50.1.2:49108    users:(("java",pid=4120,fd=513))
> ```

**Yêu cầu:**
1. Đưa ra **2 giả thuyết tách biệt** giải thích cho `28.104 TIME-WAIT` và `1.942 CLOSE-WAIT` (để ý kỹ cột `Local Address:Port` ở bảng `CLOSE-WAIT`!).
2. Giải thích vì sao `28.104 TIME-WAIT` *không* làm hết `ulimit -n = 2048`, nhưng `1.942 CLOSE-WAIT` lại làm nổ `Too many open files` lúc `10:22`!
3. Đề xuất kế hoạch khắc phục triệt để cho cả 2 nguyên nhân (Linux OS + JBoss/Java 7 + Nginx upstream).

**Tiêu chí hoàn thành:**
- [ ] Xác định đúng chiều kết nối của từng trạng thái (`10.50.1.5 -> 10.50.1.10:8443` vs `10.50.1.2 -> 10.50.1.5:8080`).
- [ ] Giải thích chính xác quan hệ giữa `CLOSE_WAIT` với `ulimit -n = 2048` vs `TIME_WAIT` với `ip_local_port_range`.
- [ ] Có giải pháp sửa rõ ràng không dùng mẹo bậy (`tcp_tw_recycle`).

<details><summary>💡 Hint</summary>

Nhìn cột `Local Address:Port` của `CLOSE-WAIT`: đó là **`10.50.1.5:8080`** (cổng server JBoss tiếp nhận từ Nginx `10.50.1.2`)! Điều này có nghĩa là khi JBoss gọi Core Banking bị nghẽn/chậm, Nginx đã hết `proxy_read_timeout` nên đóng kết nối trước (`FIN` tới `:8080`), nhưng thread JBoss đang bị kẹt hoặc xử lý lỗi sai nên **chưa trả/đóng socket `:8080`** → `CLOSE_WAIT` phình tới `1.942`, cộng với các FD log/jar/file khác vượt `2048`! Còn `28.104 TIME-WAIT` là ở chiều JBoss gọi ra Core Banking `:8443` không dùng Keep-Alive/Connection Pool!
</details>
