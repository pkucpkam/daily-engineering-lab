# JVM-05 · Đáp án — JIT: Interpreter, C1, C2, tiered compilation

> ⚠️ **Chỉ mở sau khi đã tự làm.** Đọc đáp án mà chưa tự làm = mất 80% giá trị bài tập.
> Mỗi đáp án được bọc trong `<details>` để tránh lộ khi lướt.

---

### E1
<details><summary>Xem đáp án</summary>

**Đáp án:**
- `58` = thời gian từ lúc khởi động JVM (**58 ms**).
- `14`, `15`, `16` = ID biên dịch (compilation ID tăng dần).
- `3` / `4` = **Compilation Tier** (`3` = C1 với full profiling, `4` = C2 tối ưu cao nhất).
- `%` và `@ 4` = **On-Stack Replacement (OSR)**: vòng lặp trong `hotSum` nóng lên khi đang chạy nên được compile tại bytecode index `4` và thay thế ngay khung thực thi trên stack.
- `(22 bytes)` / `(24 bytes)` = kích thước bytecode của method.
</details>

---

### E2
<details><summary>Xem đáp án</summary>

**Đáp án:**
Tier 3 (C1) tập trung **compile thật nhanh** và gắn các bộ đếm MDO (profile nhánh nào hay chạy, kiểu nào hay gặp, có null không) — nó **không** chạy các tối ưu nặng tốn thời gian phân tích như Escape Analysis sâu, Loop Unrolling hay Speculative Inlining tối đa. Khi method đủ nóng, Tier 4 (C2) dùng chính dữ liệu MDO mà Tier 3 đã thu thập để sinh ra machine code nhanh gấp nhiều lần C1 rồi đánh dấu bản Tier 3 cũ `made not entrant`.
</details>

---

### E3
<details><summary>Xem đáp án</summary>

**Đáp án:**
Call site `p.calculate(...)` chuyển từ **Monomorphic** (1 implementation → C2 devirtualize & inline trực tiếp) sang **Megamorphic** (≥ 3 implementation). C2 **không thể inline** các thân hàm `calculate` vào `apply(...)` được nữa (phải gọi gián tiếp qua vtable/itable dispatch), đồng thời chặn luôn khả năng Escape Analysis / Constant Folding xuyên qua call site đó.
</details>

---

### E4
<details><summary>Xem đáp án</summary>

**Đáp án:**
- `made not entrant: uncommon trap` nghĩa là giả định tối ưu hóa (speculative assumption) của bản native code đó vừa bị phá vỡ lúc runtime (ví dụ nhánh chưa từng chạy nay chạy tới, hoặc class thứ 2 xuất hiện ở call site monomorphic).
- JVM không ép dừng ngang các thread đang chạy dở bên trong bản native code cũ nếu chúng chưa chạm điểm sai: nhãn `not entrant` chỉ **cấm mọi lời gọi mới** đi vào bản code đó; thread nào lỡ chạm vào chỗ sai trong bản cũ sẽ deoptimize ngay tại chỗ về Interpreter, còn sau khi toàn bộ thread đã thoát khỏi bản cũ, JVM chuyển nó sang **`made zombie`** để giải phóng vùng nhớ trong Code Cache.
</details>

---

### E5
<details><summary>Xem đáp án</summary>

**Đáp án (3 lỗi):**
1. **Không có warmup:** những vòng lặp đầu chạy ở Interpreter + OSR giữa chừng, trộn lẫn thời gian compile vào phép đo.
2. **Constant Folding:** truyền hằng số cố định `42` cho phép C2 tính sẵn kết quả thành 1 hằng số ngay lúc compile.
3. **Dead Code Elimination (DCE):** kết quả trả về của `computeHash(42)` không được dùng ở đâu → C2 có quyền xóa sạch thân hàm và thậm chí xóa toàn bộ vòng `for`!
</details>

---

### E6
<details><summary>Xem đáp án</summary>

**Kết quả tham khảo từ `-XX:+PrintCompilation` (`JitWarmupRunner`, JDK 26.0.1):**
- `98 ms`: `applyTax` được compile ở Tier 3 (`id 116`) rồi Tier 4 (`id 119`) với giả định monomorphic (`StandardTax`).
- `103 ms`: ngay khi `VipTax` được truyền vào `applyTax`, dòng `103 119 4 lab.jvm.jit.JitWarmupRunner::applyTax (8 bytes) made not entrant: uncommon trap` xuất hiện!
- `105 ms`: `applyTax` được C2 compile lại ở Tier 4 (`id 123`) với profile mới (bimorphic).
</details>

---

### E7
<details><summary>Xem đáp án</summary>

**Hướng giải & code tham khảo:** xem [lab/solutions/E7MegamorphicBenchmark.java](./lab/solutions/E7MegamorphicBenchmark.java).
- Khi chạy 2 method tách biệt (`runLoopMono` chỉ gọi 1 impl vs `runLoopMega` gọi xoay vòng 4 impl `i & 3`), số đo thực tế cho 100 triệu lần gọi (Windows 11, JDK 26.0.1 `--release 21`): **Monomorphic `35 ms`** vs **Megamorphic (4 impls) `200 ms`** (**chậm hơn ~5,7×** vì mất inlining và phải dispatch gián tiếp).
</details>

