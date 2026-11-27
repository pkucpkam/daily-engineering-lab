# LNX-02 · Đáp án — File descriptor & I/O

> ⚠️ **Chỉ mở sau sau khi đã tự làm.** Đọc đáp án mà chưa tự làm = mất 80% giá trị bài tập.
> Mỗi đáp án được bọc trong `<details>` để tránh lộ khi lướt.

---

### E1
<details><summary>Xem đáp án</summary>

**Đáp án:**
- Trong Linux, tên file `/opt/jboss/standalone/log/server.log` chỉ là một liên kết thư mục trỏ tới `inode` của file trên đĩa. Lệnh `rm` chỉ gỡ bỏ tên file đó (giảm *link count* về 0), **nhưng JBoss vẫn đang giữ File Descriptor (ví dụ `fd=4`) mở trỏ tới `inode` đó**.
- Kernel quy định chỉ thu hồi các data blocks (`25 GB`) khi **cả link count == 0 VÀ số FD đang mở == 0**. Do đó `df -h` vẫn báo đầy `100%`!
- **Cách xử lý đúng ngay lập tức (không cần restart JBoss):** Dùng `lsof -nP +L1 | grep server.log` tìm `<PID>` và `<FD>` (ví dụ `4`), rồi làm rỗng trực tiếp nội dung qua procfs: `truncate -s 0 /proc/<PID>/fd/4` (hoặc `: > /proc/<PID>/fd/4`). Sau đó cấu hình `logrotate` dùng `copytruncate` hoặc xoay log chuẩn qua log4j/JBoss logging.
</details>

---

### E2
<details><summary>Xem đáp án</summary>

**Đáp án:**
- `ulimit -n` kiểm tra giới hạn của **shell SSH hiện tại** (có thể đã được cấu hình riêng cho user login hoặc shell của bạn), chứ **không phải** giới hạn của service Java đang chạy nền!
- Nếu service Java chạy qua `systemd`, systemd **không đọc `/etc/security/limits.conf`** (mà dùng `DefaultLimitNOFILE` hoặc `LimitNOFILE=` trong file `.service`). Hoặc nếu JBoss chạy bằng một user khác / trước khi chỉnh `limits.conf`, process kế thừa trần `1024` cũ.
- **Lệnh kiểm tra chính xác:** `cat /proc/<pid>/limits | grep "Max open files"` (thay `<pid>` bằng PID thật của JVM).
</details>

---

### E3
<details><summary>Xem đáp án</summary>

**Đáp án:**
- **`EMFILE` (`Too many open files`):** Process hiện tại chạm trần `RLIMIT_NOFILE` của riêng nó (`/proc/<pid>/limits`). Kiểm tra: `ls -1 /proc/<pid>/fd | wc -l` xấp xỉ Soft Limit của process đó.
- **`ENFILE` (`Too many open files in system`):** Tổng số open file description của toàn bộ các process trên máy chạm trần `fs.file-max` (`/proc/sys/fs/file-nr` so với `/proc/sys/fs/file-max`).
- Trong **99% sự cố Java**, đó là **`EMFILE`** (giới hạn riêng của tiến trình Java/JBoss bị cạn do `ulimit -n = 1024/4096` hoặc leak FD trong code).
</details>

---

### E4
<details><summary>Xem đáp án</summary>

**Đáp án:**
- Mỗi lần gọi `url.openConnection()` + `getResponseCode()`, JVM mở một socket TCP mới — tức chiếm **1 File Descriptor** trong bảng FD của process Java.
- Vì không đọc hết stream và không đóng stream (`InputStream`/`ErrorStream`), socket vẫn giữ FD mở (thậm chí không thể trả lại vào HTTP keep-alive pool) và nằm chờ GC thu gom ở tương lai bất định. Với 500 lần/phút, chỉ sau ~2 phút sẽ tích tụ ~1.000 FD!
- **Mọi chức năng khác đều bị ảnh hưởng dây chuyền:** Vì bảng FD dùng chung cho toàn bộ tiến trình JVM (cả socket, file log, JAR file, pipe), một khi chạm `RLIMIT_NOFILE`, cả `FileOutputStream` ghi log, mở kết nối DB mới, hay nhận HTTP request từ client vào JBoss đều nổ `Too many open files`!
</details>

---

### E5
<details><summary>Xem đáp án</summary>

