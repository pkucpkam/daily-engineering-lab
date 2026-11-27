# Đáp án · JVM-09 · Heap dump, Eclipse MAT, JFR

## Đáp án Bài tập

### Level 1

#### Bài 1.1 · Tính Shallow Size & Retained Size của `cache`
- **Shallow Size của `cache`**: **`64 B`** (bản thân object `HashMap`).
- **Retained Size của `cache`**:
  - Gồm: `cache` (`64 B`) + `Entry[4]` (`32 B`) + `E1` (`32 B`) + `E2` (`32 B`) + `PayloadA` (`100 KB` = `102.400 B`).
  - **Không** tính `SharedTaxTable` (`500 KB`) vì `Root2` (`TaxEngine`) vẫn giữ một đường dẫn độc lập tới `SharedTaxTable`; dù `cache` biến mất thì `SharedTaxTable` vẫn sống!
  - Tổng Retained Size của `cache` ≈ **`100 KB + 160 B`** (`102.560 B`).

#### Bài 1.2 · `jcmd` flags
1. `GC.heap_dump dump.hprof` chạy một lần quét live objects trước khi ghi (file nhỏ hơn, ít nhiễu rác chết); thêm `-all` bỏ qua bước lọc đó và xuất **mọi object** hiện hữu kể cả object đang chờ thu gom.
2. `GC.class_histogram` chỉ đếm tổng số instance & bytes theo từng class rồi in chữ ra console (nhẹ, 1–2 giây, an toàn hơn để check sơ bộ); `GC.heap_dump` xuất toàn bộ đồ thị object & giá trị field ra đĩa (nặng gấp hàng chục lần).

#### Bài 1.3 · Tại sao phải loại Weak/Soft/Phantom references
Vì chỉ **Strong References** mới ngăn được Garbage Collector thu hồi object khi thiếu RAM. Nếu lần theo Weak/Soft reference (ví dụ trong `WeakHashMap` hay cache mềm), Eclipse MAT sẽ chỉ vào những đường giữ "hờ" không phải nguyên nhân gây OOM thật.

#### Bài 1.4 · Chọn công cụ
1. Thread kẹt chờ `synchronized` → **JFR (`settings=profile`)** xem tab Lock Instances (`jdk.JavaMonitorEnter`) — overhead cực thấp và chỉ rõ stack đang giữ/chờ lock.
2. Old Gen tăng đều 6 tiếng → **Heap Dump + Eclipse MAT** (sau khi đã nhìn **GC Log** xác nhận leak) để xem Dominator Tree & Retained Size.
3. Young GC chạy dày vì parser tạo rác nhanh → **JFR (`settings=profile`)** xem Allocation Profiling (`jdk.ObjectAllocationSample`).

---

### Level 2

#### Bài 2.1 · `HotSpotDiagnosticMXBean.dumpHeap(..., true)`
1. `jcmd <pid> GC.heap_dump` bên dưới gọi cùng nội hàm JVM Diagnostic Command tương đương với MBean `HotSpotDiagnosticMXBean.dumpHeap(outputFile, live)`. Cả hai đều xuất ra chuẩn HPROF binary bắt đầu bằng header `"JAVA PROFILE"`.
2. Tham số `live = true` buộc JVM kích hoạt một pha dọn/đánh dấu STW để lọc bỏ object đã chết trước khi ghi ra đĩa, giúp file nhỏ gọn hơn (tránh rác làm loãng MAT) nhưng làm STW pause dài hơn một chút lúc bắt đầu dump.

#### Bài 2.2 · Kết quả `jcmd` + `JfrInspector` tham chiếu
Kết quả chạy thật trên OpenJDK 26.0.1 với `LeakyCacheApp`:
- `jcmd <pid> GC.heap_dump`: `Heap dump file created [43916429 bytes in 0.045 secs]`
- `JfrInspector` đọc file `.jfr` (`258.656 bytes`):
```text
JFR Summary -> totalEvents=3847 | allocEvents=262 | monitorEnterEvents=125
Top Allocation Hot Methods:
  lab.jvm.matjfr.LeakyCacheApp#allocateTempAuditPayload -> 59860272 samples/bytes
  ...
Top Contended Lock Methods (total duration ns):
  lab.jvm.matjfr.LeakyCacheApp#contendedAuditLock -> 2332.25 ms
```
`JfrInspector` tóm gọn đúng cả allocation hotspot (`allocateTempAuditPayload`) lẫn monitor lock contention (`contendedAuditLock`).

