# LNX-12 · Đáp án — TLS handshake & certificate chain

> ⚠️ **Chỉ mở sau sau khi đã tự làm.** Đọc đáp án mà chưa tự làm = mất 80% giá trị bài tập.
> Mỗi đáp án được bọc trong `<details>` để tránh lộ khi lướt.

---

### E1
<details><summary>Xem đáp án</summary>

**Đáp án:**
- **TLS 1.3 giảm từ `2 RTT` xuống `1 RTT`:** Trong TLS 1.2, client phải đợi server chọn thuật toán ở `ServerHello` rồi mới gửi `ClientKeyExchange` ở vòng thứ 2. Trong **TLS 1.3**, client đoán trước thuật toán phổ biến (`x25519`/`P-256`) và nhét luôn **`KeyShare`** ngay vào bản tin **`ClientHello`** đầu tiên → server tính được ngay khóa bí mật đối xứng và trả `ServerHello + KeyShare + Certificate (đã mã hóa) + Finished` trong đúng **1 RTT**, cho phép client gửi HTTP request ngay sau đó!
- **Rủi ro của `0-RTT Early Data`:** Gói `0-RTT` gửi kèm ngay trong `ClientHello` **không có cơ chế chống phát lại (*replay protection*) ở tầng TLS** vì chưa qua tương tác random mới giữa hai bên. Kẻ tấn công mạng có thể bắt gói `ClientHello + POST /transfer` và gửi lặp lại nhiều lần khiến hệ thống thực hiện **giao dịch trùng lặp** → chỉ bật `0-RTT` cho API đọc/idempotent (`GET`), cấm với `POST`.
</details>

---

### E2
<details><summary>Xem đáp án</summary>

**Đáp án:**
- **Nguyên nhân phổ biến nhất:** Server `partner.example.com` cấu hình **thiếu Intermediate CA** trong file chứng chỉ (chỉ gửi mỗi Leaf cert thay vì ghép `Leaf + Intermediate` thành `fullchain.pem`).
- Chrome vẫn qua được nhờ tự động tải bù Intermediate CA qua URL trong trường **AIA (Authority Information Access)** hoặc lấy từ cache trình duyệt; còn Java và `curl` **không tự tải AIA**, nên không nối được chuỗi về Root CA trong `cacerts` → nổ **`PKIX path building failed`**.
- **Lệnh OpenSSL kiểm chứng:** `openssl s_client -connect partner.example.com:443 -servername partner.example.com -showcerts` — đếm xem mục `Certificate chain` có đủ `0 s:...` (Leaf) và `1 s:...` (Intermediate) hay không.
</details>

---

### E3
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. **KeyStore** (`.p12` / `.jks`): Chứa **Private Key + Public Certificate Chain của chính ứng dụng JBoss** (dùng để chứng minh danh tính của mình khi làm HTTPS Server, hoặc khi Client xác thực mTLS).
2. **TrustStore** (`cacerts` hoặc `truststore.p12`): Chỉ chứa **Public Certificate của các Root/Intermediate CA** (hoặc cert đối tác tin cậy) — tuyệt đối không chứa Private Key của đối tác — dùng để xác minh chữ ký của đối tác.
3. Nếu ngân hàng bật `needClientAuth = true` nhưng JBoss chỉ có `TrustStore` mà quên cấu hình `KeyStore` (Client Certificate): ở bước mTLS khi server gửi `CertificateRequest`, JBoss gửi lại danh sách cert rỗng → server ngân hàng lập tức đóng kết nối với cảnh báo TLS **`bad_certificate` / `certificate_required`**!
</details>

---

### E4
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. **SNI (Server Name Indication)** nằm ngay trong bản tin **`ClientHello`** (dạng **plaintext** trước khi thương lượng mã hóa). Nó giúp Nginx biết client đang muốn truy cập tên miền nào (vd `a.com` vs `b.com`) trên cùng 1 IP + port `443` để chọn đúng **KeyStore / chứng chỉ** tương ứng gửi lại ở `ServerHello`.
2. Nếu tự dựng `SSLContext` dùng `SSLSocket` mà **quên** `params.setEndpointIdentificationAlgorithm("HTTPS")`: Java chỉ kiểm tra xem chứng chỉ đối tác có chữ ký hợp lệ từ 1 CA trong TrustStore hay không, nhưng **không kiểm tra xem tên miền `SAN` trong cert có khớp với host bạn đang gọi hay không**! Kẻ tấn công chỉ cần mua một chứng chỉ Let's Encrypt hợp lệ cho `attacker-site.com`, chặn đường mạng (MITM) và chìa chứng chỉ `attacker-site.com` ra — `SSLSocket` vẫn chấp nhận bình thường!
</details>

