# JVM-04 · Đáp án — JVM Memory: Heap, Stack, Metaspace, Direct memory

> ⚠️ **Chỉ mở sau khi đã tự làm.** Đọc đáp án mà chưa tự làm = mất 80% giá trị bài tập.
> Mỗi đáp án được bọc trong `<details>` để tránh lộ khi lướt.

---

### E1
<details><summary>Xem đáp án</summary>

**Đáp án:**
`-Xmx2g` chỉ giới hạn Java Heap. Khoảng `~1.8–2.0 GB` còn lại nằm ở các vùng **native (off-heap)** trong tổng RSS:
1. **Thread Stacks:** `350+` threads × `-Xss1m` tối đa ~`350+ MB`.
2. **Metaspace:** metadata của hàng chục nghìn class Spring/Hibernate/Proxy (~`150–350 MB`).
3. **Code Cache:** machine code của JIT C1/C2 (~`100–240 MB`).
4. **GC Internal Structures:** G1 Remembered Sets, Card Tables (~`100–300 MB` tùy tham chiếu chéo).
5. **Direct / Native Memory:** Netty off-heap buffers, `DirectByteBuffer`, file mmap, malloc/zlib JNI (~`500 MB – 1 GB+`).
</details>

---

### E2
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. **`Integer` (Compressed Oops ON):** **16 bytes** (`8B` Mark Word + `4B` Compressed Klass Pointer + `4B` `int value` = `16B`, `0B` padding).
2. **`Pair { boolean active; long id; byte kind; }`:**
   - Header: `12 bytes` (`8B` Mark + `4B` Klass).
   - HotSpot tự reorder field để lấp chỗ trống: đưa `boolean active` (1B) + `byte kind` (1B) lên offset `12..14`, padding `2B` ở `14..16`, rồi đặt `long id` (8B) ở `16..24` → **Tổng = 24 bytes** (thay vì `32 bytes` nếu không reorder!).
</details>

---

### E3
<details><summary>Xem đáp án</summary>

**Đáp án:**
Vì ngưỡng **~32 GB** là giới hạn đánh địa chỉ của **32-bit Compressed Oops** (`2^32 × 8 bytes = 32 GB`). Khi đặt `-Xmx32g` trở lên, JVM tự động **tắt Compressed Oops**:
- Object Header tăng từ `12B` lên `16B` (thêm 4B/object).
- Mỗi biến reference (`Object[]`, field tham chiếu, node trong `HashMap`) tăng gấp đôi từ `4B` lên `8B`.
Kết quả là dù tăng `-Xmx` thêm 2 GB, tỷ lệ overhead tăng khiến số object chứa được thực tế có thể ít hơn lúc `-Xmx30g` và L1/L2 cache miss tăng lên.
</details>

---

### E4
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. Vì `Point p` chỉ được tạo và dùng nội bộ bên trong `distanceSquared`, không trả ra ngoài hay gán vào cấu trúc dùng chung (**NoEscape**). JIT C2 thực hiện **Scalar Replacement**: không cấp phát object `Point` lên Heap mà tách `x`, `y` thành biến cục bộ trên thanh ghi/stack → 0 allocation trên Heap, gần như 0 GC!
2. Nếu thêm `CACHE.add(p)`, đối tượng `p` đã **thoát ra ngoài (`GlobalEscape`)** → JIT không thể Scalar Replacement được nữa, 10 triệu object phải cấp phát thật trên Heap và kích hoạt Young GC liên tục.
</details>

---

### E5
<details><summary>Xem đáp án</summary>

**Đáp án:**
`ByteBuffer.allocateDirect` cấp phát vùng nhớ **ngoài Heap** (native RAM qua `malloc`/mmap), trên Heap chỉ giữ một wrapper object `DirectByteBuffer` nhỏ vài chục byte kèm một `Cleaner` (`PhantomReference`). Khi Heap vẫn còn rất rảnh (5%), GC **chưa có lý do để chạy**, nên các wrapper object trên Heap chưa bị thu gom và `Cleaner` chưa được kích hoạt để `free()` vùng nhớ native — dẫn đến RSS tăng vọt tới khi OS OOM killer bắn chết process (hoặc chạm `-XX:MaxDirectMemorySize` ném `OutOfMemoryError: Direct buffer memory`).
</details>

---

### E6
<details><summary>Xem đáp án</summary>

**Số đo tham khảo** *(Windows 11, JDK 26.0.1 `--release 21`, JOL 0.17)*:
- `mvn -q verify`: **2/2 tests pass**.
- JOL với Compressed Oops ON (mặc định):
  - `Integer`: **16 bytes** (`8B` mark + `4B` class + `4B` int).
  - `CompactSample`: **32 bytes** (3B internal padding gap).
  - `int[1000]`: **4.016 bytes** vs `Integer[1000]`: **20.016 bytes** (**5,0×**).
- NMT (`-XX:NativeMemoryTracking=summary -Xmx256m` kèm `64 MB DirectByteBuffer`):
  - `Java Heap committed`: **10.240 KB** (`~10 MB`) trong khi `Total NMT committed`: **168.222 KB** (`~164 MB`, `reserved=1.803.218 KB`).
- Khi thêm `-XX:-UseCompressedOops`: `Integer` nhảy từ `16 bytes` lên **24 bytes** (`8B` mark + `8B` class + `4B` int + `4B` padding).
</details>

---

### E7
<details><summary>Xem đáp án</summary>

