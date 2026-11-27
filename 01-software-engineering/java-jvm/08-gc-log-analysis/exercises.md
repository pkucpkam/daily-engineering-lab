# Bài tập · JVM-08 · Đọc GC log

Làm bài trước khi mở [solutions.md](solutions.md). Thực hành trên [lab/](lab/README.md).

---

## Level 1 · Hiểu cấu trúc & chỉ số (4 bài)

### Bài 1.1 · Giải phẫu dòng `-Xlog:gc*`
Đọc dòng log thật sau đây và trả lời 4 ý:

```text
[6.790s][info][gc] GC(383) Pause Full (G1 Compaction Pause) 1023M->354M(1024M) 28.486ms
```

1. `6.790s` là wall-clock time hay JVM uptime?
2. Heap trước GC, sau GC và tổng committed/max heap là bao nhiêu?
3. Có bao nhiêu MB rác vừa được giải phóng trong chu kỳ này?
4. Nếu một dòng ZGC ghi `Minor Collection 832M(81%)->412M(40%) 0.004s`, con số `0.004s` có phải STW pause không?

### Bài 1.2 · Tính Allocation Rate & Throughput bằng tay
Cho 3 sự kiện GC liên tiếp trên một app `-Xmx512m`:
- `GC(10)` tại `10.000s`: `220M->80M(512M) 5.0ms`
- `GC(11)` tại `10.500s`: `380M->82M(512M) 6.0ms`
- `GC(12)` tại `11.000s`: `382M->81M(512M) 5.0ms`

Tính gần đúng cho khoảng `10.000s -> 11.000s` (1.0 giây):
1. Allocation Rate trung bình (MB/s)?
2. GC Throughput (%) trong khoảng 1 giây đó?
3. Với floor `80M -> 82M -> 81M`, app này có đang memory leak không?

### Bài 1.3 · Nhận diện mẫu hình từ GC log
Ghép 3 kịch bản (A/B/C) với đúng nguyên nhân gốc:
- **A**: `afterMb` giữ quanh `60M` suốt 1 giờ, nhưng Young GC xảy ra mỗi `90 ms`, `allocRate = 3200 MB/s`.
- **B**: `afterMb` tăng từ `120M` lên `1900M` (trên `-Xmx2g`) trong 40 phút; cuối cùng liên tiếp `Pause Full 1990M->1988M(2048M) 1200ms`.
- **C**: `afterMb` nhảy lên `1650M` sau 2 phút load cache xong rồi đi ngang ở `1650M` trên `-Xmx1800m`, khiến Mixed GC chạy dày đặc.

<details>
<summary>Gợi ý</summary>

Phân biệt 3 khái niệm: *High Allocation Churn*, *Tight Headroom (under-sized `-Xmx`)*, và *Unbounded Memory Leak*.

</details>

### Bài 1.4 · Chuyển đổi cờ log sang Legacy Java 7 / JBoss AS 7.1.1
Chuyển câu lệnh log hiện đại sau sang bộ JVM options tương thích **Java 7 trên JBoss AS 7.1.1**:

```text
-Xlog:gc*:file=logs/gc-%t-%p.log:time,uptime,level,tags:filecount=5,filesize=10M
```

---

## Level 2 · Thực hành parser & so sánh log (3 bài)

### Bài 2.1 · Chạy unit test & hiểu heuristic phát hiện leak
Mở [`GcLogAnalyzer.java`](lab/src/main/java/lab/jvm/gclog/GcLogAnalyzer.java) và chạy `mvn -o test`. Giải thích vì sao `GcLogAnalyzer` bỏ qua 25% số event đầu (`warmupEnd`) trước khi so sánh `firstMin` với `lastMin`.

### Bài 2.2 · Giải mã `[Times: user=... sys=... real=...]` trong Java 7/8
Trong sự cố thực tế trên máy ảo Linux/Windows chạy JBoss, bạn thấy dòng log:

```text
[Full GC ... 2100400K->1420100K(3145728K), 4.8201120 secs]
[Times: user=0.32 sys=3.95, real=4.82 secs]
```

Tại sao dù CPU user tốn chỉ `0.32s`, cả hệ thống phải dừng tới `4.82s` (`sys=3.95s`)? Liệt kê ít nhất 2 nguyên nhân tầng hệ điều hành/hạ tầng cần kiểm tra.

<details>
<summary>Gợi ý</summary>

Thời gian `sys` (kernel CPU time) lớn bất thường thường xảy ra khi JVM chạm bộ nhớ ảo bị OS đẩy ra swap, hoặc bị OS/AV/hypervisor chặn disk I/O / memory fault.

</details>

### Bài 2.3 · Khi Allocation Rate giảm lúc app sắp chết
Trong kết quả đo của [`lab/README.md`](lab/README.md), chế độ `normal` đạt `allocRate = 6952.6 MB/s`, còn chế độ `leak` chỉ còn `4103.5 MB/s`. Tại sao app bị leak lại có Allocation Rate *thấp hơn* app bình thường?

---

## Level 3 · Gỡ rối thực chiến trên log thật (1 bài)

### Bài 3.1 · Tạo 2 file GC log thật (`normal` vs `leak`) và xuất báo cáo triage
**Yêu cầu**:
1. Biên dịch lab (`mvn -o -q compile`) và chạy `GcWorkloadRunner` với `-Xmx192m -XX:+UseG1GC` cho cả hai chế độ `normal` và `leak` (ghi log vào thư mục tạm bằng `"-Xlog:gc*:file=..."`, nhớ xoá file tạm sau khi xong).
2. Chạy [`GcLogAnalyzer`](lab/src/main/java/lab/jvm/gclog/GcLogAnalyzer.java) (hoặc [`lab/solutions/LogTriageSolution.java`](lab/solutions/LogTriageSolution.java)) trên cả 2 file log.
3. Đối chiếu 4 chỉ số (`fullGcCount`, `throughputPct`, `allocationRateMbPerSec`, `firstWindowFloorMb -> lastWindowFloorMb`) và giải thích trong 5 dòng vì sao log `leak` khẳng định có rò rỉ bộ nhớ còn `normal` thì không.

**Tiêu chí đạt**: In ra được 2 dòng tổng kết thực tế với `normal` (`full=0`, `leakSuspected=false`) và `leak` (`full > 0`, `leakSuspected=true`), nêu đúng lý do định lượng.
