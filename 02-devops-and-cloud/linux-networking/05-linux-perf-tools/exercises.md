# LNX-05 · Bài tập — Công cụ hiệu năng Linux

> Làm theo thứ tự. **Tự làm trước, chỉ mở hint khi đã thử ≥ 15 phút.** Đáp án ở [solutions.md](./solutions.md) — chỉ mở sau khi đã có câu trả lời của riêng bạn.
> Ghi thời gian làm từng bài vào `my-notes.md` để theo dõi tiến bộ.

| Cấp | Mục đích | Số lượng | Thời gian gợi ý |
| :--- | :--- | :---: | :---: |
| 🟢 L1 — Recall | Nhớ & hiểu Load Average, iowait, dòng 1 vmstat, strace vs perf | 5 | 20' |
| 🟡 L2 — Apply | Đọc chẩn đoán vmstat/iostat/mpstat & ánh xạ TID -> hex nid trong lab | 3 | 70' |
| 🔴 L3 — Deep | Ticket JBoss chậm production với `Load 24` trên máy 8 core | 1 | 90' |

---

## 🟢 L1 — Recall

### E1. Giải mã `Load Average = 20` trên máy `8` core
Một server Linux `8 vCPU` báo `load average: 21.40, 19.80, 15.20`. Giải thích 2 trạng thái tiến trình được cộng vào Load Average trên Linux (`R` và `D`), và nêu 2 kịch bản nguyên nhân gốc hoàn toàn trái ngược nhau có thể tạo ra cùng con số `21.40` này.

---

### E2. Tại sao phải bỏ qua dòng đầu tiên của `vmstat 1` và `iostat -xz 1`?
Bạn gõ `vmstat 1 5`, thấy dòng 1 báo `wa = 35` còn dòng 2, 3, 4, 5 báo `wa = 0`. Giải thích dòng 1 đang hiển thị giá trị của khoảng thời gian nào.

---

### E3. Single-core bottleneck trên máy nhiều nhân
Trên server `16` core chạy JBoss, `top` báo tổng CPU của tiến trình `java` khoảng `6.2%` (`%Cpu(s): 6.3 us, 93.7 id`). Một bạn trong team nói: *"CPU còn dư 93%, không phải lỗi CPU!"*. Đúng hay Sai? Dùng lệnh nào và cột nào để kiểm chứng có 1 thread đơn đang chiếm trọn `100%` của 1 core?

---

### E4. Khi nào KHÔNG được chạy `strace -ff -p <pid>`?
Giải thích cơ chế `ptrace` khiến `strace` tạo overhead lớn gấp hàng chục lần `perf`, và nếu JBoss đang chạy 2.000 req/s bị gắn `strace` thì chuyện gì sẽ xảy ra với latency và health check của Load Balancer?

---

### E5. Ý nghĩa thật sự của `%st` (Steal Time) trong môi trường Cloud / VM
Khi chạy `vmstat 1` hoặc `mpstat 1` trên EC2/VMware VM, bạn thấy `%us = 35`, `%sy = 10`, `%id = 25`, **`%st = 30`**. Cột `%st = 30%` nghĩa là gì và ứng dụng Java trên VM này đang gặp vấn đề từ đâu?

---

## 🟡 L2 — Apply

