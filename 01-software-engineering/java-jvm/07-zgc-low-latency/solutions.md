# Đáp án · JVM-07 · ZGC / Shenandoah & low-latency GC

## Đáp án Bài tập

### Level 1

#### Bài 1.1 · G1 STW Evacuation vs ZGC Concurrent Relocate
- **G1GC** phải dừng toàn bộ mutator trong pha Evacuation/Compaction vì không có cơ chế chặn và định tuyến khi app đọc một reference đang bị di dời giữa chừng; nếu app sửa field của object ở địa chỉ cũ trong khi GC đang copy sang địa chỉ mới, dữ liệu sẽ mất đồng bộ hoặc trỏ vào rác.
- **ZGC** cho phép di dời song song vì mọi object reference là **colored pointer** và mọi lệnh đọc reference từ heap đều đi qua **Load Barrier**: nếu ZGC vừa copy object sang `newAddr` (hoặc đang nằm trong Relocation Set chưa copy xong), Load Barrier chặn ngay tại thời điểm dereference, tra off-heap Forwarding Table (hoặc tự copy bằng CAS nếu cần), trả `newAddr` cho mutator và **self-heal** ghi đè địa chỉ mới vào field.

#### Bài 1.2 · Thứ tự tại Load Barrier
1. **Lần đọc 1 (Thread A: `Customer c = order.customer;`)**:
   - Load Barrier kiểm tra color bits của pointer đọc được → phát hiện **bad color** → rẽ vào **slow path**.
   - Kiểm tra `0x1000` thuộc Relocation Set và đã được copy sang `0x9000` trong Forwarding Table (nếu GC thread chưa kịp copy thì chính Thread A tự cấp phát vùng mới và copy via CAS).
   - Thực hiện **Self-Healing**: CAS ghi đè `0x9000` (mang **good color**) ngược trở lại vào ô nhớ `order.customer`.
   - Trả về `0x9000` cho biến cục bộ `c`.
2. **Lần đọc 2 (Thread B: `Customer c2 = order.customer;`)**:
   - Load Barrier đọc được `0x9000` đã mang **good color** → đi thẳng **fast path** (chỉ 1–2 lệnh test bit CPU), không cần tra Forwarding Table hay đụng CAS nữa.

#### Bài 1.3 · Flag ZGC theo phiên bản JDK
- **Java 21 LTS**: `-XX:+UseZGC -XX:+ZGenerational` (nếu thiếu `-XX:+ZGenerational`, JDK 21 chạy ZGC single-generation).
- **Java 23**: `-XX:+UseZGC` mặc định là Generational (JEP 474).
- **Java 24+ (như JDK 26 trên máy lab)**: `-XX:+UseZGC` là đủ vì non-generational mode đã bị loại bỏ (JEP 490).

#### Bài 1.4 · 3 tình huống không nên chọn ZGC
1. **Batch job / Spark ETL / nightly report**: Ưu tiên tổng thời gian chạy nhanh nhất trên lượng CPU cố định, không cần `p99 < 1ms` → chọn **Parallel GC** (hoặc **G1GC**) để tránh overhead load/write barrier.
2. **Pod/container bị giới hạn `< 2 vCPU` hoặc CPU đã sát 85–90%**: Concurrent GC thread của ZGC bị đói CPU, không dọn kịp tốc độ cấp phát → sinh **Allocation Stall** khiến latency tệ hơn cả G1 → chọn **G1GC** (hoặc **Serial/Parallel** tuỳ heap).
3. **Heap nhỏ (`< 512 MB`) hoặc RAM container bị ép sát live set (headroom < 15–20%)**: Dễ dính Allocation Stall vì không đủ chỗ chứa floating garbage → chọn **G1GC** (hoặc tăng RAM trước khi dùng ZGC).

---

### Level 2

#### Bài 2.1 · Đọc log G1GC vs ZGC từ `LatencyComparisonRunner`
Kết quả tham chiếu đã đo trên máy lab (`-Xmx1g`, OpenJDK 26.0.1):
- **G1GC**: `p50=986 us | p99=27668 us | max=41848 us (41.85 ms)` — log ghi nhận `Evacuation Failure: Allocation` và `Pause Full (G1 Compaction Pause) 1023M->354M(1024M) 28.486ms`.
- **ZGC**: `p50=988 us | p99=2474 us | max=2653 us (2.65 ms)` — jitter ~2.5 ms thực chất do độ phân giải `Thread.sleep(1)` của Windows OS.
- **Phân biệt trong `-Xlog:gc,gc+phases=info`**:
  - Dòng cycle nền: `Minor Collection (Allocation Rate) 832M(81%)->412M(40%) 0.004s` (4 ms là **concurrent cycle**, app không dừng).
  - 3 dòng STW pause thực sự trong một cycle:
    - `Y: Pause Mark Start (Major) 0.011ms`
    - `Y: Pause Mark End 0.010ms`
    - `Y: Pause Relocate Start 0.007ms`
    Tổng STW pause < `0.03 ms`.

#### Bài 2.2 · Tác dụng của `-XX:SoftMaxHeapSize`
- Khi đặt `-Xmx1g -XX:SoftMaxHeapSize=512m`, ZGC sẽ ưu tiên giữ heap sử dụng quanh mức 512 MB và uncommit phần bộ nhớ nhàn rỗi trả OS khi tải thấp (đỡ tốn RAM cho cloud/node chạy chung nhiều service).
- Khi traffic tăng đột biến, JVM vẫn được phép mở rộng tạm lên tới `-Xmx1g` thay vì quăng `Allocation Stall` hay OOM ngay ở ngưỡng 512 MB — khác hẳn việc đặt cứng `-Xmx512m`.