**Đáp án:**
- Ở chế độ **Blocking** mặc định: khi receive buffer trong kernel rỗng, syscall `read(fd)` đưa thread đó vào trạng thái ngủ **`S` (Interruptible Sleep)** — thread OS đứng chờ cho đến khi có dữ liệu từ mạng, socket đóng, hoặc timeout.
- Ở chế độ **Non-blocking (`O_NONBLOCK`)**: `read(fd)` kiểm tra receive buffer thấy trống thì **trả về ngay lập tức `-1` với `errno = EAGAIN` / `EWOULDBLOCK`** (trong Java NIO `SocketChannel.read(buf)` trả về `0`). Thread không bị đưa vào trạng thái ngủ và có thể lập tức xử lý kênh khác.
</details>

---

### E6
<details><summary>Xem đáp án</summary>

**Hướng giải:**
- Chạy `mvn -o -B verify` trong `lab/` thấy `FdLeakTest` chứng minh rõ: với `softLimit = 16`, mở 13 socket leak chạm ngay giới hạn tại socket thứ 14 (`TooManyOpenFilesException: EMFILE`), làm `openFile("server.log")` cũng fail theo.
- Khi đóng qua `try-with-resources`, mỗi vòng lặp giải phóng FD ngay trong `close()`, giúp chạy `1.000` (hay `100.000`) request liên tiếp chỉ tốn tối đa `1` FD socket tại một thời điểm.

**Code lời giải tham khảo cho E6:** xem [lab/solutions/AutoClosingHttpWrapperTest.java](./lab/solutions/AutoClosingHttpWrapperTest.java).
</details>

---

### E7
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. **Tổng số FD đang mở:** `892 + 94 + 21 + 12 + 5 = 1.024` FD — **bằng chính xác Soft Limit `1024`** (`Max open files: 1024 / 4096`), nên mọi lệnh `open()`/`socket()` thứ 1025 đều nổ `EMFILE`!
2. **Thủ phạm:** Có ~850 dòng `IPv4 TCP 10.20.1.5:<ephemeral_port> -> 10.20.9.88:8080 (CLOSE_WAIT)`. Đây là **kết nối đi ra (outbound)** từ JBoss (`10.20.1.5`) gọi tới dịch vụ `10.20.9.88:8080`.
3. **Ý nghĩa `CLOSE_WAIT`:** Phía server đích (`10.20.9.88:8080`) đã đóng kết nối xong (gửi gói `FIN`), kernel phía JBoss đã xác nhận (`ACK`) và giữ socket ở `CLOSE_WAIT` để **chờ code Java gọi `socket.close()` / `response.close()`**. Vì code Java quên đóng nên FD kẹt vĩnh viễn ở đây cho tới khi làm cạn sạch 1024 slot!
</details>

---

### E8
<details><summary>Xem đáp án</summary>

**Đáp án:**
- Trong `NonBlockingIoDemo`, `channel.configureBlocking(false)` đặt cờ `O_NONBLOCK` lên FD của socket qua `fcntl`.
- Khi gọi `channel.read(buf)` trên loopback chưa có dữ liệu gửi sang, kernel trả ngay `EAGAIN` và Java NIO trả về `0` bytes trong thời gian dưới 1 ms (chỉ trả `-1` khi phía bên kia đóng kết nối hoàn toàn — EOF).
- Ngược lại nếu để `configureBlocking(true)` mà không đặt timeout, thread gọi `read()` sẽ bị scheduler Linux cho ngủ ở trạng thái `S` mãi mãi cho tới khi bên kia gửi ít nhất 1 byte hoặc đóng socket.
</details>

---

### E9
<details><summary>Xem đáp án</summary>

<details open><summary>Phân tích & Xử lý 2 sự cố Production JBoss</summary>

**1. Chuỗi nhân-quả:**
- **Sự cố 1:** Job mới dùng Apache `HttpClient 4` gọi `10.10.5.20:8443` nhưng quên gọi `response.close()` / `EntityUtils.consumeQuietly(entity)` (hoặc tạo mới `HttpClient` mỗi lần gọi). Mỗi HTTP call giữ lại 1 socket FD (`ESTABLISHED` hoặc `CLOSE_WAIT`). Trong khi đó JBoss chạy với **Soft Limit chỉ `1024` FD** (`/proc/<pid>/limits`). Sau 2 giờ, tổng số socket leak + JAR/file của JBoss chạm `1024`, kernel trả `EMFILE` (`Too many open files`) khiến cả đăng nhập/đọc file cấu hình cũng chết!
- **Sự cố 2:** `rm -f server.log` chỉ xóa directory entry (tên file), còn JBoss vẫn giữ mở FD trỏ vào inode `40 GB` đang ghi log nên kernel chưa giải phóng các data blocks trên đĩa (`df -h` vẫn 99%).

