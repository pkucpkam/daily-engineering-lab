# DSA-01 · Đáp án — Độ phức tạp & Big-O thực dụng

> ⚠️ **Chỉ mở sau khi đã tự làm.** Đọc đáp án mà chưa tự làm = mất 80% giá trị bài tập.
> Mỗi đáp án được bọc trong `<details>` để tránh lộ khi lướt.

---

### E1
<details><summary>Xem đáp án</summary>

**Đáp án:**
- **`int[]`** lưu `1.000.000` số nguyên liên tiếp trong `4 MB` RAM. Khi CPU đọc `arr[0]`, **CPU Hardware Prefetcher** kéo cả **Cache Line (`64 bytes = 16 int`)** vào `L1 Cache (~1 ns)`, nên các bước tiếp theo gần như `100%` cache hit và chạy song song pipeline rất nhanh.
- **`LinkedList<Integer>`** cấp phát từng đối tượng `Node` (`~24B`) + hộp `Integer` (`16B`) nằm rải rác khắp JVM Heap. Để tới phần tử tiếp theo, CPU phải giải mã địa chỉ `node.next` rồi nhảy tới vùng nhớ ngẫu nhiên (**Pointer Chasing**), gây **L3 RAM Cache Miss (`~60–100 ns`/bước)** và chặn CPU đoán trước nhánh/dữ liệu.
- Hệ quả: `1` bước $O(n)$ trên `LinkedList` chậm gấp `50–100×` so với `1` bước trên `int[]` — đủ để **$O(n)$ trên `LinkedList` chậm hơn cả $O(n \log n)$ (`~20 × n`) trên `int[]`** khi $n = 1.000.000$!
</details>

---

### E2
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. **Lệnh `add(x)` ngay tại thời điểm đầy `4.000.000` phần tử tốn đúng $O(n)$** thật sự (phải cấp phát mảng mới `6.000.000` và copy toàn bộ `4.000.000` references sang bằng `Arrays.copyOf`).
2. **Chứng minh amortized $O(1)$:** Với hệ số nhân $r = 1.5$, khi thêm liên tiếp `n` phần tử từ rỗng, các mảng trước đó có kích thước lần lượt $\approx \frac{n}{1.5}, \frac{n}{1.5^2}, \frac{n}{1.5^3}, \dots$ Tổng số phần tử phải copy bị chặn trên bởi tổng chuỗi cấp số nhân vô hạn:
   $$\sum_{k=1}^{\infty} \frac{n}{1.5^k} = \frac{n/1.5}{1 - 1/1.5} = 2n$$
   Cộng với `n` lần ghi chính, tổng chi phí cho `n` lần `add()` là $\le 3n = O(n)$. Chia đều cho `n` lần gọi → **`O(1)` amortized** mỗi lần `add()`.
</details>

---

### E3
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. **`linkedList.get(i)` trong vòng lặp `for (int i = 0; ...)`:** Độ phức tạp là **$O(n^2)$**! Vì `LinkedList.get(i)` đi từ đầu (hoặc cuối nếu `i > n/2`) nên trung bình tốn `n/4` bước. Tổng số bước cho `n = 100.000` là $\approx \frac{n^2}{4} = \mathbf{2.5 \times 10^9}$ bước nhảy pointer!
2. **`existingArrayList.contains(id)` trong vòng lặp:** Độ phức tạp là **$O(n^2)$**! Ở worst-case (ID không tồn tại trong list), mỗi lần `contains` duyệt hết `100.000` phần tử → $100.000 \times 100.000 = \mathbf{10^{10}}$ phép gọi `String.equals()`.
</details>

---

### E4
<details><summary>Xem đáp án</summary>

**Đáp án:**
Với $n = 100.000$ và trần `< 5 × 10^6` thao tác trong `50 ms`:
1. **$O(n)$ với `HashSet`:** `100.000` phép toán `<< 5.000.000` → **Chạy kịp dư sức** (`~3–10 ms`).
2. **$O(n \log n)$ với `Arrays.sort`:** $100.000 \times \log_2(100.000) \approx 1.66 \times 10^6 < 5 \times 10^6$ → **Chạy kịp** (`~5–15 ms` cho `int[]`).
3. **$O(n^2)$:** $100.000^2 = 10^{10} \gg 5 \times 10^6$ → **Chết chắc!** (mất hàng giây đến vài chục giây, vi phạm SLA `50 ms` hàng trăm lần).
</details>

