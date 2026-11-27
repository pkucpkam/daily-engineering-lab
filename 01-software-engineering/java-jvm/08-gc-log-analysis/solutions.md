# Đáp án · JVM-08 · Đọc GC log

## Đáp án Bài tập

### Level 1

#### Bài 1.1 · Giải phẫu dòng `-Xlog:gc*`
1. `6.790s` là **JVM uptime** (giây từ lúc process JVM khởi động). Muốn có giờ thực tế, thêm decorator `time` (`-Xlog:gc*:...:time,uptime,level,tags`).
2. Trước GC: `1023 MB`, sau GC: `354 MB`, dung lượng heap đang cấp/max: `1024 MB`.
3. Lượng rác vừa giải phóng: `1023 - 354 = 669 MB`.
4. **Không** — ở ZGC rút gọn `-Xlog:gc`, `0.004s` là thời gian cả **chu kỳ đồng thời (`Minor Collection` cycle)**; các STW pause thật (`Pause Mark Start`, `Pause Mark End`, `Pause Relocate Start`) nằm ở `-Xlog:gc,gc+phases=info` và chỉ cỡ vài chục micro giây.

#### Bài 1.2 · Tính Allocation Rate & Throughput
1. Từ `10.000s -> 10.500s`: cấp phát khoảng `380 - 80 = 300 MB`; từ `10.500s -> 11.000s`: `382 - 82 = 300 MB` → tổng `600 MB` trong `1.0s` ⇒ **Allocation Rate ≈ `600 MB/s`**.
2. Tổng pause trên khoảng 1 giây (tính cả 2 pause sau): `6.0 + 5.0 = 11.0 ms` (hoặc tính cả `GC(10)` là `16 ms`) ⇒ Throughput ≈ `100% * (1 - 0.011) = 98.9%` (hoặc `98.4%`).
3. **Không leak** — floor sau GC dao động ngang `80M -> 82M -> 81M`.

#### Bài 1.3 · Ghép kịch bản
- **A → High Allocation Churn** (tạo nhiều rác ngắn hạn nhưng không giữ lại, cần profile hotspot bằng JFR để tiết kiệm CPU).
- **B → Unbounded Memory Leak** (đáy răng cưa leo sát trần `2 GB` và Full GC không dọn được nữa).
- **C → Tight Headroom / Under-sized `-Xmx`** (live set hợp lệ là `1.65 GB`, chỉ thiếu RAM đệm nên cần nới `-Xmx` lên ví dụ `2.5–3 GB` hoặc tối ưu cấu trúc cache).

#### Bài 1.4 · Chuyển sang Java 7 / JBoss AS 7.1.1
```text
-XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=5 -XX:GCLogFileSize=10M -Xloggc:logs/gc.log
```

---

### Level 2

#### Bài 2.1 · Tại sao `GcLogAnalyzer` bỏ qua 25% event đầu
Khi JVM mới khởi động hoặc nạp baseline data (`24 MB` trong `GcWorkloadRunner`), các lần Young GC đầu tiên có `afterMb` tăng dần từ `2 MB -> 26 MB` chỉ vì đang xây dựng trạng thái ổn định ban đầu (*warmup*), không phải leak. Nếu không loại bỏ pha warmup, phép so sánh đầu-cuối sẽ báo giả `leakSuspected=true` ngay cả trên workload khoẻ mạnh.

#### Bài 2.2 · Tại sao `sys=3.95s` trên tổng `real=4.82s`
1. **OS Swapping / Paging**: JVM heap đang nằm trên swap ngoài đĩa; khi GC đi qua quét toàn bộ Old Gen để mark, nó chạm vào các page bị swap out buộc kernel đọc lại từ disk (`sys` time tăng vọt).
2. **Disk/VM contention khi ghi GC log hoặc JVM hsperfdata**: Nếu `/tmp` (nơi chứa `hsperfdata_<user>`) hoặc thư mục ghi `gc.log` bị nghẽn I/O nặng, hoặc hypervisor bị thiếu tài nguyên (CPU steal / memory ballooning), thread JVM bị block trong syscall.

#### Bài 2.3 · Tại sao app leak có Allocation Rate thấp hơn
Vì app leak mắc kẹt vào hàng loạt **Full GC / Evacuation Failure** (`266` pauses, `throughput = 62.25%`): 38% thời gian thread ứng dụng bị đóng băng đứng chờ GC nên không thể chạy vòng lặp cấp phát nhanh bằng app bình thường (`throughput = 90.32%`). Đây là bẫy hay gặp khi chỉ nhìn một con số đơn lẻ.

