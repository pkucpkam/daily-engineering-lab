# JVM-06 · Đáp án — GC cơ bản & G1

> ⚠️ **Chỉ mở sau sau khi đã tự làm.** Đọc đáp án mà chưa tự làm = mất 80% giá trị bài tập.
> Mỗi đáp án được bọc trong `<details>` để tránh lộ khi lướt.

---

### E1
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. **Không.** Reference Counting chỉ đếm số lượng con trỏ trỏ vào object: `NodeA` có count = 1 (từ `NodeB`), `NodeB` có count = 1 (từ `NodeA`), nên không bao giờ về 0 và bị **memory leak**.
2. **Tracing GC của JVM thu hồi được cả hai** vì xuất phát từ **GC Roots** không có đường đi nào chạm tới `NodeA` hay `NodeB` → cả hai đều bị coi là *unreachable*.
3. **3 loại GC Roots phổ biến:** (a) Biến cục bộ/tham số nằm trên Thread Stacks đang chạy; (b) Các biến `static` của các class đã load; (c) JNI references / Thread objects đang sống / synchronization monitors.
</details>

---

### E2
<details><summary>Xem đáp án</summary>

**Đáp án** *(Region size = 4 MB → ngưỡng Humongous = > 2 MB)*:
1. `1.9 MB <= 2 MB` → **Không phải** Humongous Object; cấp phát vào Eden bình thường.
2. `2.1 MB > 2 MB` → **Là Humongous Object!** Nó chiếm trọn **1 Region (`4 MB`)** trong Old Gen (bị bỏ trống/lãng phí nội bộ gần **`1.9 MB`** ở cuối Region đó không dùng chung cho object khác được).
</details>

---

### E3
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. Giai đoạn **Concurrent Marking Cycle** chạy nền song song cùng ứng dụng để đánh dấu object sống trong Old Gen. Ngưỡng kích hoạt mặc định là **`-XX:InitiatingHeapOccupancyPercent=45`** (`45%` tổng Heap, có Adaptive IHOP tự điều chỉnh theo tốc độ cấp phát).
2. **Young GC** chỉ dọn toàn bộ Eden + Survivor. **Mixed GC** vẫn dọn toàn bộ Young Regions **cộng thêm một tập Old Regions chứa nhiều rác nhất** (đã xác định từ Concurrent Marking) trong ngân sách thời gian của `MaxGCPauseMillis`.
</details>

---

### E4
<details><summary>Xem đáp án</summary>

**Đáp án:**
Đặt cứng `-Xmn2g` **khóa chết kích thước Young Gen ở 2 GB**, vô hiệu hóa hoàn toàn cơ chế adaptive của G1 (bình thường G1 tự co giảm Young Gen xuống khi thấy Young GC vượt quá `MaxGCPauseMillis=100ms`, và mở rộng lên tới 60% khi tải nhẹ). Khi có biến động tải làm nhiều object sống sót trong 2 GB Young Gen đó, thời gian copy vượt `100 ms` mà G1 không thể co nhỏ Young Gen xuống được nữa.
</details>

---

### E5
<details><summary>Xem đáp án</summary>

**Đáp án (2 rủi ro khi tăng `-Xmx4g` lên `-Xmx24g`):**
1. **Pause dài hơn khi gặp Mixed/Full GC hoặc mở rộng Young:** Nếu xảy ra Full GC (do Humongous fragmentation hoặc cấp phát quá nhanh), quét và compact `24 GB` tốn thời gian lâu hơn nhiều lần so với `4 GB`; hoặc nếu có nhiều tham chiếu chéo, Remembered Sets/Card Tables tiêu tốn thêm lượng RAM native ngoài Heap đáng kể.
2. **Thay đổi hành vi adaptive & rủi ro chạm ngưỡng `32 GB`:** Nếu tiếp tục tăng quá `~32 GB`, Compressed Oops tự tắt làm kích thước pointer tăng gấp đôi (4B → 8B), giảm hiệu quả CPU cache và tăng thêm áp lực GC.
</details>

---

### E6
<details><summary>Xem đáp án</summary>

**Số đo tham khảo** *(Windows 11, JDK 26.0.1 `--release 21`, `-Xms64m -Xmx64m -XX:+UseG1GC -XX:G1HeapRegionSize=1m`)*:

| Chế độ (tổng 128 MB cấp phát) | Số lần GC (`GC events`) | Tổng STW GC (`ms`) | Dòng sự kiện chính trong GC log |
| :--- | ---: | ---: | :--- |
| `normal` (`4 KB` chunks) | **4** | **2 ms** | `Pause Young (Normal) (G1 Evacuation Pause)` |
| `humongous` (`600 KB` chunks) | **96** (**24×**) | **20 ms** (**10×**) | `Pause Young (Concurrent Start) (G1 Humongous Allocation)` + `Concurrent Undo Cycle` |

- Khi nâng `-XX:G1HeapRegionSize=2m`, ngưỡng Humongous tăng từ `512 KB` lên **`1 MB`**. Do `600 KB <= 1 MB`, mảng `600 KB` không còn là Humongous nữa mà cấp phát vào Eden như object thường → bão `G1 Humongous Allocation` biến mất hoàn toàn!
</details>

---

### E7
<details><summary>Xem đáp án</summary>

**Hướng giải:**
- Flag `-Xlog:gc,gc+humongous=debug` in chi tiết kích thước từng mảng `600 KB` (614.416 bytes, chiếm 1 region `1 MB`) và trạng thái reclaim.
- Vì giữ lại `24 chunks` (`retainWindow = 24`), mỗi chunk `600 KB` chiếm trọn **1 Region `1 MB`** (`24 × 1 MB = 24 MB` + ~1 MB các object khác của JVM = **`~25 MB`**), nên dù dữ liệu thật chỉ là `24 × 600 KB ≈ 14 MB`, Heap báo `27M->25M(64M)` — minh họa trực tiếp phần internal fragmentation của Humongous Regions!
</details>

