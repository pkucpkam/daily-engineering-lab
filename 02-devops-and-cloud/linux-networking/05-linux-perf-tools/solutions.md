# LNX-05 · Đáp án — Công cụ hiệu năng Linux

> ⚠️ **Chỉ mở sau sau khi đã tự làm.** Đọc đáp án mà chưa tự làm = mất 80% giá trị bài tập.
> Mỗi đáp án được bọc trong `<details>` để tránh lộ khi lướt.

---

### E1
<details><summary>Xem đáp án</summary>

**Đáp án:**
Trên Linux, Load Average tính trung bình trượt 1/5/15 phút của tổng số task ở **2 trạng thái**: **`R` (Runnable/Running — đang chạy hoặc xếp hàng chờ CPU)** CỘNG VỚI **`D` (Uninterruptible Sleep — đang chờ disk/NFS I/O)**.

Do đó `Load Average ≈ 20` trên máy `8` core tương ứng 2 kịch bản trái ngược:
1. **CPU Saturation:** ~20 task đang ở trạng thái `R` tranh nhau 8 core (`vmstat 1` báo `r ≈ 20`, `b = 0`, `%us + %sy ≈ 100%`).
2. **Disk / NFS I/O Stall:** CPU rảnh nhiều (`%id` + `%wa` cao), nhưng ~18–20 thread đang kẹt ở trạng thái `D` chờ ổ đĩa/NFS (`vmstat 1` báo `r ≈ 1–2`, `b ≈ 18–20`, `%wa` cao).

**Đáp án sai phổ biến:** Nghĩ Load Average chỉ đo CPU nên cứ thấy `Load > số core` là đòi nâng cấp CPU → Sai hoàn toàn với kịch bản 2!
</details>

---

### E2
<details><summary>Xem đáp án</summary>

**Đáp án:**
Dòng đầu tiên của `vmstat 1`, `iostat -xz 1`, và `mpstat 1` luôn hiển thị **giá trị trung bình cộng dồn kể từ lúc hệ thống khởi động (boot)** chứ không phải 1 giây gần nhất! Có thể cách đây 3 ngày máy từng bị storm I/O kéo giá trị trung bình boot lên `wa = 35`. Chỉ từ **dòng thứ 2 trở đi** mới phản ánh số đo trong khoảng 1 giây hiện tại (`wa = 0` → hiện tại đĩa hoàn toàn nhàn rỗi).
</details>

---

### E3
<details><summary>Xem đáp án</summary>

**Đáp án:**
**Sai.** Trên máy `16` core, `100%` của **1 core đơn lẻ** chỉ chiếm `100% / 16 = 6.25%` tổng công suất toàn máy! Nếu một thread Java duy nhất bị kẹt vòng lặp vô hạn (ví dụ `HashMap` Java 7 infinite loop hoặc regex ReDoS), nó đốt sạch 1 core (`6.25%`) và làm request gắn vào thread đó treo cứng trong khi 15 core còn lại ngồi rỗi.
- **Lệnh kiểm chứng:** **`mpstat -P ALL 1`** (thấy 1 CPU core có `%usr ≈ 100%`, các core còn lại `%idle ≈ 100%`) và **`top -H -p <java_pid>`** (thấy 1 dòng `TID` ăn `~99.9%` CPU).
</details>

---

### E4
<details><summary>Xem đáp án</summary>

**Đáp án:**
`strace` sử dụng cơ chế **`ptrace`**, buộc kernel **tạm dừng tiến trình mục tiêu 2 lần cho mỗi một syscall** (lúc đi vào syscall và lúc đi ra để đọc thanh ghi/bộ nhớ) và context-switch sang `strace`. Với ứng dụng Java / JBoss bận rộn thực hiện hàng trăm nghìn syscall/giây (`futex`, `recvfrom`, `sendto`, `write`, `clock_gettime`), `strace` tạo overhead làm chậm hệ thống **10×–100×** → p99 latency tăng vọt lên hàng giây, thread pool tràn, và Load Balancer tưởng server chết nên đánh rớt node khỏi cụm!
Thay vào đó, trên production hãy dùng `perf` (`~1–3%` overhead), `top -H` + `kill -3`, hoặc Java Flight Recorder (`jcmd JFR.start`).
</details>

---

### E5
<details><summary>Xem đáp án</summary>

