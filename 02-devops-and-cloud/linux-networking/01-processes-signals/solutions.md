# LNX-01 · Đáp án — Process, thread & signal

> ⚠️ **Chỉ mở sau sau khi đã tự làm.** Đọc đáp án mà chưa tự làm = mất 80% giá trị bài tập.
> Mỗi đáp án được bọc trong `<details>` để tránh lộ khi lướt.

---

### E1
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. **Tiến trình `python` ở trạng thái `Z` (`<defunct>`):** Nó **đã chết xong rồi** — kernel đã thu hồi bộ nhớ và FD của nó, chỉ giữ lại 1 entry PID + exit status chờ process cha (`PPID`) gọi `wait()`/`waitpid()`. Gửi `kill -9` tới một xác chết không có tác dụng gì. **Cách xử lý:** dùng `ps -o ppid= -p <zombie_pid>` tìm process cha; gửi tín hiệu hoặc restart process cha (hoặc sửa bug quên `wait()` trong code cha). Nếu cha chết, zombie sẽ được PID 1 thu hoạch sạch.
2. **Tiến trình `java` ở trạng thái `D` (Uninterruptible Sleep):** Tiến trình đang nằm sâu trong kernel driver/filesystem chờ disk I/O hoặc NFS response. Theo thiết kế bảo vệ dữ liệu của Linux kernel, tiến trình ở `D`-state **không kiểm tra hàng đợi signal cho tới khi syscall I/O thoát ra**, nên `SIGKILL` bị xếp chờ chứ không giết ngay được. **Cách xử lý:** kiểm tra NFS/storage (`dmesg`, `iostat`, `mount`); khôi phục kết nối storage (hoặc mount `soft`/`intr`/timeout hợp lý), nếu storage chết hẳn thì đôi khi chỉ reboot node mới giải phóng được `D`-state.

**Đáp án sai phổ biến:** Nghĩ chạy `kill -9` nhiều lần hoặc dùng `sudo` sẽ giết được mọi tiến trình trên Linux → Sai với `Z` và `D`.
</details>

---

### E2
<details><summary>Xem đáp án</summary>

**Đáp án:**
- **Dockerfile A (`CMD java -Xmx1g -jar app.jar`):**
  - Docker mặc định chạy qua shell: `/bin/sh -c "java -Xmx1g -jar app.jar"`.
  - **`/bin/sh` giữ PID 1**, còn **`java` là PID con** (ví dụ PID 7).
  - Khi chạy `docker stop`, Docker gửi `SIGTERM` tới **PID 1 (`/bin/sh`)**. Shell đang chờ foreground child hoàn tất nên không forward `SIGTERM` xuống `java`. Sau 10 giây hết `stop_grace_period`, Docker gửi **`SIGKILL` (`9`)** kết liễu container với exit code **`137 = 128 + 9`**.
- **Dockerfile B (`ENTRYPOINT ["java", ...]`):**
  - Không có `/bin/sh` trung gian; tiến trình **`java` trực tiếp là PID 1**.
  - `docker stop` gửi `SIGTERM` thẳng vào `java`. JVM có signal handler cho `SIGTERM` nên chạy toàn bộ `ShutdownHook` và thoát êm sau vài trăm ms–vài giây (exit code `143` hoặc `0` nếu tự gọi `System.exit(0)` trong wrapper, không bị đếm 10s chờ `SIGKILL`).
</details>

---

### E3
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. Gửi **`kill -3 <pid>` (`SIGQUIT`)**. JVM **không bị tắt** mà in toàn bộ **Full Thread Dump** (trạng thái `RUNNABLE`/`BLOCKED`/`WAITING`, stack trace từng thread, lock đang giữ/đợi) ra `stdout` của process JVM (trên JBoss thường nằm trong `nohup.out`, `console.log`, hoặc `journalctl -u jboss`).
2. Gửi **`kill -15 <pid>` (`SIGTERM`)** (hoặc lệnh `jboss-cli.sh --connect command=:shutdown`). JVM chạy shutdown hooks rồi thoát với exit code **`143`** (`128 + 15`).
3. Vì shell session gửi **`SIGHUP` (signal 1)** cho các process gắn với terminal/session đó khi SSH đóng kết nối. Nếu chạy bằng `&` trần mà không tách session (`systemd`, `nohup`, `setsid`), JVM nhận `SIGHUP` và tắt!
</details>

---

### E4
<details><summary>Xem đáp án</summary>