#### Bài 2.3 · Legacy Java 7 / JBoss AS 7.1.1
1. **Không bật được ZGC** (chỉ có từ Java 11+, chính thức từ Java 15+). G1GC có mặt thử nghiệm ở Java 7 nhưng chưa ổn định như Java 8u40+/9+; do đó trên Java 7 production thường dùng **CMS** nếu cần giảm pause.
2. Cấu hình CMS tham khảo cho `standalone.conf` / `standalone.conf.bat`:

```text
-Xms4g -Xmx4g
-XX:MaxPermSize=256m
-XX:+UseConcMarkSweepGC
-XX:+UseParNewGC
-XX:+CMSParallelRemarkEnabled
-XX:CMSInitiatingOccupancyFraction=70
-XX:+UseCMSInitiatingOccupancyOnly
-XX:+ScavengeBeforeFullGC
```

Giải thích: đặt ngưỡng `CMSInitiatingOccupancyFraction=70` kèm `UseCMSInitiatingOccupancyOnly` buộc CMS chạy dọn nền khi Old Gen đạt 70%, tránh để Old Gen chạm 99% mới dọn dẫn tới **Concurrent Mode Failure** rơi về Serial Full GC 6–9 giây.

---

### Level 3

#### Bài 3.1 · Giải pháp cho `Allocation Stall`
Xem code mẫu hoàn chỉnh tại [`lab/solutions/AllocationStallDemo.java`](lab/solutions/AllocationStallDemo.java).

- Khi chạy `-Xmx360m -XX:+UseZGC "-Xlog:gc"`:
```text
[0.633s][info][gc] Allocation Stall (tight-allocator) 27.961ms
[0.657s][info][gc] Allocation Stall (tight-allocator) 10.125ms
[0.657s][info][gc] Allocation Stall (main) 8.288ms
```
- Khi chạy `-Xmx1g -XX:+UseZGC "-Xlog:gc"`:
```text
Live objects=1600000 | Worst allocation probe=0 ms
```
**Bài học**: `Allocation Stall` là dấu hiệu heap headroom hoặc CPU dành cho ZGC quá thiếu so với allocation rate của mutator; không thể "tune" bằng cách giảm pause target mà phải tăng `-Xmx` (hoặc giảm allocation rate / thêm vCPU).

---

## Đáp án Câu hỏi tự kiểm tra

<details>
<summary>1. Tại sao ZGC có thể giải phóng ZPage nguồn ngay sau khi copy object sống xong dù heap còn reference cũ?</summary>

Vì khi đọc bất kỳ heap reference nào, mutator đều đi qua **Load Barrier**. Nếu con trỏ vẫn mang màu cũ trỏ về ZPage đã giải phóng, Load Barrier không dereference trực tiếp mà tra **Forwarding Table** (lưu ngoài ZPage) để lấy địa chỉ mới `newAddr` và self-heal con trỏ ngay tại chỗ. Nhờ vậy ZPage cũ không còn bị truy cập nữa và có thể reclaim ngay.

</details>

<details>
<summary>2. Trong Self-Healing của Load Barrier, thao tác nào ngăn lần đọc sau lặp lại slow path?</summary>

Thao tác **CAS (Compare-And-Swap)** ghi đè địa chỉ mới kèm **good color** ngược lại vào chính field trên heap vừa đọc (`obj.field`). Các thread đến sau đọc được good pointer sẽ đi thẳng qua fast path.

</details>

<details>
<summary>3. Vì sao từ Java 21 cần ưu tiên Generational ZGC thay cho ZGC single-generation?</summary>

Vì phần lớn object chết non (**Weak Generational Hypothesis**). Single-gen ZGC phải mark toàn bộ heap mỗi chu kỳ nên tốn nhiều CPU và dễ Allocation Stall dưới tỷ lệ cấp phát cao. Generational ZGC tách Young/Old với `Minor Collection` cực nhanh trên tập object mới, giảm mạnh CPU overhead và nhu cầu RAM dư.

</details>

<details>
<summary>4. `[gc] Allocation Stall (worker-1) 42.150ms` nghĩa là gì và xử lý ra sao?</summary>

Nghĩa là thread `worker-1` cần cấp phát object mới nhưng hết ZPage trống trong khi chu kỳ GC chưa dọn xong, buộc `worker-1` bị chặn cứng `42.150 ms`. Hai hướng xử lý chính: (1) tăng RAM headroom (`-Xmx` lớn hơn live set tối thiểu 25–30%, đảm bảo CPU đủ core cho GC chạy nền), và (2) giảm allocation rate của app (tránh tạo rác thừa trong hot path) hoặc chuyển sang G1GC nếu container quá chật RAM/CPU.

</details>

<details>
<summary>5. Java 7 / JBoss AS 7.1.1 dính Full GC 8 giây nhưng chưa nâng JDK ngay thì xử lý thế nào?</summary>

Kiểm tra GC log xem có phải **Concurrent Mode Failure** (nếu đang dùng CMS) hoặc Parallel Full GC hay PermGen đầy. Nếu dùng CMS, hạ `-XX:CMSInitiatingOccupancyFraction=70` kèm `-XX:+UseCMSInitiatingOccupancyOnly` để quét sớm hơn, giữ `MaxPermSize` đủ dùng và điều tra memory/classloader leak (không thể dùng ZGC trên Java 7).

</details>