#### Bài 2.3 · Checklist 5 bước lấy heap dump trên production 12 GB heap
1. **Rút node khỏi Load Balancer (Drain)** và tạm tắt/trì hoãn healthcheck auto-restart của K8s/LB (vì 12 GB live heap sẽ STW 10–40 giây).
2. **Kiểm tra mount disk thực** (`df -h` trên Linux hoặc ổ dữ liệu thật): tránh tuyệt đối `/tmp` nếu là `tmpfs` (RAMFS), đảm bảo trống ít nhất ~18 GB (`> 1.5x` heap đang dùng).
3. **Thử `jcmd <pid> GC.class_histogram` trước** xem bảng top class có lộ ngay thủ phạm không; chỉ dump toàn bộ khi thật cần Dominator Tree.
4. **Chạy `jcmd <pid> GC.heap_dump <đường_dẫn_ổ_đĩa_thật>.hprof`**, theo dõi file hoàn tất rồi nén (`gzip`) / kéo ra máy phân tích ngoài.
5. **Bật lại healthcheck & đưa node vào Load Balancer** (hoặc rolling restart sạch node nếu node đã kiệt heap).

---

### Level 3

#### Bài 3.1 · Chữa cả 3 vấn đề trong `LeakyCacheApp`
Xem đầy đủ tại [`lab/solutions/FixedCacheAuditApp.java`](lab/solutions/FixedCacheAuditApp.java):
1. Thay `static ConcurrentHashMap` không giới hạn bằng LRU `LinkedHashMap(..., true)` override `removeEldestEntry` bọc `Collections.synchronizedMap` (hoặc Caffeine/Guava Cache có `maximumSize` & `expireAfterAccess` trên dự án thực tế).
2. Tái sử dụng `ThreadLocal<byte[]> SCRATCH_BUFFER` kèm **`finally { SCRATCH_BUFFER.remove(); }`** (cực kỳ quan trọng để không gây ClassLoader leak như bài [JVM-10](../10-metaspace-classloader-leak/README.md)!).
3. Đẩy `Thread.sleep(2)` (đại diện network/DB call) ra khỏi khối `synchronized`.

Chạy kiểm chứng:
```text
Fixed cache size=64 (max=64) | sink=42
```

---

## Đáp án Câu hỏi tự kiểm tra

<details>
<summary>1. Shallow Size 64 B vs Retained Size của ConcurrentHashMap chứa 1M byte[1KB]?</summary>

Retained Size sẽ vào khoảng **> 1 GB** (`1.000.000 * ~1 KB` + overhead của `Node[]`, `Node` entries và `64 B` của Map). Tuy nhiên bất kỳ `byte[1KB]` nào *cũng đang được một object ngoài Map giữ một Strong Reference khác* (shared object từ nhánh GC Root khác) sẽ **không** được tính vào Retained Size của Map đó.

</details>

<details>
<summary>2. Tại sao không chạy `jcmd <pid> GC.heap_dump` thẳng khi chưa rút node khỏi LB?</summary>

Vì dump heap gây Stop-The-World kéo dài hàng giây đến hàng chục giây tuỳ live set & tốc độ ghi đĩa. Load balancer hoặc Kubernetes liveness probe sẽ tưởng node chết, cắt kết nối giữa chừng hoặc bắn SIGTERM restart làm file `.hprof` bị cụt hỏng và user nhận hàng loạt HTTP 502/504.

</details>

<details>
<summary>3. Khi chưa thể dump 32 GB `.hprof`, lệnh `jcmd` nào kiểm tra nhanh nhất?</summary>

`jcmd <pid> GC.class_histogram` (tương đương `jmap -histo:live <pid>`). Lệnh chỉ in bảng text đếm số instance và tổng bytes từng class trong ~1–2 giây, không tốn hàng chục GB ổ đĩa.

</details>

<details>
<summary>4. Hai event chuẩn trong JFR để tìm allocation nóng và tranh chấp lock?</summary>

- Allocation profiling: **`jdk.ObjectAllocationSample`** (JDK 16+) hoặc `jdk.ObjectAllocationInNewTLAB` / `jdk.ObjectAllocationOutsideTLAB`.
- Lock contention profiling: **`jdk.JavaMonitorEnter`** (và `jdk.JavaMonitorWait` nếu chờ `Object.wait()`).

</details>

<details>
<summary>5. Trên JBoss Java 7 không có JFR miễn phí, tìm lock contention bằng cách nào?</summary>

Dùng **`jstack <pid>`** (hoặc `jcmd <pid> Thread.print`) lấy 3–5 snapshot cách nhau 3–5 giây lúc hệ thống đang chậm, sau đó tìm các thread trạng thái `BLOCKED (on object monitor)` và đối chiếu thread đang giữ lock (`locked <0x...>`) nằm ở đầu chuỗi block.

</details>