---

### E5
<details><summary>Xem đáp án</summary>

**Đáp án:**
- **`ArrayDeque`** lưu phần tử trong một **mảng vòng (`Object[]` + con trỏ `head`, `tail`)**:
  - **Bộ nhớ:** Chỉ tốn `4–8 bytes` cho mỗi slot reference (ít hơn `5×–10×` so với `Node` 24B của `LinkedList`).
  - **Cache Locality:** Phần tử nằm liên tiếp trên RAM, tận dụng tốt CPU cache line.
  - **Amortized $O(1)$:** Cả `addFirst`/`addLast`/`pollFirst`/`pollLast` đều $O(1)$ amortized (chỉ resize `2×` hiếm hoi khi đầy mảng). Vì vậy trong Java, **`ArrayDeque` thay thế `LinkedList` cho mọi bài toán Stack/Queue/BFS** (nhược điểm duy nhất là không cho phần tử `null`).
</details>

---

### E6
<details><summary>Xem đáp án</summary>

**Hướng giải:**
Khi chạy `mvn -o -B verify` và `mvn -o -B compile exec:java` trên `1.000.000` phần tử:
- `int[] sum` ($O(n)$) đạt tốc độ nhanh nhất nhờ liên tiếp trên RAM và không boxing (chỉ vài mili-giây hoặc dưới 1 ms!).
- `ArrayList<Integer> sum` ($O(n)$) chậm hơn `int[]` do dereference qua `Integer` object, nhưng vẫn rất nhanh nhờ mảng `Object[]` liên tiếp.
- **Fragmented `LinkedList<Integer>` ($O(n)$)** chậm hơn `int[]` tới hàng chục lần vì mỗi bước nhảy qua pointer trên Heap bị phân mảnh — và thậm chí có thể **chậm ngang hoặc chậm hơn cả `Arrays.sort(int[])` ($O(n \log n)$)** trên mảng `1.000.000` phần tử!
</details>

---

### E7
<details><summary>Xem đáp án</summary>

**Hướng giải:**
Khi mô phỏng công thức `newCapacity = oldCap + (oldCap >> 1)` của OpenJDK `ArrayList` từ dung lượng `10` lên `1.000.000`:
- Tổng số lần cấp phát lại mảng là **`29` lần**.
- Tổng số phần tử phải copy trong suốt 29 lần resize là **`2.430.972`** (hệ số `2.43 * n = O(n)` → trung bình mỗi lần `add()` chỉ tốn $O(1)$ amortized).
- Dùng **`new ArrayList<>(1_000_000)`** giảm số lần resize xuống **`0`** (`0` phần tử copy trung gian) và rút ngắn thời gian cấp phát lẫn áp lực GC!

**Code lời giải mẫu Pre-sized collections & đối soát $O(n)$:** xem [lab/solutions/PreSizedCollectionsSolution.java](./lab/solutions/PreSizedCollectionsSolution.java).
</details>

---

### E8
<details><summary>Xem đáp án</summary>

**Hướng giải:**
- Chuyển `cancelledOrders` vào **`HashSet.newHashSet(cancelledOrders.size())`** (hoặc `new HashSet<>((int)(size/0.75f)+1)` trên Java 7–18) mất $O(m)$, rồi duyệt `allOrders` bằng `ArrayList` pre-sized kiểm tra `!cancelledSet.contains(id)` mất $O(n)$.
- Tổng thời gian giảm từ hàng trăm mili-giây / vài giây ($50.000 \times 20.000$) xuống còn **`1–3 ms`** (nhanh hơn hàng trăm lần!).
</details>

---

### E9
<details><summary>Xem đáp án</summary>

<details open><summary>Phân tích Ticket Production: Batch job đối soát chạy 45 phút trên JBoss Java 7</summary>