---

### E8
<details><summary>Xem đáp án</summary>

**Hướng giải:**
Dùng `Files.newInputStream(path)` + buffer nhỏ cố định `new byte[16 * 1024]` (`16 KB < 512 KB`, nằm gọn trong Eden TLAB) cập nhật dần `MessageDigest.update(buf, 0, n)`, rồi format bằng `HexFormat.of().formatHex(md.digest())`.

**Code tham khảo đầy đủ:** xem [lab/solutions/E8StreamingHashRefactored.java](./lab/solutions/E8StreamingHashRefactored.java).
</details>

---

### E9
<details><summary>Xem đáp án</summary>

<details open><summary>Phân tích chuỗi GC log & kế hoạch khắc phục</summary>

1. **Giải thích 4 dòng GC log:**
   - `GC(412) Pause Young (Concurrent Start) (G1 Humongous Allocation) 4120M->4080M(8192M)`: Có allocation `> 2 MB` (chính là `ByteArrayOutputStream.toByteArray()` `2.4 MB` và chuỗi Base64 `3.2 MB`!) rơi vào Humongous Region thuộc Old Gen, kích hoạt Concurrent Mark sớm.
   - `GC(413) To-space exhausted` & `Pause Young 7950M->4190M(8192M) 890.2ms`: Rác tăng vọt hoặc các Humongous Regions phân mảnh làm G1 **hết vùng trống liên tiếp để copy object sống** (*Evacuation Failure* — nên Young GC nhảy lên gần 1 giây!).
   - `GC(414) Pause Full (G1 Compaction Pause) 8120M->3980M(8192M) 4420.5ms`: Sau khi evacuation thất bại và hết không gian trống liên tiếp, G1 buộc phải chạy **Full GC đơn/đa luồng dọn & ép toàn bộ Heap** trong `4.42 giây`, làm app đứng hình!

2. **2 nguyên nhân gốc rễ:**
   - **Code:** Gom toàn bộ payload `2.4 MB` rồi encode sang mảng `3.2 MB` trong RAM → cả hai đều vượt ngưỡng `2 MB` (50% của `G1HeapRegionSize = 4 MB`) → cấp phát thẳng vào Humongous Old Regions và gây phân mảnh.
   - **JVM flags:** Giữ cờ `-Xmn4g` từ thời legacy khóa cứng 4 GB cho Young Gen trên Heap 8 GB, bóp nghẹt không gian Old/Humongous còn lại xuống 4 GB và tước quyền co giãn của G1.

3. **Giải pháp ngắn hạn (tuning không sửa code) & dài hạn (refactor code):**
   - **Ngắn hạn (triển khai ngay):** (1) **Xóa `-Xmn4g`** để G1 tự co giãn Young Gen; (2) Thêm **`-XX:G1HeapRegionSize=8m`** (nâng ngưỡng Humongous lên `> 4 MB`) để các mảng `2.4 MB` và `3.2 MB` trở về cấp phát bình thường ở Eden và được dọn sạch ngay ở Young GC.
   - **Dài hạn (refactor):** Chuyển quy trình xuất XML & encode Base64 sang **streaming** (`Base64.getEncoder().wrap(outputStream)`) với buffer `16–64 KB` → triệt tiêu hoàn toàn Humongous allocation bất kể file lớn 5 MB hay 50 MB.

</details>
</details>

---

## Self-check (từ README §9) — đáp án gợi ý
<details><summary>Xem</summary>

1. **Reachability vs Reference Counting & Generational Hypothesis:** Tracing GC duyệt từ GC Roots (Thread stacks, static fields, JNI...) nên thu hồi được cả vòng tham chiếu `A ↔ B` (điều Reference Counting không làm được). Weak Generational Hypothesis nói phần lớn object chết ngay sau khi tạo → tách Young Gen (dọn nhanh bằng Copy) và Old Gen.
2. **Mark-Sweep vs Mark-Compact:** Mark-Sweep thu hồi chỗ chết nhưng để lại phân mảnh; Mark-Compact / Evacuate chép object sống dồn sát nhau, xóa sạch vùng cũ → hết phân mảnh và cấp phát mới bằng bump-the-pointer TLAB siêu nhanh.
3. **Chu trình G1:** Chia Heap thành ~2.048 Region đồng đều. Young GC copy từ Eden/Survivor (dùng RSet tránh quét cả Old). Khi vượt `IHOP` (mặc định 45%), Concurrent Mark chạy nền đánh dấu Old Regions; tiếp theo là **Mixed GC** dọn Young + những Old Regions nhiều rác nhất trong ngưỡng `MaxGCPauseMillis`.
4. **Humongous Object:** Kích thước `> G1HeapRegionSize / 2`. Cấp thẳng vào chuỗi Region liên tiếp trong Old Gen → gây lãng phí cuối region, bão `Concurrent Start (G1 Humongous Allocation)`, phân mảnh dẫn đến `To-space exhausted` / `Full GC`.
5. **Tăng Heap không luôn giảm pause:** (1) Full GC / Mixed GC quét live set và Heap lớn hơn có thể mất lâu hơn; (2) Vượt `~32 GB` tắt Compressed Oops làm phình pointer gấp đôi; (3) Tăng RAM cho Remembered Sets ngoài Heap và nếu khóa `-Xmn` thì Young pause còn tăng theo.
</details>
