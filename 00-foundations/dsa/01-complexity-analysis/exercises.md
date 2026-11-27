# DSA-01 · Bài tập — Độ phức tạp & Big-O thực dụng

> Làm theo thứ tự. **Tự làm trước, chỉ mở hint khi đã thử ≥ 15 phút.** Đáp án ở [solutions.md](./solutions.md) — chỉ mở sau khi đã có câu trả lời của riêng bạn.
> Ghi thời gian làm từng bài vào `my-notes.md` để theo dõi tiến bộ.

| Cấp | Mục đích | Số lượng | Thời gian gợi ý |
| :--- | :--- | :---: | :---: |
| 🟢 L1 — Recall | Nắm chắc Big-O, Cache Locality, Amortized $O(1)$, ước lượng từ $n$ | 5 | 20' |
| 🟡 L2 — Apply | Đo thật `int[]` vs `ArrayList` vs fragmented `LinkedList` & đếm chi phí resize | 3 | 75' |
| 🔴 L3 — Deep | Ticket Production: Job đối soát `200.000` giao dịch chạy 45 phút trên JBoss | 1 | 90' |

---

## 🟢 L1 — Recall

### E1. Vì sao duyệt $O(n)$ trên `LinkedList` chậm hơn rất nhiều so với `int[]` (và thậm chí thua cả `Arrays.sort` $O(n \log n)$)?
Giải thích cơ chế ở tầng phần cứng CPU (**Cache Line 64B**, **Hardware Prefetcher**, **Pointer Chasing**) và bố cục bộ nhớ trên JVM Heap (`Node` wrapper + `Integer` boxing vs `int[]`).

---

### E2. Giải thích đúng ý nghĩa "`ArrayList.add()` là $O(1)$ amortized"
1. Nếu một `ArrayList` đang chứa `4.000.000` phần tử và vừa đúng lúc đầy, lệnh `list.add(x)` kế tiếp tốn $O(1)$ hay $O(n)$ thật sự?
2. Chứng minh bằng công thức cấp số nhân vì sao trung bình trên một chuỗi `n` lần `add()` liên tiếp từ rỗng thì mỗi lần chỉ tốn $O(1)$ (với hệ số tăng `1.5×` của OpenJDK)?

---

### E3. Phân tích bẫy $O(n^2)$ kinh điển với `LinkedList.get(i)` và `List.contains()`
Tính tổng số phép lặp/truy cập phần tử ở worst-case khi `n = 100.000` cho hai đoạn code sau:
1. `for (int i = 0; i < linkedList.size(); i++) { sum += linkedList.get(i); }`
2. `for (String id : incomingIds) { if (!existingArrayList.contains(id)) ... }` (với `incomingIds.size() = existingArrayList.size() = 100.000`).

---

### E4. Ước lượng nhanh giới hạn kích thước đầu vào $n$ cho API `< 50 ms`
Một API thanh toán chỉ cho phép thuật toán xử lý trong `< 50 ms` (ước lượng 1 core Java xử lý an toàn `~5 × 10^6` thao tác nhẹ trong `50 ms`). Với mỗi thuật toán dưới đây, $n = 100.000$ có chạy kịp không?
1. Duyệt 1 vòng + lookup `HashSet` ($O(n)$)
2. Sắp xếp bằng `Arrays.sort` ($O(n \log n)$)
3. So sánh từng cặp phần tử ($O(n^2)$)

---

### E5. Vì sao nên thay `new LinkedList<>()` bằng `new ArrayDeque<>()` khi cần `Queue`/`Stack`?
So sánh `LinkedList` vs `ArrayDeque` về mức tiêu thụ bộ nhớ trên từng phần tử, cache locality, và lý do `ArrayDeque` cũng đạt $O(1)$ amortized ở cả hai đầu `offerLast` / `pollFirst`.

---

## 🟡 L2 — Apply

