# LNX-11 · Bài tập — DNS resolution & troubleshooting

> Làm theo thứ tự. **Tự làm trước, chỉ mở hint khi đã thử ≥ 15 phút.** Đáp án ở [solutions.md](./solutions.md) — chỉ mở sau khi đã có câu trả lời của riêng bạn.
> Ghi thời gian làm từng bài vào `my-notes.md` để theo dõi tiến bộ.

| Cấp | Mục đích | Số lượng | Thời gian gợi ý |
| :--- | :--- | :---: | :---: |
| 🟢 L1 — Recall | Recursive vs Authoritative, record rules, JVM DNS cache, `ndots:5` | 5 | 20' |
| 🟡 L2 — Apply | Chạy UDP DNS Server RFC 1035, đo JVM TTL `-1` vs `1s` & đếm packet `ndots:5` | 3 | 75' |
| 🔴 L3 — Deep | Ticket Production: Failover RDS chết cứng JBoss + Pod K8s nhảy latency 5s | 1 | 90' |

---

## 🟢 L1 — Recall

### E1. Phân biệt Recursive Resolver và Authoritative Nameserver qua output `dig`
Khi chạy `dig api.github.com @8.8.8.8` lần 1 thấy `TTL = 60` giây, chạy lại sau 15 giây thấy `TTL = 45` giây và phần `;; flags:` không có cờ `aa`. Giải thích hai hiện tượng đó và cho biết lúc nào `dig` trả về cờ `aa`.

---

### E2. Hai lỗi cấu hình DNS Record kinh điển (`CNAME` tại Apex & `MX` trỏ `CNAME`)
1. Tại sao các DNS provider tiêu chuẩn không cho phép tạo bản ghi `CNAME` cho domain gốc (`example.com`, zone apex `@`), và giải pháp thay thế trên AWS Route 53 / Cloudflare là gì?
2. Tại sao không được trỏ bản ghi `MX` tới một `CNAME`?

---

### E3. JVM cache DNS mãi mãi (`-1`) và Negative Cache (`10s`)
1. Khi nào JVM mặc định cache DNS vĩnh viễn (`networkaddress.cache.ttl = -1`)? Nêu hậu quả cụ thể khi Database RDS hoặc Load Balancer đổi IP.
2. Nếu một DNS server chỉ bị trục trặc **1 giây**, tại sao ứng dụng Java lại ném `UnknownHostException` liên tiếp suốt **10 giây**?

---

### E4. Đếm số DNS query thực tế với `options ndots:5` trong Kubernetes
Một Pod Kubernetes có `/etc/resolv.conf`:
```text
nameserver 10.96.0.10
search checkout.svc.cluster.local svc.cluster.local cluster.local ec2.internal
options ndots:5 timeout:5 attempts:2
```
Nếu một ứng dụng Python/Go/Java trong Pod gọi `https://payments.stripe.com/v1/charges` (máy bật cả IPv4 + IPv6 `getaddrinfo` trả cả `A` và `AAAA`):
1. Có tất cả bao nhiêu DNS query được gửi đi trước khi nhận được IP thật? Liệt kê tuần tự từng tên miền được hỏi.
2. Nếu đổi sang gọi `https://payments.stripe.com./v1/charges` (có dấu chấm cuối) hoặc cấu hình `ndots: 2`, số query giảm xuống còn bao nhiêu?

---

### E5. Tại sao `-Dsun.net.inetaddr.ttl=10` đôi khi "không ăn"?
Bạn thêm cờ `-Dsun.net.inetaddr.ttl=10` vào script khởi động JBoss, nhưng trong `java.security` đã có sẵn dòng `networkaddress.cache.ttl=-1`. Cờ dòng lệnh trên có ghi đè được `java.security` không? Cách nào mới đảm bảo áp dụng thành công?

---

## 🟡 L2 — Apply

