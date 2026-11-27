# Bài tập · JVM-09 · Heap dump, Eclipse MAT, JFR

Làm bài trước khi mở [solutions.md](solutions.md). Thực hành với [lab/](lab/README.md).

---

## Level 1 · Hiểu khái niệm cốt lõi (4 bài)

### Bài 1.1 · Tính tay Shallow Size & Retained Size
Cho đồ thị object sống nối từ GC Root như sau (giả sử đơn vị đã cho sẵn):
- `Root1` trỏ tới `OrderService` (`Shallow = 48 B`), `OrderService` giữ duy nhất `cache` (`HashMap`, `Shallow = 64 B`).
- `cache` giữ `Entry[4]` (`Shallow = 32 B`), chứa 2 entry `E1` (`32 B`) và `E2` (`32 B`).
- `E1` trỏ tới `PayloadA` (`100 KB`); `E2` trỏ tới `SharedTaxTable` (`500 KB`).
- Ngoài ra `Root2` (`TaxEngine`) **cũng** giữ một tham chiếu trực tiếp tới `SharedTaxTable`.

Tính **Shallow Size** và **Retained Size** của `cache` (`HashMap`).

### Bài 1.2 · `jcmd` vs `jmap` khi thao tác trực tiếp
So sánh 2 cặp lệnh sau và nêu điểm khác biệt quan trọng về tính an toàn và kết quả:
1. `jcmd <pid> GC.heap_dump dump.hprof` vs `jcmd <pid> GC.heap_dump -all dump.hprof`
2. `jcmd <pid> GC.class_histogram` vs `jcmd <pid> GC.heap_dump`

### Bài 1.3 · Nhận diện đường dẫn GC Root hợp lệ trên Eclipse MAT
Khi chạy **Path to GC Roots** trên một object `UserSession`, Eclipse MAT hiện các lựa chọn lọc reference. Tại sao kỹ sư luôn phải chọn **"exclude all phantom/weak/soft etc. references"**?

<details>
<summary>Gợi ý</summary>

Nếu không loại trừ Weak/Soft reference (ví dụ entry của `WeakHashMap` hoặc `Finalizer`/`SoftReference`), bạn sẽ bị dẫn nhầm sang một nhánh mà GC hoàn toàn có quyền thu hồi chứ không phải nhánh đang ép object sống mãi.

</details>

### Bài 1.4 · Chọn công cụ đúng cho triệu chứng
Với mỗi triệu chứng production dưới đây, chọn công cụ phù hợp nhất (**Heap Dump + MAT**, **JFR (`settings=profile`)**, hay **GC Log**):
1. App tụt throughput, CPU chỉ 25%, nghi ngờ các thread đang kẹt chờ chung 1 `synchronized` lock.
2. Old Gen sau Full GC tăng đều từ 300 MB lên 3.8 GB trong 6 giờ rồi crash `OOM: Java heap space`.
3. Heap không tăng, nhưng Young GC chạy mỗi 50 ms vì một hàm parse XML tạo rác quá nhanh.

---

## Level 2 · Thực hành CLI `jcmd` & `JfrInspector` (3 bài)

### Bài 2.1 · Chạy test verify & đọc magic header `.hprof`
Chạy `mvn -o test` trong `09-heap-dump-mat-jfr/lab/`, đọc [`HeapMatJfrTest.java`](lab/src/test/java/lab/jvm/matjfr/HeapMatJfrTest.java) và giải thích:
1. Tại sao `HotSpotDiagnosticMXBean.dumpHeap(..., true)` tạo ra file có định dạng giống hệt `jcmd <pid> GC.heap_dump`?
2. Tham số `true` (`live`) trong `dumpHeap` ảnh hưởng gì tới STW pause trước lúc ghi file?

### Bài 2.2 · Chụp live `.hprof` và `.jfr` từ một process đang chạy bằng `jcmd`
Thực hiện theo [lab/README.md](lab/README.md):
1. Khởi chạy `LeakyCacheApp` với thời lượng đủ dài (`5000 ms`).
2. Từ cửa sổ terminal thứ 2 (hoặc script), dùng `jcmd <pid> GC.heap_dump` chụp file `.hprof` và `jcmd <pid> JFR.start` → `JFR.dump` → `JFR.stop` chụp file `.jfr` (nhớ xoá file sau khi xong).
3. Chạy `JfrInspector` trên file `.jfr` thu được và ghi lại **Top 1 Allocation Hot Method** cùng **Top 1 Contended Lock Method**.

**Tiêu chí đạt**: Có file `.hprof` > 30 MB, file `.jfr` hợp lệ và `JfrInspector` chỉ đúng tên method `allocateTempAuditPayload` và `contendedAuditLock`.

### Bài 2.3 · Quy trình an toàn trên Production Node
Sếp yêu cầu bạn lấy ngay 1 heap dump từ server JBoss/microservice Java 21 (heap đang dùng 12 GB trên container RAM 14 GB) đang phục vụ 500 req/s phía sau Load Balancer. Hãy viết checklist 5 bước thao tác trước, trong và sau khi chạy `jcmd`.

---

## Level 3 · Thiết kế lại & kiểm chứng bản sửa (1 bài)

### Bài 3.1 · Chữa cả 3 bệnh trong `LeakyCacheApp`
Dựa trên kết quả MAT (leak qua `SESSION_CACHE`) và JFR (`allocateTempAuditPayload` + `contendedAuditLock`), hãy viết hoặc đối chiếu [`lab/solutions/FixedCacheAuditApp.java`](lab/solutions/FixedCacheAuditApp.java) để xử lý đồng thời cả 3 vấn đề:
1. Không để `SESSION_CACHE` tăng vô hạn theo số session (giới hạn LRU `max = 64` entries).
2. Giảm rác `byte[16KB]` trong hot path bằng tái sử dụng `ThreadLocal` scratch buffer (nhớ `.remove()` khi xong!).
3. Đưa thao tác chậm (`Thread.sleep(2)` mô phỏng I/O) ra ngoài vùng `synchronized`.

**Tiêu chí đạt**: Chạy `java solutions\FixedCacheAuditApp.java` in ra `Fixed cache size=64 (max=64)`, và khi tự chụp JFR kiểm tra lại, sự kiện block `jdk.JavaMonitorEnter` biến mất hoàn toàn.
