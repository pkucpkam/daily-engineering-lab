# LNX-03 · Bài tập — Virtual memory, page cache, swap & OOM killer

> Làm theo thứ tự. **Tự làm trước, chỉ mở hint khi đã thử ≥ 15 phút.** Đáp án ở [solutions.md](./solutions.md) — chỉ mở sau khi đã có câu trả lời của riêng bạn.
> Ghi thời gian làm từng bài vào `my-notes.md` để theo dõi tiến bộ.

| Cấp | Mục đích | Số lượng | Thời gian gợi ý |
| :--- | :--- | :---: | :---: |
| 🟢 L1 — Recall | Nhớ & hiểu VSZ/RSS, Page Cache, Swap, OOM score | 5 | 20' |
| 🟡 L2 — Apply | Đo Direct Buffer off-heap & phân tích `free -m` / `oom_score` trong lab | 3 | 70' |
| 🔴 L3 — Deep | Ticket JBoss mất tích (Exit 137) không để lại log OOM Java | 1 | 90' |

---

## 🟢 L1 — Recall

### E1. Đọc 2 output `free -m` thật — máy nào sắp sập?
So sánh 2 máy Linux đều có **16 GB RAM**:
```text
# Máy A
               total        used        free      shared  buff/cache   available
Mem:           16000        4800         200         120       11000       10800
Swap:           2048           0        2048

# Máy B
               total        used        free      shared  buff/cache   available
Mem:           16000       14100         250         100        1650         820
Swap:           2048        1890         158
```
Cả hai máy đều có cột `free` ~`200–250 MB` (~1,5% RAM). Máy nào hoàn toàn khỏe mạnh, máy nào đang nguy kịch? Giải thích cơ chế Page Cache và ý nghĩa cột `available` + `Swap used`.

---

### E2. Tại sao `top` báo `VIRT = 14 GB` trên máy 8 GB RAM?
Khi chạy JBoss với `-Xmx4g` trên server `8 GB`, đồng nghiệp mở `top` thấy cột `VIRT` (`VSZ`) của process `java` lên tới `14.2g`, nhưng `RES` (`RSS`) chỉ là `3.1g`. Có phải Java đang leak bộ nhớ vượt quá 8 GB không? Vì sao?

---

### E3. Tác hại của việc chạy cron `echo 3 > /proc/sys/vm/drop_caches`
Một blog cũ hướng dẫn chạy `sync; echo 3 > /proc/sys/vm/drop_caches` mỗi 30 phút để "giải phóng RAM cho app server". Giải thích tại sao trên production việc này **phá hiệu năng** của JBoss / PostgreSQL chứ không hề làm app nhanh hơn.

---

### E4. Vì sao `oom_score_adj = -1000` cho JBoss là ý tưởng tồi?
Một người bị OOM Killer giết JBoss 2 lần nên bực mình chạy `echo -1000 > /proc/<jboss_pid>/oom_score_adj` để bảo vệ JBoss. Nếu JBoss hoặc app khác tiếp tục leak RAM, hậu quả tệ hại hơn nào sẽ xảy ra cho toàn bộ máy chủ?

---

### E5. Swap làm GC Java chậm đi như thế nào?
Giải thích vì sao chỉ cần `500 MB` vùng Heap của JVM bị đẩy ra đĩa Swap (`so`/`si` trong `vmstat`) cũng đủ làm thời gian **Stop-the-World GC pause** tăng từ `40 ms` lên tới vài giây.

---

## 🟡 L2 — Apply

### E6. Đo trực tiếp phần bộ nhớ Off-Heap không nằm trong `-Xmx`
**Bối cảnh:** Dùng [lab/](./lab/README.md) chạy `MemoryFootprintAnalyzer` và `MemoryOomLabRunner`.
**Yêu cầu:**
1. Chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`.
2. Quan sát sự thay đổi của `HeapMemoryUsage` vs `BufferPoolMXBean("direct")` trước và sau khi cấp phát `64 MB` qua `ByteBuffer.allocateDirect(...)`.
3. Tính toán thử ngân sách RAM cho một container `2 GB` (`2048 MB`) chạy service Java có `200` threads (`-Xss512k`), `Metaspace 256 MB`, `DirectBuffer 256 MB`, `CodeCache + GC overhead 250 MB`: mức `-Xmx` an toàn tối đa nên đặt là bao nhiêu MB (chừa thêm `~15%` headroom cho OS/page cache)?

**Tiêu chí hoàn thành:**
- [ ] `mvn -o -B verify` pass 100%.
- [ ] Chứng minh qua số đo lab rằng `Direct Buffer` tăng `64 MB` trong khi `Heap used` gần như không đổi.
- [ ] Bảng cộng từng thành phần RSS cho container `2048 MB` ra con số `-Xmx` hợp lý (`~900–1024 MB`, không vượt trần).

<details><summary>💡 Hint</summary>

`200 threads × 0.5 MB = 100 MB` + `256 MB Metaspace` + `256 MB Direct` + `250 MB CodeCache/GC` = `862 MB` off-heap! Nếu chừa `150–200 MB` headroom trên `2048 MB`, `-Xmx` chỉ nên nằm quanh `900 MB – 1024 MB` (~50% ở container nhỏ!).
</details>

---

### E7. Viết bộ phân tích `free -m` & dự đoán nạn nhân OOM Killer
**Bối cảnh:** Mở `lab/src/main/java/lab/linux/oom/FreeOutputParser.java` và `OomScoreCalculator.java`.
**Yêu cầu:**
1. Chạy test phân tích 3 mẫu output `free -m` (khỏe mạnh nhờ Page Cache, cảnh báo thiếu `available`, và swap thrashing).
2. Mở rộng (hoặc viết test tương tự `lab/solutions/OomVictimPredictionTest.java`) cho kịch bản host `16 GB` RAM có 4 tiến trình:
   - `sshd`: `RSS = 128 MB`, `Swap = 0`, `oom_score_adj = -1000`
   - `postgres`: `RSS = 4.000 MB`, `Swap = 0`, `oom_score_adj = -300`
   - `jboss-java`: `RSS = 9.000 MB`, `Swap = 200 MB`, `oom_score_adj = 0`
   - `log-collector`: `RSS = 1.600 MB`, `Swap = 0`, `oom_score_adj = 500`
   Tính `effectiveScore` từng process và xác định kernel OOM Killer sẽ giết tiến trình nào trước!

**Tiêu chí hoàn thành:**
- [ ] Tính đúng `log-collector` (`100 + 500 = 600`) vs `jboss-java` (`575 + 0 = 575`) → `log-collector` bị giết trước vì được ưu tiên `+500`!

<details><summary>💡 Hint</summary>

Công thức đơn giản hóa sát với `oom_badness`: `baseScore = round((rss + swap) * 1000.0 / totalRamPlusSwap)`, `effective = clamp(baseScore + oomScoreAdj, 0, 1000)` (nếu `oom_score_adj == -1000` thì miễn nhiễm = `0`).
</details>

---

### E8. Đọc hiểu bảng `vmstat 1` trong sự cố Swap Thrashing & Page Cache Pressure
**Bối cảnh:** Xem mẫu đầu ra `vmstat 1` dưới đây từ một server Java chậm đột biến:
```text
procs -----------memory---------- ---swap-- -----io---- -system-- ------cpu-----
 r  b   swpd   free   buff  cache   si   so    bi    bo   in   cs us sy id wa st
 2  0 120000 185000  92000 8400000   0    0    12   140 1200 2100 18  4 77  1  0
 9  4 980000 112000  18000  420000 640 1280 24500 8200 6800 18400 22 14  8 56  0