**Đáp án:**
Khi Java 7 gọi `Runtime.exec(...)` trên Linux (trước khi JDK chuyển sang `posix_spawn` làm mặc định từ JDK 13+), JVM dùng **`fork()` + `execve()`**.
Tại thời điểm `fork()`, kernel nhân bản bảng page table của process cha `6 GB` RAM và kiểm tra khả năng dự phòng bộ nhớ ảo (đặc biệt nếu `vm.overcommit_memory=2` hoặc không có swap và RAM trống `< 6 GB`). Do đó dù lệnh `gzip` sau `execve()` chỉ tốn vài MB, ngay bước `fork()` tạm thời nhân đôi mức cam kết địa chỉ nhớ `6 GB + 6 GB = 12 GB > 8 GB`, kernel từ chối với lỗi `ENOMEM (Cannot allocate memory)`.
</details>

---

### E5
<details><summary>Xem đáp án</summary>

**Đáp án:**
- Sẽ thấy **~250+ dòng** (250 application threads + thêm các thread nội bộ của JVM như GC threads, JIT compiler C1/C2, Finalizer, Signal Dispatcher, Reference Handler).
- **`PID` (`TGID` — Thread Group ID):** mã định danh chung của cả tiến trình JVM (`java`). Tất cả thread trong cùng JVM có chung `PID`.
- **`LWP` / `TID` (`SPID`):** mã định danh từng thread dưới góc nhìn kernel scheduler. Khi chuyển `TID` sang hệ thập lục phân (`printf "0x%x\n" <tid>`), nó khớp chính xác với trường **`nid=0x...`** trong file Thread Dump lấy từ `kill -3`!
</details>

---

### E6
<details><summary>Xem đáp án</summary>

**Hướng giải:**
1. Chạy `mvn -o -B verify` trong `lab/` thấy `ProcessLifecycleTest` verify rõ 2 nhánh: Graceful shutdown đợi đủ in-flight requests hoàn tất (`completed=3, aborted=0, exitCode=0`), còn forcible kill cắt ngang giữa chừng (`completed=0`).
2. Khi `drainTimeout < taskDuration`, shutdown hook gọi `executor.shutdownNow()` sau khi hết hạn chờ để tự động thoát sạch trong thời hạn thay vì treo đợi vô tận cho tới khi bị Kubernetes/Docker ép `SIGKILL` (`137`).
3. Nguyên tắc vàng: luôn đặt `drainTimeout <= gracePeriod - 5s` (ví dụ K8s chờ `30s` thì Java chỉ drain tối đa `20–25s`).

**Code lời giải tham khảo cho E6:** xem [lab/solutions/ShutdownTimeoutTest.java](./lab/solutions/ShutdownTimeoutTest.java).
</details>

---

### E7
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. Trong `pid1-shell-vs-exec.sh`: dòng dùng `exec ...` thay thế hoàn toàn process shell bằng process đích (nên `SIGTERM` đi thẳng tới app); còn dòng thiếu `exec` khiến shell giữ vai trò cha và lờ đi `SIGTERM` không chuyển cho con.
2. Trong `observe-zombie-and-signals.sh`: process cha sinh subshell con thoát ngay (`exit 0`) nhưng cha lại ngủ `sleep` mà chưa gọi `wait` → trong khoảng thời gian cha đang ngủ đó, con nằm ở trạng thái **`Z` (`defunct`)** trong `ps`.
3. Nếu thiếu `exec` trong entrypoint container, `kill -TERM $WRAPPER_PID` chỉ giết/ngắt wrapper (hoặc bị PID 1 shell bỏ qua), tiến trình Java con vẫn kẹt sống cho tới khi hết timeout bị `SIGKILL`.
</details>

---

### E8
<details><summary>Xem đáp án</summary>

**Hướng giải:**
- Khi cha quên gọi `Process.waitFor()`, mỗi lần gọi `wkhtmltopdf` để lại 1 slot zombie (`Z`).
- Khi cha chết, 100 zombie/orphan đó được gán `PPID = 1`.
- Nếu PID 1 là app bình thường (`dumb-java-app`) không chạy vòng lặp `waitpid(-1, WNOHANG)`, 100 zombie vẫn nằm mãi cho đến khi cạn bảng PID. Nếu PID 1 là `tini` hoặc `systemd`, nó tự động bắt `SIGCHLD` và gọi `waitpid(-1, ...)` dọn toàn bộ về `0`.

**Code lời giải tham khảo cho E8:** xem [lab/solutions/ZombieReaperSimulationTest.java](./lab/solutions/ZombieReaperSimulationTest.java).
</details>

---

### E9
<details><summary>Xem đáp án</summary>

<details open><summary>Giải pháp sửa triệt để 3 lỗi trong invoice-pdf-service</summary>