**Hướng giải & code mẫu:** xem [lab/solutions/E7EscapeAnalysisDemo.java](./lab/solutions/E7EscapeAnalysisDemo.java).
- Khi chạy với mặc định (Escape Analysis ON) và `-Xmx128m -Xlog:gc*`: sau vài nghìn vòng đầu kích hoạt JIT C2, hầu như **không phát sinh Young GC** trong phần lớn 100 triệu vòng lặp và chạy xong rất nhanh.
- Khi tắt `-XX:-DoEscapeAnalysis`: toàn bộ 100 triệu `Point` đều cấp phát thật trên Eden (`100M × 16B ≈ 1.6 GB` rác!) → kích hoạt hàng chục lần Young GC liên tiếp và thời gian chạy chậm hơn rõ rệt.
</details>

---

### E8
<details><summary>Xem đáp án</summary>

**Đáp án:**
- Với `class Parent { byte a; } class Child extends Parent { long b; byte c; }` (Compressed Oops ON):
  - Phần của `Parent`: 12B header + 1B `byte a` = 13B → nhưng theo quy tắc HotSpot, toàn bộ field của class con phải bắt đầu sau ranh giới căn chỉnh của class cha (**4-byte boundary** ở offset 16!), nên `Parent` bị mất **3 bytes padding gap** (`13..16`) không thể nhét `byte c` của `Child` lên lấp được!
  - Phần của `Child`: `long b` (offset `16..24`) + `byte c` (offset `24..25`) + 7 bytes alignment padding (`25..32`) = **32 bytes** (mất tổng cộng 10 bytes padding).
- Nếu gộp phẳng 3 field vào 1 class (không kế thừa), `byte a` và `byte c` nằm cạnh nhau ở `12..14`, padding 2B ở `14..16`, `long b` ở `16..24` → chỉ còn **24 bytes**!
</details>

---

### E9
<details><summary>Xem đáp án</summary>

<details open><summary>Phân tích sự cố Pod OOMKilled dù Heap mới dùng 58%</summary>

1. **Tính toán thực tế từ snapshot NMT:**
   - Prometheus báo Heap *used* = `890 MB`, nhưng do cấu hình **`-Xms1536m`**, JVM đã **commit cứng `1.536 MB` RAM vật lý** cho Heap với OS ngay từ lúc khởi động!
   - Tổng RAM committed từ NMT: `1536 (Heap) + 210 (Metaspace) + 180 (Thread) + 115 (Code) + 390 (Internal/Direct) = 2.431 MB`!
   - Con số `2.431 MB` vượt quá trần `limits.memory = 2 GiB (2.048 MB)` của container (chưa tính OS page/cgroup overhead) → Linux cgroup OOM Killer giết container ngay lập tức!

2. **3 sai lầm trong bộ JVM flags cũ:**
   - `-Xms1536m -Xmx1536m` đặt Heap chiếm tới **75%** giới hạn `2 GiB` (`1.536 / 2.048`), chỉ chừa lại `512 MB` cho toàn bộ Metaspace + 420 Threads + Code Cache + Direct Memory + GC!
   - `-XX:MaxDirectMemorySize=1024m` đặt trần Direct Memory tới `1 GiB`, cộng với `Xmx=1.5 GiB` vượt trần pod `2 GiB`.
   - Không có `-XX:MaxMetaspaceSize`, không kiểm soát số thread (`#420 threads` tốn 180 MB stack committed) và không pool buffer/close `Deflater`/`Inflater`.

3. **Bộ JVM flags mới an toàn cho container `2 GiB` + 2 bước kiểm tra code:**
   - Flags đề xuất:
     `-XX:MaxRAMPercentage=60.0 -XX:MaxMetaspaceSize=256m -XX:MaxDirectMemorySize=256m -Xss512k` (Heap tối đa ~`1.2 GiB`, còn dư ~`800 MB` cho off-heap; không set `-Xms` quá lớn hoặc dùng `-XX:InitialRAMPercentage=30.0`).
   - Kiểm tra code: (1) Đảm bảo mọi `GZIPOutputStream` / `ZipInputStream` / Netty `ByteBuf` được `close()` / `release()` chuẩn (vì `Deflater` giữ native memory nếu không đóng); (2) Giới hạn thread pool Tomcat/gRPC hợp lý thay vì để 420 threads.

</details>
</details>

---

## Self-check (từ README §9) — đáp án gợi ý
<details><summary>Xem</summary>

1. **5 vùng nhớ ngoài Heap:** Metaspace, Thread Stacks (`N × -Xss`), Code Cache (JIT), GC internal metadata (Remembered Sets/Card Tables), và Direct/Native Buffers (`ByteBuffer.allocateDirect`, JNI/malloc).
2. **Kích thước 1 `Integer`:** Với Compressed Oops ON (`-Xmx < 32 GB`): **16 bytes** (`8B` Mark Word + `4B` Compressed Klass Pointer + `4B` `int value`). Với Compressed Oops OFF (`>= 32 GB`): **24 bytes** (`8B` Mark + `8B` Klass + `4B` `int` + `4B` padding).
3. **Ngưỡng ~32 GB:** Vượt `~32 GB`, Compressed Oops tự tắt khiến mỗi reference tăng từ 4B → 8B và mỗi Object Header tăng từ 12B → 16B, làm phình bộ nhớ của mọi object/collection và giảm hiệu quả CPU cache.
4. **Escape Analysis & Scalar Replacement:** JIT C2 chứng minh object không thoát khỏi method/thread, từ đó hủy cấp phát trên Heap và tách thẳng các field thành biến cục bộ trên stack/thanh ghi CPU, triệt tiêu áp lực GC.
5. **Direct Buffer gây OOM khi Heap trống:** Vùng native buffer chỉ được giải phóng khi GC thu gom wrapper `DirectByteBuffer` trên Heap. Nếu Heap đang rất rảnh, GC ít chạy → native memory tích tụ ngoài Heap cho tới khi OS OOMKilled.
</details>