---

### Level 3

#### Bài 3.1 · Báo cáo triage từ 2 log thật
Xem code so sánh tại [`lab/solutions/LogTriageSolution.java`](lab/solutions/LogTriageSolution.java). Kết quả mẫu đo được với `-Xmx192m -XX:+UseG1GC` (`24.000` iterations):

```text
NORMAL : events=32 (full=0) | duration=0.232s | totalPause=22.45ms | p95=1.39ms | max=2.72ms | throughput=90.32% | allocRate=6952.6 MB/s | floor=26MB->26MB | leakSuspected=false
LEAK   : events=266 (full=9) | duration=0.396s | totalPause=149.48ms | p95=1.74ms | max=8.95ms | throughput=62.25% | allocRate=4103.5 MB/s | floor=169MB->177MB | leakSuspected=true
Delta live floor growth: normal=+0 MB vs leak=+8 MB
```

Giải thích định lượng:
- Workload `normal` có `0` Full GC, floor steady-state phẳng tuyệt đối `26 MB -> 26 MB` dù đã bơm gần `1.5 GB` byte array tạm thời.
- Workload `leak` giữ lại 1/8 số allocation tới `177 MB` trên `-Xmx192m`, làm số GC tăng gấp 8 lần (`266` vs `32`), xuất hiện **9 lần `Pause Full (G1 Compaction Pause)`**, GC throughput rơi xuống `62.25%`, và live floor bị dồn lên `177 MB / 192 MB` (~92% trần heap).

---

## Đáp án Câu hỏi tự kiểm tra

<details>
<summary>1. Khác biệt giữa `-Xlog:gc` và `-Xlog:gc*`? Tác dụng của `:time,uptime:filecount=10,filesize=20M`?</summary>

`-Xlog:gc` chỉ bật đúng tag `gc` mức `info` (tổng kết thô từng event). `-Xlog:gc*` (dấu sao wildcard) bật toàn bộ sub-tag như `gc,heap` (Eden/Survivor/Old), `gc,phases`, `gc,age`, `gc,ergo` để tính được promotion và xác định nguyên nhân gốc. Phần sau thêm giờ wall-clock + uptime và cấu hình log rotation tự xoay vòng tối đa 10 file x 20 MB để không làm đầy đĩa production.

</details>

<details>
<summary>2. Làm sao phân biệt Memory Leak với Heap thiếu headroom?</summary>

Cả hai đều có nhiều GC và pause cao, nhưng **Memory Leak** có đường đáy `afterMb` leo dốc liên tục không dừng sau mỗi Full/Mixed GC cho đến lúc chạm `-Xmx`, còn **thiếu headroom** thì `afterMb` sau khi warmup sẽ dao động ngang quanh một mức ổn định (ví dụ ~85–90% `-Xmx`).

</details>

<details>
<summary>3. Allocation rate 4 GB/s toàn object sống < 2 ms nhưng sao Old Gen vẫn đầy?</summary>

Vì hiện tượng **Premature Promotion**: Young GC kích hoạt quá dày làm những object trung gian đang dở dang (~50–100 ms từ một HTTP request chưa kịp xong) nhanh chóng vượt ngưỡng tenuring hoặc làm tràn Survivor (`To-space exhausted`), buộc phải tràn sớm sang Old Gen trước khi kịp chết.

</details>

<details>
<summary>4. `[Times: user=0.05 sys=0.40, real=0.45 secs]` báo động điều gì?</summary>

`sys` (kernel time) chiếm gần hết thời gian `real` trong khi `user` rất nhỏ: thường do OS đang **swap heap ra đĩa**, contention I/O ghi file `hsperfdata` / `gc.log` ở ổ đĩa chậm/NFS, hoặc hypervisor quá tải.

</details>

<details>
<summary>5. Sau khi GC log xác nhận memory leak, bước tiếp theo là gì?</summary>

Chụp **Heap Dump (`.hprof`)** bằng `jcmd <pid> GC.heap_dump` (hoặc kích `-XX:+HeapDumpOnOutOfMemoryError`) rồi mở bằng **Eclipse MAT** xem **Dominator Tree / Retained Size** và **Leak Suspects**, kết hợp **JFR** để tìm call stack gây allocation nóng — nội dung của bài [JVM-09](../09-heap-dump-mat-jfr/README.md).

</details>