---

### E5
<details><summary>Xem đáp án</summary>

**Đáp án:**
Khi truyền **`-Djavax.net.ssl.trustStore=partner-only-truststore.p12`**, JVM **thay thế hoàn toàn** file `$JAVA_HOME/lib/security/cacerts` mặc định bằng file `partner-only-truststore.p12` (không gộp chung!). Vì file mới chỉ có 1 cert nội bộ và thiếu sạch hơn 100 Root CA công cộng của DigiCert, GlobalSign, Let's Encrypt, Amazon Root CA..., mọi kết nối ra `api.github.com` và AWS S3 lập tức vỡ với `PKIX path building failed`!
**Cách sửa đúng:** Copy `$JAVA_HOME/lib/security/cacerts` thành `app-truststore.p12`, import thêm CA nội bộ vào bản copy đó, rồi mới truyền `-Djavax.net.ssl.trustStore=app-truststore.p12` (hoặc tạo `SSLContext` riêng trong code chỉ dùng cho đối tác đó).
</details>

---

### E6
<details><summary>Xem đáp án</summary>

**Hướng giải:**
Khi chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`:
- `TlsLabDemo` gọi `keytool` của JDK tạo keystore có `SAN=dns:localhost,ip:127.0.0.1`, sau đó mở `SSLServerSocket` thật trên `127.0.0.1`.
- Lần gọi 1 (dùng `empty-truststore.p12` không chứa cert server): ném đúng **`SSLHandshakeException`** (thông điệp `PKIX path building failed: unable to find valid certification path to requested target`).
- Lần gọi 2 (import `server.crt` vào `client-truststore.p12`, bật `HTTPS` endpoint identification & `SNIHostName("localhost")`): kết nối hoàn tất và in giao thức **`TLSv1.3`**.
</details>

---

### E7
<details><summary>Xem đáp án</summary>

**Hướng giải:**
Khi `server.setNeedClientAuth(true)`:
- Server dùng `server-keystore.p12` để chứng minh danh tính mình và `server-truststore.p12` (đã import `client.crt`) để xác minh client.
- Client không nạp `KeyManagerFactory` (`client-keystore.p12`): server từ chối handshake (`SSLHandshakeException` / `SocketException`).
- Client nạp đủ cả `client-keystore.p12` và `client-truststore.p12`: trao đổi chứng chỉ 2 chiều thành công và nhận payload `"MTLS_OK"`.

**Code giải pháp mẫu TrustStore + SNI + Hostname Verification:** xem [lab/solutions/CustomTrustStoreSolution.java](./lab/solutions/CustomTrustStoreSolution.java).
</details>

---

### E8
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. Vai trò từng file:
   - `rootCA.crt`: Chứng chỉ Root CA tự ký làm neo tin cậy (*trust anchor*).
   - `intermediateCA.crt`: Chứng chỉ CA trung gian do `rootCA` ký.
   - `server-fullchain.pem`: Ghép `server.crt` (Leaf) + `intermediateCA.crt` theo thứ tự từ dưới lên để gửi cho client nối chuỗi tới `rootCA`.
   - `client.crt` & `client.key`: Leaf cert + Private Key của client dùng để xác thực mTLS khi Nginx yêu cầu.
2. **`ssl_certificate`** trỏ tới `server-fullchain.pem` để Nginx gửi đủ chuỗi Leaf + Intermediate cho client; còn **`ssl_client_certificate`** là TrustStore phía Nginx dùng để xác minh `client.crt` khi bật `ssl_verify_client on;`.
</details>

---

### E9
<details><summary>Xem đáp án</summary>

<details open><summary>Phân tích sự cố TLS trên JBoss Java 7: Thiếu Intermediate + Ghi đè cacerts + TLS version</summary>

**1. Phân tích 3 nguyên nhân gốc:**
1. **Lỗi ban đầu lúc `09:00` (`PKIX path building failed`):** Output `openssl s_client` lúc `09:30` chỉ trả duy nhất `0 s:CN=gw.fin-partner.vn` mà không hề thấy chứng chỉ số `1` (`DigiCert Global G2 TLS RSA SHA256 2020 CA1`). Điều đó chứng tỏ **server đối tác cấu hình thiếu Intermediate CA**! Chrome tự lấy bù qua AIA nên vẫn xanh, còn Java thì đứt chuỗi xác minh!
2. **Lỗi S3 & SendGrid chết lúc `10:00`:** Dev tạo file mới `only-partner.jks` chỉ có 1 cert rồi truyền `-Djavax.net.ssl.trustStore=/opt/jboss/only-partner.jks`. Tham số này **ghi đè hoàn toàn** `$JAVA_HOME/jre/lib/security/cacerts`, khiến JVM mất hết toàn bộ Root CA công cộng của AWS S3 và SendGrid!
3. **Lỗi `protocol_version / handshake_failure` trên `Java 1.7.0_80`:** Java 7 mặc định ở client chỉ bật **`TLSv1.0`** (`TLSv1.2` bị tắt mặc định ở client socket Java 7, và **`TLSv1.3` hoàn toàn không tồn tại trên Java 7**). Nếu `gw.fin-partner.vn` yêu cầu tối thiểu TLS 1.2 (hoặc TLS 1.3 / ECDHE cipher mới), Java 7 handshake gãy ngay lập tức!

**2. Giải pháp chuẩn production:**
1. **Không sửa bằng TrustAll:** Yêu cầu đối tác bổ sung đủ Intermediate CA (`DigiCert Global G2 ...`) vào cấu hình HTTPS server của họ; trong lúc chờ (hoặc nếu dùng CA nội bộ), **copy `cacerts` gốc** thành `jboss-truststore.jks` rồi mới import đúng CA cert vào bản copy đó — đảm bảo không làm rớt S3/SendGrid!
2. **Xử lý TLS version cho Java 7 / JBoss AS 7.1.1:**
   - Nếu đối tác còn hỗ trợ TLS 1.2 với cipher tương thích Java 7: thêm `-Dhttps.protocols=TLSv1.2 -Djdk.tls.client.protocols=TLSv1.2` vào `standalone.conf`.
   - **Giải pháp triệt để và an toàn nhất cho Java 7 legacy:** Đặt một **Nginx Reverse Proxy nội bộ** ngay trên cùng VM/LAN (JBoss gọi `http://127.0.0.1:8085` → Nginx gọi `https://gw.fin-partner.vn` bằng **TLS 1.3 / 1.2** hiện đại với OpenSSL mới nhất và quản lý mTLS/TrustStore tập trung tại Nginx)!

