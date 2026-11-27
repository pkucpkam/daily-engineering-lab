# LNX-04 · Đáp án — systemd & quản lý service

> ⚠️ **Chỉ mở sau sau khi đã tự làm.** Đọc đáp án mà chưa tự làm = mất 80% giá trị bài tập.
> Mỗi đáp án được bọc trong `<details>` để tránh lộ khi lướt.

---

### E1
<details><summary>Xem đáp án</summary>

**Đáp án:**
- Khi bạn chạy `systemctl stop order-api.service`, `systemd` gửi tín hiệu **`SIGTERM` (số 15)** tới tiến trình `java`.
- HotSpot JVM có cài signal handler cho `SIGTERM` để chạy các `ShutdownHook` và kết thúc tiến trình với exit code theo quy ước POSIX **`128 + 15 = 143`**.
- Theo mặc định, `systemd` chỉ coi exit code `0` là tắt thành công cho exit chủ động từ app; thấy mã `143`, nó đánh dấu unit ở trạng thái **`failed (Result: exit-code)`**.
- **Cách sửa:** Thêm dòng **`SuccessExitStatus=143`** vào mục `[Service]` của file unit rồi chạy `sudo systemctl daemon-reload`.
</details>

---

### E2
<details><summary>Xem đáp án</summary>

**Đáp án:**
- `/etc/security/limits.conf` được áp dụng bởi module **`pam_limits.so`** khi user đăng nhập qua shell/SSH/PAM session.
- Ngược lại, các dịch vụ hệ thống do **`systemd` (PID 1)** khởi chạy **không đi qua PAM login session**, nên systemd hoàn toàn bỏ qua `limits.conf` và giữ mặc định hệ thống (thường chỉ là `1024` hoặc `4096` trên các distro cũ).
- **Cách cấu hình đúng:** Thêm **`LimitNOFILE=65536`** trực tiếp trong phần `[Service]` của `jboss.service` (sau đó `sudo systemctl daemon-reload && sudo systemctl restart jboss.service`) và kiểm tra lại bằng `cat /proc/$(systemctl show -p MainPID --value jboss)/limits`.
</details>

---

### E3
<details><summary>Xem đáp án</summary>

**Đáp án — 3 tình huống gây hại của `Restart=always` + `RestartSec=1`:**
1. **Crash loop ngốn CPU/IO & làm trôi log gốc:** Nếu thiếu config hoặc lỗi startup, app khởi động → chết sau vài giây → bật lại mỗi 1s liên tục, lấp đầy journal/log và che khuất stack trace lỗi thật.
2. **Làm hỏng ý định dừng thủ công/bảo trì (`exit 0`):** Nếu app tự thoát sạch với `exit 0` (hoặc tắt qua lệnh quản trị nội bộ không qua `systemctl stop`), `Restart=always` vẫn ép bật lại ngay lập tức.
3. **Che giấu sự cố trước dashboard kiểm tra PID:** Monitor đơn giản thấy tiến trình `java` vẫn tồn tại nên báo xanh, trong khi người dùng bị ngắt kết nối liên tục mỗi vài giây.
</details>

---

### E4
<details><summary>Xem đáp án</summary>

**Đáp án:**
`systemd` đọc và cache toàn bộ nội dung các unit file vào bộ nhớ của PID 1. Chỉnh sửa file trên đĩa chưa tự động cập nhật cấu hình đang lưu trong RAM của `systemd`; nếu bạn chỉ chạy `systemctl restart`, systemd vẫn dùng định nghĩa unit cũ (`MemoryMax=4G`). Phải chạy **`sudo systemctl daemon-reload`** trước, rồi mới chạy `sudo systemctl restart order-api`.
</details>

---

### E5
<details><summary>Xem đáp án</summary>

