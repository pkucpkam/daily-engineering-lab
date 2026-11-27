# LNX-08 · Đáp án — TCP: handshake, state machine, backlog

> ⚠️ **Chỉ mở sau sau khi đã tự làm.** Đọc đáp án mà chưa tự làm = mất 80% giá trị bài tập.
> Mỗi đáp án được bọc trong `<details>` để tránh lộ khi lướt.

---

### E1
<details><summary>Xem đáp án</summary>

**Đáp án:**
Trong Linux kernel, kích thước tối đa của **Accept Queue** được tính bằng:
$$\text{Accept Queue Max} = \min(\text{application } \texttt{backlog},\; \texttt{net.core.somaxconn})$$
Trên kernel `4.18` cũ, giá trị mặc định của `net.core.somaxconn` là **`128`**. Do đó dù Java truyền `backlog = 1000`, kernel vẫn âm thầm cắt trần xuống `128` (`Send-Q = 128` trong `ss -lnt`)!
**Cách sửa:** Chạy `sudo sysctl -w net.core.somaxconn=4096` (và lưu vĩnh viễn vào `/etc/sysctl.d/99-network.conf`), sau đó khởi động lại ứng dụng Java để lệnh `listen()` áp dụng backlog mới.
</details>

---

### E2
<details><summary>Xem đáp án</summary>

**Đáp án:**
- **Khi `tcp_abort_on_overflow = 0` (mặc định Linux):** Kernel **âm thầm vứt bỏ gói `ACK`** thứ 3 của client trong 3-way handshake và giữ socket ở `SYN_RCVD`. Phía client Java nghĩ mình đã `ESTABLISHED` nên bắt đầu gửi dữ liệu (HTTP/SQL) rồi **treo chờ** tới khi hết `SocketTimeoutException` (hoặc nhận `SYN-ACK` gửi lại theo backoff `1s, 2s, 4s...` gây latency spike đột biến).
- **Khi `tcp_abort_on_overflow = 1`:** Ngay khi nhận `ACK` thứ 3 mà Accept Queue đầy, server gửi thẳng gói **`RST`**, khiến client nhận lỗi tức thì **`java.net.SocketException: Connection reset`** (fail fast).
</details>

---

### E3
<details><summary>Xem đáp án</summary>

**Đáp án:**
- Bên **chủ động đóng trước (`Client`)** đi qua `FIN_WAIT_1 → FIN_WAIT_2 → TIME_WAIT` (giữ trong `2 × MSL = 60s` trên Linux).
- Bên **bị động đóng (`JBoss`)** nhận `FIN`, trả `ACK`, rồi chuyển vào **`CLOSE_WAIT`** để chờ code Java bên trong JBoss gọi `socket.close()`.
- Nếu thấy hàng nghìn **`CLOSE_WAIT`** trên JBoss, gần như 100% là **lỗi ứng dụng Java quên gọi `.close()`** (bên kia đã đóng xong rồi, kernel đã báo sẵn sàng đóng, chỉ còn đợi code Java thả FD ra mà thôi — xem [LNX-09](../09-time-wait-close-wait/README.md) & [INC-12](../../../03-production-troubleshooting/incident-labs/12-close-wait-leak/)).

**Đáp án sai phổ biến:** Nghĩ `TIME_WAIT` và `CLOSE_WAIT` đều do mạng chậm → Sai; `TIME_WAIT` là cơ chế bảo vệ bình thường ở bên chủ động đóng, còn `CLOSE_WAIT` tích tụ là bug leak socket của code app tại máy đang hiển thị `CLOSE_WAIT`.
</details>

---

### E4
<details><summary>Xem đáp án</summary>

**Đáp án:**
Đây là tương tác kinh điển giữa **Nagle** (bên gửi) và **TCP Delayed ACK** (bên nhận):
1. `out.write(header4Bytes)` gửi gói 4 byte sang server Linux. Server nhận xong, theo **Delayed ACK**, giữ lại tối đa **`40 ms`** để chờ xem có response gửi kèm không.
2. Ngay sau đó app gọi `out.write(payload30Bytes)` — nhưng **Nagle** thấy gói 4 byte trước đó **chưa được ACK** nên giữ gói 30 byte lại chưa gửi!
3. Phía app chuyển sang `in.read()` đợi server trả lời, phía server đợi hết **`40 ms`** mới gửi ACK → Nagle mới nhả gói 30 byte → server mới xử lý xong! Vì mỗi request mất cố định `40 ms`, 1 kết nối tuần tự kẹt ở `1000 / 40 = 25 req/s`.
- **2 cách sửa:** (1) Bật **`socket.setTcpNoDelay(true)`** để tắt Nagle; và (2) Gói cả header + payload vào chung 1 buffer (`BufferedOutputStream` hoặc `ByteBuffer`) rồi gửi trong **1 lần `write()` / `flush()`** duy nhất.
</details>

