# LNX-08 · Bài tập — TCP: handshake, state machine, backlog

> Làm theo thứ tự. **Tự làm trước, chỉ mở hint khi đã thử ≥ 15 phút.** Đáp án ở [solutions.md](./solutions.md) — chỉ mở sau khi đã có câu trả lời của riêng bạn.
> Ghi thời gian làm từng bài vào `my-notes.md` để theo dõi tiến bộ.

| Cấp | Mục đích | Số lượng | Thời gian gợi ý |
| :--- | :--- | :---: | :---: |
| 🟢 L1 — Recall | Nhớ & hiểu handshake, Accept Queue, Keep-Alive, Nagle + Delayed ACK | 5 | 20' |
| 🟡 L2 — Apply | Benchmark Keep-Alive vs New Connection & kiểm chứng State Machine | 3 | 75' |
| 🔴 L3 — Deep | Ticket burst traffic 08:30 gây timeout trên JBoss dù CPU 25% | 1 | 90' |

---

## 🟢 L1 — Recall

### E1. Vì sao server `new ServerSocket(8080)` truyền backlog = 1000 nhưng `ss -lnt` vẫn chỉ báo `Send-Q = 128`?
Trên một server Linux kernel `4.18` (RHEL 8 cũ / CentOS 7), bạn sửa code Java sang `new ServerSocket(8080, 1000)`, khởi động lại app rồi chạy `ss -lnt` kiểm tra, nhưng cột `Send-Q` vẫn hiện đúng `128`. Giải thích lý do ở tầng kernel và lệnh sửa.

---

### E2. Client thấy gì khi Accept Queue tràn ở `tcp_abort_on_overflow = 0` vs `= 1`?
Một backend quá tải làm Accept Queue đầy (`Recv-Q == Send-Q`). Ở phía client Java gọi tới server đó, hành vi lỗi quan sát được khác nhau ra sao giữa `tcp_abort_on_overflow = 0` và `tcp_abort_on_overflow = 1`?

---

### E3. Phân biệt `TIME_WAIT` và `CLOSE_WAIT` khi đóng kết nối TCP
Trong 4-way teardown giữa `Client` và `JBoss`, nếu phía `Client` gọi `socket.close()` trước, bên nào kết thúc ở trạng thái **`TIME_WAIT`** và bên nào nằm ở **`CLOSE_WAIT`** (nếu quên đóng)? Giải thích vì sao một server thấy hàng nghìn `CLOSE_WAIT` gần như chắc chắn là lỗi code ứng dụng chứ không phải lỗi mạng.

---

### E4. Hiện tượng tốc độ kẹt ở đúng `25 req/s` khi không bật `TCP_NODELAY`
Một team viết giao thức TCP nhị phân nội bộ: mỗi request gửi 2 lần (`out.write(header4Bytes); out.write(payload30Bytes);`) rồi gọi `in.read()` chờ server trả lời. Server là Linux với RTT mạng LAN `< 0.3 ms`, nhưng benchmark 1 kết nối chỉ đạt tối đa **`25 req/s`** (`~40 ms/request`). Giải thích chính xác sự tương tác giữa **Nagle** và **Delayed ACK** gây ra con số `40 ms` này và 2 cách sửa.

---

### E5. Tính toán lợi ích Latency của HTTP Keep-Alive trên mạng WAN
Một microservice tại Singapore gọi API đối tác tại Tokyo (`1 RTT = 70 ms`, chạy HTTPS/TLS 1.2 tốn 2 RTT TLS handshake, payload request/response nhỏ nằm gọn trong 14 KB). Tính độ trễ tối thiểu trước khi nhận được byte response đầu tiên cho 2 trường hợp:
1. Mở kết nối TCP + TLS mới cho mỗi request.
2. Tái sử dụng kết nối đang mở sẵn qua **HTTP Keep-Alive / Connection Pool**.

---

## 🟡 L2 — Apply