**2. Lệnh xử lý khẩn cấp cho Sự cố 2 (0 downtime):**
```bash
# Bước 1: Tìm PID JBoss và số FD trỏ tới file (deleted)
lsof -nP +L1 -p <JBOSS_PID> | grep server.log
# Ví dụ output: java 4821 jboss 4w REG ... 42949672960 ... /opt/jboss/standalone/log/server.log (deleted)

# Bước 2: Cắt rỗng file qua procfs (FD số 4) -> giải phóng ngay 40 GB mà JBoss không crash!
truncate -s 0 /proc/<JBOSS_PID>/fd/4
```
*(Sau đó nhớ giảm log level khỏi `DEBUG` và cấu hình log rotation trong JBoss `standalone.xml` dùng `periodic-rotating-file-handler` hoặc `size-rotating-file-handler`.)*

**3. Hai bước sửa dứt điểm cho Sự cố 1:**
- **Tầng OS / Service:** Nếu chạy systemd, đặt `LimitNOFILE=65536` trong `jboss.service` (`systemctl daemon-reload` + restart); nếu dùng script `init.d`/user shell `jboss`, cấu hình `/etc/security/limits.d/99-jboss.conf` (`jboss soft nofile 65536`, `jboss hard nofile 65536`) và xác minh lại bằng `cat /proc/<new_pid>/limits`.
- **Tầng Code Java 7:** Tái sử dụng 1 instance `CloseableHttpClient` với `PoolingHttpClientConnectionManager` có timeout rõ ràng, và luôn đóng `CloseableHttpResponse` bằng `try-with-resources`:
  ```java
  try (CloseableHttpResponse resp = httpClient.execute(request)) {
      int status = resp.getStatusLine().getStatusCode();
      String body = EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
      // xử lý body...
  }
  ```

</details>
</details>

---

## Self-check (từ README §9) — đáp án gợi ý
<details><summary>Xem</summary>

1. **3 tầng FD & `rm` file log:** Per-process FD table → System-wide Open File Description table (offset, flags) → Inode/Socket. `rm` chỉ giảm link count của tên file về 0; chừng nào JVM còn giữ FD mở tới `inode`, kernel vẫn giữ data blocks trên đĩa (chỉ thả khi link count = 0 và FD count = 0).
2. **4 bước điều tra `Too many open files`:** (1) `cat /proc/$PID/limits | grep "Max open files"`; (2) `ls -1 /proc/$PID/fd | wc -l`; (3) `lsof -nP -p $PID | awk 'NR>1 {print $5}' | sort | uniq -c | sort -rn`; (4) soi chi tiết `IPv4` (`ss -tanp` / `CLOSE_WAIT`) hoặc `REG` / `(deleted)`.
3. **HTTP/JDBC connection không đóng:** Mỗi socket chiếm 1 FD trong chính bảng FD dùng chung của process JVM. Khi FD bị rò rỉ tới chạm trần `RLIMIT_NOFILE`, mọi `open()`/`socket()` mới (ghi log, mở JAR, accept client) đều nổ `EMFILE`.
4. **`EMFILE` vs `ENFILE` & `ulimit -n`:** `EMFILE` là hết FD của riêng process (`RLIMIT_NOFILE`), `ENFILE` là hết FD toàn OS (`fs.file-max`). `ulimit -n` chỉ đọc giới hạn của shell hiện tại; service `systemd` bỏ qua `limits.conf` nên phải kiểm tra `/proc/<pid>/limits`.
5. **Blocking vs `O_NONBLOCK`:** Blocking `read(fd)` làm thread ngủ ở trạng thái `S` nếu receive buffer rỗng; `O_NONBLOCK` làm kernel trả về ngay `-1` (`EAGAIN` / Java NIO trả `0` bytes) để thread không bị treo.
</details>
