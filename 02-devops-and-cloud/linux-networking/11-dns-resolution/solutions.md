# LNX-11 · Đáp án — DNS resolution & troubleshooting

> ⚠️ **Chỉ mở sau khi đã tự làm.** Đọc đáp án mà chưa tự làm = mất 80% giá trị bài tập.
> Mỗi đáp án được bọc trong `<details>` để tránh lộ khi lướt.

---

### E1
<details><summary>Xem đáp án</summary>

**Đáp án:**
- `8.8.8.8` là **Recursive Resolver**, đang trả lời dữ liệu từ **cache** của nó nên **không có cờ `aa`** (*Authoritative Answer*). Giá trị `TTL = 60` ban đầu đếm ngược từng giây xuống `45` (còn 45 giây trước khi hết hạn cache trên `8.8.8.8` và phải hỏi lại Authoritative NS).
- Cờ **`aa`** chỉ xuất hiện khi bạn truy vấn trực tiếp **Authoritative Nameserver** chính chủ của zone `github.com` (ví dụ tìm bằng `dig NS github.com` rồi hỏi thẳng nameserver đó).
</details>

---

### E2
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. **Không được đặt `CNAME` ở zone apex (`@`):** Theo RFC 1034, nếu một tên miền có bản ghi `CNAME` thì **không được tồn tại bất kỳ bản ghi nào khác** cùng tên đó. Nhưng zone apex (`example.com`) bắt buộc phải có `SOA` và `NS`! Để trỏ domain gốc vào CloudFront/ALB có IP động, AWS Route 53 dùng **`ALIAS` record** (Cloudflare dùng *CNAME Flattening*) — trả thẳng về bản ghi `A`/`AAAA` cho client ở phía Authoritative Server.
2. **Không trỏ `MX` tới `CNAME`:** Theo RFC 5321/2181, `MX` phải trỏ tới tên miền có bản ghi `A`/`AAAA` trực tiếp; một số mail server cũ sẽ từ chối gửi hoặc phát sinh thêm vòng tra cứu không chuẩn nếu gặp `CNAME`.
</details>

---

### E3
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. JVM mặc định đặt `networkaddress.cache.ttl = -1` (**cache mãi mãi**) khi **bật `SecurityManager`** (rất hay gặp ở JBoss/WebLogic legacy hoặc script có `-Djava.security.manager`) hoặc khi `java.security` cấu hình `-1`. Hậu quả: khi RDS/ALB đổi IP, JVM giữ mãi IP cũ cho tới khi restart process, mọi kết nối mới đâm thẳng vào IP đã chết (`Connection timed out`).
2. Vì **`networkaddress.cache.negative.ttl`** mặc định là **`10` giây**: một lần lookup thất bại tạm thời khiến JVM lưu kết quả âm tính (`UnknownHostException`) trong 10 giây và tự trả lỗi ngay lập tức mà không thèm gửi gói DNS hỏi lại!
</details>

---

### E4
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. `payments.stripe.com` có đúng **`2` dấu chấm (`< ndots:5`)**, nên resolver coi là tên tương đối và lần lượt thử qua `4` `search` domain trước, mỗi bước hỏi 2 record `A` + `AAAA` → tổng cộng **`10` DNS queries**:
   - Query 1–2: `payments.stripe.com.checkout.svc.cluster.local` (`A`, `AAAA` → `NXDOMAIN`)
   - Query 3–4: `payments.stripe.com.svc.cluster.local` (`A`, `AAAA` → `NXDOMAIN`)
   - Query 5–6: `payments.stripe.com.cluster.local` (`A`, `AAAA` → `NXDOMAIN`)
   - Query 7–8: `payments.stripe.com.ec2.internal` (`A`, `AAAA` → `NXDOMAIN`)
   - Query 9–10: `payments.stripe.com` (`A`, `AAAA` → IP thật!)
2. Dùng trailing dot `payments.stripe.com.` hoặc đặt `ndots: 2` (`2 >= 2`): resolver đi thẳng hỏi `payments.stripe.com` ngay lập tức → giảm từ **`10` queries xuống còn `2` queries** (`A` + `AAAA`, giảm **80%** tải DNS)!
</details>

---

### E5
<details><summary>Xem đáp án</summary>

