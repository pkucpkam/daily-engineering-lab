# LNX-03 · Đáp án — Virtual memory, page cache, swap & OOM killer

> ⚠️ **Chỉ mở sau sau khi đã tự làm.** Đọc đáp án mà chưa tự làm = mất 80% giá trị bài tập.
> Mỗi đáp án được bọc trong `<details>` để tránh lộ khi lướt.

---

### E1
<details><summary>Xem đáp án</summary>

**Đáp án:**
- **Máy A hoàn toàn khỏe mạnh.** Dù cột `free` chỉ còn `200 MB`, Linux đang dùng `11.000 MB` làm **`buff/cache` (Page Cache)** để tăng tốc đọc ghi đĩa. Phần lớn Page Cache là *clean pages* có thể thu hồi ngay lập tức nên cột **`available` còn tới `10.800 MB` (`67.5%`)** và `Swap used = 0`.
- **Máy B đang nguy kịch.** Phần mềm đang chiếm thực sự `14.100 MB` (`used`), Page Cache bị ép teo còn `1.650 MB`, **`available` chỉ còn `820 MB` (`5.1%`)**, và hệ thống đã phải đẩy **`1.890 / 2.048 MB` ra Swap** (`92%` swap!). Chỉ cần thêm một đợt spike bộ nhớ vài trăm MB là OOM Killer sẽ nổ.

**Đáp án sai phổ biến:** Thấy cột `free = 200 MB` trên Máy A và báo động hết RAM → Sai vì không phân biệt `free` (RAM bỏ không) với `available` (RAM sẵn sàng cấp phát không cần swap).
</details>

---

### E2
<details><summary>Xem đáp án</summary>

**Đáp án:**
**Không phải leak vượt 8 GB.** Cột `VIRT` (`VSZ`) chỉ là tổng không gian địa chỉ ảo đã được tiến trình `mmap`/`reserve` (bao gồm Heap chưa chạm trang, thư viện dùng chung `.so`, các file `.jar` map vào bộ nhớ, vùng address space dự trữ cho thread stack, Code Cache, v.v.). Trên kiến trúc 64-bit, ánh xạ địa chỉ ảo gần như miễn phí và không tiêu thụ RAM vật lý cho tới khi trang nhớ thực sự bị ghi/đọc (*demand paging*). Con số phản ánh RAM vật lý thật đang dùng là **`RES` (`RSS` = `3.1 GB`)** (và chính xác hơn nữa là `PSS`).
</details>

---

### E3
<details><summary>Xem đáp án</summary>

**Đáp án:**
- `echo 3 > /proc/sys/vm/drop_caches` buộc kernel vứt bỏ toàn bộ **Page Cache, dentries và inodes** đang nằm trong RAM.
- Hậu quả: JBoss/PostgreSQL đang đọc rất nhanh từ Page Cache (tốc độ RAM, vài chục nanosecond) bỗng dưng toàn bộ các lượt đọc file tiếp theo thành **cache miss**, buộc phải đọc thẳng từ ổ đĩa (chậm hơn **1.000×–100.000×**), làm IOPS đĩa và CPU iowait tăng vọt, p99 latency của ứng dụng tăng đột ngột sau mỗi lần cron chạy! Linux kernel tự biết thu hồi Page Cache khi app thật sự cần RAM; không cần phải xóa thủ công.
</details>

---

### E4
<details><summary>Xem đáp án</summary>

**Đáp án:**
- `oom_score_adj = -1000` vô hiệu hóa hoàn toàn OOM Killer đối với process `java`/JBoss.
- Nếu chính JBoss bị rò rỉ off-heap/Direct Buffer hoặc có process khác tăng RAM liên tục, kernel **không được phép giết kẻ ăn nhiều RAM nhất (JBoss)**. Thay vào đó, OOM Killer buộc phải đi giết các tiến trình hệ thống quan trọng khác (`sshd`, `systemd-journald`, monitoring agent, cron) hoặc cuối cùng hết sạch tiến trình có thể giết dẫn tới **Kernel Panic / treo cứng cả server** không SSH vào cứu được!
</details>

---

### E5
<details><summary>Xem đáp án</summary>

**Đáp án:**
- Thuật toán GC (như G1GC, Parallel GC, CMS) trong pha **Stop-the-World (STW)** hoặc quét vùng Heap phải đi dọc theo đồ thị object reference chạm tới rất nhiều vùng địa chỉ rải rác trên Heap.
- Khi các page Heap này đang nằm trên **Swap disk** (HDD hoặc network SSD có latency 0.5–10 ms mỗi random I/O so với ~100 ns của RAM), mỗi lần thread GC chạm vào một page đang bị swap-out sẽ kích hoạt **Major Page Fault** → thread GC bị OS chặn chờ đọc 4 KB từ đĩa về RAM! Hàng nghìn major page fault cộng dồn biến pha GC bình thường `40 ms` thành **`5–30 giây` đóng băng toàn bộ JVM** (và dễ khiến heartbeat/Zookeeper/ALB tưởng node đã chết!).
</details>

