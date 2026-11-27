# JVM-05 · Bài tập — JIT: Interpreter, C1, C2, tiered compilation

> Làm theo thứ tự. **Tự làm trước, chỉ mở hint khi đã thử ≥ 15 phút.** Đáp án ở [solutions.md](./solutions.md) — chỉ mở sau khi đã có câu trả lời của riêng bạn.
> Ghi thời gian làm từng bài vào `my-notes.md` để theo dõi tiến bộ.

| Cấp | Mục đích | Số lượng | Thời gian gợi ý |
| :--- | :--- | :---: | :---: |
| 🟢 L1 — Recall | Nhớ & hiểu khái niệm | 5 | 20' |
| 🟡 L2 — Apply | Đọc log PrintCompilation & kiểm chứng benchmark | 3 | 75' |
| 🔴 L3 — Deep | Điều tra sự cố warmup & Code Cache | 1 | 90' |

---

## 🟢 L1 — Recall

### E1. Đọc 3 dòng log `-XX:+PrintCompilation`
Giải thích chính xác ý nghĩa các cột (`58`, `14`/`15`, `%`, `3`/`4`, `@ 4`, `made not entrant`) trong 3 dòng sau:
```text
58   14       3       lab.jvm.jit.JitWarmupRunner::pureWork (22 bytes)
58   15       4       lab.jvm.jit.JitWarmupRunner::pureWork (22 bytes)
58   16 %     3       lab.jvm.jit.JitWarmupRunner::hotSum @ 4 (24 bytes)
```

---

### E2. Tại sao C1 Tier 3 đã compile rồi vẫn phải tốn công C2 Tier 4?
Nếu Tier 3 (C1) đã biến bytecode thành machine code chạy nhanh hơn Interpreter, tại sao HotSpot không dừng luôn ở Tier 3 mà còn thu thập MDO profile để đẩy tiếp lên Tier 4 (C2)?

---

### E3. Monomorphic vs Megamorphic call site
Một interface `DiscountPolicy` ban đầu chỉ có `StandardDiscount`. Sau đó trong plugin mới, hệ thống nạp thêm `VipDiscount`, `HolidayDiscount`, `StaffDiscount` (tổng 4 implementation cùng chạy qua một hàm `apply(DiscountPolicy p)`). Điều gì xảy ra với tối ưu **inlining** tại lời gọi `p.calculate(...)`?

---

### E4. Vì sao `made not entrant` xuất hiện?
Sự kiện `made not entrant: uncommon trap` có nghĩa là gì? JVM xử lý thế nào với những thread đang chạy dở bên trong bản code cũ vừa bị đánh dấu `made not entrant`?

---

### E5. Hai lỗi sai kinh điển khi benchmark bằng `System.nanoTime()`
Cho đoạn benchmark sau:
```java
long t0 = System.nanoTime();
for (int i = 0; i < 5_000_000; i++) {
    computeHash(42);
}
System.out.println((System.nanoTime() - t0) / 1_000_000 + " ms");
```
Chỉ ra **3 lỗi** khiến kết quả thời gian in ra hoàn toàn vô nghĩa so với tải thật trên production.

---

## 🟡 L2 — Apply

### E6. Quan sát chuyển Tier 3 → 4 và Deoptimization bằng `-XX:+PrintCompilation`
**Bối cảnh:** Dùng [lab/](./lab/README.md) để tận mắt xem HotSpot compile và deoptimize code của bạn.

**Yêu cầu:**
1. Chạy `mvn -q verify` và `mvn -q compile`.
2. Chạy `java -XX:+PrintCompilation -cp target/classes lab.jvm.jit.JitWarmupRunner` và lọc tìm các dòng của `JitWarmupRunner`.
3. Xác định:
   - Thời điểm (ms) `applyTax` được compile ở Tier 3 và Tier 4.
   - Đúng dòng `applyTax ... made not entrant: uncommon trap` xuất hiện ngay sau khi nạp `VipTax`, và thời điểm `applyTax` được compile lại ở Tier 4.

**Tiêu chí hoàn thành:**
- [ ] `mvn -q verify` pass 1/1 test.
- [ ] Trích xuất được 3 mốc thời gian (Tier 4 lần 1, `uncommon trap`, Tier 4 lần 2) từ log `-XX:+PrintCompilation`.

<details><summary>💡 Hint</summary>

Trên PowerShell:
`java "-XX:+PrintCompilation" -cp target/classes lab.jvm.jit.JitWarmupRunner | Select-String "JitWarmupRunner"`
</details>

---

### E7. So sánh hiệu năng Call Site: Monomorphic vs Bimorphic vs Megamorphic (4 impl)
**Bối cảnh:** Trong `JitWarmupRunner`, thí nghiệm `[3]` mới kiểm tra từ 1 implementation (`StandardTax`) sang 2 implementation (`VipTax` — vẫn inline được ở chế độ bimorphic!).

