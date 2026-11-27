# LNX-02 · Bài tập — File descriptor & I/O

> Làm theo thứ tự. **Tự làm trước, chỉ mở hint khi đã thử ≥ 15 phút.** Đáp án ở [solutions.md](./solutions.md) — chỉ mở sau khi đã có câu trả lời của riêng bạn.
> Ghi thời gian làm từng bài vào `my-notes.md` để theo dõi tiến bộ.

| Cấp | Mục đích | Số lượng | Thời gian gợi ý |
| :--- | :--- | :---: | :---: |
| 🟢 L1 — Recall | Nhớ & hiểu FD table, ulimit, lsof, O_NONBLOCK | 5 | 20' |
| 🟡 L2 — Apply | Tái hiện & dập FD leak trong Java lab + soi script lsof | 3 | 70' |
| 🔴 L3 — Deep | Điều tra ticket `Too many open files` + đĩa đầy 100% trên JBoss | 1 | 90' |

---

## 🟢 L1 — Recall

### E1. Vì sao xóa file log đang mở bằng `rm` mà `df -h` vẫn báo đầy 100%?
Một đồng nghiệp thấy đĩa chứa log JBoss đầy 100%, liền chạy `rm /opt/jboss/standalone/log/server.log` (dung lượng `25 GB`). Sau khi xóa, `ls -lh` không còn thấy `server.log`, nhưng `df -h` vẫn báo ổ đĩa đầy `100%`. Giải thích chính xác cơ chế kernel ở 3 tầng FD và cách xử lý đúng không cần restart JBoss ngay lập tức.

---

### E2. Shell `ulimit -n` vs `/proc/<pid>/limits`
Bạn SSH vào server production, chạy `ulimit -n` thấy `65536`, nhưng ứng dụng Java chạy dưới dạng service vẫn báo `Too many open files` khi mới có ~`1.020` kết nối đồng thời. Giải thích vì sao hai con số này lại lệch nhau và câu lệnh nào mới cho biết giới hạn thật của JVM?

---

### E3. Phân biệt `EMFILE` và `ENFILE`
Khi Java ném lỗi `Too many open files`, làm sao phân biệt đây là lỗi `EMFILE` (đụng trần từng process) hay `ENFILE` (đụng trần toàn hệ điều hành)? Trong 99% sự cố ứng dụng Java trên Linux là lỗi nào?

---

### E4. Quên đóng `InputStream` của HTTP response trong Java
Một hàm gọi API nội bộ bằng `HttpURLConnection`:
```java
HttpURLConnection conn = (HttpURLConnection) url.openConnection();
int code = conn.getResponseCode();
if (code != 200) {
    return false;
}
```
Đoạn code này không đọc `ErrorStream` hay `InputStream`, cũng không gọi `disconnect()` / `close()`. Nếu hàm này được gọi 500 lần/phút khi downstream trả lỗi `500`, chuyện gì xảy ra với bảng FD của JVM và những chức năng tưởng chừng không liên quan (ví dụ ghi log ra file) có bị ảnh hưởng không?

---

### E5. Blocking `read(fd)` vs Non-blocking `O_NONBLOCK`
Nếu một process mở 1 socket ở chế độ Blocking mặc định và gọi `read()` khi client chưa gửi dữ liệu, thread đó rơi vào trạng thái nào trong Linux? Nếu chuyển FD sang `O_NONBLOCK` (`configureBlocking(false)`), giá trị trả về / hành vi syscall khác gì?

---

## 🟡 L2 — Apply