**1. 3 lỗi Big-O / Cấu trúc dữ liệu gây chậm `45 phút`:**
1. **`bankRefs.get(i)` trên `LinkedList` ($O(n^2)$):** `200.000` vòng lặp × trung bình `50.000` bước nhảy pointer = **`10 tỷ` bước pointer chasing**!
2. **Vòng lặp tìm `dbTxs` tuyến tính ($O(n^2)$ thứ 2!):** Với mỗi `ref`, lại quét qua `LinkedList<TxRecord>` kích thước `200.000` → thêm **`~20 tỷ` phép so sánh chuỗi & pointer chasing**! *(Tổng `~30 tỷ` thao tác nặng cache miss → giải thích vì sao `1.000` dòng UAT chỉ mất `0.2s` nhưng tăng `200×` input lại tăng thời gian lên $200^2 = 40.000\times$!)*
3. **Dùng `LinkedList` không cần thiết & không pre-size:** Tốn gấp ~`5×` RAM cho `400.000` node wrapper làm đầy Heap gây Full GC liên tục.

**2. Code sửa chuẩn cho Java 7 / JBoss AS 7.1.1 (giảm xuống $O(n)$):**
```java
// Dùng ArrayList có dung lượng ước lượng thay cho LinkedList
List<String> bankRefs = new ArrayList<String>(200_000); // load từ CSV
List<TxRecord> dbTxs = loadFromDbAsArrayList();       // query trả về ArrayList

// Pre-size HashSet tránh resize trên Java 7: ceil(200_000 / 0.75) + 1 = 266_668
Set<String> dbRefSet = new HashSet<String>((int) (dbTxs.size() / 0.75f) + 1);
for (TxRecord tx : dbTxs) {
    dbRefSet.add(tx.getRef());
}

List<String> missingInDb = new ArrayList<String>();
for (String ref : bankRefs) { // Duyệt tuần tự trên ArrayList -> cache-friendly!
    if (!dbRefSet.contains(ref)) { // O(1) lookup!
        missingInDb.add(ref);
    }
}
```

**3. Thời gian chạy mới sau khi sửa:**
- Chỉ còn `~200.000` lần `HashSet.add` + `~200.000` lần `HashSet.contains` = **`~400.000` thao tác $O(1)$** thay vì `~30 tỷ` bước!
- Tổng thời gian xử lý phần đối soát trong RAM giảm từ **`45 phút` xuống còn `~50–150 ms`** (nhanh hơn **hơn 15.000 lần**)!

</details>
</details>

---

## Self-check (từ README §9) — đáp án gợi ý
<details><summary>Xem</summary>

1. **$O(n)$ `LinkedList` vs $O(n \log n)$ `int[]`:** `int[]` tận dụng `64-byte` Cache Line & CPU Hardware Prefetcher (`L1 ~1 ns`, không boxing); `LinkedList` rải `Node` (`24B`) + `Integer` (`16B`) khắp Heap gây Pointer Chasing & RAM Cache Miss (`~60–100 ns`/bước) chậm gấp `50–100×` trên mỗi phần tử → đủ bù lại hệ số $\log_2(10^6) \approx 20$.
2. **`ArrayList.add` amortized $O(1)$:** Bản thân lần gọi gây đầy mảng tốn $O(n)$ để `Arrays.copyOf` sang mảng `1.5×`. Nhưng vì dung lượng tăng theo cấp số nhân, tổng phần tử copy qua `n` lần thêm hội tụ về $2n = O(n)$ → chia đều cho `n` lần gọi được $O(1)$ amortized.
3. **`ArrayDeque` vs `LinkedList` cho Queue:** `ArrayDeque` dùng mảng vòng liên tiếp nên tiết kiệm `5×–10×` RAM, không rác `Node` wrapper cho GC, và cache-friendly hơn hẳn.
4. **Ngưỡng $n$ cho `< 100 ms` trên 1 core Java:** $O(n^2) \lesssim 3.000\text{–}10.000$; $O(n \log n) \lesssim 10^6$; $O(n) \lesssim 10^7\text{–}10^8$ (mảng liên tiếp).
5. **Bẫy microbenchmark Java:** Thiếu Warmup khiến đo lúc JVM còn chạy Interpreter thay vì C2 JIT; thiếu Blackhole (không dùng kết quả tính) khiến C2 JIT xóa luôn cả vòng lặp (*Dead-Code Elimination*). Phải dùng JMH hoặc harness có đủ warmup + trả checksum kết quả.
</details>