**Đáp án — 3 điểm vượt trội của `systemd timer`:**
1. **Giới hạn tài nguyên cgroup cho job phụ:** `archive-jboss-logs.service` có thể đặt `MemoryMax=256M` và `CPUQuota=50%`, đảm bảo lúc nén file log `20 GB` không tranh cướp RAM/CPU với JBoss đang phục vụ khách hàng.
2. **`RandomizedDelaySec=` chống thundering herd:** Rải đều thời điểm chạy trên 20 server thay vì đồng loạt bắn vào NFS lúc `02:00:00`.
3. **Log tập trung `journalctl` + `Persistent=true`:** Toàn bộ stdout/stderr nằm ngay trong `journalctl -u archive-jboss-logs`, không bị nuốt bởi `> /dev/null`, và nếu server bảo trì tắt lúc 02:00 thì timer tự chạy bù ngay khi máy khởi động lại.
</details>

---

### E6
<details><summary>Xem đáp án</summary>

**Hướng giải:**
6 lỗi trong `lab/systemd/jboss-legacy-broken.service`:
1. `User=root` (chạy app dưới quyền root — rủi ro bảo mật nghiêm trọng).
2. `ExecStart=/bin/sh -c "java ..."` (dùng shell wrapper không `exec`, làm shell giữ MainPID và nuốt `SIGTERM`).
3. `Restart=always` không đi kèm `StartLimitBurst`/`StartLimitIntervalSec`.
4. Thiếu `SuccessExitStatus=143` (khiến `systemctl stop` gắn cờ `failed`).
5. Thiếu `LimitNOFILE=` (dễ chạm `1024` FD khi tải tăng).
6. Thiếu `MemoryMax=` (không giới hạn cgroup memory).

**Code kiểm chứng thêm rule `TimeoutStopSec` (E6):** xem [lab/solutions/StrictTimeoutRuleTest.java](./lab/solutions/StrictTimeoutRuleTest.java).
</details>

---

### E7
<details><summary>Xem đáp án</summary>

**Đáp án:**
- Khi app thoát mã `143`, `SystemdStateSimulator` chứng minh `SuccessExitStatus=143` đưa service về trạng thái `INACTIVE_DEAD` sạch sẽ (ngược lại rơi vào `FAILED`).
- Trong kịch bản crash loop (`exit 1` mỗi 4s):
  - **Trường hợp A (`Restart=always`, không giới hạn burst):** `NRestarts` tăng lên `10` và tiếp tục lặp vô tận.
  - **Trường hợp B (`Restart=on-failure`, `StartLimitBurst=5`):** Sau 5 lần khởi động thất bại liên tiếp trong cửa sổ `StartLimitIntervalSec`, systemd ngắt vòng lặp và chuyển unit sang `FAILED` (`start-limit-hit`) để bảo vệ CPU và giữ nguyên log lỗi.
</details>

---

### E8
<details><summary>Xem đáp án</summary>

**File `archive-jboss-logs.service`:**
```ini
[Unit]
Description=Archive JBoss Logs Safely

[Service]
Type=oneshot
User=jboss
Group=jboss
ExecStart=/opt/scripts/archive-jboss-logs.sh
MemoryMax=256M
CPUQuota=50%
Nice=10
```

**File `archive-jboss-logs.timer`:**
```ini
[Unit]
Description=Daily JBoss Log Archive at 02:00

[Timer]
OnCalendar=*-*-* 02:00:00
RandomizedDelaySec=15m
Persistent=true

[Install]
WantedBy=timers.target
```

**Lệnh kiểm tra kết quả chạy hôm qua:**
```bash
journalctl -u archive-jboss-logs.service -S yesterday --no-pager
systemctl list-timers archive-jboss-logs.timer
```
</details>

---

### E9
<details><summary>Xem đáp án</summary>

<details open><summary>Phân tích & Unit file chuẩn cho payment-gateway.service</summary>

**1. Tại sao PID đổi mỗi 4s và không thấy lỗi gốc trong `-n 50`?**
- `app.jar` mất ~3s khởi động rồi crash ngay (ví dụ sai DB password, port bận, hoặc OOM).
- Vì đặt `Restart=always` + `RestartSec=1`, cứ mỗi 4s systemd lại spawn tiến trình mới (PID mới) và in ra hàng chục dòng khởi động Spring/JBoss tràn qua 50 dòng cuối cùng của `journalctl -n 50`!
- **Lệnh tìm lại log gốc:**
  ```bash
  systemctl show payment-gateway -p NRestarts,ExecMainStatus
  journalctl -u payment-gateway -S "03:00:00" -U "03:02:00" --no-pager
  journalctl -u payment-gateway -p err -n 100 --no-pager
  ```