---

### E8
<details><summary>Xem đáp án</summary>

**Hướng giải:**
Chạy:
```powershell
java "-XX:+UnlockDiagnosticVMOptions" "-XX:+PrintInlining" -cp target/classes lab.jvm.jit.JitWarmupRunner | Select-String "pureWork"
```
Sẽ thấy dòng dạng `@ ... lab.jvm.jit.JitWarmupRunner::pureWork (22 bytes)   inline (hot)` bên trong `hotSum` và `main`, xác nhận phương thức 22 bytes (< 35 bytes `MaxInlineSize`) được C2 inline hoàn toàn.
</details>

---

### E9
<details><summary>Xem đáp án</summary>

<details open><summary>Phân tích 2 sự cố JIT & giải pháp production</summary>

1. **Hiện tượng A (chậm 3 phút đầu sau deploy & crash loop probe):**
   - **Nguyên nhân:** Pod mới lên đang chạy Tier 0 Interpreter + nạp class/Spring bean + JIT compiler threads chạy nặng song song. Bơm ngay 400 req/s vào lúc pod chưa warmup làm latency vượt 500ms, kích hoạt `readinessProbe` (hoặc tệ hơn là `livenessProbe`) fail dẫn tới restart lặp lại.
   - **Giải pháp:**
     1. Tách rõ `startupProbe` (cho phép khởi động + warmup, vd `failureThreshold: 30, periodSeconds: 5`) khỏi `livenessProbe` (không bao giờ dùng liveness quá nhạy để kill pod đang warmup!).
     2. Chạy **Application Warmup** trước khi mở `readinessProbe = UP` (ví dụ `ApplicationReadyEvent` tự gửi ~200–500 request nội bộ qua đường chính serialization/DB/mapping rồi mới báo `READY`), hoặc dùng canary traffic tăng dần.

2. **Hiện tượng B (`CodeCache is full. Compiler has been disabled` trên Java 7):**
   - **Nguyên nhân:** Trên Java 7, `-XX:ReservedCodeCacheSize` mặc định rất nhỏ (~`48 MB`). Khi đầy Code Cache, JVM buộc phải **tắt hoàn toàn JIT compiler**. Mọi class mới hoặc method bị deoptimize về sau bị khóa vĩnh viễn ở **Interpreter (Tier 0)** — chậm gấp 10× dù CPU và GC trông rất nhàn!
   - **Khắc phục & phòng ngừa:**
     1. Phải **restart** instance JBoss đang bị tắt compiler (vì khi đã tắt compiler trên Java 7 thường không tự phục hồi nếu không bật flushing).
     2. Bổ sung JVM flags vào `standalone.conf`: `-XX:ReservedCodeCacheSize=256m -XX:+UseCodeCacheFlushing`.
     3. Kiểm tra code xem có tạo động class/proxy/Groovy/Nashorn/Pattern không cache liên tục gây rò rỉ Code Cache + Metaspace không.

</details>
</details>

---

## Self-check (từ README §9) — đáp án gợi ý
<details><summary>Xem</summary>

1. **Tiered Compilation (Tier 0 → 4):** Tier 0 Interpreter → Tier 1 C1 đơn giản (không profile) → Tier 2 C1 limited profile → **Tier 3 C1 full profile (thu MDO: xác suất nhánh, kiểu runtime, null)** → Tier 4 C2 tối ưu tối đa dựa trên MDO.
2. **4 lý do Java chậm vài phút đầu:** (1) Chạy ở Interpreter chậm 5–50×; (2) JIT compiler threads tranh CPU để compile hàng nghìn method; (3) Lazy class loading + bean/pool/TLS handshake đầu tiên; (4) Profile chưa ổn định gây deoptimization & re-compile.
3. **Inlining & Monomorphic/Bimorphic/Megamorphic:** Inlining chép thân hàm con vào hàm cha (`MaxInlineSize = 35B`, `FreqInlineSize = 325B`), mở khóa Escape Analysis & DCE. Monomorphic (1 impl) và Bimorphic (2 impl) inline được; Megamorphic (≥ 3 impl) mất inline và rơi về itable/vtable dispatch.
4. **Deoptimization (`made not entrant`):** Xảy ra khi giả định tối ưu của C2 bị phá vỡ lúc runtime — ví dụ: (1) nạp thêm implementation thứ 2 phá vỡ CHA/monomorphic inline (`applyTax` gặp `VipTax`); (2) một nhánh hiếm `uncommon_trap` hoặc giá trị `null` lần đầu xuất hiện.
5. **3 lỗi microbenchmark `nanoTime()`:** (1) Thiếu warmup (đo cả Interpreter & JIT); (2) Dead Code Elimination (không dùng giá trị trả về nên C2 xóa vòng lặp); (3) Constant Folding (truyền input hằng số) hoặc OSR bias.
</details>