### E6. Quan sát JVM DNS Cache trên Real Loopback UDP DNS Server (`ttl = -1` vs `1s`)
**Bối cảnh:** Dùng [lab/](./lab/README.md) chạy `DnsLabTest` và `DnsLabRunner`.
**Yêu cầu:**
1. Chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`.
2. Quan sát `MiniUdpDnsServer` (UDP socket `127.0.0.1` nói chuẩn gói tin DNS RFC 1035) phục vụ tên miền `db.prod.internal`:
   - Ban đầu trả `10.10.1.10` (`TTL = 2s`), sau đó chuyển IP sang `10.10.2.20` (mô phỏng DB failover).
   - So sánh kết quả resolve từ client cấu hình `cacheTtl = -1` (Forever) vs `cacheTtl = 1s` sau thời điểm failover!
3. Kiểm chứng thêm cách JVM thật đọc `Security.getProperty("networkaddress.cache.ttl")`.

**Tiêu chí hoàn thành:**
- [ ] `mvn -o -B verify` pass 100%.
- [ ] Chứng minh client `ttl = -1` bám mãi IP cũ `10.10.1.10` dù UDP DNS server đã đổi sang `10.10.2.20`, còn `ttl = 1s` cập nhật sang `10.10.2.20` và gửi query mới.

<details><summary>💡 Hint</summary>

Xem `JvmDnsCacheDemo.java`: vì `InetAddressCachePolicy` của chính JDK chỉ đọc `networkaddress.cache.ttl` đúng 1 lần trong toàn bộ vòng đời JVM process, `JvmDnsCacheDemo` tách thành 2 client policy độc lập (`ttl = -1` và `ttl = 1s`) cùng gọi truy vấn UDP thật qua `MiniUdpDnsServer` để bạn chứng kiến cả hai hành vi trong cùng một lần chạy test!
</details>

---

### E7. Đo lượng UDP packets thật của `ndots:5` vs `ndots:2` vs Trailing Dot
**Bối cảnh:** Mở `lab/src/main/java/lab/linux/dns/NdotsResolverSimulator.java` và `lab/solutions/PodDnsConfigSolution.yaml`.
**Yêu cầu:**
1. Gửi truy vấn cho tên miền `api.partner.com` qua `MiniUdpDnsServer` thật bằng 3 cấu hình:
   - `ndots = 5` với 4 `search` domains chuẩn Kubernetes (hỏi cả `A` + `AAAA`).
   - `ndots = 2` với cùng 4 `search` domains.
   - `ndots = 5` nhưng dùng FQDN trailing dot `api.partner.com.`.
2. So sánh tổng số UDP DNS packets server nhận được (`serverQueryCount`) và số `NXDOMAIN` vô ích ở mỗi chế độ.

**Tiêu chí hoàn thành:**
- [ ] Ghi lại chính xác bảng số liệu `totalUdpQueries` và `nxdomainCount` cho 3 cấu hình (`10` vs `2` vs `2`).

<details><summary>💡 Hint</summary>

`api.partner.com` chỉ có `2` dấu chấm (`2 < 5`), nên khi `ndots = 5` nó đi hết 4 `search` domains trước khi hỏi tên gốc (`(4 + 1) × 2 = 10` UDP queries, trong đó `8` cái nhận `NXDOMAIN`)! Còn khi `ndots = 2` (`2 >= 2`), resolver đi thẳng tới `api.partner.com` chỉ tốn `2` queries (`0` `NXDOMAIN`).
</details>

---

### E8. Đọc phân tích `dig +trace` & `/etc/resolv.conf`
**Bối cảnh:** Cho output mô phỏng sau trên Linux:
```text
$ cat /etc/resolv.conf
nameserver 10.0.0.2
nameserver 10.0.0.3
search internal.corp
options timeout:5 attempts:2