### E6. Tái hiện FD exhaustion với `BoundedFdTable` & Loopback Sockets
**Bối cảnh:** Dùng [lab/](./lab/README.md) để thực hành trên `BoundedFdTable` và `FdLeakLabRunner`.
**Yêu cầu:**
1. Chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`.
2. Quan sát kịch bản `BoundedFdTable` (giả lập `RLIMIT_NOFILE = 16`, trong đó `0,1,2` dành cho stdin/stdout/stderr):
   - Mở 13 HTTP socket không đóng → đến socket thứ 14 ném `TooManyOpenFilesException` (`EMFILE`), và thao tác hoàn toàn khác như `openFile("server.log")` cũng **chết theo**!
   - Sau đó sửa sang `try-with-resources` (`AutoCloseable`) và chứng minh chạy lặp **1.000** lần liên tiếp vẫn không bao giờ vượt quá `4` FD mở cùng lúc!
3. Mở rộng hoặc viết JUnit test mới (đối chiếu với `lab/solutions/AutoClosingHttpWrapperTest.java`) bọc một HTTP client giả lập bị leak thành `AutoCloseable` dùng `try-with-resources`.

**Tiêu chí hoàn thành:**
- [ ] `mvn -o -B verify` pass 100%.
- [ ] Chứng minh chạy 1.000 vòng lặp trong giới hạn `softLimit = 16` thành công 100% khi đóng FD đúng cách.

<details><summary>💡 Hint</summary>

Trong `BoundedFdTable`, mỗi lần gọi `acquire(...)` cấp `fd` nhỏ nhất đang trống; khi `close()` trả `fd` về, lần mở kế tiếp dùng lại chính `fd` đó nên tổng số FD mở cùng lúc chỉ bằng số resource sống tại cùng một thời điểm.
</details>

---

### E7. Chẩn đoán đầu ra `lsof` mẫu & Script Linux `reproduce-emfile.sh`
**Bối cảnh:** Trên máy Linux production của một app Java gặp sự cố `Too many open files`, bạn chạy `lsof -nP -p 4821` và thu được thống kê sau:
```text
$ cat /proc/4821/limits | grep "Max open files"
Max open files            1024                 4096                 files

$ lsof -nP -p 4821 | awk 'NR>1 {print $5}' | sort | uniq -c | sort -rn
    892 IPv4
     94 REG
     21 FIFO
     12 DIR
      5 unix
