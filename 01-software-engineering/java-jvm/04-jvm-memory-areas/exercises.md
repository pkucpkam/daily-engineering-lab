# JVM-04 · Bài tập — JVM Memory: Heap, Stack, Metaspace, Direct memory

> Làm theo thứ tự. **Tự làm trước, chỉ mở hint khi đã thử ≥ 15 phút.** Đáp án ở [solutions.md](./solutions.md) — chỉ mở sau khi đã có câu trả lời của riêng bạn.
> Ghi thời gian làm từng bài vào `my-notes.md` để theo dõi tiến bộ.

| Cấp | Mục đích | Số lượng | Thời gian gợi ý |
| :--- | :--- | :---: | :---: |
| 🟢 L1 — Recall | Nhớ & hiểu khái niệm | 5 | 20' |
| 🟡 L2 — Apply | Đo layout bằng JOL, NMT & Escape Analysis | 3 | 75' |
| 🔴 L3 — Deep | Điều tra rò rỉ RSS ngoài Heap | 1 | 90' |

---

## 🟢 L1 — Recall

### E1. 4 GB RSS dù `-Xmx2g`
Một container chạy Java service với `-Xms2g -Xmx2g` và đang mở 350 worker threads (`-Xss1m` mặc định), dùng Netty I/O và Spring/Hibernate. Giải thích vì sao `docker stats` báo RSS ~3.8–4.0 GB và liệt kê 5 vùng nhớ tạo nên ~1.8–2.0 GB nằm ngoài Heap.

---

### E2. Tính bằng tay kích thước 1 object `Integer` và 1 class tự định nghĩa
Trên HotSpot 64-bit bật Compressed Oops (object alignment 8 bytes):
1. Một object `Integer` tốn bao nhiêu byte?
2. Một class `class Pair { boolean active; long id; byte kind; }` tốn tổng cộng bao nhiêu byte cho mỗi instance? (Gợi ý: HotSpot có tự reorder các field cho khít không?)

---

### E3. Ngưỡng ~32 GB và Compressed Oops
Tại sao khi tăng `-Xmx` từ `30 GB` lên `32 GB`, một số ứng dụng lại chứa được **ít object hơn** và tốn thêm bộ nhớ trên mỗi reference?

---

### E4. Escape Analysis & Scalar Replacement
Cho hàm sau chạy trong vòng lặp 10 triệu lần:
```java
static long distanceSquared(int x, int y) {
    Point p = new Point(x, y);
    return (long) p.x() * p.x() + (long) p.y() * p.y();
}
```
Vì sao trên Java 21 chạy đủ nóng, hàm này gần như **không tốn GC** nào dù tạo 10 triệu lần `new Point(x, y)`? Điều gì xảy ra ngay lập tức nếu bạn thêm dòng `CACHE.add(p)`?

---

### E5. Direct Buffer leak khi Heap vẫn trống
Vì sao cấp phát liên tục `ByteBuffer.allocateDirect(10 * 1024 * 1024)` mà không giải phóng có thể khiến OS giết process, ngay cả khi Heap chỉ mới dùng 5%?

---

## 🟡 L2 — Apply

### E6. Dùng JOL & NMT đo thực tế trên máy bạn
**Bối cảnh:** Chạy [lab/](./lab/README.md) để tự kiểm chứng kích thước object và phân bổ Native Memory.

**Yêu cầu:**
1. Chạy `mvn -q verify` (kiểm tra 2 test JOL pass).
2. Chạy `MemoryLayoutRunner` với `-XX:NativeMemoryTracking=summary -Xmx256m` (đúng lệnh trong `lab/README.md`) và ghi lại:
   - Kích thước của `Integer`, `CompactSample`, `int[1000]`, `Integer[1000]`.
   - So sánh `Java Heap committed` vs `Total committed` trong bảng NMT.
3. Chạy thử lại `ClassLayout.parseClass(Integer.class)` nhưng thêm JVM flag `-XX:-UseCompressedOops` để tận mắt thấy `Integer` nhảy từ **16 bytes** lên **24 bytes**.

**Tiêu chí hoàn thành:**
- [ ] Pass 2/2 test trong `MemoryLayoutTest`.
- [ ] Ghi được bảng so sánh JOL khi bật vs tắt `UseCompressedOops`.

<details><summary>💡 Hint</summary>

Để chạy JOL thấy layout 24 bytes của `Integer`, thêm `-XX:-UseCompressedOops` vào lệnh `java` khi chạy `MemoryLayoutRunner`.
</details>

---

### E7. Chứng minh Escape Analysis (`-XX:+DoEscapeAnalysis` vs `-XX:-DoEscapeAnalysis`)
**Bối cảnh:** Ta muốn đo thực nghiệm xem Scalar Replacement có thật sự loại bỏ cấp phát `new Point(...)` trên Heap hay không.

**Yêu cầu:**
Viết chương trình nhỏ `E7EscapeAnalysisDemo.java` (hoặc chạy bản mẫu trong `lab/solutions/E7EscapeAnalysisDemo.java`) chạy vòng lặp `100_000_000` lần tạo một object nhỏ `Point(i, i + 1)` không thoát khỏi hàm và cộng dồn kết quả:
1. Chạy với `-Xmx128m -Xlog:gc*` (mặc định bật Escape Analysis): đếm số lần Young GC xảy ra và thời gian chạy.
2. Chạy lại với `-Xmx128m -Xlog:gc* -XX:-DoEscapeAnalysis`: đếm số lần Young GC xảy ra và thời gian chạy.