---

### E6
<details><summary>Xem đáp án</summary>

**Hướng giải:**
1. Chạy `mvn -o -B verify` và `mvn -o -B compile exec:java` trong `lab/` thấy `BufferPoolMXBean("direct")` tăng chính xác `64 MB` khi gọi `ByteBuffer.allocateDirect`, còn Heap hầu như không đổi.
2. Tính ngân sách cho container `2048 MB`:
   - Off-heap cố định ước tính: `200 × 0.5 MB` (Thread Stacks = `100 MB`) + `256 MB` (Metaspace) + `256 MB` (Direct Buffers) + `250 MB` (Code Cache + GC metadata + glibc) = **`862 MB`**.
   - Chừa headroom OS/page cache/an toàn ~`15%` (`~300 MB`).
   - Còn lại cho Heap: `2048 - 862 - 300 = 886 MB` → Đặt `-Xmx896m` (hoặc `-Xmx900m`), tuyệt đối không đặt `-Xmx1500m` trở lên!
</details>

---

### E7
<details><summary>Xem đáp án</summary>

**Đáp án:**
Tổng RAM + Swap của host: `16.000 MB` (hoặc quy đổi tương đương trên `totalRam`). Tính trên `16.000 MB`:
- `sshd`: `oom_score_adj = -1000` → miễn nhiễm (`effective = 0`).
- `postgres`: base ≈ `4000 / 16000 × 1000 = 250`, cộng `adj = -300` → `max(0, 250 - 300) = 0`.
- `jboss-java`: base ≈ `(9000 + 200) / 16000 × 1000 = 575`, cộng `adj = 0` → **`575`**.
- `log-collector`: base ≈ `1600 / 16000 × 1000 = 100`, cộng `adj = +500` → **`600`**.

→ Kết quả: **`log-collector` (`600`) bị OOM Killer chọn giết trước `jboss-java` (`575`)** nhờ `oom_score_adj = +500`!

**Code lời giải tham khảo:** xem [lab/solutions/OomVictimPredictionTest.java](./lab/solutions/OomVictimPredictionTest.java).
</details>

---

### E8
<details><summary>Xem đáp án</summary>

**Đáp án:**
So sánh 2 dòng `vmstat 1`:
- **`r/b`:** Số task runnable (`r`) vọt từ `2` lên `9`; đặc biệt **`b` (blocked chờ I/O không ngắt được)** vọt từ `0` lên **`4`**.
- **`cache`:** Tụt thảm hại từ `8.4 GB` (`8400000 KB`) xuống còn `420 MB` (`420000 KB`) vì kernel bị ép thu hồi hết page cache để nhường RAM cho tiến trình đang ngốn bộ nhớ.
- **`si/so` & `swpd`:** Swap đã dùng (`swpd`) tăng từ `120 MB` lên `980 MB`, tốc độ swap out `so = 1280 KB/s` và swap in `si = 640 KB/s` → **Swap Thrashing** đang xảy ra!
- **`bi/bo` & `wa`:** Đọc ghi block đĩa (`bi=24500, bo=8200`) tăng vọt kéo **`wa` (iowait)** từ `1%` lên **`56%`** (hơn nửa thời gian CPU ngồi chờ đĩa xoay vì swap và cache miss!).
</details>

---

### E9
<details><summary>Xem đáp án</summary>

<details open><summary>Phân tích sự cố JBoss chết lúc 14:03</summary>

**1. Tính tổng bộ nhớ worst-case lúc 14:03:**
- **JBoss JVM:**
  - Heap `-Xmx12g`: `12.0 GB` (đã commit ngay từ lúc khởi động do `-Xms12g`!)
  - PermGen `-XX:MaxPermSize=1024m`: tới `1.0 GB`
  - Thread Stacks (`350 threads × 1 MB -Xss1m`): `~0.35 GB`
  - Code Cache, Direct Buffers, GC data structures, native C heap (`malloc`): `~0.6–1.0 GB`
  - → Tổng RSS của riêng JBoss ≈ **`14.0–14.3 GB`**