**Đáp án:**
`%st = 30%` (**Steal Time**) nghĩa là trong `30%` thời gian đó, OS bên trong VM có thread sẵn sàng chạy (`Runnable`) nhưng **hypervisor của nhà cung cấp cloud/VMware đang cấp CPU vật lý cho VM hàng xóm ("noisy neighbor")**, không cho VM của bạn chạy! Đây là nghẽn ở tầng hạ tầng ảo hóa (cần liên hệ đội hạ tầng/cloud đổi sang instance không overcommit CPU hoặc di chuyển VM sang host khác ít tải hơn).
</details>

---

### E6
<details><summary>Xem đáp án</summary>

**Hướng giải:**
1. Chạy `mvn -o -B verify` và `mvn -o -B compile exec:java` trong `lab/` thấy `CpuBurnWorkload` ghi nhận thread `cpu-burner` tiêu tốn hàng trăm ms CPU time trong `300ms` thực chạy (gần 100% 1 core), còn thread `idle-waiter` dùng `~0 ms`.
2. Phép đổi `TID` → hex `nid=0x...`:
   - `4839` = `0x12e7`
   - `5120` = `0x1400`
   - `9012` = `0x2334`
   So khớp với `nid=0x12e7` trong thread dump mẫu tìm ra ngay thread `"http-executor-17"` đang kẹt ở `HashMap.get`.
</details>

---

### E7
<details><summary>Xem đáp án</summary>

**Đáp án:**
- Bộ 1 (`r=21, b=0, us=88, wa=1` trên 8 core): **`CPU_SATURATION`** (nhiều thread runnable tranh nhau 8 core).
- Bộ 2 (`r=1, b=19, wa=66` + `iostat await=84.5ms, aqu-sz=18.2, %util=99.4`): **`DISK_IO_BOTTLENECK`** (Load cao do chờ I/O đĩa).
- Bộ 3 (`mpstat`: core 3 = `99.5%`, các core khác `< 2%`): **`SINGLE_CORE_HOTSPOT`** (1 thread kẹt vòng lặp đốt trọn 1 core).
- Bộ 4 bổ sung (`st >= 15%`): **`CPU_STEAL_HYPERVISOR`**.

**Code lời giải tham khảo:** xem [lab/solutions/StealTimeTriageTest.java](./lab/solutions/StealTimeTriageTest.java).
</details>

---

### E8
<details><summary>Xem đáp án</summary>

**Đáp án:**
- **`sdb` đang quá tải nghiêm trọng**: `await = 92.40 ms` (mỗi lệnh I/O mất gần 0.1 giây!), `aqu-sz = 14.80` (gần 15 I/O request xếp hàng chờ), và `%util = 99.8%` (bão hòa tuyệt đối); trong khi `nvme0n1` hoàn toàn nhàn rỗi (`await = 0.45 ms`, `%util = 18.2%`).
- Nếu JBoss ghi `server.log` đồng bộ vào `/var/log` trên `sdb`, mỗi lời gọi `logger.info(...)` trong `synchronized` appender của Log4j/JBoss Logging sẽ bị chặn chờ OS flush xuống đĩa chậm `92 ms` → **toàn bộ worker thread HTTP của JBoss xếp hàng chờ khóa log**, làm app treo hàng loạt dù CPU rảnh!
</details>

---

### E9
<details><summary>Xem đáp án</summary>

<details open><summary>Phân tích sự cố Load Average 24.85 trên JBoss 8 core</summary>

**1. Kết luận nguyên nhân gốc & Lý do nâng CPU vô ích:**
- Bỏ qua dòng 1 của `vmstat 1` (trung bình từ lúc boot), nhìn dòng 2 & 3: **`r = 1..2`**, **`b = 21..22`** (có 21–22 thread bị block không ngắt được), **`us + sy = 10..11%`** (CPU dùng rất ít!), **`wa = 71..75%`** (hơn 70% thời gian CPU rỗi chờ đĩa), **`bo = 48.200..51.400`** (ghi đĩa cực nặng), `si = 0, so = 0` (không phải swap thrashing).
- Tổng `r + b ≈ 23–24`, khớp chính xác với `load average: 24.85`!
- **`pidstat -d 1`** chỉ đích danh `PID 4812 (java)` đang ghi **`51.200 kB/s` (`~50 MB/s`)** xuống đĩa.
- **Nâng từ 8 lên 32 vCPU sẽ thất bại 100%** vì CPU hiện tại đang rảnh (`idle + wa = 89%`), nút cổ chai nằm ở **Disk Write I/O** chứ không phải năng lực tính toán CPU!

