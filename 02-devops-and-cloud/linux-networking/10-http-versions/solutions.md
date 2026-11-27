# LNX-10 · Đáp án — HTTP/1.1 vs HTTP/2 vs HTTP/3

> ⚠️ **Chỉ mở sau khi đã tự làm.** Đọc đáp án mà chưa tự làm = mất 80% giá trị bài tập.
> Mỗi đáp án được bọc trong `<details>` để tránh lộ khi lướt.

---

### E1
<details><summary>Xem đáp án</summary>

**Đáp án:**
**SAI.**
- **HTTP/2** chỉ giải quyết được HOL blocking ở **tầng Application (HTTP)** nhờ chia request/response thành các Binary Frames gắn `Stream ID` để multiplex trên cùng 1 TCP connection.
- HTTP/2 **vẫn còn sót HOL blocking ở tầng Transport (TCP)**: Linux kernel bắt buộc giao byte theo đúng thứ tự. Chỉ cần 1 gói TCP bị mất trên đường truyền, toàn bộ mọi HTTP/2 stream đang chạy chung socket TCP đó bị kernel giữ lại trong receive buffer để chờ gói mất retransmit xong!
- Chỉ tới **HTTP/3 (QUIC trên UDP)** mới loại bỏ luôn HOL blocking ở tầng vận chuyển nhờ xử lý phục hồi mất gói độc lập cho từng stream.
</details>

---

### E2
<details><summary>Xem đáp án</summary>

