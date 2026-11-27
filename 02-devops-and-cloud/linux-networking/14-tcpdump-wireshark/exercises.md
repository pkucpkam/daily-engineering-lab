# LNX-14 · Bài tập — Bắt gói tin: tcpdump & Wireshark

> Làm theo thứ tự. **Tự làm trước, chỉ mở hint khi đã thử ≥ 15 phút.** Đáp án ở [solutions.md](./solutions.md) — chỉ mở sau khi đã có câu trả lời của riêng bạn.
> Ghi thời gian làm từng bài vào `my-notes.md` để theo dõi tiến bộ.

| Cấp | Mục đích | Số lượng | Thời gian gợi ý |
| :--- | :--- | :---: | :---: |
| 🟢 L1 — Recall | Cờ `tcpdump`, BPF filter, đọc flags/RTT, 5 giả thuyết `RST`, capture Pod K8s | 5 | 20' |
| 🟡 L2 — Apply | Sinh & phân tích `.pcap` thật có `RST` và `Retransmission` + loopback `RST` thật | 3 | 75' |
| 🔴 L3 — Deep | Ticket Production: Điều tra capture `.pcap` giữa Nginx, Firewall và JBoss | 1 | 90' |

---

## 🟢 L1 — Recall

### E1. Giải mã từng cờ trong lệnh `tcpdump` an toàn cho production
Giải thích ý nghĩa của từng cờ trong câu lệnh:
`sudo tcpdump -i any -nn -S -s 96 -C 100 -W 5 -w /tmp/jboss.pcap 'host 10.20.1.5 and port 8080'`
Tại sao tuyệt đối không được bỏ `-nn` và `-C 100 -W 5` trên server production?

---

### E2. Đọc nhanh 4 dấu hiệu bất thường trong output `tcpdump` / Wireshark
Cho biết mỗi dấu hiệu sau chỉ ra vấn đề gì và ở phía nào:
1. `Flags [S]` gửi đi lúc `00.000`, `0.1 ms` sau nhận ngay `Flags [R.]`.
2. `win 0` xuất hiện từ Server `10.10.1.5:8080`.
3. Client gửi liên tiếp 3 gói `Flags [.]` có cùng một số `ack 14601`.
4. `SYN` lúc `0.0000` → `SYN-ACK` lúc `0.0005` → `PSH+ACK` request lúc `0.0010` → `PSH+ACK` response đầu tiên lúc `2.8510`.

---

### E3. Phân biệt 2 nguyên nhân gây `RST` khác nhau trong kết nối TCP
Trong file `.pcap`:
- **Ca A:** Client gửi `SYN` → nhận `SYN-ACK` → gửi `ACK` → ngay lập tức Server bắn `Flags [R.]`.
- **Ca B:** Kết nối đã truyền xong request `POST /pay`, im lặng `31 giây`, sau đó **Client** bắn `Flags [F.]`, và `50 ms` sau **Server** gửi payload response `PSH+ACK` → Client bắn tiếp **`Flags [R.]`**.
Hãy giải thích kịch bản thực tế tương ứng với Ca A và Ca B.

---

### E4. Vì sao `tcpdump` trên máy gửi lại báo `TCP Checksum Incorrect`?
Bạn chạy `tcpdump -i eth0 -w out.pcap` trên chính server JBoss rồi mở bằng Wireshark, thấy **toàn bộ gói tin đi ra** từ server JBoss đều bị Wireshark bôi đen cảnh báo `Bad checksum` nhưng kết nối vẫn chạy bình thường. Giải thích cơ chế kernel & card mạng gây ra hiện tượng này.

---

### E5. Bắt gói tin trong Kubernetes Pod dùng image `gcr.io/distroless/java21`
Image production chỉ có đúng binary `java`, hoàn toàn không có `sh`, `bash`, `apt`, `apk`, `tcpdump`, và chạy bằng user non-root `uid=1000`. Nêu **2 cách chuẩn** để bạn lấy được file `.pcap` các gói tin ra/vào Pod đó mà không phải rebuild image hay restart Pod.

---

## 🟡 L2 — Apply

