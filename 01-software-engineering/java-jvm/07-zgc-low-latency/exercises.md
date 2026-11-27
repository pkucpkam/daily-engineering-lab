# Bài tập · JVM-07 · ZGC / Shenandoah & low-latency GC

Làm bài trước khi mở [solutions.md](solutions.md). Phần lớn bài tập sử dụng trực tiếp [lab/](lab/README.md).

---

## Level 1 · Hiểu cơ chế (4 bài)

### Bài 1.1 · Tại sao G1 bị STW khi compact còn ZGC thì không?
Giải thích ngắn gọn (5–7 dòng) tại sao G1 phải dừng toàn bộ mutator thread trong pha Evacuation/Compaction, còn ZGC cho phép mutator chạy song song ngay trong pha `Concurrent Relocate`.

### Bài 1.2 · Dựng luồng đi qua Load Barrier
Giả sử trường `order.customer` từng nằm trong ZPage vừa bị dọn và được copy từ `0x1000` sang `0x9000`, nhưng field `order.customer` vẫn đang mang bit xấu trỏ `0x1000`.
Hãy liệt kê theo thứ tự từng bước điều gì xảy ra ở:
- **Lần đọc 1** của Thread A khi thực thi `Customer c = order.customer;`
- **Lần đọc 2** của Thread B trên cùng field `order.customer` đó.

### Bài 1.3 · Nhận diện flag bật ZGC qua các phiên bản JDK
Chạy lệnh kiểm tra flag mặc định của ZGC trên JDK đang cài ở máy (mở rộng so sánh với Java 21):

```powershell
java -XX:+UseZGC -XX:+PrintFlagsFinal -version | Select-String -Pattern "ZGenerational|SoftMaxHeapSize"
```

Ghi lại flag bật Generational ZGC cho **Java 21 LTS** và **Java 24+**.

<details>
<summary>Gợi ý</summary>

Ở Java 21, JEP 439 vừa ra mắt nên vẫn giữ single-gen làm mặc định nếu chỉ truyền `-XX:+UseZGC`. Từ JDK 23–24, chế độ Generational trở thành mặc định/duy nhất.

</details>

### Bài 1.4 · Phản biện lựa chọn kỹ thuật
Liệt kê **3 tình huống thực tế** mà việc bật `-XX:+UseZGC -XX:+ZGenerational` trên service Java 21 là **quyết định sai lầm**, kèm collector thay thế phù hợp hơn.

---

## Level 2 · Thực hành đọc log & vận hành (3 bài)

### Bài 2.1 · Chạy lab so sánh G1GC vs ZGC và đọc đúng STW pause
Làm theo [lab/README.md](lab/README.md):
1. Chạy `mvn -o test` và xác nhận `ZgcLabTest` PASS.
2. Chạy `LatencyComparisonRunner` với `-Xmx1g -XX:+UseG1GC "-Xlog:gc"` và `-Xmx1g -XX:+UseZGC "-Xlog:gc,gc+phases=info"`.
3. Ghi lại bảng so sánh `p50`, `p99`, `max` jitter giữa G1 và ZGC, đồng thời trích ra **3 dòng `Pause ...`** của 1 chu kỳ ZGC để chứng minh STW pause < 1ms khác với thời gian cycle chạy nền.

**Tiêu chí đạt**: Có bảng số đo thực tế từ máy của bạn và phân biệt đúng STW `Pause ...` (sub-ms) với `Minor/Major Collection` cycle (vài ms).

### Bài 2.2 · Thử nghiệm `-XX:SoftMaxHeapSize` với ZGC
Chạy `LatencyComparisonRunner` với:
- Cấu hình A: `-Xms1g -Xmx1g -XX:+UseZGC "-Xlog:gc"`
- Cấu hình B: `-Xmx1g -XX:SoftMaxHeapSize=512m -XX:+UseZGC "-Xlog:gc"`

Quan sát kích thước heap sau collection (`-> ...M(...)`) ở cấu hình B so với A. Giải thích tác dụng của `SoftMaxHeapSize` khi service ban ngày tải cao nhưng ban đêm ít traffic.

<details>
<summary>Gợi ý</summary>

`-XX:SoftMaxHeapSize` chỉ là giới hạn mềm: ZGC cố gắng giữ mức dùng heap thấp hơn hoặc bằng giá trị này bằng cách tăng tần suất GC nhẹ nếu cần, nhưng vẫn được phép dùng tới `-Xmx` khi có burst allocation đột ngột để tránh `Allocation Stall`.

</details>

### Bài 2.3 · Chuyển ngữ cảnh về Java 7 / JBoss AS 7.1.1
Một ứng dụng web legacy chạy **JBoss AS 7.1.1 trên Java 7**, heap `-Xmx4g`, thỉnh thoảng bị khách hàng phàn nàn request treo 6–9 giây đúng lúc Old Gen chạm ngưỡng 99%.
1. Có bật được ZGC hoặc G1 production hoàn chỉnh trên Java 7 không? Vì sao?
2. Viết bộ JVM options hợp lý hơn cho `standalone.conf` trên Java 7 sử dụng CMS để giảm nguy cơ *Concurrent Mode Failure*.

---

## Level 3 · Tái hiện sự cố & Gỡ rối (1 bài)

### Bài 3.1 · Tái hiện và xử lý `Allocation Stall` trên ZGC
Trong điều kiện heap bị ép sát gần bằng live set (ví dụ ~220 MB live graph nhưng `-Xmx360m`), ZGC không đủ không gian cho floating garbage trong lúc concurrent cycle đang chạy.

**Yêu cầu**:
1. Viết một chương trình nhỏ (hoặc chạy tham khảo `lab/solutions/AllocationStallDemo.java`) giữ ~1.600.000 `byte[96]` (~220 MB) và spawn thread cấp phát `byte[64KB]` liên tục.
2. Chạy ở `-Xmx360m -XX:+UseZGC "-Xlog:gc"` và chụp ít nhất 1 dòng `[gc] Allocation Stall (...) ...ms`.
3. Chạy lại cùng chương trình ở `-Xmx1g -XX:+UseZGC "-Xlog:gc"` và xác nhận `Allocation Stall` biến mất, probe latency về `0 ms` (hoặc sub-ms).

**Tiêu chí đạt**: Tái hiện được dòng `Allocation Stall` thật trong `-Xlog:gc` khi thiếu headroom và chứng minh được tác động của việc cấp đủ RAM headroom (`-Xmx1g`).
