# LNX-01 · Bài tập — Process, thread & signal

> Làm theo thứ tự. **Tự làm trước, chỉ mở hint khi đã thử ≥ 15 phút.** Đáp án ở [solutions.md](./solutions.md) — chỉ mở sau khi đã có câu trả lời của riêng bạn.
> Ghi thời gian làm từng bài vào `my-notes.md` để theo dõi tiến bộ.

| Cấp | Mục đích | Số lượng | Thời gian gợi ý |
| :--- | :--- | :---: | :---: |
| 🟢 L1 — Recall | Nhớ & hiểu process state, signal, PID 1 | 5 | 20' |
| 🟡 L2 — Apply | Chạy lab Java graceful shutdown & phân tích script Linux | 3 | 70' |
| 🔴 L3 — Deep | Điều tra sự cố shutdown container & zombie trên JBoss/Docker | 1 | 90' |

---

## 🟢 L1 — Recall

### E1. Giải thích `kill -9` thất bại với Zombie (`Z`) và `D`-state
Một kỹ sư junior thấy `ps aux` báo 2 tiến trình: một tiến trình `python` ở trạng thái **`Z`** (`<defunct>`) và một tiến trình `java` ở trạng thái **`D`** (đang đọc file từ NFS bị mất mạng). Bạn ấy gõ `sudo kill -9` vào cả hai PID nhưng không tiến trình nào biến mất ngay. Giải thích cơ chế kernel đằng sau cả 2 trường hợp và cách xử lý đúng cho mỗi trường hợp.

---

### E2. Shell form vs Exec form trong Container PID 1
So sánh cây tiến trình (PID 1 và PID con) và đường đi của tín hiệu `SIGTERM` khi chạy `docker stop` cho 2 `Dockerfile` dưới đây:
- **Dockerfile A:** `CMD java -Xmx1g -jar app.jar`
- **Dockerfile B:** `ENTRYPOINT ["java", "-Xmx1g", "-jar", "app.jar"]`
Vì sao Dockerfile A thường dừng sau **đúng 10 giây** với exit code `137`?

---

### E3. Phân biệt `kill -3`, `kill -15`, `kill -9`, và `SIGHUP`
Khi JBoss AS 7.1.1 trên Linux bất ngờ phản hồi rất chậm (CPU 15%, tất cả request HTTP timeout):
1. Nếu bạn muốn xem **toàn bộ các thread Java đang kẹt ở đâu** mà vẫn giữ server sống để không cắt kết nối người dùng, gửi signal nào? Output đi về đâu?
2. Sau khi đã có bằng chứng và cần restart sạch để chạy shutdown hooks, gửi signal nào và JVM thoát với exit code bao nhiêu?
3. Vì sao khởi chạy JBoss bằng `./standalone.sh &` qua SSH rồi đóng cửa sổ terminal có thể khiến JBoss tự tắt?

---

### E4. Copy-on-Write (`fork`) & `Cannot allocate memory` trên Java legacy
Một ứng dụng **Java 7** đang dùng `6 GB` RAM (`-Xmx6g`) thỉnh thoảng gọi `Runtime.getRuntime().exec("gzip ...")` trên server `8 GB` RAM và bỗng ném `IOException: error=12, Cannot allocate memory` dù file nén chỉ `1 MB`. Giải thích liên hệ giữa cơ chế `fork()` cổ điển và lỗi thiếu RAM ảo dù `gzip` tốn rất ít bộ nhớ.

---

### E5. Đếm Thread Java dưới góc nhìn Linux Kernel
Nếu một ứng dụng JBoss tạo **250 thread** trong JVM, trên Linux bạn chạy `ps -eLf | grep java` hay `top -H -p <pid>` sẽ thấy bao nhiêu dòng/task? Thuật ngữ `PID` vs `LWP` (hoặc `TID`) khác nhau ra sao?

---

## 🟡 L2 — Apply

