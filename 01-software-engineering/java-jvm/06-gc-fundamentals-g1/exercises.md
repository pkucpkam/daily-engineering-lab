# JVM-06 · Bài tập — GC cơ bản & G1

> Làm theo thứ tự. **Tự làm trước, chỉ mở hint khi đã thử ≥ 15 phút.** Đáp án ở [solutions.md](./solutions.md) — chỉ mở sau khi đã có câu trả lời của riêng bạn.
> Ghi thời gian làm từng bài vào `my-notes.md` để theo dõi tiến bộ.

| Cấp | Mục đích | Số lượng | Thời gian gợi ý |
| :--- | :--- | :---: | :---: |
| 🟢 L1 — Recall | Nhớ & hiểu khái niệm | 5 | 20' |
| 🟡 L2 — Apply | Đọc log G1 & đo tác động Humongous | 3 | 75' |
| 🔴 L3 — Deep | Debug sự cố Humongous & Full GC | 1 | 90' |

---

## 🟢 L1 — Recall

### E1. GC Roots & Reference Counting
Hai object `NodeA` và `NodeB` trỏ qua lại lẫn nhau (`a.next = b; b.next = a;`), nhưng không còn bất kỳ biến local hay `static` nào trỏ vào cả hai.
1. Reference Counting có thu hồi được 2 object này không?
2. Tracing GC của JVM xử lý thế nào? Kể 3 loại **GC Roots** phổ biến.

---

### E2. Ngưỡng Humongous Object trong G1
Cho Heap cấu hình `-Xms8g -Xmx8g -XX:+UseG1GC` (giả sử G1 tự chia thành 2.048 Region → mỗi Region kích thước **4 MB**).
1. Một mảng `byte[]` kích thước `1.9 MB` có phải Humongous Object không?
2. Một mảng `byte[]` kích thước `2.1 MB` có phải Humongous Object không, và nó chiếm trọn mấy Region trên Heap (lãng phí bao nhiêu MB internal fragmentation)?

---

### E3. Phân biệt Young GC, Concurrent Mark và Mixed GC trong G1
1. Trong G1, giai đoạn nào thực hiện đánh dấu các object sống trong Old Gen song song cùng ứng dụng, và cờ nào (mặc định `45%`) kích hoạt nó?
2. Mixed GC khác Young GC bình thường ở điểm nào?

---

### E4. Vì sao đặt `-Xmn2g` (Young Gen cứng) lại phá hỏng cơ chế của G1?
Nếu kỹ sư chuyển từ Parallel/CMS sang G1 nhưng vẫn giữ nguyên `-Xmn2g` và `-XX:MaxGCPauseMillis=100`, chuyện gì xảy ra với khả năng giữ pause time của G1?

---

### E5. Tăng Heap luôn giảm GC pause?
Một service có live set `1.5 GB`, dùng G1GC với `-Xmx4g`. Nếu nâng lên `-Xmx24g` để "ít GC hơn", nêu **2 rủi ro** khiến p99 hoặc max STW pause có thể **tệ hơn** chứ không tốt hơn.

---

## 🟡 L2 — Apply

### E6. Quan sát log `Normal` vs `Humongous` trên G1GC
**Bối cảnh:** Dùng [lab/](./lab/README.md) để tạo áp lực cấp phát 128 MB theo 2 kích thước chunk khác nhau (`4 KB` vs `600 KB`) trên Heap `64 MB` với `G1HeapRegionSize=1m`.

**Yêu cầu:**
1. Chạy `mvn -q verify` và `mvn -q compile`.
2. Chạy chế độ `normal` và chế độ `humongous` kèm `-Xlog:gc`, ghi lại bảng so sánh:
   - Số lần GC (`GC events`)
   - Tổng thời gian STW GC (`ms`)
   - Tên nguyên nhân in trong GC log (ví dụ `G1 Evacuation Pause` vs `G1 Humongous Allocation`).
3. Thử nâng `-XX:G1HeapRegionSize=2m` khi chạy lại chế độ `humongous` (`600 KB` chunks!) và quan sát số lần GC giảm xuống ra sao.

**Tiêu chí hoàn thành:**
- [ ] Pass test trong `GcLabTest`.
- [ ] Giải thích chính xác tại sao nâng Region từ `1m` lên `2m` làm mảng `600 KB` biến mất khỏi diện Humongous (`600 KB < 1 MB`).

<details><summary>💡 Hint</summary>

Với `G1HeapRegionSize=2m`, ngưỡng Humongous là `> 1 MB` (`50%` của `2 MB`). Mảng `600 KB` trở lại thành object bình thường cấp phát vào Eden!
</details>

---

### E7. Tìm điểm gây Humongous Allocation bằng `-Xlog:gc+humongous=debug`
**Bối cảnh:** Trên JDK 21, flag `-Xlog:gc+humongous=debug` in chi tiết kích thước chính xác (tính theo byte/region) của từng Humongous object được cấp phát!

**Yêu cầu:**
Chạy `GcAllocationRunner` chế độ `humongous` với `-Xlog:gc,gc+humongous=debug` trên `-Xms64m -Xmx64m -XX:G1HeapRegionSize=1m`:
1. Đọc 1 dòng log `gc,humongous` để kiểm tra: kích thước object (`bytes`), số Region bị chiếm (`regions`), và có `Live` / `Eagerly Reclaimed` không.
2. Ghi lại lý do tại sao trong `GcAllocationRunner` với `retained = 24 chunks` (`24 × 1 MB = 24 MB`), Heap sau mỗi GC lại dừng ở khoảng `25 MB` (`27M->25M(64M)`).

**Tiêu chí hoàn thành:**
- [ ] Trích xuất được log dòng `gc,humongous` thật từ JDK 21.

