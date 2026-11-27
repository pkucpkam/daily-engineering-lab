# LNX-04 · Bài tập — systemd & quản lý service

> Làm theo thứ tự. **Tự làm trước, chỉ mở hint khi đã thử ≥ 15 phút.** Đáp án ở [solutions.md](./solutions.md) — chỉ mở sau khi đã có câu trả lời của riêng bạn.
> Ghi thời gian làm từng bài vào `my-notes.md` để theo dõi tiến bộ.

| Cấp | Mục đích | Số lượng | Thời gian gợi ý |
| :--- | :--- | :---: | :---: |
| 🟢 L1 — Recall | Nhớ & hiểu unit file, exit 143, cgroup limits, journald | 5 | 20' |
| 🟡 L2 — Apply | Lint unit file lỗi, mô phỏng state 143 & viết timer | 3 | 70' |
| 🔴 L3 — Deep | Ticket JBoss restart lặp vô tận & failed mỗi lần tắt | 1 | 90' |

---

## 🟢 L1 — Recall

### E1. Giải mã exit code `143` khi chạy `systemctl stop`
Khi chạy `systemctl stop order-api.service`, `systemctl status` hiện dòng:
`Main PID: 2941 (code=exited, status=143)` kèm trạng thái **`Active: failed (Result: exit-code)`**.
Giải thích tại sao số `143` xuất hiện đúng lúc systemd gửi `SIGTERM` cho JVM và dòng nào trong unit file sửa dứt điểm cảnh báo sai này.

---

### E2. Vì sao sửa `/etc/security/limits.conf` không nâng được `ulimit -n` cho `jboss.service`?
Ops đã thêm `jboss soft nofile 65536` vào `/etc/security/limits.conf` và chạy `sudo systemctl restart jboss.service`, nhưng `cat /proc/$(pgrep -f jboss)/limits` vẫn báo `1024`. Giải thích nguyên nhân và cách cấu hình đúng chuẩn systemd.

---

### E3. Nguy hiểm của `Restart=always` + `RestartSec=1`
Đội vận hành đặt `Restart=always` và `RestartSec=1` cho tất cả microservices Java để "đạt uptime 99.99%". Giải thích 3 tình huống thực tế mà bộ đôi này gây hại nặng hơn là giữ ổn định.

---

### E4. Quên `systemctl daemon-reload`
Bạn vừa giảm `MemoryMax=4G` xuống `MemoryMax=2G` trong `/etc/systemd/system/order-api.service` rồi chạy ngay `sudo systemctl restart order-api`. Khi kiểm tra bằng `systemctl show -p MemoryMax order-api`, giá trị vẫn hiện `4294967296` (4 GB). Vì sao?

---

### E5. So sánh `systemd timer` vs `cron`
Nêu 3 tình huống cụ thể mà `systemd timer` xử lý tốt hơn `crontab` khi vận hành một script kiểm tra và nén log JBoss lúc 02:00 sáng mỗi ngày.

---

## 🟡 L2 — Apply