### E6. Benchmark thực tế: Keep-Alive Reuse vs New TCP Connection per Request
**Bối cảnh:** Dùng [lab/](./lab/README.md) chạy `TcpLabRunner` và `TcpHandshakeLabTest`.
**Yêu cầu:**
1. Chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`.
2. Ghi lại thời gian hoàn thành `200` requests liên tiếp giữa chế độ **Mở kết nối mới mỗi request** vs **Keep-Alive Reuse** ngay trên loopback `127.0.0.1` (đo trực tiếp trên máy bạn).
3. Giải thích vì sao dù loopback có độ trễ vật lý gần `0 ms`, việc mở/đóng TCP connection liên tục vẫn chậm hơn nhiều lần so với giữ kết nối sống.

**Tiêu chí hoàn thành:**
- [ ] `mvn -o -B verify` pass 100%.
- [ ] Có bảng so sánh ms giữa `new-connection-per-request` và `keep-alive` từ kết quả chạy thật của `TcpLabRunner`.

<details><summary>💡 Hint</summary>

Mỗi lần mở mới một TCP socket trên OS tốn syscall `socket()` + `connect()` + 3-way handshake (`SYN`, `SYN-ACK`, `ACK`) + cấp phát socket buffer/FD mới + `close()` 4-way teardown (`TIME_WAIT`)!
</details>

---

### E7. Mô phỏng State Machine & Accept Queue Overflow trong `TcpStateMachine`
**Bối cảnh:** Mở `lab/src/main/java/lab/linux/tcp/TcpStateMachine.java` và `TcpHandshakeLabTest.java`.
**Yêu cầu:**
1. Kiểm chứng các bước chuyển trạng thái hợp lệ của bên `CLIENT` và `SERVER` trong 3-way handshake và 4-way teardown.
2. Viết (hoặc đối chiếu với `lab/solutions/TcpDropAndRetransmitTest.java`) test kiểm chứng kịch bản:
   - Accept Queue capacity = `2`, đã có `2` kết nối chờ `accept()`.
   - Kết nối thứ `3` gửi `SYN` → server giữ ở `SYN_RCVD` (trong SYN Queue), sau đó client gửi `ACK` cuối:
     - Nếu `tcpAbortOnOverflow = false` → server drop `ACK` (không vào Accept Queue), client vẫn nghĩ mình `ESTABLISHED`!
     - Nếu `tcpAbortOnOverflow = true` → server bắn `RST`, client nhận `RESET`.

**Tiêu chí hoàn thành:**
- [ ] Pass toàn bộ assertion kiểm chứng 3-way handshake, `TIME_WAIT`/`CLOSE_WAIT`, và 2 chế độ Accept Queue overflow.

<details><summary>💡 Hint</summary>

Xem hàm `TcpStateMachine.ServerListener#onAckFromClient(...)`: khi `acceptQueue.size() >= acceptQueueCapacity`, nó kiểm tra cờ `abortOnOverflow` để quyết định drop `ACK` hay phát `RST`!
</details>

---

### E8. Đọc phân tích gói tin `tcpdump` mẫu (3-way handshake, `PSH+ACK`, `FIN`, `RST`)
**Bối cảnh:** Cho một đoạn log `tcpdump -nn -S` bắt tại port `8080` (chỉ hiển thị cờ TCP `[Flags]`):
```text
10:00:01.000100 IP 10.0.1.10.52104 > 10.0.2.20.8080: Flags [S], seq 1000, win 64240, options [mss 1460], length 0
10:00:01.000450 IP 10.0.2.20.8080 > 10.0.1.10.52104: Flags [S.], seq 5000, ack 1001, win 65160, options [mss 1460], length 0
10:00:01.000600 IP 10.0.1.10.52104 > 10.0.2.20.8080: Flags [.], ack 5001, length 0
10:00:01.000800 IP 10.0.1.10.52104 > 10.0.2.20.8080: Flags [P.], seq 1001:1085, ack 5001, length 84
10:00:01.012100 IP 10.0.2.20.8080 > 10.0.1.10.52104: Flags [P.], seq 5001:5221, ack 1085, length 220
10:00:01.012300 IP 10.0.1.10.52104 > 10.0.2.20.8080: Flags [F.], seq 1085, ack 5221, length 0
10:00:01.012500 IP 10.0.2.20.8080 > 10.0.1.10.52104: Flags [R.], seq 5221, ack 1086, length 0
```
**Yêu cầu:**
1. Xác định RTT mạng giữa `10.0.1.10` và `10.0.2.20`, và thời gian xử lý của server từ lúc nhận 84 bytes request tới lúc trả 220 bytes response.
2. Cho biết dòng cuối `Flags [R.]` (`RST+ACK`) sau khi client gửi `FIN` (`Flags [F.]`) thường xảy ra khi nào và cờ `[S.]`, `[P.]`, `[.]` nghĩa là gì?