**2. Cơ chế tạo ra `22` thread `D`-state từ `50 MB/s` ghi đĩa:**
- JBoss (`java` PID 4812) xả log/file tạm liên tục (ví dụ ai đó vừa bật log level `DEBUG`/`TRACE` in toàn bộ payload XML/SQL ra `server.log` qua synchronized appender, hoặc app đang xuất báo cáo/flush `fsync` liên tục trên phân vùng đĩa chậm). Khi dirty pages hoặc request ghi xếp hàng quá khả năng thiết bị đĩa, kernel chặn các thread gọi `write()`/`fsync()` trong trạng thái **`D` (Uninterruptible Sleep)**, đẩy Load Average vọt lên 24.85 và đẩy p99 lên `9.4s`.

**3. 3 bước điều tra tiếp theo & Cách khắc phục:**
1. Chạy **`iostat -xz 1`** xem phân vùng đĩa nào có `await` / `%util` gần 100%, và chạy **`lsof -nP -p 4812 | grep -E "REG.*(w|u) "`** (hoặc `ls -lh /proc/4812/fd`) xem file nào đang mở ghi phình nhanh nhất.
2. Chạy **`kill -3 4812`** (2 lần cách 5s) xem các thread HTTP của JBoss có đang kẹt ở `FileOutputStream.writeBytes` / `FileAppender` không.
3. **Khắc phục:** Tắt ngay log `DEBUG` quá đà trên JBoss CLI hoặc `standalone.xml`, chuyển sang Async Appender có kích thước buffer hợp lý, hoặc chuyển thư mục log/temp sang ổ SSD/NVMe tách biệt với hệ điều hành.

</details>
</details>

---

## Self-check (từ README §9) — đáp án gợi ý
<details><summary>Xem</summary>

1. **Checklist 60 giây (10 lệnh):** `uptime` (xu hướng load) → `dmesg -T | tail` (OOM/hardware error) → `vmstat 1` (`r/b/si/so/wa/st`) → `mpstat -P ALL 1` (lệch core) → `pidstat 1` (thủ phạm PID) → `iostat -xz 1` (`await`/`aqu-sz`/`%util`) → `free -m` (`available`/`buff/cache`) → `sar -n DEV 1` (băng thông/drop NIC) → `sar -n TCP,ETCP 1` (`retrans/s`) → `top` / `top -H -p <pid>`.
2. **Load Average 20 trên 8 core:** Linux cộng cả task `R` (runnable) và `D` (chờ disk/NFS I/O). Kịch bản 1: CPU bão hòa (`r≈20, b=0, us+sy≈100%`). Kịch bản 2: Disk/NFS nghẽn (`r≈1-2, b≈18-20, wa` cao). Phân biệt ngay bằng `vmstat 1` (`r` vs `b` và `wa`).
3. **`%iowait` & 4 bước điều tra:** `%wa` là thời gian CPU đang *idle* trong khi có task chờ I/O chưa xong. Điều tra: (1) `vmstat 1` xem `b`, `bi/bo`, `si/so`; (2) `iostat -xz 1` xem `await`, `aqu-sz`, `%util`; (3) `pidstat -d 1` tìm PID gây I/O; (4) `ps` lọc `D` + `lsof` xem file/NFS nào đang kẹt.
4. **Ánh xạ `top -H` sang Thread Dump:** Lấy cột `PID` (chính là `TID`/`LWP`) của thread ăn CPU trong `top -H -p <pid>` → đổi sang hệ hex bằng `printf "0x%x\n" <tid>` → chạy `kill -3 <pid>` rồi grep `nid=0x...` trong log.
5. **Tại sao tránh `strace` trên prod:** `strace` dùng `ptrace` chặn mọi syscall vào/ra gây 2 lần context switch cho mỗi syscall, làm chậm app nhiều syscall 10×–100×. Trên prod dùng `perf` (`~1-3%`), `top -H` + `kill -3`, hoặc `JFR`/`async-profiler`.
</details>
