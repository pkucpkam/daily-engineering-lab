# LNX-10 · Bài tập — HTTP/1.1 vs HTTP/2 vs HTTP/3

> Làm theo thứ tự. **Tự làm trước, chỉ mở hint khi đã thử ≥ 15 phút.** Đáp án ở [solutions.md](./solutions.md) — chỉ mở sau khi đã có câu trả lời của riêng bạn.
> Ghi thời gian làm từng bài vào `my-notes.md` để theo dõi tiến bộ.

| Cấp | Mục đích | Số lượng | Thời gian gợi ý |
| :--- | :--- | :---: | :---: |
| 🟢 L1 — Recall | Nắm chắc HOL blocking 2 tầng, HPACK/QPACK, QUIC/UDP, gRPC LB | 5 | 20' |
| 🟡 L2 — Apply | Đo Multiplexing trên loopback TCP & mô phỏng Packet Loss HOL | 3 | 75' |
| 🔴 L3 — Deep | Ticket sản phẩm: mất cân bằng tải gRPC + suy giảm HTTP/2 trên mạng yếu | 1 | 90' |

---

## 🟢 L1 — Recall

### E1. Phân biệt tầng giải quyết HOL blocking của HTTP/2 vs HTTP/3
Một đồng nghiệp nói: *"Chuyển lên HTTP/2 là hết hoàn toàn Head-of-Line blocking trên đường truyền."* Câu này đúng hay sai? Nếu sai, hãy chỉ ra chính xác HTTP/2 giải quyết HOL ở tầng nào và vẫn kẹt ở tầng nào.

---

### E2. Vì sao trình duyệt mở `6` kết nối TCP cho HTTP/1.1, còn HTTP/2 chỉ dùng `1`?
Giải thích hệ quả của quy ước mở **6 TCP connections/origin** ở HTTP/1.1 đối với TCP Slow Start (`cwnd`), số lượng handshake TLS, và bộ nhớ socket; đồng thời nêu lý do HTTP/2 chỉ cần **1 kết nối** cho hàng chục stream song song.

---

### E3. Vì sao HTTP/3 không chọn xây một giao thức Layer 4 mới thay cho TCP, mà phải đóng gói trên UDP?
Nêu **2 lý do** kỹ thuật và thực tiễn hạ tầng Internet (*middlebox ossification*) giải thích vì sao QUIC/HTTP/3 chạy trên UDP port `443`.

---

### E4. Vì sao HPACK hoạt động tốt trên TCP nhưng vỡ trên UDP/QUIC?
Giải thích ngắn gọn vai trò của **Dynamic Table** trong HPACK (HTTP/2) và lý do HTTP/3 bắt buộc thay thế bằng **QPACK**.

---

### E5. Có nên chạy Nginx → JBoss AS 7.1.1 nội bộ bằng HTTP/3 không?
Một hệ thống có Nginx đứng ngoài Internet, proxy vào JBoss AS 7.1.1 trên cùng mạng LAN (`RTT = 0.2 ms`, không mất gói). Thiết kế nào hợp lý hơn:
- **Phương án A:** Bật HTTP/2 + HTTP/3 ở tầng Edge (User ↔ Nginx), còn nhánh Nginx ↔ JBoss chạy **HTTP/1.1 Keep-Alive** (`upstream keepalive`).
- **Phương án B:** Cố gắng ép JBoss AS 7.1.1 nâng cấp để nói chuyện trực tiếp HTTP/3 với Nginx.
Giải thích vì sao.

---

## 🟡 L2 — Apply

