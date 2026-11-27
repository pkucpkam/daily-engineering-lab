# LNX-14 · Đáp án — Bắt gói tin: tcpdump & Wireshark

> ⚠️ **Chỉ mở sau sau khi đã tự làm.** Đọc đáp án mà chưa tự làm = mất 80% giá trị bài tập.
> Mỗi đáp án được bọc trong `<details>` để tránh lộ khi lướt.

---

### E1
<details><summary>Xem đáp án</summary>

**Đáp án:**
- `-i any`: Bắt gói trên mọi interface (`eth0`, `lo`, `veth*`...).
- `-nn`: Tắt phân giải tên miền DNS ngược (IP → hostname) và tên port (`/etc/services`), giúp giữ nguyên số IP:port và **tránh làm chậm server / nhiễu file pcap bằng bão DNS lookup**.
- `-S`: Hiển thị sequence number tuyệt đối (*absolute sequence numbers*).
- `-s 96`: Chỉ chụp tối đa `96 bytes` đầu mỗi gói (đủ Ethernet + IP + TCP + đầu HTTP header), tiết kiệm băng thông đĩa và ẩn payload nhạy cảm.
- `-C 100 -W 5`: Xoay vòng file khi đạt `100 MB`, giữ tối đa `5` file (`jboss.pcap0..4`) để **không bao giờ làm đầy ổ đĩa production**.
- `-w /tmp/jboss.pcap`: Ghi nhị phân raw pcap (mở được bằng Wireshark, nhanh hơn in text ra console nhiều lần).
- `'host 10.20.1.5 and port 8080'`: Bộ lọc BPF lọc ngay trong kernel.
</details>

---

### E2
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. **`Flags [S]` → `0.1 ms` sau `Flags [R.]`:** Port đích **đang đóng** (không có process `LISTEN` hoặc firewall chủ động từ chối bằng `RST`) → lỗi `Connection refused`.
2. **`win 0` từ Server `10.10.1.5:8080`:** **TCP ZeroWindow** — Receive Buffer của socket phía server đã đầy vì **app Java/JBoss không kịp `read()` dữ liệu** (thread kẹt, deadlock hoặc Full GC pause).
3. **3 gói `Flags [.]` cùng `ack 14601`:** **3 Duplicate ACKs** — bên nhận báo thiếu dữ liệu bắt đầu từ byte `14601` do một gói trước đó bị mất/trễ trên đường truyền, kích hoạt Fast Retransmission.
4. **Handshake `0.5 ms` (`0.0005 - 0.0000`), `Resp - Req = 2.850 s`:** Đường mạng LAN rất nhanh (`RTT = 0.5 ms`), toàn bộ `2.85 giây` là **Server Processing Time** (lỗi nằm trong code/DB của server, không phải tại mạng!).
</details>

---

### E3
<details><summary>Xem đáp án</summary>

**Đáp án:**
- **Ca A (`SYN -> SYN-ACK -> ACK -> Server bắn RST`):** Đây là triệu chứng kinh điển của **Accept Queue tràn trên Linux khi bật `net.ipv4.tcp_abort_on_overflow = 1`** (hoặc server/proxy đóng ngay sau handshake do chặn policy/IP/connection limit)!
- **Ca B (Sau `31s` Client bắn `FIN`, `50 ms` sau Server gửi response → Client bắn `RST`):** **Client đã hết Timeout (`30s`)** nên chủ động đóng socket trước bằng `FIN` (`FIN_WAIT_1 -> FIN_WAIT_2` hoặc đóng hẳn). `50 ms` sau, Server mới xử lý xong và cố đẩy response `PSH+ACK` vào kết nối phía Client vừa dọn mất → kernel Client không còn socket mở nên bắt buộc trả **`Flags [R.]` (`RST`)** (gây ra dòng `Broken pipe / Connection reset` trong log của Server!).
</details>

---

### E4
<details><summary>Xem đáp án</summary>

**Đáp án:**
Đây là hiệu ứng bình thường của **TCP Checksum Offloading** trên NIC Linux hiện đại:
- Để tiết kiệm CPU, Linux kernel giao việc tính toán giá trị CRC/TCP checksum cho con chip trên card mạng vật lý (NIC).
- Móc bắt gói tin `AF_PACKET` của `tcpdump` nằm **trước khi gói tin đi xuống phần cứng NIC**. Do đó tại thời điểm `tcpdump` chụp lại các gói **đi ra (*outbound*)**, ô checksum trong TCP header mới chứa giá trị tạm chưa tính → Wireshark báo `Incorrect checksum`.
- *(Mẹo: trong Wireshark chỉ cần tắt option `Validate the TCP checksum if possible` nếu muốn bỏ cảnh báo giả ở gói outbound; chỉ gói **inbound** nhận từ ngoài về bị sai checksum mới là lỗi thật!)*
</details>