### E6. Thực nghiệm `CpuBurnWorkload` & Ánh xạ `TID` → `nid=0x...`
**Bối cảnh:** Dùng [lab/](./lab/README.md) chạy `CpuBurnWorkload` và `PerfTriageLabRunner`.
**Yêu cầu:**
1. Chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`.
2. Quan sát phép đo CPU time thật của các thread (`ThreadMXBean`) so sánh thread `cpu-burner` vs thread `idle-waiter`.
3. Chuyển đổi các Linux TID giả lập (`4839`, `5120`, `9012`) sang dạng hex `nid=0x...` dùng hàm chuẩn trong `PerfTriageAnalyzer` (hoặc viết tay kiểm tra bằng `Long.toHexString(...)`) và ghép đúng với thread tương ứng trong mẫu Thread Dump.

**Tiêu chí hoàn thành:**
- [ ] `mvn -o -B verify` pass 100%.
- [ ] Ánh xạ chính xác `4839` → `0x12e7`, `5120` → `0x1400`, `9012` → `0x2334`.

<details><summary>💡 Hint</summary>

`4839 / 16 = 302 dư 7` → `302 = 0x12e` → `0x12e7`! Trên Linux dùng lệnh: `printf "0x%x\n" 4839`.
</details>

---

### E7. Phân tích 3 bộ log `vmstat 1` + `iostat -xz 1` + `mpstat -P ALL 1` bằng `PerfTriageAnalyzer`
**Bối cảnh:** Mở `lab/src/main/java/lab/linux/perf/PerfTriageAnalyzer.java` và `PerfToolsTest.java`.
**Yêu cầu:**
Tự đọc bằng mắt các số liệu đầu vào trong `PerfToolsTest` *trước* khi xem assertion, tự kết luận bottleneck của từng bộ (CPU Saturation, Disk I/O / D-state, Single-core Hotspot, hay Swap Thrashing), rồi chạy `mvn -o -B verify` để kiểm chứng. Bổ sung thêm 1 case chẩn đoán **`CPU_STEAL_HYPERVISOR`** (hoặc đối chiếu `lab/solutions/StealTimeTriageTest.java`) khi `st >= 15%`.

**Tiêu chí hoàn thành:**
- [ ] Đọc đúng nguyên nhân của cả 3 bộ log mẫu + bộ `CPU_STEAL_HYPERVISOR` bổ sung.

<details><summary>💡 Hint</summary>

Nhìn cột `st` ở cuối bảng `vmstat 1`: nếu `st >= 15`, VM đang bị hypervisor cướp CPU cycle do noisy neighbor trên physical host!
</details>

---

### E8. Diễn giải chỉ số `iostat -xz 1` để tìm ổ đĩa nghẽn thật
**Bối cảnh:** Xem mẫu output `iostat -xz 1` trên server DB/JBoss bên dưới:
```text
Device            r/s     w/s     rkB/s     wkB/s   await   aqu-sz  %util
nvme0n1         120.0   450.0    1920.0   18400.0    0.45     0.25   18.2
sdb              15.0   680.0     480.0   62000.0   92.40    14.80   99.8
```
**Yêu cầu:**
So sánh `nvme0n1` và `sdb`: ổ đĩa nào đang bị quá tải nghiêm trọng, chỉ số nào khẳng định điều đó (`await`, `aqu-sz`, `%util`), và nếu JBoss ghi `server.log` vào `/var/log` (mount trên `sdb`), hiện tượng gì sẽ xảy ra trên JBoss thread pool?

**Tiêu chí hoàn thành:**
- [ ] Chỉ ra đúng `sdb` (`await = 92.40 ms`, `aqu-sz = 14.80`, `%util = 99.8%`).
- [ ] Giải thích được nếu JBoss dùng logger đồng bộ ghi vào `sdb`, worker thread JBoss có thể kẹt `write()` / `D`-state làm treo toàn bộ HTTP request!

<details><summary>💡 Hint</summary>

`nvme0n1` chỉ mất `0.45 ms` cho mỗi I/O request (`aqu-sz = 0.25`). Trong khi đó `sdb` mất tận `92.40 ms` mỗi lần đọc/ghi và có trung bình `14.8` I/O request xếp hàng chờ cùng lúc (`%util = 99.8%`).
</details>

---

## 🔴 L3 — Deep

### E9. Ticket Production: Server JBoss `8 core` báo Load Average `24.8` lúc 10:15 — nâng CPU hay sửa cái khác?
**Tình huống:**
> Hệ thống **JBoss AS 7.1.1** chạy trên VM Linux `8 vCPU`, `16 GB RAM`. Lúc `10:15`, p99 latency tăng từ `120 ms` lên `9.400 ms`. Bạn chạy 3 lệnh đầu trong checklist 60 giây và thu được:
> ```text
> $ uptime
>  10:15:42 up 45 days,  3:12,  2 users,  load average: 24.85, 18.20, 9.10
>
> $ vmstat 1 4
> procs -----------memory---------- ---swap-- -----io---- -system-- ------cpu-----
>  r  b   swpd   free   buff  cache   si   so    bi    bo   in   cs us sy id wa st
>  3  8 120000 420000  95000 5200000  12   28  2100  4500 1800 3200 22  6 52 20  0
>  1 21 120000 380000  94000 5180000   0    0   120 48200 2400 4100  8  3 18 71  0
>  2 22 120000 375000  94000 5150000   0    0    80 51400 2500 4250  7  3 15 75  0
>
> $ pidstat -d 1 2
> 10:15:48      UID       PID   kB_rd/s   kB_wr/s kB_ccwr/s  Command
> 10:15:49      998      4812      0.00  51200.00      0.00  java
> 10:15:49        0       512      0.00    120.00      0.00  systemd-journal
> ```

**Yêu cầu:**
1. Kết luận ngay **nguyên nhân gốc tại tầng OS** dựa trên số liệu thực tế (`r`, `b`, `bo`, `wa`, `pidstat -d`). Tại sao đề xuất *"nâng từ 8 vCPU lên 32 vCPU"* của team infra sẽ **thất bại hoàn toàn**?
2. Giải thích **cơ chế làm thế nào tiến trình `java` (PID `4812`)** lại tạo ra `51.200 kB/s` (`~50 MB/s`) ghi đĩa và khiến `22` thread rơi vào trạng thái `b` (D-state) kéo Load Average lên `24.85`.
3. Nêu **3 bước tiếp theo** để xác nhận dòng code/cấu hình cụ thể trên JBoss đang gây ghi đĩa 50 MB/s (gồm `lsof -p 4812`, `kill -3 4812` + `iostat -xz 1`), và đề xuất cách khắc phục triệt để.

**Tiêu chí hoàn thành:**
- [ ] Bỏ qua dòng 1 của `vmstat`, đọc đúng dòng 2–3 (`r=1..2`, `b=21..22`, `us+sy=10..11%`, `wa=71..75%`, `bo=48200..51400`).
- [ ] Chỉ ra chính xác PID `4812` (`java`) đang xả `~50 MB/s` ghi đĩa (thường do bật `DEBUG`/dump log đồng bộ hoặc xuất file tạm trên ổ đĩa chậm, hoặc ghi heap dump), và nêu đúng `lsof` + `kill -3` để bắt file/class đang ghi.

<details><summary>💡 Hint</summary>

1. CPU dùng có `7-8% us + 3% sy ≈ 10-11%`, còn `71-75%` là `wa` (iowait) và `21-22` task nằm ở cột `b` (blocked D-state)! Tổng `r + b = 2 + 22 = 24`, khớp y chang `load average: 24.85`!
2. `pidstat -d 1` chỉ đích danh `PID 4812 (java)` ghi **`51.200 kB/s` (`50 MB/s`)** xuống đĩa!
3. Bước tiếp: `lsof -nP -p 4812 | grep -E "w |u "` (hoặc `ls -l /proc/4812/fd`) tìm file đang ghi to/tăng nhanh (vd `server.log` bị bật log `DEBUG` ghi cả SQL/XML payload đồng bộ qua appender có `flush`), kết hợp `kill -3 4812` để xem 22 thread HTTP đang kẹt ở hàm `Log4j FileAppender.append` hoặc `FileOutputStream.writeBytes`!
</details>
