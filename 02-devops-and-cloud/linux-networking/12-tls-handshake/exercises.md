# LNX-12 · Bài tập — TLS handshake & certificate chain

> Làm theo thứ tự. **Tự làm trước, chỉ mở hint khi đã thử ≥ 15 phút.** Đáp án ở [solutions.md](./solutions.md) — chỉ mở sau khi đã có câu trả lời của riêng bạn.
> Ghi thời gian làm từng bài vào `my-notes.md` để theo dõi tiến bộ.

| Cấp | Mục đích | Số lượng | Thời gian gợi ý |
| :--- | :--- | :---: | :---: |
| 🟢 L1 — Recall | Nắm chắc TLS 1.3 `1-RTT/0-RTT`, Chain, SNI, KeyStore vs TrustStore, PKIX | 5 | 20' |
| 🟡 L2 — Apply | Tái hiện & sửa đúng lỗi `PKIX`, đàm phán TLS 1.3 + SNI và mTLS trên Java 21 | 3 | 75' |
| 🔴 L3 — Deep | Ticket Production: Chrome xanh nhưng JBoss `PKIX failed`, sau đó lại rớt `api.github.com` | 1 | 90' |

---

## 🟢 L1 — Recall

### E1. Vì sao TLS 1.3 tiết kiệm được 1 RTT so với TLS 1.2, và bẫy của `0-RTT`?
Giải thích sự khác biệt ở bản tin **`ClientHello`** giúp TLS 1.3 hoàn tất handshake chỉ trong **`1 RTT`**; đồng thời cho biết lý do tuyệt đối không được bật `0-RTT Early Data` cho endpoint `POST /api/v1/payment`.

---

### E2. Tại sao Chrome mở HTTPS thành công còn Java / `curl` lại báo `PKIX path building failed`?
Bạn gọi tới `https://partner.example.com`: trên trình duyệt Chrome hiện ổ khóa xanh bình thường, nhưng chạy `curl` hoặc ứng dụng Java 21 đều lỗi xác minh chuỗi chứng chỉ. Cho biết **nguyên nhân phổ biến nhất ở phía server** và lệnh OpenSSL để kiểm chứng ngay giả thuyết này.

---

### E3. Phân biệt KeyStore và TrustStore trong bài toán mTLS
Một dịch vụ JBoss kết nối **mTLS** tới cổng ngân hàng (`https://gw.bank.vn:8443`), đồng thời bản thân JBoss cũng mở port HTTPS nhận traffic. Liệt kê rõ:
1. File `.p12`/`.jks` nào là **KeyStore**, chứa những thành phần gì?
2. File nào là **TrustStore**, chứa những thành phần gì?
3. Điều gì xảy ra nếu server ngân hàng bật `needClientAuth = true` nhưng JBoss chỉ cấu hình `TrustStore` mà quên cấu hình `KeyStore`?

---

### E4. Vai trò của SNI và bẫy thiếu Hostname Verification trong `SSLSocket`
1. **SNI** nằm ở bước nào của TLS handshake và giải quyết vấn đề gì khi 1 Nginx phục vụ 50 domain trên cùng IP port `443`?
2. Khi tự khởi tạo `SSLSocket` bằng `SSLContext` trong Java, nếu chỉ cung cấp `TrustStore` chứa toàn bộ CA công cộng mà **quên** gọi `setEndpointIdentificationAlgorithm("HTTPS")`, lỗ hổng bảo mật nào sẽ xuất hiện?

---

### E5. Bẫy `-Djavax.net.ssl.trustStore` làm gãy toàn bộ kết nối khác
Một dev tạo file `partner-only-truststore.p12` chỉ chứa đúng 1 chứng chỉ CA nội bộ của đối tác, rồi khởi động ứng dụng Java bằng tham số `-Djavax.net.ssl.trustStore=partner-only-truststore.p12`. Ngay lập tức gọi đối tác được nhưng gọi sang `https://api.github.com` và AWS S3 bị nổ **`PKIX path building failed`**! Giải thích nguyên nhân và cách khắc phục.

---

## 🟡 L2 — Apply