### E6. Tái hiện `RST` thật trên Loopback TCP & Phân tích `.pcap` chuẩn Wireshark
**Bối cảnh:** Dùng [lab/](./lab/README.md) chạy `PcapLabTest` và `PcapLabRunner`.
**Yêu cầu:**
1. Chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`.
2. Quan sát 2 phần kết quả:
   - **Loopback TCP thật:** Client kết nối vào port đóng (`Connection refused` — `SYN -> RST+ACK`) và server đóng ngang với `setSoLinger(true, 0)` (bắn `RST` thật gây `SocketException: Connection reset`).
   - **Phân tích file `.pcap` chuẩn RFC libpcap (`0xA1B2C3D4`):** Mở `http-server-rst.pcap` và `retransmission-burst.pcap` thông qua `MiniPcapAnalyzer` để đọc `networkRttMs`, `serverProcessingMs`, `retransmissionCount`, `dupAckCount`, `rstCount` và dòng chẩn đoán.

**Tiêu chí hoàn thành:**
- [ ] `mvn -o -B verify` pass 100%.
- [ ] Ghi lại bảng số liệu `networkRttMs`, `serverProcessingMs`, `retransmissions`, `rstCount` của 2 file `.pcap`.

<details><summary>💡 Hint</summary>

Các file `.pcap` sinh ra trong `target/generated-pcaps/` tuân theo đúng cấu trúc `24-byte Global Header (0xA1B2C3D4)` + `16-byte Packet Header` + Ethernet (14B) + IPv4 (20B) + TCP (20B) + Payload — bạn có thể giữ lại hoặc copy ra mở trực tiếp bằng Wireshark trên Windows!
</details>

---

### E7. Bổ sung bộ lọc nhận diện `TCP ZeroWindow` (`win == 0`) vào `MiniPcapAnalyzer`
**Bối cảnh:** Mở `lab/src/main/java/lab/linux/pcap/MiniPcapAnalyzer.java` và `lab/solutions/ZeroWindowDetectorSolution.java`.
**Yêu cầu:**
1. Hiện tại `MiniPcapAnalyzer` đếm `SYN`, `FIN`, `RST`, `Retransmission`, `DupACK`. Hãy đọc xem trường **`Window Size`** (2 bytes ở offset `14..15` trong TCP header 20 bytes) được lưu và giải mã như thế nào.
2. Đối chiếu với `ZeroWindowDetectorSolution.java`: viết logic phát hiện gói có `windowSize == 0` (và không mang cờ `RST`/`SYN` bất thường) để cảnh báo *"Receive Buffer Full — App chậm đọc / Full GC"*.

**Tiêu chí hoàn thành:**
- [ ] Hiểu đúng vị trí 2 bytes `Window Size` trong TCP Header và ý nghĩa khi `win == 0`.

<details><summary>💡 Hint</summary>

Trong TCP Header (20 bytes tính từ sau IPv4 Header): bytes `0..1` Source Port, `2..3` Dest Port, `4..7` Seq, `8..11` Ack, `12` Data Offset, `13` TCP Flags, **`14..15` Window Size (unsigned big-endian 16-bit)**!
</details>

---

### E8. Viết BPF Filter cho 4 tình huống thực chiến
**Bối cảnh:** Bạn đang đứng SSH trên Linux server chạy JBoss + Nginx + PostgreSQL.
**Yêu cầu:** Viết đúng câu lệnh `tcpdump` (kèm cờ an toàn `-nn`, `-i`, xoay vòng file khi cần) cho 4 yêu cầu:
1. Chỉ bắt gói tin HTTP/TCP giữa Nginx (`10.10.1.5`) và JBoss (`10.10.1.10`) trên port `8080`.
2. Chỉ bắt các gói **tạo/đóng/reset kết nối** (`SYN`, `FIN`, `RST`) đi tới cổng PostgreSQL `5432` (bỏ qua toàn bộ payload `PSH+ACK` để file siêu nhẹ).
3. Bắt 50 gói DNS (`port 53` cả UDP & TCP) đầu tiên rồi tự thoát.
4. Bắt toàn bộ traffic trên Pod K8s có PID container là `19452` qua `nsenter`.

**Tiêu chí hoàn thành:**
- [ ] Cả 4 lệnh đúng cú pháp BPF và đều có `-nn` (tránh DNS reverse lookup).

<details><summary>💡 Hint</summary>

Với yêu cầu #2, dùng bitmask byte TCP flags ở offset 13: `'tcp[tcpflags] & (tcp-syn|tcp-fin|tcp-rst) != 0 and port 5432'`. Với #4, dùng `sudo nsenter -t 19452 -n tcpdump -i eth0 -nn ...`.
</details>

---

## 🔴 L3 — Deep

### E9. Ticket Production: Đọc log `tcpdump` điều tra lỗi `Connection reset` giữa Nginx, Firewall và JBoss
**Tình huống:**
> Ứng dụng client bên ngoài gọi API qua **Firewall → JBoss (`10.20.1.15:8080`)**. Mỗi sáng lúc `09:00`, khoảng 5% request đầu tiên từ connection pool của client bị lỗi `Connection reset by peer`.
> Đội mạng gửi đoạn trace `tcpdump -i any -nn -S` bắt ngay trên card mạng của JBoss (`10.20.1.15`), trong đó kết nối nguồn `10.10.5.9.51422` là pool phía client đã mở từ `08:35` (nằm im 25 phút):
> ```text
> 08:35:00.000100 IP 10.10.5.9.51422 > 10.20.1.15.8080: Flags [S], seq 1000, win 64240, length 0
> 08:35:00.000500 IP 10.20.1.15.8080 > 10.10.5.9.51422: Flags [S.], seq 9000, ack 1001, win 65160, length 0
> 08:35:00.000700 IP 10.10.5.9.51422 > 10.20.1.15.8080: Flags [.], ack 9001, length 0
> ... (1 request thành công lúc 08:35:00, rồi im lặng 25 phút) ...
> 09:00:00.100000 IP 10.10.5.9.51422 > 10.20.1.15.8080: Flags [P.], seq 1120:1340, ack 9420, length 220
> 09:00:00.100020 IP 10.20.1.15.8080 > 10.10.5.9.51422: Flags [R], seq 9420, win 0, length 0
> ```
> Ngoài ra, một đoạn trace khác cùng buổi sáng cho một kết nối mới lúc cao điểm `09:15` ghi nhận:
> ```text
> 09:15:01.000000 IP 10.10.5.9.52010 > 10.20.1.15.8080: Flags [P.], seq 200:500, ack 1, length 300
> 09:15:01.204000 IP 10.10.5.9.52010 > 10.20.1.15.8080: Flags [P.], seq 200:500, ack 1, length 300
> 09:15:01.612000 IP 10.10.5.9.52010 > 10.20.1.15.8080: Flags [P.], seq 200:500, ack 1, length 300
> 09:15:02.428000 IP 10.10.5.9.52010 > 10.20.1.15.8080: Flags [P.], seq 200:500, ack 1, length 300
> ```

**Yêu cầu:**
1. Phân tích **Đoạn trace 1 (`08:35` → `09:00`)**: Tại sao JBoss `10.20.1.15.8080` lại bắn **`Flags [R]`** sau `20 microseconds` (`09:00:00.100020`) khi client gửi tiếp dữ liệu trên kết nối cũ? (Liên hệ với *Keep-Alive idle timeout* của JBoss/OS hoặc Firewall!).
2. Phân tích **Đoạn trace 2 (`09:15:01`)**: Chuyện gì đang xảy ra với 4 dòng có cùng `seq 200:500` và khoảng cách thời gian `204 ms`, `408 ms`, `816 ms`?
3. Đề xuất **bộ giải pháp khắc phục** cho cả hai hiện tượng.

**Tiêu chí hoàn thành:**
- [ ] Giải thích chính xác vì sao gói `09:00:00` chạm socket đã bị JBoss/Server đóng trước (hoặc JBoss idle timeout < client pool idle timeout) nên kernel JBoss bắn `RST` sau `20 µs`.
- [ ] Nhận diện đúng **TCP Retransmission với Exponential Backoff** ở đoạn trace 2 (`~200ms -> ~400ms -> ~800ms`) do mất gói chiều đi hoặc mất ACK chiều về!

<details><summary>💡 Hint</summary>

1. Nhìn delta `09:00:00.100020 - 09:00:00.100000 = 20 µs`: phản ứng siêu nhanh ngay tại kernel của JBoss! Vì JBoss Web/Tomcat connector có `connectionTimeout` / `keepAliveTimeout` ngắn hơn 25 phút (thường `60s` hoặc vài phút) nên JBoss đã đóng socket từ lâu (hoặc `FIN` bị Firewall ở giữa nuốt mất / client pool không đọc EOF khi idle)! Đến `09:00`, client pool tưởng socket còn sống nên gửi `PSH+ACK`, kernel JBoss không còn socket 4-tuple đó nữa bèn trả thẳng `RST` (`Flags [R]`)!
2. Đoạn 2 là **TCP Retransmission** (`seq 200:500` giống hệt nhau 4 lần) với chu kỳ nhân đôi **`~200 ms`, `~400 ms`, `~800 ms`** — mất gói hoặc mất ACK trên đường truyền giữa client và JBoss!
</details>