### E6. Đo thực nghiệm Loopback TCP: HTTP/1.1 Tuần tự vs HTTP/2 Multiplexing Frames
**Bối cảnh:** Dùng [lab/](./lab/README.md) chạy `HttpVersionsLabTest` và `HttpVersionsRunner`.
**Yêu cầu:**
1. Chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`.
2. Cho `1` request chậm (`200 ms`) và `10` request nhanh (`5 ms`) gửi đồng thời từ client tới server:
   - Ghi lại tổng thời gian và `median latency` của 10 request nhanh khi chạy **HTTP/1.1 (1 TCP socket tuần tự)** vs **Multiplexed Binary Framing (1 TCP socket)** vs **HTTP/1.1 (6 TCP sockets)**.
3. Giải thích vì sao với **chỉ 1 kết nối TCP**, cơ chế frame multiplexing cứu được toàn bộ 10 request nhanh khỏi việc phải chờ 200 ms.

**Tiêu chí hoàn thành:**
- [ ] `mvn -o -B verify` pass 100%.
- [ ] Có bảng số đo thời gian & số lượng TCP socket sử dụng giữa 3 chế độ.

<details><summary>💡 Hint</summary>

Trong `HttpMultiplexingComparison.java`, HTTP/1.1 đơn kênh phải đợi request #1 (`200 ms`) trả xong trọn vẹn rồi mới gửi request #2, khiến 10 request nhanh cộng dồn `>= 200 ms`. Trong mô hình HTTP/2 framing, server đọc ngay frame `DATA` của mọi stream, gửi ngay các response nhanh xong trước trên cùng 1 TCP socket khi stream chậm vẫn đang xử lý!
</details>

---

### E7. Mô phỏng TCP HOL Blocking khi Packet Loss tăng (`0%` → `3%` → `5%`)
**Bối cảnh:** Mở `lab/src/main/java/lab/linux/http/PacketLossHolSimulator.java` và đối chiếu `lab/solutions/ConcurrentHttp2ClientSolution.java`.
**Yêu cầu:**
1. Chạy `PacketLossHolSimulator` với `20` streams song song, mỗi stream truyền `10` packet (RTT = `50 ms`) ở các mức mất gói `0%`, `2%`, `5%`.
2. So sánh tổng thời gian hoàn thành (`completionMs`) và độ trễ trung vị của từng stream giữa **HTTP/2 (1 TCP socket)** vs **HTTP/3 QUIC (1 UDP socket với các stream độc lập)**.
3. Xem `ConcurrentHttp2ClientSolution.java` để hiểu cách gửi song song các HTTP/2 request qua 1 instance `java.net.http.HttpClient`.

**Tiêu chí hoàn thành:**
- [ ] Chứng minh được khi mất gói `> 0%`, HTTP/3 QUIC có `medianStreamLatencyMs` và `totalCompletionMs` thấp hơn rõ rệt so với HTTP/2 trên cùng 1 kết nối TCP.

<details><summary>💡 Hint</summary>

Khi 1 trong `20` stream có packet bị rơi ở vòng truyền thứ `k`, trong **HTTP/2 (1 TCP)** tất cả `19` stream còn lại đều bị dừng vòng truyền `k+1` trong `1 RTT` để đợi retransmit của TCP; còn trong **HTTP/3 (QUIC)** chỉ riêng stream có packet rơi phải chờ retransmit, `19` stream khác vẫn tiến lên bình thường!
</details>

---

### E8. Cấu hình Nginx HTTP/2 (+ gợi ý HTTP/3) làm Edge Proxy cho JBoss
**Bối cảnh:** Khi có môi trường Linux/Docker với Nginx (`>= 1.25`), mở `lab/scripts/nginx-http2.conf` và `lab/scripts/nginx-http2-http3-compare.sh`.
**Yêu cầu:**
1. Đọc và giải thích tác dụng từng directive trong `nginx-http2.conf`: `http2 on;`, `keepalive 64;`, `proxy_http_version 1.1;`, `proxy_set_header Connection "";`.
2. Tại sao nhánh `location /` nối về JBoss lại phải dùng `proxy_http_version 1.1` và xóa header `Connection`?

**Tiêu chí hoàn thành:**
- [ ] Giải thích chính xác cả 4 directive trên.
- [ ] (Nếu có máy Linux/Nginx) chạy `bash scripts/nginx-http2-http3-compare.sh` đối chiếu `curl --http1.1` và `curl --http2`.

<details><summary>💡 Hint</summary>

Nginx mặc định dùng HTTP/1.0 cho `proxy_pass` và gửi `Connection: close` về upstream nếu bạn không khai báo `proxy_http_version 1.1;` và `proxy_set_header Connection "";` — thiếu 2 dòng đó thì `keepalive 64;` trong block `upstream` hoàn toàn vô tác dụng!
</details>

---

## 🔴 L3 — Deep

### E9. Ticket Production: Chuyển REST sang gRPC làm 1 Pod gánh 100% tải + user 4G than chậm hơn HTTP/1.1
**Tình huống:**
> Team vừa ra mắt dịch vụ mới với 2 thay đổi kiến trúc cùng lúc:
> 1. **Nội bộ:** Đổi API giữa `OrderService` và `InventoryService` (chạy `6` pods trên Kubernetes) từ REST HTTP/1.1 sang **gRPC trên HTTP/2**, đi qua một `ClusterIP Service` (kube-proxy iptables / L4 load balancing). Ngay sau deploy, metrics cho thấy **Pod `inventory-0` chạy 95% CPU còn 5 pod kia ngồi chơi ở 2% CPU**, đồng thời request đồng thời cao làm client gRPC báo timeout dù DB bình thường!
> 2. **Bên ngoài (Mobile app qua 4G):** Team bật HTTP/2 trên Nginx edge nhưng thu gọn lại thành 1 host duy nhất, kết quả trên khu vực sóng 4G yếu (packet loss ~3%), thời gian tải bộ dữ liệu khởi động app bị tăng từ `650 ms` lên `1.100 ms` so với lúc trình duyệt/app dùng nhiều kết nối HTTP/1.1!

**Yêu cầu:**
1. Giải thích **cơ chế bên trong** gây ra hiện tượng 1 Pod gánh 100% tải ở câu chuyện (1) — liên hệ với `SETTINGS_MAX_CONCURRENT_STREAMS` nếu client dồn quá nhiều RPC vào 1 channel.
2. Giải thích **cơ chế bên trong** khiến HTTP/2 bị chậm hơn khi mất gói 3% trên sóng 4G ở câu chuyện (2).
3. Đưa ra **giải pháp sửa cụ thể** cho cả hai tình huống.

**Tiêu chí hoàn thành:**
- [ ] Phân tích đúng bản chất L4 Load Balancing với kết nối HTTP/2 trường tồn của gRPC.
- [ ] Phân tích đúng TCP-level HOL blocking ở HTTP/2 so với HTTP/3 QUIC trên mạng di động mất gói.
- [ ] Nêu đủ giải pháp khắc phục chuẩn production.

<details><summary>💡 Hint</summary>

1. Kubernetes `ClusterIP` mặc định dùng iptables/IPVS hoạt động ở **Layer 4**: nó chỉ chọn random 1 Pod tại thời điểm 3-way handshake TCP đầu tiên (`SYN`). gRPC mở **1 HTTP/2 TCP connection** và giữ mãi suốt hàng giờ, nên toàn bộ RPC đều chui vào đúng Pod đã bốc thăm lúc đầu! Ngoài ra mặc định server giới hạn ~`100` concurrent streams trên 1 TCP connection.
2. Mạng 4G packet loss `3%` kích hoạt TCP HOL blocking trên 1 kết nối HTTP/2 duy nhất (1 gói rớt khóa tất cả các stream), trong khi bật **HTTP/3 (QUIC)** ở Edge hoặc tối ưu retry/gói tin giải phóng từng stream độc lập!
</details>