### E6. Benchmark thực tế `1.000.000` phần tử: `int[]` vs `ArrayList` vs `LinkedList` vs `Arrays.sort`
**Bối cảnh:** Dùng [lab/](./lab/README.md) chạy `ComplexityLabTest` và `ComplexityLabRunner`.
**Yêu cầu:**
1. Chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`.
2. Đọc `CacheLocalityBenchmark.java` để hiểu vì sao lab trộn ngẫu nhiên thứ tự node `LinkedList` (*shuffled allocation*) thay vì add tuần tự một mạch trên TLAB (mô phỏng Heap thật sau nhiều vòng đời request!).
3. Ghi lại số đo ms của:
   - Duyệt $O(n)$ `int[]` (`1M` phần tử)
   - Duyệt $O(n)$ `ArrayList<Integer>` (`1M` phần tử)
   - Duyệt $O(n)$ fragmented `LinkedList<Integer>` (`1M` phần tử)
   - Sắp xếp $O(n \log n)$ `Arrays.sort(int[])` (`1M` phần tử)

**Tiêu chí hoàn thành:**
- [ ] `mvn -o -B verify` pass 100%.
- [ ] Ghi được bảng đo thật chứng minh `LinkedList` $O(n)$ chậm hơn `int[]` $O(n)$ nhiều lần (và có thể chậm hơn/xấp xỉ $O(n \log n)$ `Arrays.sort` trên mảng!).

<details><summary>💡 Hint</summary>

Nếu chỉ tạo `for (int i = 0; i < 1_000_000; i++) linkedList.add(i);` trong 1 vòng lặp sạch sẽ, bộ cấp phát TLAB (*Thread-Local Allocation Buffer*) của JVM vô tình đặt 1 triệu `Node` nằm gần sát nhau tuần tự trên Heap — làm che giấu phần lớn hiệu ứng phân mảnh Heap thực tế trên server chạy lâu! Khi xáo trộn thứ tự tạo/liên kết node (`shuffledLinkedList`), bạn mới nhìn thấy bộ mặt thật của *pointer chasing*!
</details>

---

### E7. Đo chi phí Resize thật của `ArrayList` (Default `10` vs Pre-sized `1.000.000`)
**Bối cảnh:** Tiếp tục với `CacheLocalityBenchmark.java` và xem `lab/solutions/PreSizedCollectionsSolution.java`.
**Yêu cầu:**
1. Mô phỏng & đo thật việc thêm `1.000.000` phần tử vào `new ArrayList<>()` (từ capacity mặc định `10`, tăng `1.5×`) vs `new ArrayList<>(1_000_000)`.
2. Đếm chính xác số lần `grow()` kích hoạt và tổng số phần tử bị copy lại trong mảng (`totalCopiedElements`).
3. So sánh thời gian chạy và lượng rác cấp phát giữa 2 cách.

**Tiêu chí hoàn thành:**
- [ ] Đếm đúng số lần resize từ capacity `10` lên `1.000.000` và chứng minh `totalCopiedElements` xấp xỉ `~2.43M` (`O(n)` — amortized `O(1)`!).

<details><summary>💡 Hint</summary>

Dãy capacity `10 -> 15 -> 22 -> 33 -> 49 -> ...` theo công thức `oldCap + (oldCap >> 1)` của OpenJDK `ArrayList#newCapacity`. Cộng tổng các kích thước mảng cũ bị copy lại sẽ thấy tổng xấp xỉ `2.43n` (`O(n)`).
</details>

---

### E8. Tối ưu một hàm đối soát từ $O(n^2)$ xuống $O(n)$ & giữ thứ tự
**Bối cảnh:** Mở `lab/solutions/PreSizedCollectionsSolution.java` (hoặc tự viết trước trong test).
**Yêu cầu:**
Cho `List<Long> allOrders` (`50.000` phần tử) và `List<Long> cancelledOrders` (`20.000` phần tử).
1. Viết hàm `findActiveOrdersNaive` dùng `cancelledOrders.contains(id)` ($O(n \times m)$).
2. Viết hàm `findActiveOrdersFast` dùng `HashSet.newHashSet(cancelledOrders.size())` + `ArrayList` pre-sized ($O(n + m)$), đảm bảo kết quả giống hệt nhau.
3. Đo speedup giữa 2 phiên bản.