```
Và phần chi tiết `IPv4`:
```text
java 4821 jboss  140u  IPv4  918271  0t0  TCP 10.20.1.5:48210->10.20.9.88:8080 (CLOSE_WAIT)
java 4821 jboss  141u  IPv4  918272  0t0  TCP 10.20.1.5:48212->10.20.9.88:8080 (CLOSE_WAIT)
... (lặp lại ~850 dòng tương tự)
```
**Yêu cầu:**
1. Cộng tổng số FD đang mở và so sánh với Soft/Hard limit.
2. Kết luận nguyên nhân gốc: lỗi nằm ở phía server nhận request hay phía code Java đang gọi đi `10.20.9.88:8080`? Giải thích trạng thái `CLOSE_WAIT` này có ý nghĩa gì đối với bảng FD.
3. Đọc `lab/scripts/reproduce-emfile.sh` và `lab/scripts/inspect-java-fds.sh` để nắm quy trình tái hiện & chẩn đoán khi có môi trường Linux/WSL.

**Tiêu chí hoàn thành:**
- [ ] Tính đúng tổng FD (`1024`) vừa chạm đúng Soft Limit (`1024`).
- [ ] Chỉ ra đúng thủ phạm là code gọi tới `10.20.9.88:8080` không đóng socket phía Java (client đã đóng phía nó gửi `FIN`, nhưng Java giữ FD không gọi `close()` nên kẹt ở `CLOSE_WAIT`).

<details><summary>💡 Hint</summary>

`892 + 94 + 21 + 12 + 5 = 1024` (cộng thêm stdin/out/err hay các dòng CHR khác nếu có — chạm trần Soft Limit `1024`!). Xem thêm trạng thái `CLOSE_WAIT`: bên remote (`10.20.9.88:8080`) đã gửi `FIN` đóng kết nối, kernel phía Java đã `ACK` và chuyển socket vào `CLOSE_WAIT` để **chờ tiến trình Java gọi `close()`**, nhưng code Java không bao giờ gọi `close()`.
</details>

---

### E8. Thực hành Blocking vs Non-blocking `SocketChannel`
**Bối cảnh:** Mở `lab/src/main/java/lab/linux/fd/NonBlockingIoDemo.java`.
**Yêu cầu:**
1. Giải thích lý do trong test `NonBlockingIoDemo`, khi server chưa gửi dữ liệu gì, gọi `channel.read(buffer)` ở chế độ `configureBlocking(false)` trả về `0` ngay tức thì, trong khi chế độ `configureBlocking(true)` (nếu không có `soTimeout`) sẽ treo thread vô thời hạn.
2. Giải thích mối liên hệ giữa `configureBlocking(false)` ở Java NIO và syscall `fcntl(fd, F_SETFL, O_NONBLOCK)` trên Linux.

**Tiêu chí hoàn thành:**
- [ ] Pass test `nonBlockingReadReturnsZeroImmediatelyWhenNoDataAvailable` trong `FdLeakTest`.

<details><summary>💡 Hint</summary>

Trên Linux, OpenJDK `SocketChannelImpl.configureBlocking(false)` gọi thẳng `IOUtil.configureBlocking(fd, false)` → syscall `fcntl(fd, F_SETFL, flags | O_NONBLOCK)`. Khi buffer nhận rỗng, kernel trả `-1` với `errno = EAGAIN`, JVM chuyển thành giá trị trả về `0` bytes.
</details>

---

## 🔴 L3 — Deep

### E9. Ticket Production kép trên JBoss AS 7.1.1: Sáng nổ `Too many open files`, trưa đĩa `/var` đầy 100%
**Tình huống:**
> Hệ thống **JBoss AS 7.1.1** chạy trên RHEL (systemd / init script):
> - **Sự cố 1 (09:15):** Một job đồng bộ khách hàng mới triển khai tối qua dùng Apache `HttpClient 4` gọi sang CRM (`10.10.5.20:8443`). Sau 2 giờ chạy, JBoss báo `java.net.SocketException: Too many open files`, và người dùng không đăng nhập được nữa vì JBoss không mở nổi file đọc cấu hình/JAR. Ops thử chạy `ulimit -n` bằng user `root` thấy `1024`, nhưng khi kiểm tra `cat /proc/<jboss_pid>/limits` thấy `Soft=1024, Hard=4096`.
> - **Sự cố 2 (12:30):** Để "giải phóng ổ đĩa" đang bị log DEBUG phình lên `40 GB` (`/opt/jboss/standalone/log/server.log`), một bạn Ops chạy `rm -f /opt/jboss/standalone/log/server.log`. Dung lượng ổ đĩa `df -h` vẫn giữ nguyên `99%` không giảm byte nào!

**Yêu cầu:**
1. Lập chuỗi nhân-quả cho cả **Sự cố 1** và **Sự cố 2** (giải thích ở mức FD table + Apache HttpClient + inode).
2. Đưa ra **lệnh xử lý khẩn cấp không làm mất service** cho Sự cố 2 (`lsof +L1` + `/proc/<pid>/fd/<fd>` truncate) và giải thích vì sao phải dùng `truncate` / `copytruncate` thay vì `rm`.
3. Đưa ra **2 bước sửa dứt điểm** cho Sự cố 1 (1 bước ở tầng OS/JBoss limit, 1 bước ở tầng code Java 7/HttpClient).

**Tiêu chí hoàn thành:**
- [ ] Nêu đủ lệnh tra cứu `/proc/<pid>/fd/<fd>` có symlink trỏ tới `(deleted)` và lệnh `> /proc/<pid>/fd/<fd>` (hoặc `truncate -s 0`) giải phóng dung lượng ngay.
- [ ] Sửa đúng cách đóng `CloseableHttpResponse` / `EntityUtils.consumeQuietly` + cấu hình `LimitNOFILE=65536` (hoặc `limits.d/99-jboss.conf` cho user `jboss`).

<details><summary>💡 Hint</summary>

1. Chạy `lsof -nP +L1 -p <jboss_pid>` sẽ thấy dòng có `(deleted)` và số FD (ví dụ FD `4`). Trên Linux, ghi đè rỗng trực tiếp qua `/proc/<pid>/fd/4`: `: > /proc/<pid>/fd/4` (hoặc `truncate -s 0 /proc/<pid>/fd/4`) giải phóng ngay `40 GB` data blocks mà vẫn giữ nguyên FD mở cho JBoss ghi tiếp!
2. Với `HttpClient 4`: đóng response và/hoặc tiêu thụ entity trong `try/finally` (hoặc `try-with-resources` trên `CloseableHttpResponse`), đồng thời tái sử dụng 1 `PoolingHttpClientConnectionManager` có `setSocketTimeout` + `setConnectTimeout`.
</details>