---

### E5
<details><summary>Xem đáp án</summary>

**Đáp án:**
- **1. Mở kết nối mới mỗi request:**
  - TCP 3-way handshake: `1 RTT = 70 ms` (SYN → SYN-ACK → ACK + request gửi kèm ngay sau SYN-ACK).
  - TLS 1.2 handshake: thêm `2 RTT = 140 ms` (TLS 1.3 tốn `1 RTT = 70 ms`).
  - Gửi HTTP request & chờ byte response đầu: `+ 1 RTT = 70 ms` (+ thời gian xử lý server).
  - → Tổng trễ mạng tối thiểu: **`4 RTT = 280 ms`** (với TLS 1.2) hoặc `3 RTT = 210 ms` (với TLS 1.3), chưa tính thời gian app xử lý!
- **2. Dùng HTTP Keep-Alive / Connection Pool:**
  - Kết nối đã handshake TCP + TLS sẵn và `cwnd` đã mở → gửi ngay HTTP request và nhận response trong **`1 RTT = 70 ms`**!
  - Tiết kiệm được **`140–210 ms`** trên *mỗi một request*!
</details>

---

### E6
<details><summary>Xem đáp án</summary>

**Hướng giải:**
- Khi chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`, `TcpLabRunner` đo trên loopback thấy `200` request qua Keep-Alive nhanh hơn nhiều lần so với mở mới TCP connection mỗi request (thường gấp `5×–15×` trên loopback Windows).
- Lý do: mở kết nối mới mỗi lần buộc OS chạy toàn bộ quy trình cấp phát FD, tìm ephemeral port, 3-way handshake, tạo socket buffer kernel, và 4-way teardown đẩy socket cũ vào `TIME_WAIT`; còn Keep-Alive chỉ tái sử dụng thẳng buffer đã sẵn sàng `ESTABLISHED`.
</details>

---

### E7
<details><summary>Xem đáp án</summary>

**Hướng giải:**
- Trong `TcpStateMachine`, khi Accept Queue đạt sức chứa (`2`), gói `ACK` của kết nối thứ 3 không thể chuyển vào Accept Queue:
  - Với `abortOnOverflow = false`: server giữ nguyên trong SYN Queue (`SYN_RCVD`) và drop `ACK`; phía client đã nhận `SYN-ACK` trước đó nên tưởng kết nối đã `ESTABLISHED`!
  - Với `abortOnOverflow = true`: server hủy kết nối bằng `RST`, client chuyển sang `RESET`.

**Code kiểm chứng đầy đủ cho E7:** xem [lab/solutions/TcpDropAndRetransmitTest.java](./lab/solutions/TcpDropAndRetransmitTest.java).
</details>

---

### E8
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. **RTT mạng & thời gian xử lý:**
   - Gói `SYN` gửi lúc `10:00:01.000100`, nhận `SYN-ACK` lúc `10:00:01.000450` → **RTT mạng ≈ `0.35 ms`** (rất nhanh, cùng LAN).
   - Server nhận request `84 bytes` lúc `10:00:01.000800` và gửi response `220 bytes` lúc `10:00:01.012100` → **Thời gian server xử lý ≈ `11.3 ms`**.
2. **Ý nghĩa các cờ `tcpdump` & `Flags [R.]` cuối:**
   - `[S]` = `SYN`, `[S.]` = `SYN + ACK`, `[.]` = `ACK`, `[P.]` = `PSH + ACK` (yêu cầu đẩy dữ liệu lên app ngay), `[F.]` = `FIN + ACK`, `[R.]` = `RST + ACK`.
   - Dòng `Flags [R.]` từ server ngay sau `FIN` thường xảy ra khi phía server đóng socket đột ngột (ví dụ `SO_LINGER=0`, process crash/đóng khi phía kia vừa gửi thêm data, hoặc server đã đóng kết nối từ trước khi client gửi gói tiếp theo).
</details>

---

### E9
<details><summary>Xem đáp án</summary>

<details open><summary>Phân tích sự cố Accept Queue Overflow 08:30 trên Nginx + JBoss</summary>

**1. Chuỗi nhân quả từ Full GC 2.8s → Timeout trên Nginx:**
- Lúc `08:30:12`, JBoss bị **Full GC Pause 2.8 giây** (*Stop-the-World*) → toàn bộ thread của JVM đứng hình, bao gồm cả **Acceptor thread** đang gọi `ServerSocket.accept()`.
- Trong `2.8` giây đó, Nginx vẫn tiếp tục mở kết nối TCP mới đẩy vào JBoss port `8080`. Vì `somaxconn = 128` (và JBoss `backlog = 100`), Accept Queue chỉ chứa tối đa **`100–128` socket**. Trong chưa đầy 0.2s, `Recv-Q` đầy vượt `Send-Q` (`129 > 128`).
- Vì `tcp_abort_on_overflow = 0`, kernel Linux **âm thầm drop `ACK`** của các kết nối tiếp theo (`TcpExtListenOverflows = 4821`). Nginx phía client tưởng đã `ESTABLISHED` rồi gửi HTTP request, nhưng JBoss kernel vẫn giữ socket ở `SYN_RCVD` hoặc bỏ rơi → Nginx hết `proxy_connect_timeout`/`proxy_read_timeout` và ghi log `110: Connection timed out`!

**2. Vì sao sau `08:30:15` vẫn chậm 1s / 3s?**
- Các kết nối đã bị drop `ACK` trước đó nằm trong hàng đợi retry `SYN-ACK` với exponential backoff (**`1s, 2s, 4s...`**). Cho dù JBoss đã hết GC và đang rảnh, các client lỡ rơi vào cửa sổ overflow đó vẫn phải chờ tới nhịp retry `SYN-ACK` tiếp theo (hoặc chờ Nginx timeout rồi retry sang backend khác) mới khôi phục!

**3. 4 bước khắc phục dứt điểm:**
1. **Linux Kernel (`sysctl`):** Nâng `net.core.somaxconn = 4096` và `net.ipv4.tcp_max_syn_backlog = 4096` trong `/etc/sysctl.d/99-network.conf` (`sysctl -p`).
2. **JBoss AS 7.1.1:** Đặt `backlog` (hoặc `accept-count`) lên `512`–`1024`, nâng `max-connections`, và **tối ưu GC / Heap** (kiểm tra nguyên nhân Full GC 2.8s — xem `JVM-06` / `JVM-08`) để Acceptor thread không bị đóng băng lâu!
3. **Nginx Upstream Keep-Alive:** Cấu hình Nginx giữ pool kết nối TCP tái sử dụng tới JBoss thay vì bắt tay 3 bước lại cho từng HTTP request:
   ```nginx
   upstream jboss_backend {
       server 10.10.2.15:8080;
       keepalive 64;
   }
   location / {
       proxy_pass http://jboss_backend;
       proxy_http_version 1.1;
       proxy_set_header Connection "";
   }
   ```
4. **Giám sát liên tục:** Đưa `TcpExtListenOverflows` (`nstat`) và `Full GC pause` vào alert trước khi người dùng báo lỗi.

</details>
</details>

---

## Self-check (từ README §9) — đáp án gợi ý
<details><summary>Xem</summary>

1. **3-way handshake & 4-way teardown:** Mở: `SYN` (`SYN_SENT` / `SYN_RCVD`) → `SYN-ACK` → `ACK` (`ESTABLISHED`). Đóng: bên chủ động gửi `FIN` (`FIN_WAIT_1 → FIN_WAIT_2 → TIME_WAIT` giữ `60s`), bên bị động gửi `ACK` (`CLOSE_WAIT` chờ app gọi `close()` → `LAST_ACK → CLOSED`).
2. **SYN Queue vs Accept Queue:** SYN Queue chứa kết nối nửa mở `SYN_RCVD` (`tcp_max_syn_backlog` / `syncookies`); Accept Queue chứa kết nối đã xong handshake chờ `accept()`, kích thước `min(app_backlog, net.core.somaxconn)` (Java default `backlog=50`).
3. **Accept Queue đầy:** Với `tcp_abort_on_overflow=0` (mặc định), kernel drop `ACK` cuối âm thầm, client tưởng đã `ESTABLISHED` rồi treo chờ timeout hoặc chậm `1s/3s` theo backoff; với `=1`, server trả `RST` (`Connection reset`). Kiểm tra bằng `ss -lnt` (`Recv-Q >= Send-Q`) và `nstat -az TcpExtListenOverflows`.
4. **Keep-Alive giảm latency & TCP vs HTTP Keep-Alive:** HTTP Keep-Alive tái sử dụng kết nối đã mở giúp bỏ qua 1 RTT TCP + 1–2 RTT TLS handshake và giữ TCP Slow Start `cwnd` rộng sẵn. TCP Keep-Alive (`SO_KEEPALIVE`, mặc định 7200s) chỉ gửi gói probe phát hiện peer chết âm thầm khi idle lâu.
5. **Nagle + Delayed ACK 40 ms:** Khi app gửi 2 gói nhỏ liên tiếp (`write-write-read`), Delayed ACK bên nhận giữ ACK tới `40 ms`, còn Nagle bên gửi chặn gói thứ 2 chờ ACK gói 1 → trễ `40 ms`/request. Khắc phục bằng `socket.setTcpNoDelay(true)` và gộp buffer ghi 1 lần.
</details>