**2. 6 lỗ hổng trong unit file cũ:**
1. Chạy dưới mặc định **`User=root`** (thiếu `User=`/`Group=`).
2. `ExecStart=/bin/sh -c "java ..."` bọc qua `/bin/sh` thiếu `exec` → `SIGTERM` không đi thẳng vào JVM.
3. **`Restart=always`** + `RestartSec=1` thiếu `StartLimitIntervalSec`/`StartLimitBurst`.
4. Thiếu **`SuccessExitStatus=143`** (`SIGTERM` shutdown hook bị gắn cờ `failed`).
5. Thiếu **`LimitNOFILE=65536`** (dính trần 1024 FD).
6. Thiếu **`MemoryMax=`** / `TimeoutStopSec=` (có `-Xmx4g` nên cần `MemoryMax=5500M` hoặc dùng `-XX:MaxRAMPercentage=70.0`).

**3. File `/etc/systemd/system/payment-gateway.service` chuẩn:**
```ini
[Unit]
Description=Payment Gateway Java Service
After=network-online.target
Wants=network-online.target
StartLimitIntervalSec=120
StartLimitBurst=5

[Service]
Type=simple
User=appuser
Group=appuser
WorkingDirectory=/opt/payment
EnvironmentFile=-/etc/payment/env
ExecStart=/usr/bin/java -Xmx4g -XX:+ExitOnOutOfMemoryError -jar /opt/payment/app.jar
SuccessExitStatus=143
Restart=on-failure
RestartSec=5s
TimeoutStopSec=30
LimitNOFILE=65536
MemoryHigh=4800M
MemoryMax=5500M

[Install]
WantedBy=multi-user.target
```

**4. Lệnh áp dụng & kiểm chứng:**
```bash
sudo systemd-analyze verify /etc/systemd/system/payment-gateway.service
sudo systemctl daemon-reload
sudo systemctl restart payment-gateway.service
sudo systemctl status payment-gateway.service
cat /proc/$(systemctl show -p MainPID --value payment-gateway)/limits | grep "Max open files"
```

</details>
</details>

---

## Self-check (từ README §9) — đáp án gợi ý
<details><summary>Xem</summary>

1. **3 phần Unit File & 8 thiết lập ổn định:** `[Unit]` (metadata, `After=`, `StartLimitBurst`/`Interval`), `[Service]` (`Type=simple`, `User=`, `ExecStart=` trực tiếp, `SuccessExitStatus=143`, `LimitNOFILE=65536`, `MemoryMax=`, `Restart=on-failure`, `TimeoutStopSec=`), `[Install]` (`WantedBy=multi-user.target`).
2. **Tại sao Java bị `failed` khi `systemctl stop`:** JVM bắt `SIGTERM`, chạy Shutdown Hook và thoát với mã `143` (`128+15`). Nếu thiếu `SuccessExitStatus=143`, systemd coi exit code khác `0` là crash.
3. **`Restart=always` nguy hiểm thế nào:** Tự động khởi động lại cả khi app chủ ý `exit 0`, và gây crash-loop vô hạn làm trôi log gốc / đốt CPU nếu thiếu `StartLimitBurst`. Nên thay bằng `Restart=on-failure` + `RestartSec=5s` + `StartLimitIntervalSec=120` + `StartLimitBurst=5`.
4. **Cơ chế áp giới hạn của systemd:** Systemd dùng **cgroups** của Linux kernel (cho `MemoryMax`, `CPUQuota`) và `setrlimit()` trực tiếp trước `execve` (cho `LimitNOFILE`). Vì sinh trực tiếp từ PID 1 không qua PAM login shell, systemd bỏ qua `/etc/security/limits.conf`.
5. **4 ưu điểm `systemd timer` vs `cron`:** (1) Log vào `journalctl` dễ lọc; (2) Giới hạn RAM/CPU cgroup tránh đánh sập app chính; (3) `RandomizedDelaySec` chống chạy dồn đồng loạt; (4) `Persistent=true` tự chạy bù nếu server từng tắt đúng giờ hẹn.
</details>