$ dig +noall +answer +ttlid payments.example.com
payments.example.com.   42   IN   CNAME   alb-prod-123.ap-southeast-1.elb.amazonaws.com.
alb-prod-123.ap-southeast-1.elb.amazonaws.com.  18  IN  A  54.169.10.22
alb-prod-123.ap-southeast-1.elb.amazonaws.com.  18  IN  A  52.77.88.91
```
**Yêu cầu:**
1. Nếu `10.0.0.2` bị tắc nghẽn không trả lời các gói UDP DNS, điều gì xảy ra với ứng dụng gọi `payments.example.com` và độ trễ tăng thêm tối thiểu bao nhiêu giây trước khi fallback sang `10.0.0.3`?
2. Giải thích vì sao dòng 1 lại là `CNAME` (`TTL 42`) còn dòng 2 & 3 trả về `2` bản ghi `A` có `TTL 18`. Nếu ứng dụng Java lưu cache kết quả này trong **`3600` giây** (hoặc `-1`), rủi ro nào xảy ra với AWS ELB/ALB?

**Tiêu chí hoàn thành:**
- [ ] Tính đúng cú nhảy trễ `5 giây` (`timeout:5`).
- [ ] Giải thích chính xác sự thay đổi IP thường xuyên đằng sau ALB của AWS (`TTL 60s` thực tế).

<details><summary>💡 Hint</summary>

AWS ALB tự scale up/down và thay đổi IP đằng sau tên miền `*.elb.amazonaws.com` liên tục với TTL ngắn (60s, ở đây cache còn 18s)! Nếu JVM cache `-1` hoặc lâu hơn `60s`, các IP cũ của ALB khi scale down bị thu hồi sẽ gây lỗi timeout cho client.
</details>

---

## 🔴 L3 — Deep

### E9. Ticket Production: Failover DB làm JBoss chết cứng tới sáng + Pod K8s thỉnh thoảng treo đúng `5.000 ms`
**Tình huống:**
> Hệ thống có 2 sự cố DNS xuất hiện trong cùng tuần:
> - **Sự cố 1 (JBoss AS 7.1.1 trên VM Linux):** Lúc `01:30` sáng, AWS RDS thực hiện bảo trì failover sang Standby instance, Route 53 cập nhật CNAME `rds-core.internal` từ `10.20.1.10` sang `10.20.2.25` (`TTL = 60s`). Lúc `01:35`, admin kiểm tra trên chính VM JBoss bằng `dig +short rds-core.internal` thấy đã trả về `10.20.2.25`. Tuy nhiên **JBoss vẫn báo `ORA-17002 / SocketTimeoutException` tới `10.20.1.10` suốt cả đêm**, chỉ hết sau khi restart JBoss lúc `07:00`! Kiểm tra JVM startup thấy có cờ `-Djava.security.manager`.
> - **Sự cố 2 (Service mới chạy trên Kubernetes `1.28`):** Microservice gọi API đối tác `https://api.bank-gw.vn/transfer` ở mức `800 req/s`. Trace OpenTelemetry ghi nhận `99%` request hoàn thành trong `45 ms`, nhưng khoảng `1%` request ở `p99` nhảy thẳng lên **`5.045 ms`** (hoặc `10.045 ms`). Kiểm tra CPU CoreDNS đang vọt lên 90%, đồng thời `dmesg` trên Worker Node có dòng `nf_conntrack: table full, dropping packet`.

**Yêu cầu:**
1. Giải thích **cơ chế bên trong** của Sự cố 1 (liên hệ trực tiếp tới `-Djava.security.manager`, `networkaddress.cache.ttl` và `dig` vẫn ra IP mới trong khi JVM bám IP cũ).
2. Giải thích **cơ chế bên trong** của Sự cố 2 (tại sao `api.bank-gw.vn` lại gây `conntrack` full trên CoreDNS và tạo đúng con số nhảy bậc **`5.000 ms`** / **`10.000 ms`**?).
3. Đề xuất **bộ giải pháp khắc phục vĩnh viễn** cho cả JBoss lẫn Kubernetes Pod (kèm cấu hình cụ thể).

**Tiêu chí hoàn thành:**
- [ ] Kết nối chuẩn xác `-Djava.security.manager` → `networkaddress.cache.ttl = -1` trong JDK + connection pool JBoss giữ socket cũ.
- [ ] Giải thích chính xác chuỗi `ndots:5` (`api.bank-gw.vn` chỉ có 2 dấu chấm) → `10` UDP queries/request → `nf_conntrack` drop UDP → `timeout:5` retry (`5.000 ms` / `10.000 ms`).
- [ ] Đưa ra giải pháp đúng chuẩn cả hai môi trường.

<details><summary>💡 Hint</summary>

1. Trên Java, khi `SecurityManager` được bật, giá trị mặc định của `networkaddress.cache.ttl` trong `InetAddressCachePolicy` tự động chuyển thành **`-1` (cache FOREVER)**! Còn lệnh `dig` đi thẳng ra OS/Recursive Resolver chứ không đi qua JVM cache, nên `dig` thấy IP mới còn JBoss vẫn ôm IP cũ! (Cộng thêm connection pool JDBC chưa kịp test/evict socket chết).
2. `api.bank-gw.vn` có `2` dấu chấm `< ndots:5` → mỗi lần resolve tạo ~10 UDP packets → làm tràn `nf_conntrack` → node drop packet UDP port 53 → glibc/musl chờ hết `options timeout:5` (5.000 ms) cho mỗi lần mất gói (`attempts:2` -> 5s hoặc 10s)!
</details>