**Yêu cầu:**
Chạy hoặc viết lớp kiểm chứng cho trường hợp **4 implementation** (`Impl1..Impl4` — megamorphic) gọi xen kẽ qua cùng 1 interface method (tham khảo code mẫu tại `lab/solutions/E7MegamorphicBenchmark.java`) và so sánh tốc độ 100M lần gọi giữa:
1. Monomorphic (100% `Impl1`)
2. Megamorphic (xoay vòng 4 implementation `Impl1..Impl4`)

**Tiêu chí hoàn thành:**
- [ ] Ghi lại số đo thực tế (ms) của Monomorphic vs Megamorphic trên máy bạn, giải thích vì sao Megamorphic chậm hơn (mất inlining + phải tra `itable`/indirect jump).

<details><summary>💡 Hint</summary>

Dùng mảng `TaxRule[] rules = { new R1(), new R2(), new R3(), new R4() }` và truy cập `rules[i & 3]` sau khi warmup riêng trường hợp 1-impl vs 4-impl (tốt nhất chạy 2 phương thức tách biệt để 1-impl không bị ô nhiễm profile bởi 4-impl!).
</details>

---

### E8. Kiểm chứng ngưỡng Inlining `FreqInlineSize` (`-XX:+PrintInlining`)
**Bối cảnh:** HotSpot có cờ chẩn đoán xem từng lời gọi hàm có thực sự được inline hay bị từ chối với lý do gì (như `hot method too big` hoặc `already compiled into a big method`).

**Yêu cầu:**
Chạy `JitWarmupRunner` với các cờ diagnostic của HotSpot:
`java -XX:+UnlockDiagnosticVMOptions -XX:+PrintInlining -cp target/classes lab.jvm.jit.JitWarmupRunner`
Tìm trong output các dòng chứa `JitWarmupRunner` và kiểm tra xem `pureWork` (`22 bytes`) có ghi `inline (hot)` hay không.

**Tiêu chí hoàn thành:**
- [ ] Tìm được dòng log xác nhận `pureWork` được `@ ... inline (hot)`.

<details><summary>💡 Hint</summary>

`-XX:+PrintInlining` bắt buộc phải đi kèm `-XX:+UnlockDiagnosticVMOptions` phía trước nếu không JVM sẽ từ chối khởi động!
</details>

---

## 🔴 L3 — Deep

### E9. Sự cố Production: Pod vừa lên thì timeout 5 phút đầu, 3 ngày sau lại chậm vĩnh viễn
**Tình huống:**
> Một hệ thống thanh toán có 2 triệu chứng ở hai thời điểm khác nhau:
> - **Hiện tượng A (lúc mới deploy / scale-out pod mới):** Trong 3 phút đầu khi Kubernetes đưa pod vào nhận `400 req/s`, p99 latency nhảy lên `600 ms` (bình thường sau đó là `22 ms`), làm `readinessProbe` (timeout `500 ms`) đánh fail liên tục khiến pod khởi động lại vòng lặp.
> - **Hiện tượng B (trên một con server legacy Java 7 chạy 5 ngày liên tục):** Không có GC pause dài, CPU thấp (15%), DB nhanh (`3 ms`), nhưng toàn bộ API chậm gấp 8–10 lần bình thường từ sáng nay. Kiểm tra `server.log` ngày hôm qua thấy đúng 1 dòng cảnh báo trôi qua:
>   `Java HotSpot(TM) 64-Bit Server VM warning: CodeCache is full. Compiler has been disabled.`

**Yêu cầu:**
1. Giải thích nguyên nhân gốc rễ tầng JIT cho **Hiện tượng A** và **Hiện tượng B**.
2. Đề xuất **giải pháp thực tế** cho Hiện tượng A (cả ở tầng Kubernetes `startupProbe`/`readinessProbe` lẫn bước warmup ứng dụng).
3. Đề xuất **cách xử lý khẩn cấp và phòng ngừa lâu dài** cho Hiện tượng B trên Java 7 / JBoss AS 7.1.1 (flags JVM cần thêm, và kiểm tra gì trong code nếu app tự sinh class/proxy động liên tục).

**Tiêu chí hoàn thành:**
- [ ] Giải thích chính xác vì sao sau thông báo `CodeCache is full. Compiler has been disabled`, app chạy chậm đi 8–10× dù CPU thấp và không có GC pause.
- [ ] Có cấu hình JVM cụ thể (`-XX:ReservedCodeCacheSize=...`, `-XX:+UseCodeCacheFlushing`) và chiến lược probe/warmup cho Hiện tượng A.

<details><summary>💡 Hint</summary>

1. Hiện tượng A: pod lạnh vừa chạy Interpreter vừa đổ 100% CPU/compiler vào JIT compile hàng nghìn method Spring/Jackson; đồng thời readiness probe timeout `500ms` quá gắt cho giai đoạn warmup.
2. Hiện tượng B: trên Java 7 mặc định `ReservedCodeCacheSize` rất nhỏ (~`48 MB`). Khi đầy, JVM tắt hẳn JIT compiler → mọi code mới gọi tới hoặc bị deoptimize đều chạy vĩnh viễn ở **Interpreter**!
</details>