### E6. Tái hiện thật lỗi `PKIX path building failed` & sửa chuẩn với Java TrustStore
**Bối cảnh:** Dùng [lab/](./lab/README.md) chạy `TlsLabTest` và `TlsLabRunner`.
**Yêu cầu:**
1. Chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`.
2. Quan sát quy trình `TlsLabDemo`:
   - Sinh khóa & cert thật bằng `keytool` (`server-keystore.p12`, `server.crt` có `SAN=DNS:localhost,IP:127.0.0.1`).
   - Client thử kết nối với một **TrustStore trống** → bắt được chính xác **`SSLHandshakeException`** (`PKIX path building failed`).
   - Import `server.crt` vào `client-truststore.p12`, bật `HTTPS` endpoint identification + `SNIHostName("localhost")` → kết nối thành công và in ra protocol **`TLSv1.3`**.

**Tiêu chí hoàn thành:**
- [ ] `mvn -o -B verify` pass 100%.
- [ ] Chứng minh được khi dùng TrustStore sai/trống thì ném `SSLHandshakeException` (PKIX), còn khi import cert vào TrustStore thì đàm phán `TLSv1.3` thành công.

<details><summary>💡 Hint</summary>

Xem `TlsLabDemo.java` & `CustomTrustStoreSolution.java`: chú ý `SAN` (`-ext SAN=dns:localhost,ip:127.0.0.1`) khi tạo cert bằng `keytool`, vì với `setEndpointIdentificationAlgorithm("HTTPS")`, nếu thiếu `SAN` tương ứng thì kể cả cert có nằm trong TrustStore cũng sẽ bị từ chối ở bước kiểm tra hostname!
</details>

---

### E7. Kiểm chứng mTLS (`needClientAuth = true`) trên Loopback `SSLServerSocket`
**Bối cảnh:** Tiếp tục với `TlsLabDemo.java` và `CustomTrustStoreSolution.java` trong `lab/`.
**Yêu cầu:**
1. Bật chế độ **`server.setNeedClientAuth(true)`** (yêu cầu client phải trình Client Certificate được `server-truststore.p12` tin cậy).
2. Kiểm chứng 2 trường hợp chạy thật trên loopback:
   - Client chỉ có `client-truststore.p12` (không có `client-keystore.p12`) → handshake **thất bại** (`SSLHandshakeException` / `SocketException`).
   - Client có đủ cả `client-keystore.p12` lẫn `client-truststore.p12` → handshake **thành công** và gửi/nhận payload bình thường!

**Tiêu chí hoàn thành:**
- [ ] Pass test kiểm chứng mTLS từ chối client không có cert hợp lệ và chấp nhận client có cert hợp lệ.

<details><summary>💡 Hint</summary>

Ở server mTLS, `SSLContext.init(serverKmf.getKeyManagers(), serverTmf.getTrustManagers(), null)` cần cả `KeyManagerFactory` (chứng minh danh tính server) lẫn `TrustManagerFactory` (xác minh Client Certificate gửi lên)!
</details>

---

### E8. Thực hành OpenSSL & Nginx mTLS (khi có môi trường Linux/Docker)
**Bối cảnh:** Mở `lab/scripts/gen-ca-nginx-mtls.sh` và `lab/scripts/nginx-mtls.conf`.
**Yêu cầu:**
1. Giải thích vai trò của từng file được sinh ra trong script: `rootCA.crt`, `intermediateCA.crt`, `server-fullchain.pem` (`server.crt` + `intermediateCA.crt`), `client.crt` + `client.key`.
2. Tại sao trong `nginx-mtls.conf`, chỉ thị `ssl_certificate` phải trỏ tới `server-fullchain.pem` (chứa Leaf + Intermediate) còn `ssl_client_certificate` trỏ tới `rootCA.crt` (hoặc chain CA xác minh client)?

**Tiêu chí hoàn thành:**
- [ ] Trả lời chính xác vai trò 5 file chứng chỉ và 2 directive Nginx trên.
- [ ] (Nếu có Linux/OpenSSL) chạy thử `bash scripts/gen-ca-nginx-mtls.sh` và kiểm tra bằng `openssl x509 -noout -text`.

<details><summary>💡 Hint</summary>

`ssl_certificate` là thứ Nginx **gửi cho trình duyệt/Java** (nên phải ghép `Leaf + Intermediate` để client nối chuỗi về Root); còn `ssl_client_certificate` là **TrustStore của Nginx** để xác minh chứng chỉ client gửi lên khi `ssl_verify_client on;`.
</details>

---

## 🔴 L3 — Deep

### E9. Ticket Production: Chuỗi sự cố TLS từ `PKIX failed` → sửa bậy làm gãy toàn bộ API công cộng + `protocol_version` trên Java 7
**Tình huống:**
> Một hệ thống **JBoss AS 7.1.1 (Java `1.7.0_80`)** kết nối tới đối tác mới `https://gw.fin-partner.vn`.
> - **Lúc 09:00:** Team deploy lên staging. Chrome trên laptop dev mở `https://gw.fin-partner.vn/health` trả `200 OK` màu xanh, nhưng JBoss log lỗi:
>   `javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException: PKIX path building failed: unable to find valid certification path to requested target`
> - **Lúc 09:30:** Dev chạy `openssl s_client -connect gw.fin-partner.vn:443 -servername gw.fin-partner.vn -showcerts` thấy output chỉ có đúng `0 s:CN=gw.fin-partner.vn` (1 cert duy nhất, `i:CN=DigiCert Global G2 TLS RSA SHA256 2020 CA1`).
> - **Lúc 10:00:** Vì vội, một dev lấy tạm file `partner-leaf.crt` bỏ vào một file mới `only-partner.jks`, rồi thêm `-Djavax.net.ssl.trustStore=/opt/jboss/only-partner.jks` vào `standalone.conf`. Kết quả:
>   1. Lỗi với `gw.fin-partner.vn` vẫn **hoặc** đổi thành `Received fatal alert: handshake_failure / protocol_version`!
>   2. Đồng thời toàn bộ kết nối cũ sang `https://s3.ap-southeast-1.amazonaws.com` và `https://api.sendgrid.com` bỗng đồng loạt chết vì `PKIX path building failed`!