</details>
</details>

---

## Self-check (từ README §9) — đáp án gợi ý
<details><summary>Xem</summary>

1. **TLS 1.3 `1-RTT` & rủi ro `0-RTT`:** Client gửi sẵn **`KeyShare`** ngay trong `ClientHello` đầu tiên (không phải đợi `ServerHello` mới gửi key exchange như TLS 1.2), nên hoàn tất handshake trong `1 RTT`. `0-RTT` không có chống replay ở tầng TLS nên dễ bị tấn công phát lại nếu dùng cho `POST /transfer`.
2. **Certificate Chain:** Leaf (chứa `SAN` + Public Key server) → Intermediate CA (ký bởi Root, **server bắt buộc phải gửi kèm** `fullchain`) → Root CA (tự ký, nằm sẵn trong TrustStore client, server không cần gửi).
3. **KeyStore vs TrustStore & mTLS:** KeyStore giữ **Private Key + Cert của chính mình**; TrustStore giữ **CA công khai tin cậy để verify đối tác**. Với mTLS, cả Server lẫn Client đều phải có đủ cả **KeyStore và TrustStore**.
4. **Xử lý `PKIX path building failed` chuẩn:** (1) Check `openssl s_client -showcerts` xem server có thiếu Intermediate CA không; (2) Xác định đúng `JAVA_HOME`/TrustStore đang chạy; (3) Copy `cacerts` gốc rồi `keytool -importcert` CA tin cậy vào bản copy (hoặc nạp `SSLContext` riêng); (4) Tuyệt đối không dùng `TrustAllManager`.
5. **SNI (Server Name Indication):** Nằm ở `ClientHello` dưới dạng plaintext; cho phép Nginx/ALB biết client đang gọi tên miền nào để chọn đúng chứng chỉ trước khi giải mã HTTP `Host` header.
</details>