**Tiêu chí hoàn thành:**
- [ ] Quan sát được sự khác biệt rõ rệt về số lần Young GC (0 hoặc vài lần lúc warmup vs hàng chục/trăm lần khi tắt `DoEscapeAnalysis`).

<details><summary>💡 Hint</summary>

Trong `lab/solutions/E7EscapeAnalysisDemo.java`, giữ `blackhole` (`volatile long`) bên ngoài để JIT không xóa toàn bộ vòng lặp (Dead Code Elimination), chỉ xóa việc cấp phát `Point`!
</details>

---

### E8. Tối ưu layout struct để giảm internal padding
**Bối cảnh:** Thiết kế một class lưu 1 triệu bản ghi trong Heap. Dù HotSpot tự động reorder các primitive field trong cùng class, nhưng nếu có kế thừa (class cha + class con) thì HotSpot **không** trộn field của con lên khe hở của cha!

**Yêu cầu:**
Dùng JOL `ClassLayout.parseClass(...)` trong `MemoryLayoutRunner` (hoặc test riêng) để đo kích thước của:
1. `class Parent { byte a; } class Child extends Parent { long b; byte c; }`
2. So sánh với một class gộp phẳng hoặc sắp xếp hợp lý. Giải thích vì sao `Child` lại bị mất padding gap ở cuối phần của `Parent`.

**Tiêu chí hoàn thành:**
- [ ] Trích được `ClassLayout` cho thấy khoảng `(alignment/padding gap)` giữa `Parent.a` và `Child.b`.

<details><summary>💡 Hint</summary>

Field của class cha luôn nằm trước toàn bộ field của class con, và `long b` cần align 8-byte (hoặc 4-byte tùy JVM version), tạo khoảng hở ngay sau `byte a` của `Parent` mà JVM không nhét `byte c` của `Child` lên lấp chỗ đó được.
</details>

---

## 🔴 L3 — Deep

### E9. Sự cố Production: Pod `OOMKilled` dù Heap mới 58%
**Tình huống:**
> Một service xử lý upload/nén file chạy trên Kubernetes container có **`limits.memory = 2Gi`**, cấu hình JVM cũ từ VM bê sang:
> `-Xms1536m -Xmx1536m -XX:MaxDirectMemorySize=1024m`
> Sau 4 giờ chạy tải cao (300 request đồng thời mỗi phút, xử lý PDF & Netty gRPC):
> - Prometheus báo **JVM Heap used = `890 MB / 1.536 MB` (58%)**, **Old Gen = `420 MB`**, GC pause bình thường (`< 15 ms`).
> - Nhưng pod bị Kubernetes kill bằng `OOMKilled (Exit Code 137)`!
> - Snapshot `jcmd 1 VM.native_memory summary` thu được ngay trước khi chết 5 phút cho thấy:
>   - `Java Heap`: committed `1536 MB`
>   - `Class (Metaspace)`: committed `210 MB`
>   - `Thread`: `#420 threads`, stack committed `180 MB`
>   - `Code`: committed `115 MB`
>   - `Internal / Direct / Other` (kèm malloc từ `GZIPOutputStream`/JNI): committed `390 MB`

**Yêu cầu:**
1. Cộng tổng các vùng nhớ trong snapshot NMT trên và chỉ ra vì sao Heap mới dùng `58%` (`890 MB`) mà container `2 GiB` (`2.048 MB`) vẫn bị **OOMKilled**? *(Chú ý phân biệt Heap `used` 890 MB vs Heap `committed` 1.536 MB do `-Xms1536m`!)*
2. Chỉ ra 3 sai lầm trong chuỗi JVM flags `-Xms1536m -Xmx1536m -XX:MaxDirectMemorySize=1024m` đối với container `2 GiB`.
3. Đề xuất bộ JVM flags mới an toàn cho container `2 GiB` và 2 bước kiểm tra code gốc để cắt giảm phần `Internal / Direct / Thread` đang ăn tới ~685 MB.

**Tiêu chí hoàn thành:**
- [ ] Tính đúng tổng `committed ≈ 2.431 MB > 2.048 MB` (giải thích chính xác vai trò của `-Xms1536m` giữ cứng committed Heap ở 1.536 MB).
- [ ] Đề xuất bộ flags cụ thể có `-XX:MaxRAMPercentage`, `-XX:MaxMetaspaceSize`, `-XX:MaxDirectMemorySize` phù hợp với trần `2 GiB`.

<details><summary>💡 Hint</summary>

- `-Xms1536m` ép JVM giữ committed Heap tối thiểu `1536 MB` ở OS dù app chỉ dùng `890 MB` object sống!
- `1536 (Heap) + 210 (Metaspace) + 180 (Thread) + 115 (Code) + 390 (Internal/Direct) = 2431 MB > 2048 MB`!
- Chưa kể `MaxDirectMemorySize=1024m` cho phép Direct Memory phình tới `1 GB` trên một pod tổng chỉ có `2 GB`!
</details>