### E6. Audit Unit File bằng `SystemdUnitLinter` & viết lại chuẩn production
**Bối cảnh:** Dùng [lab/](./lab/README.md) kiểm tra file unit lỗi `lab/systemd/jboss-legacy-broken.service` và chạy `SystemdUnitLinter`.
**Yêu cầu:**
1. Chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`.
2. Tự đọc `lab/systemd/jboss-legacy-broken.service` **trước** khi xem output linter, ghi ra ít nhất **5 lỗi** trong file đó.
3. Đối chiếu với file chuẩn `lab/systemd/jboss-legacy.service` và viết thêm 1 rule vào `SystemdUnitLinter` (hoặc kiểm tra qua `lab/solutions/StrictTimeoutRuleTest.java`) để bắt thêm lỗi **`TimeoutStopSec` vắng mặt hoặc quá nhỏ (`< 10s`)** khiến JVM không kịp drain request.

**Tiêu chí hoàn thành:**
- [ ] `mvn -o -B verify` pass 100%.
- [ ] Chỉ ra đúng cả 6 lỗi trong `jboss-legacy-broken.service` (`User=root`, shell wrapper, `Restart=always`, thiếu `SuccessExitStatus=143`, thiếu `LimitNOFILE`, thiếu `MemoryMax`).

<details><summary>💡 Hint</summary>

Mở `lab/systemd/jboss-legacy-broken.service` và đối chiếu từng dòng với 8 quy tắc vàng trong README §3.2.
</details>

---

### E7. Mô phỏng trạng thái Exit 143 & Crash Loop trong `SystemdStateSimulator`
**Bối cảnh:** Mở `lab/src/main/java/lab/linux/systemd/SystemdStateSimulator.java`.
**Yêu cầu:**
1. Quan sát sự khác biệt trạng thái kết thúc khi `stop()` dịch vụ Java trả exit code `143` có/thiếu `SuccessExitStatus=143`.
2. Mô phỏng một app cấu hình sai bị crash (`exit 1`) 10 lần liên tiếp trong `60s`:
   - Trường hợp A: `Restart=always`, không đặt `StartLimitBurst` → đếm `NRestarts`.
   - Trường hợp B: `Restart=on-failure`, `StartLimitBurst=5` → kiểm chứng sau 5 lần restart thất bại trong cửa sổ `StartLimitIntervalSec`, unit chuyển sang `FAILED (start-limit-hit)` và ngừng đốt CPU.

**Tiêu chí hoàn thành:**
- [ ] Pass toàn bộ test chứng minh trong `SystemdUnitTest`.

<details><summary>💡 Hint</summary>

Khi `StartLimitBurst=5` được kích hoạt trong cửa sổ thời gian `StartLimitIntervalSec`, systemd ngừng cố khởi động lại và giữ nguyên log crash ban đầu trong `journalctl` để kỹ sư điều tra!
</details>

---

### E8. Thiết kế cặp `systemd timer` + `service` thay thế `crontab` cho JBoss Diagnostic / Log Archive
**Bối cảnh:** Hiện tại server JBoss chạy dòng crontab:
`0 2 * * * /opt/scripts/archive-jboss-logs.sh > /dev/null 2>&1`
Do `> /dev/null 2>&1`, tháng trước script lỗi hết dung lượng đĩa suốt 10 ngày không ai biết, và đúng 02:00 cả 20 server cùng đẩy file qua NFS làm nghẽn mạng.
**Yêu cầu:**
Viết 2 file hoàn chỉnh `archive-jboss-logs.service` (`Type=oneshot`, `User=jboss`, `MemoryMax=256M`, `CPUQuota=50%`) và `archive-jboss-logs.timer` (`OnCalendar=*-*-* 02:00:00`, `RandomizedDelaySec=15m`, `Persistent=true`), kèm lệnh `journalctl` kiểm tra kết quả chạy hôm qua.

**Tiêu chí hoàn thành:**
- [ ] Unit file `.service` dùng `Type=oneshot` và giới hạn cgroup bảo vệ JBoss.
- [ ] Unit file `.timer` có `RandomizedDelaySec` chống thundering herd và `Persistent=true`.

<details><summary>💡 Hint</summary>

Tham khảo cặp `lab/systemd/heap-check.service` và `lab/systemd/heap-check.timer`, đổi `Type=oneshot`, bỏ `> /dev/null` để log chảy tự nhiên vào `journald`!
</details>

---

## 🔴 L3 — Deep

### E9. Ticket Production: Service Java "vẫn UP" trong `ps`, nhưng mất 500 error/phút và log biến mất sạch
**Tình huống:**
> Lúc 03:00 sáng sau một đợt nâng cấp cấu hình, hệ thống cảnh báo báo lỗi `502 Bad Gateway` chập chờn liên tục trên `payment-gateway`, nhưng khi Ops gõ `ps -ef | grep payment` thì vẫn thấy tiến trình `java` chạy! Tuy nhiên để ý kỹ thấy **PID liên tục đổi mới mỗi 4 giây**, và khi chạy `journalctl -u payment-gateway -n 50` chỉ thấy toàn dòng `Started Payment Gateway` lặp đi lặp lại không thấy stack trace lỗi gốc đâu nữa!
> Dưới đây là file `/etc/systemd/system/payment-gateway.service` hiện tại:
> ```ini
> [Unit]
> Description=Payment Gateway
>
> [Service]
> ExecStart=/bin/sh -c "java -Xmx4g -jar /opt/payment/app.jar"
> Restart=always
> RestartSec=1
>
> [Install]
> WantedBy=multi-user.target
> ```

**Yêu cầu:**
1. Giải thích vì sao PID đổi mỗi 4 giây, tại sao `journalctl -n 50` không thấy nguyên nhân crash (liên hệ `Restart=always` + `RestartSec=1`), và làm thế nào tìm lại log lỗi gốc bằng `journalctl` theo thời gian (`-S` / `-p err`).
2. Chỉ ra **6 lỗ hổng** trong unit file trên (bao gồm cả `KillMode`/PID wrapper, `User=root`, `LimitNOFILE`, `SuccessExitStatus=143`, `MemoryMax`, `Restart` policy).
3. Viết lại file `payment-gateway.service` chuẩn production và nêu đầy đủ các lệnh reload + kiểm chứng sau khi sửa (`daemon-reload`, `systemd-analyze verify`, kiểm tra `/proc/<pid>/limits`).

**Tiêu chí hoàn thành:**
- [ ] Nêu đủ lệnh `journalctl -u payment-gateway -p err -S "..."` (hoặc `--since "03:00"` + kiểm tra `NRestarts`).
- [ ] Unit file mới sửa đủ cả 6 điểm yếu và pass tiêu chuẩn `SystemdUnitLinter`.

<details><summary>💡 Hint</summary>

1. App mất ~3s khởi động rồi crash (ví dụ sai DB password hoặc port 8080 bận) → `RestartSec=1` lập tức bật lại tạo chu kỳ 4 giây/lần.
2. Cách xem lại lỗi ngay sau khi app crash: `journalctl -u payment-gateway -S "03:00:00" -U "03:02:00"` (xem chu kỳ đầu tiên) hoặc `systemctl show -p NRestarts payment-gateway`.
3. Khi sửa unit: bỏ `/bin/sh -c`, đặt `User=appuser`, `SuccessExitStatus=143`, `LimitNOFILE=65536`, `MemoryMax=5G` (trên `-Xmx4g` hoặc `-XX:MaxRAMPercentage=70.0`), `Restart=on-failure`, `RestartSec=5s`, `StartLimitIntervalSec=120`, `StartLimitBurst=5`.
</details>