```
**Yêu cầu:**
So sánh dòng 1 và dòng 2 trên 5 chỉ số (`r/b`, `cache`, `si/so`, `bi/bo`, `wa`) và kết luận điều gì vừa xảy ra ở dòng 2.

**Tiêu chí hoàn thành:**
- [ ] Chỉ ra đúng hiện tượng: Page Cache bị ép tụt từ `8.4 GB` xuống `420 MB`, swap tăng mạnh (`swpd` lên `980 MB`, `si=640, so=1280` KB/s), kéo theo `b=4` blocked và `wa=56%` iowait.

<details><summary>💡 Hint</summary>

Dòng 1: `cache` cao (`8.4 GB`), `si/so = 0`, CPU idle `77%`, `wa = 1%`. Dòng 2: có tiến trình ăn sạch RAM buộc kernel xả page cache và đẩy dữ liệu ra swap (`so=1280`, `si=640`), đĩa làm việc nặng (`bi=24500`, `wa=56%`).
</details>

---

## 🔴 L3 — Deep

### E9. Ticket Production: JBoss biến mất không dấu vết lúc cao điểm 14:00 (Exit 137)
**Tình huống:**
> Hệ thống **JBoss AS 7.1.1** chạy trên VM Linux `16 GB` RAM (`Swap = 2 GB`, `vm.swappiness = 60`). Trong file `standalone.conf` có cấu hình:
> `JAVA_OPTS="-Xms12g -Xmx12g -XX:MaxPermSize=1024m -Xss1m"`
> Trong JBoss chạy ~`350` threads, và trên cùng VM có thêm một service báo cáo Python/batch export Excel chạy lúc `14:00` mỗi ngày (ăn ~`2.5 GB` RSS khi chạy).
> Hôm nay lúc `14:03`, JBoss đột ngột dừng hẳn. Kiểm tra `standalone/log/server.log` không có `OutOfMemoryError`, không có `hs_err_pid*.log` trong `/opt/jboss`.

**Yêu cầu:**
1. Tính tổng **worst-case RSS** của JBoss + OS + batch service ở thời điểm `14:03` để chứng minh vì sao sự cố chắc chắn xảy ra.
2. Liệt kê **3 lệnh Linux** để xác nhận 100% thủ phạm là **OOM Killer** và kiểm tra điểm `oom_score` của JBoss.
3. Đề xuất **4 biện pháp sửa** (cả cấu hình JVM/JBoss, `vm.swappiness`, tách workload/cgroup, và flag chẩn đoán).

**Tiêu chí hoàn thành:**
- [ ] Tính đúng các phần bộ nhớ ngoài Heap (`PermGen 1 GB + 350*1MB Stack + CodeCache/Direct/Native`) khiến tổng nhu cầu vượt `16 GB RAM + 2 GB Swap`.
- [ ] Ghi đúng lệnh `dmesg -T | grep -i -E "oom|killed process"` (hoặc `/var/log/messages`) và `/proc/<pid>/oom_score`.

<details><summary>💡 Hint</summary>

1. Chỉ riêng JBoss: `12 GB Heap` + `1 GB PermGen` + `350 MB Thread Stack` + `~500 MB–1 GB CodeCache/Direct/Native` ≈ **`14–14.5 GB` RSS**! Cộng thêm `~1 GB` OS/systemd/kernel slab + `2.5 GB` batch report lúc `14:00` = **`~17.5–18 GB`**, chạm trần `16 GB RAM` (và đẩy `2 GB` Swap tới khi cạn sạch).
2. Khi cạn RAM + Swap, `java` có RSS `~14 GB` nên `oom_score` đạt `~800+` → bị Linux OOM Killer bắn `SIGKILL` trước tiên, khiến JVM chết ngay không kịp viết bất kỳ log `OutOfMemoryError` nào!
</details>