**Yêu cầu:**
1. Giải thích **3 nguyên nhân gốc** nằm sau 3 triệu chứng trên (`09:30` thiếu gì? `10:00` tại sao S3/SendGrid chết? Và tại sao Java `1.7.0_80` dính `handshake_failure / protocol_version`?).
2. Đề xuất **cách xử lý chuẩn production** để vừa giữ an toàn bảo mật, vừa không làm gãy S3/SendGrid, vừa giải quyết dứt điểm bài toán TLS 1.2/1.3 cho JBoss Java 7!

**Tiêu chí hoàn thành:**
- [ ] Chỉ ra đủ 3 lỗ hổng: thiếu Intermediate CA trên server đối tác, `-Djavax.net.ssl.trustStore` ghi đè `cacerts` gốc, và giới hạn `TLSv1.0` mặc định / thiếu `TLS 1.3` của Java 7.
- [ ] Đưa ra kiến trúc sửa đúng chuẩn (yêu cầu đối tác cấu hình fullchain hoặc copy `cacerts` làm base, cộng với đặt **Nginx Reverse Proxy** nội bộ hoặc cấu hình `-Dhttps.protocols=TLSv1.2`).

<details><summary>💡 Hint</summary>

1. Output `openssl s_client` chỉ hiện đúng 1 cert `0 s:CN=gw.fin-partner.vn` mà không có `1 s:CN=DigiCert Global G2 TLS RSA SHA256 2020 CA1` → **Server đối tác cấu hình thiếu Intermediate CA!**
2. Gán `-Djavax.net.ssl.trustStore=only-partner.jks` đã thay thế hoàn toàn `$JAVA_HOME/jre/lib/security/cacerts` → Java mất sạch toàn bộ Root CA công cộng của Amazon/DigiCert/Let's Encrypt!
3. Java `1.7.0_80` mặc định chỉ bật `TLSv1.0` cho client và hoàn toàn **không có `TLSv1.3`**. Nếu đối tác bắt buộc TLS `1.2` (cần `-Dhttps.protocols=TLSv1.2`) hoặc chỉ mở TLS `1.3`/cipher hiện đại, hãy đặt một Nginx proxy nội bộ (`http://127.0.0.1:8085 -> https://gw.fin-partner.vn`) hoặc nâng cấp JVM!
</details>