- **OS + systemd + sshd + kernel slab + page cache tối thiểu:** `~0.8–1.0 GB`
- **Batch export Excel Python lúc 14:00:** `~2.5 GB`
- **Tổng nhu cầu đồng thời lúc 14:03:** `14.3 + 0.9 + 2.5 ≈ 17.7 GB` — vượt `16 GB RAM` vật lý và ăn nốt `2 GB Swap` (khi `free` + swap chạm 0)! JBoss là process ngốn nhiều RAM nhất (`oom_score ~ 800+`) nên bị OOM Killer bắn **`SIGKILL` (Exit 137)** mà JVM không có cơ hội in `OutOfMemoryError` hay `hs_err_pid`.

**2. 3 lệnh Linux xác nhận OOM Killer:**
```bash
# 1. Xem nhật ký kernel OOM Killer (khẳng định 100% thủ phạm & thời điểm)
dmesg -T | grep -i -E "oom|killed process|out of memory"
# (Trên RHEL/CentOS xem thêm: grep -i oom /var/log/messages)

# 2. Xem điểm oom_score hiện tại của JBoss mới khởi động lại
cat /proc/$(pgrep -f jboss)/oom_score

# 3. Xem mức độ dùng RAM + Swap thực tế hiện tại
free -m && ps -o pid,rss,vsz,comm -p $(pgrep -f jboss)
```

**3. 4 biện pháp khắc phục triệt để:**
1. **Hạ `-Xmx` của JBoss cho phù hợp VM `16 GB`:** Giảm từ `-Xms12g -Xmx12g` xuống ví dụ **`-Xms8g -Xmx8g`** (hoặc `9g` tối đa) và giảm `-Xss256k` hoặc `-Xss512k` nếu stack không sâu, chừa đủ headroom cho PermGen/Metaspace, Direct Buffer và Page Cache.
2. **Tách job batch export Excel khỏi VM JBoss (hoặc chạy với cgroup `MemoryMax=2G` qua `systemd-run`):** Không để một batch job ăn RAM đột biến tranh chấp trực tiếp với app server production trên cùng host không giới hạn.
3. **Tinh chỉnh `vm.swappiness=1`:** Giảm xu hướng đẩy Heap JVM ra swap disk (và bật `-XX:+ExitOnOutOfMemoryError` trên JDK 8u92+/11+/21 hoặc `-XX:OnOutOfMemoryError="kill -9 %p"` trên Java 7 nếu muốn fail fast khi chính Heap cạn).
4. **Cảnh báo `available < 15%` và metric `RSS` của JBoss** qua Prometheus/monitoring trước khi chạm ngưỡng OOM.

</details>
</details>

---

## Self-check (từ README §9) — đáp án gợi ý
<details><summary>Xem</summary>

1. **`VSZ`, `RSS`, `PSS`, `USS`:** `VSZ` là tổng không gian địa chỉ ảo đã xin/map (lazy, chưa cần RAM thật); `RSS` là RAM vật lý đang giữ (`Anon + File`); `PSS` chia đều phần shared library theo số process dùng chung; `USS` là RAM riêng biệt sẽ giải phóng ngay khi kill process. `-Xmx4g` làm `VSZ` reserve sẵn hàng GB địa chỉ ảo, còn `RSS` tăng từ từ theo các trang 4 KB thực sự được chạm.
2. **`free = 250 MB`, `available = 9.500 MB`:** Không hề thiếu RAM. Linux chủ động tận dụng RAM nhàn rỗi làm Page Cache (`buff/cache`) để tăng tốc I/O đĩa; khi app cần RAM, kernel thu hồi clean Page Cache gần như tức thì, nên `available` (`9.5 GB`) mới là con số thật.
3. **4 vùng nhớ ngoài `-Xmx` làm RSS cao:** Thread Stacks (`N × -Xss`), Metaspace (hoặc PermGen trên Java 7), Direct ByteBuffers (`ByteBuffer.allocateDirect`/Netty), JIT Code Cache (+ GC card tables/Remembered Sets).
4. **Swap & JVM GC:** Trong pha Stop-the-World GC, JVM quét qua nhiều vùng Heap; nếu vùng đó đã bị swap ra đĩa, mỗi page fault phải chờ disk I/O (hàng ms thay vì ns), đẩy GC pause lên hàng chục giây. `vm.swappiness` kiểm soát độ ưu tiên đẩy anonymous memory ra swap so với thu hồi page cache.
5. **OOM Killer chọn nạn nhân:** Kernel tính `oom_score` (`0..1000`) tỷ lệ với tỷ lệ `RSS + Swap + PageTable` của process trên tổng RAM/cgroup, rồi cộng `oom_score_adj` (`-1000..+1000`). Process có tổng điểm cao nhất bị bắn `SIGKILL` (`137`) — JBoss thường chiếm nhiều RAM nhất nên hay là nạn nhân đầu tiên.
</details>