<details><summary>💡 Hint</summary>

`java -Xms64m -Xmx64m "-XX:+UseG1GC" "-XX:G1HeapRegionSize=1m" "-Xlog:gc,gc+humongous=debug" -cp target\classes lab.jvm.gc.GcAllocationRunner humongous | Select-Object -First 25`
</details>

---

### E8. Refactor đoạn code đọc/hash file để xóa bỏ Humongous Allocation
**Bối cảnh:** Một service xử lý webhook đọc file JSON 2 MB vào bộ nhớ bằng `byte[] raw = Files.readAllBytes(path)` rồi tính SHA-256 (`MessageDigest.getInstance("SHA-256").digest(raw)`), gây bão Humongous Allocation trên G1 (`G1HeapRegionSize=2m`, ngưỡng > 1 MB).

**Yêu cầu:**
Viết lại phương thức Java 21 (tham khảo mẫu tại `lab/solutions/E8StreamingHashRefactored.java`) dùng streaming buffer `16 KB` để tính cùng một mã SHA-256 hex mà **không cấp phát mảng > 16 KB** (tức 0 Humongous object), kèm 1 JUnit test kiểm chứng kết quả SHA-256 giống hệt bản `Files.readAllBytes`.

**Tiêu chí hoàn thành:**
- [ ] Kết quả SHA-256 của file 2 MB khớp 100% giữa bản cũ và bản streaming.

<details><summary>💡 Hint</summary>

Dùng `DigestInputStream` hoặc vòng `while ((n = in.read(buf)) != -1) { md.update(buf, 0, n); }` với `byte[] buf = new byte[16 * 1024]`, sau đó `HexFormat.of().formatHex(md.digest())` (có từ Java 17!).
</details>

---

## 🔴 L3 — Deep

### E9. Sự cố Production: Service G1 `-Xmx8g` bỗng dính Full GC 4,5 giây dù Heap mới dùng 52%
**Tình huống:**
> Service xuất hóa đơn điện tử chạy Java 21, `-Xms8g -Xmx8g -XX:+UseG1GC` (G1 mặc định chọn `Region = 4 MB`, ngưỡng Humongous = `> 2 MB`).
> Sau khi triển khai tính năng xuất file XML/PDF gộp lô, mỗi ngày 2–3 lần p99 latency nhảy lên **4.500 ms**.
> Đoạn GC log ngay tại thời điểm sự cố:
> ```text
> [14:22:01.104][info][gc] GC(412) Pause Young (Concurrent Start) (G1 Humongous Allocation) 4120M->4080M(8192M) 18.4ms
> [14:22:01.410][info][gc] GC(413) To-space exhausted
> [14:22:01.412][info][gc] GC(413) Pause Young (Normal) (G1 Evacuation Pause) 7950M->4190M(8192M) 890.2ms
> [14:22:01.950][info][gc] GC(414) Pause Full (G1 Compaction Pause) 8120M->3980M(8192M) 4420.5ms
> ```
> Khi soi code, bạn thấy:
> 1. Mỗi yêu cầu xuất lô đọc kết quả XML `~2.4 MB` vào `ByteArrayOutputStream` rồi gọi `.toByteArray()` (tạo mảng `2.4 MB`), đồng thời tạo `String` ghép nối chuyển base64 (`~3.2 MB` char/byte array!).
> 2. Trong `standalone` / Helm values có một người từng copy cấu hình cũ thêm cờ `-Xmn4g`.

**Yêu cầu:**
1. Giải thích từng dòng trong 4 dòng GC log trên (`G1 Humongous Allocation`, `To-space exhausted`, `Pause Full (G1 Compaction Pause)` 4.420ms) tại sao xảy ra dù lúc 14:22:01 Heap trước đó mới báo `4.120M / 8.192M` (~50%)!
2. Chỉ ra 2 nguyên nhân gốc rễ (1 từ code tạo `byte[]` / `String`, 1 từ flag `-Xmn4g` + kích thước Region `4 MB`).
3. Đề xuất kế hoạch xử lý cả **ngắn hạn (tuning JVM flag không sửa code)** và **dài hạn (refactor code)** để xóa triệt để `Pause Full` 4,5 giây.

**Tiêu chí hoàn thành:**
- [ ] Giải thích chính xác mối liên hệ giữa `2.4 MB` / `3.2 MB` > `2 MB` (50% của Region `4 MB`) → Humongous fragmentation → `To-space exhausted` → `Pause Full`.
- [ ] Chỉ ra tại sao bỏ `-Xmn4g` và sửa streaming (hoặc tạm tăng `G1HeapRegionSize=8m`/`16m`) giải quyết đúng vấn đề.

<details><summary>💡 Hint</summary>

1. `2.4 MB` và `3.2 MB` đều lớn hơn `2 MB` (`G1HeapRegionSize=4m / 2`), nên cấp phát thẳng thành **Humongous Regions** trong Old Gen, cộng thêm `toByteArray()` nhân bản mảng và `-Xmn4g` khóa cứng 4 GB cho Young Gen (khiến Old + Humongous chỉ còn 4 GB trống, cực nhanh cạn region liên tiếp cho `To-space` -> `To-space exhausted` -> `Pause Full`!).
2. Ngắn hạn: **xóa `-Xmn4g`** để G1 tự co giãn Young Gen, và nâng `-XX:G1HeapRegionSize=8m` (ngưỡng Humongous lên `4 MB` để các mảng `2.4–3.2 MB` về lại Eden!). Dài hạn: dùng streaming Base64 / `OutputStream` thay vì gom nguyên cục `toByteArray()` vào RAM.
</details>