**Đáp án:**
- Do HTTP/1.1 bị Application HOL blocking trên mỗi socket TCP (response #2 phải chờ response #1 xong mới đi tiếp), các trình duyệt buộc phải mở tối đa **`6` kết nối TCP song song mỗi domain** để tải đồng thời JS/CSS/ảnh.
- **Hệ quả tiêu cực của 6 TCP connections:** Tốn **6×** chi phí 3-way TCP handshake & TLS handshake, tốn **6×** socket memory trên server/LB, và mỗi kết nối đều phải tự leo lại từ đầu **TCP Slow Start (`cwnd = 10 MSS`)** thay vì dùng chung 1 đường ống đã mở rộng `cwnd` sẵn!
- **HTTP/2** dùng Binary Framing multiplexing nên **chỉ cần 1 TCP connection** cho cả trăm stream đồng thời, chia sẻ chung cửa sổ tắc nghẽn `cwnd` ấm sẵn và bảng nén header HPACK.
</details>

---

### E3
<details><summary>Xem đáp án</summary>

**Đáp án:**
2 lý do cốt lõi khiến HTTP/3 chạy trên UDP:
1. **Loại bỏ TCP HOL blocking ở user-space:** Giao thức mới cần tách biệt cơ chế khôi phục mất gói theo từng stream riêng thay vì một byte stream duy nhất như TCP kernel.
2. **Vượt rào *Middlebox Ossification* & tốc độ triển khai:** Hàng trăm triệu firewall, router, NAT trên Internet mặc định vứt bỏ bất kỳ giao thức tầng 4 nào lạ ngoài **TCP** và **UDP**, đồng thời kernel hệ điều hành cập nhật cực chậm. Đóng gói QUIC trên **UDP** giúp đi xuyên NAT/firewall sẵn có và triển khai/nâng cấp nhanh toàn bộ logic transport + TLS 1.3 ngay ở **user-space**.
</details>

---

### E4
<details><summary>Xem đáp án</summary>

**Đáp án:**
- **HPACK** dùng một **Dynamic Table** tích lũy dần trên toàn kết nối: gói A vừa thêm một header mới vào vị trí index `62`, gói B phía sau chỉ gửi số `62` để tra lại giá trị đó. Vì TCP đảm bảo gói A luôn đến trước gói B nên bảng không bao giờ bị lệch pha.
- Trên **UDP/QUIC**, gói tin của stream 3 có thể tới đích **trước** gói của stream 1! Nếu dùng chung HPACK, stream 3 tra index `62` khi stream 1 chưa tới để chèn vào bảng → **giải mã sai hoặc đứng đợi nhau (tái phát sinh HOL blocking khi nén header!)**. Vì thế HTTP/3 dùng **QPACK**, tách stream riêng để cập nhật bảng động và chỉ tham chiếu những index đã được xác nhận nhận xong.
</details>

---

### E5
<details><summary>Xem đáp án</summary>

**Đáp án:**
**Phương án A vượt trội hơn hẳn:**
- Người dùng Internet (mạng di động, RTT cao, mất gói) nối tới **Nginx** cần hưởng toàn bộ lợi ích của **HTTP/2 & HTTP/3** (multiplexing, HPACK/QPACK, 1-RTT/0-RTT TLS 1.3, chống mất gói).
- Nhánh nội bộ **Nginx ↔ JBoss AS 7.1.1** chạy trong mạng LAN `0.2 ms` gần như `0%` packet loss và JBoss 7 không hỗ trợ HTTP/2/HTTP/3 native. Dùng **HTTP/1.1 + `upstream keepalive`** giữ sẵn pool TCP nội bộ vừa cực nhanh, ổn định tuyệt đối vừa không phải vá/hack runtime Java 7 cũ!
</details>

---

### E6
<details><summary>Xem đáp án</summary>

**Hướng giải:**
Khi chạy `mvn -o -B verify` và `mvn -o -B compile exec:java` với `1` request chậm (`200 ms`) + `10` request nhanh (`5 ms`):
- **HTTP/1.1 (1 kết nối tuần tự):** Tổng thời gian là `>= 250 ms` và **median latency của các request nhanh bị vọt lên `> 200 ms`** vì toàn bộ phải xếp hàng sau request chậm đầu tiên!
- **HTTP/1.1 (6 kết nối song song):** Tổng thời gian rút ngắn xuống xấp xỉ `~205–215 ms` nhưng phải mở **`6` TCP connections**.
- **HTTP/2-style Multiplexing (1 kết nối TCP duy nhất):** Tổng thời gian hoàn thành vẫn chỉ `~205–215 ms` nhưng **`10` request nhanh hoàn thành chỉ trong ~`10–25 ms` (median)** *trên đúng `1` TCP socket duy nhất* vì các frame của stream nhanh chen ngay trước khi stream chậm xong!
</details>

---

### E7
<details><summary>Xem đáp án</summary>

**Hướng giải:**
Khi chạy `PacketLossHolSimulator` (`20` streams × `10` packets, RTT `50 ms`):
- Tại **`0%` packet loss**: HTTP/2 (1 TCP) và HTTP/3 (QUIC) cho thời gian hoàn thành tương đương nhau (`10 × 50 ms = 500 ms`).
- Tại **`3%` và `5%` packet loss**: Trong HTTP/2, xác suất có ít nhất 1 trong 20 packet ở mỗi lượt bị mất là `1 - (1 - 0.03)^20 ≈ 45.6%` → gần **một nửa số RTT** toàn bộ cả 20 stream bị dừng chung để chờ TCP retransmit! Ngược lại trong HTTP/3 QUIC, mỗi stream chỉ tự chịu mất gói của chính nó (~`3%`), nên `medianStreamLatencyMs` và `totalCompletionMs` nhanh hơn đáng kể.

**Code lời giải tham khảo HTTP/2 Java 21:** xem [lab/solutions/ConcurrentHttp2ClientSolution.java](./lab/solutions/ConcurrentHttp2ClientSolution.java).
</details>

---

### E8
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. **`http2 on;`** (Nginx `>= 1.25.1`, thay cho `listen 443 ssl http2;` cũ): Bật giao thức HTTP/2 over TLS (ALPN `h2`) cho phía client kết nối vào Nginx.
2. **`keepalive 64;`** (trong block `upstream`): Giữ tối đa `64` kết nối TCP nhàn rỗi trên mỗi worker process Nginx tới backend JBoss để tái sử dụng, tránh `TIME_WAIT` exhaustion ([LNX-09](../09-time-wait-close-wait/README.md)).
3. **`proxy_http_version 1.1;` & `proxy_set_header Connection "";`**: Mặc định Nginx dùng HTTP/1.0 cho upstream và gửi kèm `Connection: close`, làm backend JBoss đóng ngay TCP connection sau mỗi response. Phải đặt `1.1` và xóa header `Connection` thì pool `keepalive 64` mới hoạt động thật sự!
</details>

---

### E9
<details><summary>Xem đáp án</summary>

<details open><summary>Phân tích sự cố gRPC Load Balancing & HTTP/2 trên mạng 4G</summary>

**1. Vì sao 1 Pod `inventory-0` gánh 100% tải gRPC qua `ClusterIP`?**
- Kubernetes `ClusterIP` (kube-proxy iptables/IPVS) hoạt động ở **Layer 4**: nó chỉ thực hiện NAT chọn ngẫu nhiên 1 Pod tại thời điểm **mở kết nối TCP mới (`SYN`)**.
- Vì gRPC dùng **HTTP/2 multiplexing**, client `OrderService` mở **1 kết nối TCP trường tồn** và nhồi tất cả mọi RPC vào đúng kết nối đó! Kết quả là mọi RPC mãi mãi đi vào đúng 1 Pod duy nhất (`inventory-0`), 5 Pod mới scale ra hoàn toàn nhàn rỗi.
- Khi lưu lượng đồng thời vượt ngưỡng `MAX_CONCURRENT_STREAMS` (mặc định thường `100` trên nhiều gRPC/Nginx server), các RPC thứ `101+` bị xếp hàng ở client và dính timeout!

**2. Vì sao người dùng 4G mất gói `3%` bị HTTP/2 chậm hơn HTTP/1.1 nhiều kết nối?**
- Trên HTTP/2, toàn bộ các API/resource chạy trên **1 kết nối TCP**. Với xác suất mất gói `3%`, bất kỳ gói TCP nào rơi trên mạng 4G đều khiến kernel phía nhận phải giữ toàn bộ các gói của mọi stream khác trong TCP Receive Buffer chờ retransmit (`>= 1 RTT`, thường `80–200 ms` trên 4G) — gọi là **TCP-level Head-of-Line blocking**. Trong khi trước đây app/browser dùng 6 kết nối HTTP/1.1 thì mất gói trên socket #1 không chặn 5 socket còn lại!

**3. Giải pháp khắc phục triệt để:**
- **Với gRPC nội bộ K8s:**
  1. Đưa qua **L7 Load Balancer hỗ trợ HTTP/2 / gRPC** (như **Envoy**, Istio/Linkerd service mesh, hoặc Nginx `grpc_pass`), **HOẶC** cấu hình Kubernetes **Headless Service** (`clusterIP: None`) kết hợp gRPC client-side `round_robin` Load Balancing Policy (tạo kết nối tới IP của *từng* Pod thay vì 1 VIP duy nhất!).
  2. Cấu hình server gRPC `maxConnectionAge` (vd `5 phút`) để định kỳ xoay vòng kết nối sang các Pod mới vừa scale.
- **Với người dùng Mobile 4G ở Edge:**
  1. Bật **HTTP/3 (QUIC trên UDP `443`)** ở CDN/Nginx/ALB phía ngoài (kèm fallback HTTP/2 TCP cho mạng chặn UDP) để loại bỏ TCP HOL blocking và giảm TLS handshake xuống `1-RTT/0-RTT`.
  2. Đóng gói các payload khởi động hợp lý và nén response (`gzip`/`brotli`) để giảm tổng số packet có nguy cơ rơi trên 4G.

</details>
</details>

---

## Self-check (từ README §9) — đáp án gợi ý
<details><summary>Xem</summary>

1. **HOL blocking 3 thế hệ:** HTTP/1.1 dính cả Application HOL (phải mở 6 TCP conn) lẫn TCP HOL. HTTP/2 hết Application HOL nhờ multiplexing binary frames theo `Stream ID` trên 1 TCP conn, nhưng **vẫn còn TCP HOL** (1 TCP packet mất làm kẹt mọi stream trong receive buffer). HTTP/3 chạy QUIC trên UDP nên hết cả TCP HOL.
2. **Binary Framing & Multiplexing HTTP/2:** 1 kết nối TCP chia thành các Frame (`HEADERS`, `DATA`, `SETTINGS`, `WINDOW_UPDATE`, `RST_STREAM`), mỗi frame mang `Stream ID` (client mở dùng số lẻ `1, 3, 5...`, server mở dùng số chẵn `2, 4, 6...`). Các frame đan xen nhau để không stream nào phải chờ stream khác hoàn thành toàn bộ response.
3. **HPACK vs QPACK:** HPACK gồm Static Table (61 entries), Dynamic Table theo kết nối, và mã hóa Huffman. HPACK cần thứ tự byte nghiêm ngặt của TCP để 2 đầu giữ Dynamic Table đồng bộ. Trên UDP/QUIC gói tin đến lệch thứ tự nên HTTP/3 phải đổi sang **QPACK** (dùng stream đồng bộ bảng riêng và chỉ tham chiếu entry đã được xác nhận).
4. **2 lý do HTTP/3 chọn UDP:** (1) Thực thi kiểm soát từng stream độc lập ở user-space để dẹp TCP HOL blocking (và nhúng TLS 1.3 + `Connection ID` di động); (2) Vượt rào *Middlebox Ossification* — router/firewall trên Internet chỉ cho TCP/UDP đi qua ổn định, và nâng cấp kernel TCP toàn cầu là bất khả thi.
5. **L4 LB với gRPC:** L4 LB chỉ phân phối lúc mở TCP connection, trong khi gRPC/HTTP/2 dùng 1 TCP connection dài hạn để multiplex mọi RPC → dồn 100% tải vào 1 Pod. Khắc phục bằng L7 proxy hiểu HTTP/2 (Envoy/Nginx `grpc_pass`) hoặc gRPC Headless Service + client-side `round_robin` + `maxConnectionAge`.
</details>