---

### E5
<details><summary>Xem đáp án</summary>

**Đáp án:**
2 cách chuẩn production trên Kubernetes mà không chạm vào container image hay restart Pod:
1. **`kubectl debug` với Ephemeral Debug Container:** Chạy container chứa sẵn đồ nghề mạng (`nicolaka/netshoot`) dùng chung Network Namespace với container mục tiêu:
   `kubectl debug -it <pod> -n <ns> --image=nicolaka/netshoot --target=<container> -- tcpdump -i eth0 -nn -s 0 -w /tmp/pod.pcap 'port 8080'`
2. **`nsenter` trực tiếp từ Worker Node Linux:** SSH vào Node chứa Pod, lấy PID container từ runtime (`crictl inspect <id> | grep pid`) rồi chạy `tcpdump` có sẵn trên Node trong network namespace của PID đó:
   `sudo nsenter -t <PID> -n tcpdump -i eth0 -nn -s 0 -w /tmp/pod.pcap 'port 8080'`
</details>

---

### E6
<details><summary>Xem đáp án</summary>

**Hướng giải:**
Khi chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`:
- Trên loopback TCP thật: kết nối vào port đóng bị `ConnectException` (`SYN -> RST+ACK`), còn server đóng với `setSoLinger(true, 0)` phát gói `RST` thật khiến client văng `SocketException: Connection reset`.
- `MiniPcapAnalyzer` đọc 2 file `.pcap` chuẩn RFC libpcap (`0xA1B2C3D4`):
  - `http-server-rst.pcap`: đo chính xác `networkRttMs` (`~0.40 ms`), `serverProcessingMs` (`~18 ms`), `rstCount = 1` → kết luận server phát `RST` sau khi nhận request (`SO_LINGER=0` / crash / đóng kết nối sớm).
  - `retransmission-burst.pcap`: đếm chính xác `dupAckCount = 3` và `retransmissionCount = 1` (`~205 ms` mất để phát hiện & truyền lại).
</details>

---

### E7
<details><summary>Xem đáp án</summary>

**Hướng giải:**
- Trong một TCP Header (20 bytes chuẩn không options), trường **Window Size** nằm ở 2 bytes **offset `14` và `15`** (tính từ đầu TCP header), mã hóa kiểu **big-endian unsigned 16-bit**:
  `int win = ((tcp[14] & 0xFF) << 8) | (tcp[15] & 0xFF);`
- Khi `win == 0` xuất hiện ở một gói không có cờ `RST` (`(flags & 0x04) == 0`), đó là bản tin **TCP ZeroWindow** cảnh báo Receive Buffer phía phát gói đã đầy vì ứng dụng đọc quá chậm.

**Code mẫu:** xem [lab/solutions/ZeroWindowDetectorSolution.java](./lab/solutions/ZeroWindowDetectorSolution.java).
</details>

---

### E8
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. **Nginx (`10.10.1.5`) ↔ JBoss (`10.10.1.10`) trên port `8080`:**
   `sudo tcpdump -i any -nn -C 100 -W 5 -w /tmp/ngx-jb.pcap 'host 10.10.1.5 and host 10.10.1.10 and port 8080'`
2. **Chỉ bắt `SYN`, `FIN`, `RST` trên PostgreSQL port `5432`:**
   `sudo tcpdump -i any -nn -S 'tcp[tcpflags] & (tcp-syn|tcp-fin|tcp-rst) != 0 and port 5432'`
3. **Bắt đúng 50 gói DNS (`port 53`) đầu tiên rồi thoát:**
   `sudo tcpdump -i any -nn -c 50 'port 53'`
4. **Bắt trong K8s Pod PID `19452` qua `nsenter`:**
   `sudo nsenter -t 19452 -n tcpdump -i eth0 -nn -s 0 -w /tmp/pod-19452.pcap 'port 8080'`
</details>

---

### E9
<details><summary>Xem đáp án</summary>

<details open><summary>Phân tích Ticket `tcpdump`: Stale Idle Socket RST & TCP Retransmission</summary>

**1. Phân tích Đoạn trace 1 (`08:35` → `09:00`, `RST` sau `20 µs`):**
- Kết nối được mở lúc `08:35` rồi **nằm im `25 phút`**. Trong khoảng thời gian đó, JBoss (ví dụ `keepAliveTimeout` mặc định `60s`) đã chủ động đóng socket đó từ rất lâu — nhưng hoặc là gói `FIN` đã bị Firewall ở giữa chặn mất, hoặc Firewall đã xóa state table sau 15 phút idle, hoặc client pool không đọc/validate socket idle!
- Lúc `09:00:00.100000`, client pool bốc nhầm socket nửa chết đó ra và gửi `Flags [P.]` (`220 bytes`).
- Vì kernel JBoss (`10.20.1.15:8080`) **không còn bất kỳ socket `ESTABLISHED` nào khớp với 4-tuple này nữa**, kernel lập tức trả về **`Flags [R]` (`RST`, `win 0`)** chỉ trong vòng **`20 microseconds`** (`0.000020s`)! Client ném `Connection reset by peer`!

**2. Phân tích Đoạn trace 2 (`09:15:01`, `seq 200:500` lặp 4 lần):**
- Cả 4 gói đều là chiều `10.10.5.9 -> 10.20.1.15.8080` mang đúng **`seq 200:500` (`length 300`)**, với khoảng thời gian chờ tăng gấp đôi: `0.204s` (~`200 ms`), `0.408s` (~`400 ms`), `0.816s` (~`800 ms`).
- Đây là **TCP Retransmission với Exponential Backoff**: gói 300 byte bị mất trên mạng (hoặc ACK từ JBoss gửi về bị mất dọc đường) nên bên gửi không nhận được ACK và cứ nhân đôi RTO gửi lại!

**3. Giải pháp khắc phục:**
- **Với hiện tượng 1 (Stale idle socket `Connection reset`):**
  1. Đặt `idleTimeout` / `evictIdleConnections` trên **client connection pool** **ngắn hơn** cả `keepAliveTimeout` của JBoss lẫn idle timeout của Firewall (ví dụ JBoss `60s`, Firewall `900s` → client pool evict ở `30s–45s`, khớp bài [LNX-09](../09-time-wait-close-wait/README.md)!).
  2. Bật `validateAfterInactivity` (hoặc retry an toàn cho `GET` khi socket idle vừa nhận `NoHttpResponseException` / `Connection reset` ngay ở lần gửi đầu).
- **Với hiện tượng 2 (Mất gói `Retransmission`):**
  1. Kiểm tra tỷ lệ drop/error trên switch, interface (`ip -s link show`, `netstat -s`), MTU mismatch (`PMTUD` / fragmentation), hoặc `conntrack` table full giữa `10.10.5.9` và `10.20.1.15`!

</details>
</details>

---

## Self-check (từ README §9) — đáp án gợi ý
<details><summary>Xem</summary>

1. **Ý nghĩa cờ `tcpdump`:** `-i any` (mọi interface), `-nn` (tắt DNS/port name tránh chậm & nhiễu), `-S` (seq tuyệt đối), `-s 96` (chụp 96B đầu bảo vệ payload & nhẹ đĩa), `-C 100 -W 5` (xoay vòng 5 file × 100MB chống nổ đĩa), `-w` (ghi file nhị phân `.pcap`).
2. **Tính Network RTT & Server Processing Time:** `Network RTT` = `Time(SYN-ACK) - Time(SYN)`. `Server Processing Time` = `Time(PSH+ACK Response đầu) - Time(PSH+ACK Request)`.
3. **5 giả thuyết khi thấy `RST`:** (1) Port đóng (`SYN -> RST+ACK` sau 0.1ms); (2) App crash / OOM-kill / `SO_LINGER=0` / peer gửi data vào socket đã đóng; (3) Firewall/LB xóa state idle; (4) Accept Queue đầy với `tcp_abort_on_overflow=1`; (5) Client hết timeout cắt kết nối giữa chừng.
4. **3 DupACKs vs ZeroWindow:** `3 DupACKs` báo **mất gói trên mạng** ở giữa dải `seq` (cần kiểm tra đường truyền/NIC/switch); `TCP ZeroWindow (win 0)` báo **Receive Buffer bên nhận đã đầy** vì **app chậm đọc `read()`** (Full GC hoặc deadlock thread).
5. **Bắt gói trong K8s Pod tối giản:** (1) `kubectl debug -it <pod> --image=nicolaka/netshoot --target=<container> -- tcpdump ...`; (2) `sudo nsenter -t <PID> -n tcpdump ...` từ Worker Node (hoặc `kubectl sniff`).
</details>