**Tiêu chí hoàn thành:**
- [ ] Hai hàm trả về danh sách giống nhau từng phần tử.
- [ ] Phiên bản $O(n + m)$ nhanh hơn hàng trăm lần so với bản $O(n \times m)$.

<details><summary>💡 Hint</summary>

Với `50.000 × 20.000`, bản naive làm tối đa `1 tỷ` bước so sánh `Long.equals()`, còn bản `HashSet` chỉ làm `~70.000` phép tính hash!
</details>

---

## 🔴 L3 — Deep

### E9. Ticket Production: Job đối soát cuối ngày chạy `45 phút` trên JBoss với `200.000` giao dịch
**Tình huống:**
> Một batch job Java 7 chạy lúc 23:00 trên **JBoss AS 7.1.1** dùng để đối soát file CSV ngân hàng (`200.000` dòng) với danh sách giao dịch DB trong ngày (`200.000` dòng). Lúc test UAT chỉ có `1.000` dòng chạy mất `0.2 giây`, nhưng lên production `200.000` dòng mất tận **`45 phút`**, ăn full 1 CPU core và kéo theo nhiều đợt Full GC dài!
>
> Trích đoạn code thật của job:
> ```java
> List<String> bankRefs = new LinkedList<String>(); // load 200.000 dòng từ CSV
> List<TxRecord> dbTxs = loadFromDb();              // load 200.000 dòng vào LinkedList
>
> List<String> missingInDb = new ArrayList<String>();
> for (int i = 0; i < bankRefs.size(); i++) {
>     String ref = bankRefs.get(i);
>     boolean found = false;
>     for (TxRecord tx : dbTxs) {
>         if (tx.getRef().equals(ref)) {
>             found = true;
>             break;
>         }
>     }
>     if (!found) {
>         missingInDb.add(ref);
>     }
> }
> ```

**Yêu cầu:**
1. Chỉ ra **tất cả 3 lỗi Big-O / Cấu trúc dữ liệu** trong đoạn code trên (tính số phép toán xấp xỉ cho `200.000` dòng để giải thích vì sao UAT `1.000` dòng nhanh mà `200.000` dòng chậm tới `45 phút`!).
2. Viết lại đoạn code đối soát chuẩn cho **Java 7 / JBoss AS 7.1.1** đạt $O(n)$, thân thiện với CPU Cache và không làm phình bộ nhớ/GC.
3. Ước lượng thời gian chạy mới sau khi sửa với `n = 200.000`.

**Tiêu chí hoàn thành:**
- [ ] Phát hiện đủ: `LinkedList.get(i)` ($O(n^2)$), nested loop tìm `dbTxs` ($O(n^2)$), và `LinkedList` pointer chasing + pre-sizing `HashSet`/`ArrayList`.
- [ ] Đưa ra code Java 7 chạy trong `< 1 giây` (thực tế vài chục đến vài trăm ms).

<details><summary>💡 Hint</summary>

Để ý cả **2 vòng lặp $O(n^2)$ riêng biệt** trong code trên!
1. Dòng `bankRefs.get(i)` trên `LinkedList` tốn `n^2 / 4` bước nhảy pointer cho 200.000 phần tử (~`10 tỷ` bước nhảy!).
2. Vòng `for (TxRecord tx : dbTxs)` cho mỗi `ref` tốn trung bình `100.000` bước (worst-case `200.000` bước) → `200.000 × 100.000 = 20 tỷ` phép so sánh chuỗi trên Heap phân mảnh của `LinkedList`!
</details>