**Tiêu chí hoàn thành:**
- [ ] Tính đúng RTT từ handshake (`~0.5 ms`: `0.000450 - 0.000100 = 0.35 ms` / tổng handshake `0.5 ms`) và server processing time (`~11.3 ms`).
- [ ] Giải thích chính xác ý nghĩa `[S]` (`SYN`), `[S.]` (`SYN+ACK`), `[.]` (`ACK`), `[P.]` (`PSH+ACK`), `[F.]` (`FIN+ACK`), `[R.]` (`RST+ACK`).

<details><summary>💡 Hint</summary>

Trong ký hiệu của `tcpdump`, dấu chấm `.` đại diện cho cờ `ACK`! Vậy `[S.]` = `SYN + ACK`, `[P.]` = `PSH + ACK` (đẩy payload lên app ngay), `[F.]` = `FIN + ACK`, `[R.]` = `RST + ACK`.
</details>

---

## 🔴 L3 — Deep

### E9. Ticket Production: Cơn bão đăng nhập 08:30 gây timeout hàng loạt dù CPU JBoss chỉ 25%
**Tình huống:**
> Sáng thứ Hai lúc `08:30`, `Nginx` proxy (`upstream` tới `JBoss AS 7.1.1` port `8080` trên Linux kernel `4.18`) ghi nhận hàng trăm log:
> `upstream timed out (110: Connection timed out) while connecting/reading response from upstream 10.10.2.15:8080`
> Khi bạn kiểm tra nhanh trên server JBoss lúc `08:31`:
> ```text
> $ cat /proc/sys/net/core/somaxconn
> 128
>
> $ ss -lnt 'sport = :8080'
> State      Recv-Q  Send-Q   Local Address:Port   Peer Address:Port
> LISTEN     129     128      0.0.0.0:8080         0.0.0.0:*
>
> $ nstat -az TcpExtListenOverflows TcpExtListenDrops
> TcpExtListenOverflows           4821               0.0
> TcpExtListenDrops               4821               0.0
> ```
> Ngoài ra, kiểm tra `gc.log` của JBoss lúc `08:30:12` thấy có **1 lần Full GC pause kéo dài 2.8 giây**, và `standalone.xml` đang để cấu hình mặc định HTTP connector (`backlog` chưa set → mặc định JBoss Web là `100`, `max-connections="200"`).

**Yêu cầu:**
1. Giải thích **cơ chế đầy đủ** nối liền từ sự kiện **Full GC 2.8s trên JBoss** → **`Send-Q = 128`, `Recv-Q = 129`** → **`TcpExtListenOverflows = 4821`** → lỗi timeout trên Nginx!
2. Tại sao sau khi Full GC kết thúc lúc `08:30:15`, hàng loạt user mới vẫn bị chậm đúng `1s` hoặc `3s` trong vài giây tiếp theo?
3. Đề xuất **4 bước sửa dứt điểm** ở cả 3 tầng: Linux sysctl (`somaxconn`, `tcp_max_syn_backlog`), JBoss AS 7.1.1 (`backlog`, tuning GC tránh Full GC pause dài), và Nginx (`keepalive` upstream tới JBoss để giảm số handshake mới lúc 08:30!).

**Tiêu chí hoàn thành:**
- [ ] Giải thích chính xác `Recv-Q (129) > Send-Q (128)` nghĩa là Accept Queue đã tràn sạch vì JBoss dừng trong 2.8s Full GC không gọi `accept()`, và `tcp_abort_on_overflow=0` khiến kernel drop `ACK` gây `TcpExtListenOverflows = 4821` và Nginx gặp timeout / `SYN-ACK` backoff `1s/2s/4s`.
- [ ] Đưa ra cấu hình cụ thể ở tầng Linux (`net.core.somaxconn=4096`), JBoss (`backlog` / giảm Full GC), và Nginx (`keepalive 64;` + `proxy_http_version 1.1;`).

<details><summary>💡 Hint</summary>

1. Trong 2.8s Full GC (STW), mọi thread của JBoss kể cả `Acceptor` thread đều đóng băng — không ai gọi `accept()` lấy socket ra khỏi Accept Queue!
2. Với tải ~1.700 kết nối mới/giây (hoặc vài trăm kết nối burst) đổ vào Accept Queue chỉ có `min(100..128, 128)` chỗ, hàng đợi đầy trong chưa đầy `0.1 giây`!
3. Bật `keepalive` giữa Nginx ↔ JBoss (`proxy_http_version 1.1; proxy_set_header Connection "";`) tái sử dụng các socket đã `ESTABLISHED` sẵn thay vì phải đi qua Accept Queue 3-way handshake lại từ đầu ở mỗi HTTP request!
</details>