**1. Ba nguyên nhân gốc:**
- **(a) Mất đúng 30s rồi exit 137:** `CMD ["/bin/sh", "/app/entrypoint.sh"]` đặt `/bin/sh` làm PID 1, và trong `entrypoint.sh` dòng `java ...` **thiếu `exec`** → `SIGTERM` từ Kubernetes chỉ tới `/bin/sh`, Java không nhận được, đứng im 30s hết `terminationGracePeriodSeconds` rồi ăn `SIGKILL` (exit `137`).
- **(b) Mất transaction:** Do ăn `SIGKILL` thay vì `SIGTERM`, JVM Shutdown Hook không bao giờ chạy để hoàn thành PDF/transaction đang xử lý dở.
- **(c) 1.200 `<defunct>` (Zombie):** Code Java gọi `wkhtmltopdf` nhưng quên `process.waitFor()` (hoặc sub-process con của nó mồ côi), cộng thêm việc thiếu một init reaper (`tini`) làm PID 1 để `reap` các orphan/zombie trong container.

**2. Sửa `Dockerfile` (dùng `tini` hoặc `--init` làm PID 1 để vừa forward `SIGTERM` vừa reap zombie):**
```dockerfile
FROM eclipse-temurin:21-jre
RUN apt-get update && apt-get install -y --no-install-recommends tini \
    && rm -rf /var/lib/apt/lists/*
COPY target/invoice.jar /app/invoice.jar
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh
ENTRYPOINT ["/usr/bin/tini", "--"]
CMD ["/app/entrypoint.sh"]
```

**3. Sửa `entrypoint.sh` (bắt buộc `exec`):**
```sh
#!/bin/sh
set -eu
echo "Starting invoice service..."
exec java -Xmx1g -jar /app/invoice.jar
```

**4. Sửa Java gọi `wkhtmltopdf` + Shutdown Hook (< 30s):**
```java
Process process = new ProcessBuilder("wkhtmltopdf", inputHtml, outputPdf)
        .redirectErrorStream(true)
        .start();
try {
    boolean finished = process.waitFor(15, TimeUnit.SECONDS);
    if (!finished) {
        process.destroyForcibly();
        process.waitFor(2, TimeUnit.SECONDS);
        throw new IllegalStateException("wkhtmltopdf timed out");
    }
    if (process.exitValue() != 0) {
        throw new IllegalStateException("wkhtmltopdf exited with " + process.exitValue());
    }
} finally {
    if (process.isAlive()) {
        process.destroyForcibly();
        process.waitFor(); // đảm bảo không để lại zombie!
    }
}
```
Và Shutdown Hook của `invoice.jar` đặt timeout drain tối đa `20s` (`< 30s` của K8s `terminationGracePeriodSeconds`).

</details>
</details>

---

## Self-check (từ README §9) — đáp án gợi ý
<details><summary>Xem</summary>

1. **`fork()` + `execve()` & Copy-on-Write:** `fork()` nhân bản cấu trúc process cha dùng CoW (chỉ copy page table, chỉ nhân bản page 4 KB khi có ghi) rồi `execve()` nạp binary mới đè vào không gian nhớ; CoW tránh sao chép vô ích hàng GB Heap của JVM ngay thời điểm fork.
2. **5 trạng thái:** `R` (đang chạy/chờ CPU), `S` (ngủ chờ event, nhận signal), `D` (ngủ sâu chờ disk/NFS I/O, không nhận signal kể cả `SIGKILL`), `T` (bị dừng bởi `SIGSTOP`/debugger), `Z` (đã exit, chờ cha `wait()`). `kill -9` không giết được `Z` vì nó đã chết sẵn và không giết được `D` vì kernel chưa cho kiểm tra signal khi chưa xong I/O.
3. **Zombie vs Orphan:** Zombie là con chết trước cha nhưng cha chưa gọi `wait()` để đọc exit code; Orphan là cha chết trước con, khi đó kernel gán `PPID = 1` để PID 1 tự động gọi `wait()` thu hoạch khi con kết thúc.
4. **Container `CMD java ...` không dừng gọn:** Shell form bọc qua `/bin/sh -c` làm `sh` giữ PID 1 và không forward `SIGTERM` xuống `java`, nên `docker stop` đợi hết 10s rồi bắn `SIGKILL` (`137`). Sửa bằng exec-form `ENTRYPOINT ["java", ...]` hoặc `exec java ...` trong entrypoint script (kèm `tini` nếu cần reap zombie).
5. **Signals JVM:** `SIGHUP` (1) tắt app chạy nền qua SSH nếu không dùng systemd/nohup; `SIGQUIT` (`kill -3`) xuất Thread Dump ra stdout mà không giết app; `SIGTERM` (`kill -15`) kích hoạt ShutdownHook thoát êm (code `143`); `SIGKILL` (`kill -9`) kết liễu ngay không cleanup (code `137`).
</details>