### E6. Thực nghiệm Graceful Shutdown vs Forcible Kill với JVM Child Process
**Bối cảnh:** Dùng [lab/](./lab/README.md) chạy `GracefulShutdownApp` và `ProcessLifecycleLabRunner`.
**Yêu cầu:**
1. Chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`.
2. Quan sát sự khác biệt số request hoàn thành giữa kịch bản **Graceful Stop** (gửi tín hiệu drain → shutdown hook chờ `inFlight` hoàn tất) và **Forcible Kill** (`destroyForcibly()` tương tự `SIGKILL`).
3. Thêm 1 test case mới vào `ProcessLifecycleTest` (hoặc đối chiếu với `lab/solutions/ShutdownTimeoutTest.java`) chứng minh nếu shutdown hook bị cấu hình timeout **nhỏ hơn** thời gian xử lý request đang chạy (`drainTimeout < taskDuration`), hệ thống phải ép thoát theo giới hạn thời gian thay vì treo vĩnh viễn ăn `SIGKILL`.

**Tiêu chí hoàn thành:**
- [ ] `mvn -o -B verify` pass 100%.
- [ ] Giải thích chính xác vì sao shutdown timeout trong Java luôn phải nhỏ hơn `terminationGracePeriodSeconds` của Kubernetes / `TimeoutStopSec` của systemd.

<details><summary>💡 Hint</summary>

Xem `GracefulShutdownApp.java` phần `executor.awaitTermination(drainTimeoutMs, TimeUnit.MILLISECONDS)`: khi `awaitTermination` trả về `false`, gọi `executor.shutdownNow()` để trả exit code kiểm soát được trước khi container runtime bắn `SIGKILL`.
</details>

---

### E7. Đọc hiểu & kiểm tra Script Linux PID 1 Wrapper và Zombie Observation
**Bối cảnh:** Xem 2 script trong `lab/scripts/`:
- `pid1-shell-vs-exec.sh`
- `observe-zombie-and-signals.sh`

**Yêu cầu:**
1. Đọc từng dòng 2 script trên, chỉ ra dòng lệnh nào quyết định process con có nhận được `SIGTERM` hay không và dòng nào cố tình tạo ra Zombie (`Z`).
2. Giải thích điều gì sẽ xảy ra trên Linux/Docker nếu đổi `exec sleep 30` thành `sleep 30` (thiếu `exec`) trong script entrypoint và gửi `kill -TERM $WRAPPER_PID`.
3. *(Nếu có máy Linux / WSL)* Chạy thật 2 script và dán output `ps` + exit code; *(nếu trên Windows)* ghi chú các lệnh cần kiểm chứng lên Linux VM.

**Tiêu chí hoàn thành:**
- [ ] Chỉ đúng dòng thiếu `exec` làm `sh` nuốt `SIGTERM` và cơ chế cha `sleep` không gọi `wait` tạo ra trạng thái `Z`.

<details><summary>💡 Hint</summary>

Trong POSIX shell, nếu viết `cmd & wait`, hoặc `sh -c "child"`, khi nhận `SIGTERM`, shell không tự chuyển tín hiệu tới process con trừ khi cài `trap 'kill -TERM $child_pid; wait $child_pid' TERM` hoặc dùng `exec` để thay thế hẳn chính shell!
</details>

---

### E8. Mô phỏng Parent Reaping & Zombie Accumulation
**Bối cảnh:** Mở `lab/src/main/java/lab/linux/processes/ProcessLifecycleSimulator.java`.
**Yêu cầu:**
1. Viết code hoặc mở rộng simulator kiểm chứng kịch bản: một `JavaWebApp` (không phải PID 1 reaper) liên tục gọi `spawnChild("wkhtmltopdf")` 100 lần nhưng quên gọi `waitFor()`, sau đó cha chết đột ngột.
2. Mô phỏng chuyển giao toàn bộ 100 orphan/zombie đó về `PID 1` và kiểm chứng sự khác biệt giữa PID 1 là `tini / systemd` (có `reap()`) vs PID 1 là `dumb-java-app` (không có `reap()`).

**Tiêu chí hoàn thành:**
- [ ] Mô hình hóa rõ số lượng zombie `= 100` khi PID 1 không reap và `= 0` khi `tini` reap hết.

<details><summary>💡 Hint</summary>

Khi cha chết, các process con đang chạy trở thành orphan và đổi `ppid = 1`. Nếu con cũng đã thoát trước khi cha chết mà cha chưa `wait()`, khi cha chết thì kernel chuyển cả zombie đó sang cho PID 1 (hoặc subreaper) để gọi `wait()` giải phóng slot khỏi bảng tiến trình!
</details>

---

## 🔴 L3 — Deep

### E9. Ticket Production: Container Java mất 30 giây mỗi lần deploy, log mất transaction & 1.200 Zombie `<defunct>`
**Tình huống:**
> Team triển khai `invoice-pdf-service` (Java 21 + thư viện gọi CLI `wkhtmltopdf` sinh PDF) lên Kubernetes:
> 1. Mỗi lần rolling update pod mới, Kubernetes mất **đúng 30 giây** mới tắt được pod cũ, sau đó trạng thái thoát ghi `Exit Code 137 (OOMKilled? / SIGKILL)` dù RAM chỉ dùng 45%.
> 2. Trong pod chạy lâu ngày thấy `ps aux` báo hơn **1.200 dòng `[wkhtmltopdf] <defunct>`**, và cuối cùng `fork` thất bại `Resource temporarily unavailable`.
> 3. Dưới đây là `Dockerfile` và `entrypoint.sh` hiện tại của team:
> ```dockerfile
> FROM eclipse-temurin:21-jre
> COPY target/invoice.jar /app/invoice.jar
> COPY entrypoint.sh /app/entrypoint.sh
> CMD ["/bin/sh", "/app/entrypoint.sh"]
> ```
> ```bash
> #!/bin/sh
> echo "Starting invoice service..."
> java -Xmx1g -jar /app/invoice.jar
> ```

**Yêu cầu:**
1. Xác định **đủ 3 nguyên nhân gốc** gây ra: (a) đợi đúng 30s rồi exit 137, (b) mất transaction khi bị kill, (c) 1.200 process `<defunct>`.
2. Viết lại hoàn chỉnh `Dockerfile`, `entrypoint.sh`, và đoạn Java gọi `wkhtmltopdf` + shutdown hook để khắc phục triệt để cả 3 vấn đề trên.

**Tiêu chí hoàn thành:**
- [ ] Sửa đúng `entrypoint.sh` bằng `exec` + cấu hình `tini` (hoặc `--init`) làm PID 1 reap zombie.
- [ ] Bắt buộc có `process.waitFor(timeout, TimeUnit.SECONDS)` trong Java để không tạo zombie ngay từ phía ứng dụng.
- [ ] Đặt timeout drain trong Java nhỏ hơn `terminationGracePeriodSeconds` của Kubernetes.

<details><summary>💡 Hint</summary>

1. Trong `entrypoint.sh`: `/bin/sh` là PID 1 vì dòng cuối viết `java -Xmx1g ...` thiếu `exec` → `SIGTERM` dừng ở `/bin/sh`, JVM không biết → K8s đợi hết 30s rồi bắn `SIGKILL` (`137`).
2. Ngay cả khi đổi sang `exec java ...`, `java` trở thành PID 1 nhưng JVM không có vòng lặp `waitpid(-1, ...)` cho các orphan child mà `wkhtmltopdf` có thể đẻ ra nếu sub-process bị timeout/quên `waitFor()`! Do đó: (a) Java phải luôn gọi `process.waitFor(...)` + `destroyForcibly()` trong `finally`, VÀ (b) bọc container bằng `/usr/bin/tini --` làm PID 1 (hoặc `ENTRYPOINT ["tini", "--", "/app/entrypoint.sh"]` với `exec java ...`).
</details>