**Đáp án:**
**KHÔNG ghi đè được!** Theo đặc tả của `InetAddressCachePolicy` trong OpenJDK, Security property **`networkaddress.cache.ttl`** (đọc từ `java.security` hoặc `Security.setProperty(...)`) có độ ưu tiên **cao hơn** System property `-Dsun.net.inetaddr.ttl`. Nếu `java.security` đã khai báo giá trị `-1`, `-Dsun.net.inetaddr.ttl=10` sẽ bị bỏ qua hoàn toàn!
**Cách đảm bảo thành công:**
1. Sửa trực tiếp `networkaddress.cache.ttl=10` trong `$JAVA_HOME/conf/security/java.security` (hoặc `jre/lib/security/java.security` ở Java 7/8), **HOẶC**
2. Truyền `-Djava.security.properties=/path/to/custom-dns.security` (chứa `networkaddress.cache.ttl=10`) khi khởi chạy JVM, **HOẶC** gọi `Security.setProperty("networkaddress.cache.ttl", "10")` ở dòng đầu tiên của `main()` trước bất kỳ lệnh mạng nào.
</details>

---

### E6
<details><summary>Xem đáp án</summary>

**Hướng giải:**
Khi chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`:
- Server `MiniUdpDnsServer` chạy UDP socket thật trên `127.0.0.1` ban đầu phân giải `db.prod.internal` → `10.10.1.10`.
- Sau khi gọi `server.updateARecord("db.prod.internal", "10.10.2.20")` (mô phỏng DB failover):
  - Client cấu hình `cacheTtlSeconds = -1`: **không gửi thêm bất kỳ gói UDP nào** lên DNS server (`queryCount` giữ nguyên `1`) và vẫn trả mãi IP cũ `10.10.1.10`!
  - Client cấu hình `cacheTtlSeconds = 1`: sau khi hết hạn 1 giây, tự động gửi truy vấn UDP mới (`queryCount` tăng lên `2`) và nhận ngay IP mới `10.10.2.20`!
</details>

---

### E7
<details><summary>Xem đáp án</summary>

**Hướng giải:**
Khi chạy `NdotsResolverSimulator` gửi truy vấn UDP thật vào `MiniUdpDnsServer` với 4 `search` domains K8s:
- `ndots = 5`, query `api.partner.com`: phát sinh **`10` UDP packets** (`8` `NXDOMAIN` + `2` success).
- `ndots = 2`, query `api.partner.com`: chỉ phát sinh **`2` UDP packets** (`0` `NXDOMAIN`).
- `ndots = 5`, query `api.partner.com.` (trailing dot): chỉ phát sinh **`2` UDP packets** (`0` `NXDOMAIN`).

**Mẫu cấu hình Pod Kubernetes tham khảo:** xem [lab/solutions/PodDnsConfigSolution.yaml](./lab/solutions/PodDnsConfigSolution.yaml).
</details>

---

### E8
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. Stub resolver Linux gửi yêu cầu tới `nameserver 10.0.0.2` trước. Nếu `10.0.0.2` không phản hồi, resolver đợi đúng **`5 giây`** (`options timeout:5`) rồi mới thử gửi tiếp sang `10.0.0.3` → ứng dụng dính độ trễ tăng thêm tối thiểu **5.000 ms** cho mỗi lần DNS miss!
2. Tên miền `payments.example.com` là **`CNAME`** trỏ sang tên Load Balancer của AWS (`alb-prod-123...elb.amazonaws.com`), và ALB dùng **Round-Robin DNS với 2+ IP `A` có TTL ngắn (`60s`)** để scale/thay node ngầm khi tải tăng hoặc node ALB hỏng. Nếu JVM cache `-1` hoặc `3600s`, khi AWS thay IP `54.169.10.22` bằng IP mới, JVM vẫn gửi traffic tới IP cũ đã bị thu hồi → lỗi `Connection timed out` hàng loạt!
</details>

---

### E9
<details><summary>Xem đáp án</summary>

<details open><summary>Phân tích Ticket Production: JBoss DNS Cache Vĩnh Viễn + K8s ndots:5 Latency 5s</summary>

**1. Sự cố 1 (JBoss AS 7.1.1 bám IP cũ `10.20.1.10` suốt đêm):**
- Startup script bật **`-Djava.security.manager`**: theo thiết kế của Java `InetAddressCachePolicy`, khi có `SecurityManager`, giá trị mặc định của `networkaddress.cache.ttl` tự động chuyển thành **`-1` (`FOREVER`)**!
- Vì `dig` chạy trực tiếp từ OS tới Route 53/Recursive DNS nên lúc `01:35` `dig` đã thấy IP mới `10.20.2.25`. Nhưng bên trong process JBoss, `InetAddress` không bao giờ tra DNS lại nữa — toàn bộ các kết nối JDBC mới tạo đều nối về IP cũ `10.20.1.10` đã bị tắt! (Đồng thời các connection cũ đang nằm trong JDBC pool của JBoss cũng chỉ được làm mới nếu có `check-valid-connection-sql` / `background-validation`).

**2. Sự cố 2 (K8s Pod nhảy đúng `5.045 ms` / `10.045 ms`):**
- Tên miền `api.bank-gw.vn` chỉ có **`2` dấu chấm (`< ndots:5`)**. Ở `800 req/s`, nếu app/pool chưa cache hoặc lookup lại định kỳ, mỗi lần tra phát sinh **`8–10` UDP DNS queries** (`4` search domains × `A`+`AAAA`) đổ vào CoreDNS!
- Toàn bộ UDP port 53 đi qua Linux **`nf_conntrack`**. Khi bảng `conntrack` bị đầy (log `nf_conntrack: table full, dropping packet`), gói UDP hỏi hoặc trả lời DNS bị kernel node **vứt bỏ âm thầm**.
- Stub resolver đợi hết đúng **`options timeout:5` (`5.000 ms`)** rồi mới retry (`attempts:2` → 1 lần mất = `+5.000 ms`, 2 lần mất = `+10.000 ms` cộng vào thời gian gọi API `45 ms` = `5.045 ms` / `10.045 ms`!).

**3. Bộ giải pháp khắc phục vĩnh viễn:**
- **Cho JBoss AS 7.1.1:**
  1. Thiết lập `networkaddress.cache.ttl=15` và `networkaddress.cache.negative.ttl=1` trong `$JAVA_HOME/jre/lib/security/java.security` (hoặc `-Djava.security.properties=...`), kiểm chứng không bị ghi đè khi chạy `SecurityManager`.
  2. Cấu hình JDBC datasource có `validation` (`validate-on-match` hoặc `background-validation-millis`) và timeout hợp lý để pool tự loại connection cũ khi RDS failover.
- **Cho Kubernetes Pod:**
  1. Thêm `dnsConfig.options: [{ name: "ndots", value: "2" }]` vào Pod/Deployment spec (giảm ngay **80%** lượng DNS UDP queries thừa).
  2. Triển khai **NodeLocal DNSCache** (chạy cache DNS ngay trên từng node bằng DaemonSet và dùng **TCP** tới CoreDNS upstream, bỏ qua `conntrack` bằng `NOTRACK` rule).
  3. Bật HTTP Connection Pool (`Keep-Alive`) khi gọi `api.bank-gw.vn` để tránh mở TCP + resolve DNS lặp lại trên từng request, kết hợp nâng `net.netfilter.nf_conntrack_max` nếu node còn dư RAM.

</details>
</details>

---

## Self-check (từ README §9) — đáp án gợi ý
<details><summary>Xem</summary>

1. **Hành trình DNS Lookup:** `JVM InetAddress Cache` → `OS Stub Resolver (/etc/nsswitch.conf -> /etc/hosts -> /etc/resolv.conf)` → `Recursive Resolver (CoreDNS/8.8.8.8)` → (nếu cache miss) `Iterative queries: Root (.) -> TLD (.com) -> Authoritative NS` → trả IP + `TTL`.
2. **Recursive vs Authoritative & `dig`:** Recursive làm trung gian dò hộ và giữ cache theo TTL; Authoritative giữ bản ghi gốc chính chủ zone. Trong `dig`, câu trả lời trực tiếp từ Authoritative có cờ **`aa`** (*Authoritative Answer*) và TTL đầy đủ không bị giảm dần như ở Recursive cache.
3. **JVM `networkaddress.cache.ttl = -1` & `negative.ttl = 10`:** Khi bật `SecurityManager` (hoặc cấu hình `-1`), JVM cache IP vĩnh viễn → sau RDS/ALB failover vẫn gọi mãi IP cũ gây timeout tới khi restart. `negative.ttl = 10` làm lỗi DNS chớp nhoáng 1s gây kẹt `UnknownHostException` suốt 10 giây.
4. **`ndots:5` trong K8s:** Mọi tên miền có `< 5` dấu chấm bị ép nối thử qua 4 `search` domains × 2 (`A`+`AAAA`) = 8 query `NXDOMAIN` dư thừa trước khi hỏi tên gốc → nhân `5×` tải CoreDNS + làm đầy/drop packet UDP ở `nf_conntrack` → kích hoạt `options timeout:5` làm latency nhảy bậc `5.000 ms`.
5. **Tại sao `-Dsun.net.inetaddr.ttl=5` hay vô hiệu:** Property `networkaddress.cache.ttl` trong `java.security` có ưu tiên cao hơn system property `sun.net.inetaddr.ttl`, và giá trị chỉ đọc 1 lần lúc class load. Phải sửa trong `java.security`, dùng `-Djava.security.properties`, hoặc gọi `Security.setProperty(...)` ngay đầu `main()`.
</details>
