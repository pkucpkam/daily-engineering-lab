# DSA-02 · Đáp án — HashMap internals

> ⚠️ **Chỉ mở sau sau khi đã tự làm.** Đọc đáp án mà chưa tự làm = mất 80% giá trị bài tập.
> Mỗi đáp án được bọc trong `<details>` để tránh lộ khi lướt.

---

### E1
<details><summary>Xem đáp án</summary>

**Đáp án:**
- Khi gọi `new HashMap<>(100)`, hàm `tableSizeFor(100)` làm tròn lên lũy thừa của `2` gần nhất $\ge 100$ là **`capacity = 128`**.
- Với `loadFactor` mặc định `0.75f`, ngưỡng kích hoạt resize là:
  $$\texttt{threshold} = 128 \times 0.75 = \mathbf{96}$$
- Do đó ngay khi bạn thêm **phần tử thứ `97` (`97 > 96`)**, `HashMap` buộc phải **`resize()` lên `256`**!
- **Khởi tạo đúng để chứa `100` phần tử không resize:**
  - Java 19+: `HashMap.newHashMap(100)` (JDK tự tính `ceil(100 / 0.75) = 134`, `tableSizeFor(134) = 256`).
  - Java 7–18: `new HashMap<>((int) Math.ceil(100 / 0.75) + 1)`.
</details>

---

### E2
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. **Vì sao XOR `(h >>> 16)`:** Phép tính chỉ số bucket `(n - 1) & hash` chỉ xét các **bit thấp** (vd 4 bit cuối với `n = 16`). Nếu `hashCode()` của key chỉ biến thiên ở các bit cao, nhiều key khác nhau sẽ rơi cùng 1 bucket. Trộn 16 bit cao xuống 16 bit thấp giúp giảm đụng độ đáng kể với chi phí chỉ 2 phép toán bit.
2. **KHÔNG lập tức treeify khi `table.length == 32`:** Java 8 yêu cầu đồng thời **2 điều kiện**: độ dài chuỗi $\ge 8$ (`TREEIFY_THRESHOLD`) **VÀ** `table.length >= 64` (`MIN_TREEIFY_CAPACITY`). Nếu `table.length == 32`, `treeifyBin()` sẽ gọi **`resize()` lên `64`** để dàn trải lại phần tử trước chứ chưa đổi sang cây Đỏ-Đen!
</details>

---

### E3
<details><summary>Xem đáp án</summary>

**Đáp án:**
Khi `hashCode()` luôn trả về `1`, toàn bộ `10.000` phần tử đều bị ném vào **cùng 1 bucket duy nhất**:
1. **Java 7:** Bucket là danh sách liên kết đơn thuần → mọi `get()` và `put()` (phải kiểm tra trùng key hết chuỗi) tốn **$O(n)$**, tổng chi phí chèn `10.000` key là **$O(n^2)$**!
2. **Java 8+ với key `Comparable`:** Sau khi `table.length >= 64` và chuỗi đạt `8`, bucket chuyển thành **Red-Black Tree** sắp xếp theo `compareTo()` → `get`/`put` còn **$O(\log n)$** (nhưng vẫn chậm hơn rất nhiều so với $O(1)$ bình thường và tốn gấp đôi RAM `TreeNode`).
3. **Java 8+ với key KHÔNG `Comparable`:** Vì không thể so sánh thứ tự khi tạo object key mới lúc `get()` (`System.identityHashCode` của object mới khác object đã lưu trong cây!), `TreeNode#find` không định hướng trái/phải được mà phải duyệt tìm ở cả hai nhánh con → hiệu năng lookup tụt gần về **$O(n)$**!
</details>

---

### E4
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. **Override `equals()` quên `hashCode()`:** Hai instance `new CustomerKey("C1")` và `new CustomerKey("C1")` bằng nhau theo `equals()`, nhưng lại dùng `Object.hashCode()` mặc định khác nhau → rơi vào **2 bucket khác nhau**! Kết quả: `map.get(new CustomerKey("C1"))` trả về **`null`**, còn `map.put(...)` chèn thành nhiều bản sao trùng lặp!
2. **Sửa field tham gia `hashCode()` sau khi đã `put`:** `Node` ban đầu đang nằm ở bucket ứng với `hashCode` cũ. Khi bạn đổi `key.setRegion("SOUTH")`, lời gọi `map.get(key)` tính `hashCode` mới và đi tìm ở **bucket mới (trống rỗng)** → trả về **`null`**, nhưng entry cũ vẫn nằm kẹt mãi trong bucket cũ khiến **`map.size() == 1`** không thể giải phóng (Memory Leak)!
</details>

---

### E5
<details><summary>Xem đáp án</summary>

**Đáp án:**
- Trong **Java 7**, `HashMap#transfer` duyệt từng phần tử trong bucket cũ và dùng **Head Insertion** chèn vào đầu bucket mới — làm **đảo ngược thứ tự** `A -> B` thành `B -> A`.
- Khi **2 thread cùng `resize()` đồng thời**: Thread 1 dừng ngay sau khi giữ `e = A, next = B`; Thread 2 chạy xong làm `B.next = A`; Thread 1 chạy tiếp lại nối `A.next = B` → sinh ra **chu trình vòng tròn `A -> B -> A`**! Mọi lệnh `get()` sau đó rơi vào bucket này đều chạy vòng lặp vô hạn ăn 100% CPU.
- **Java 8+** chuyển sang **Tail Insertion** (giữ nguyên thứ tự node bằng `loHead`/`loTail`) nên triệt tiêu bug vòng lặp resize — **nhưng `HashMap` vẫn không thread-safe** (vẫn dính race condition mất dữ liệu khi 2 thread ghi cùng bucket trống, `size` sai hoặc nhìn thấy dữ liệu chưa hoàn chỉnh). Phải dùng **`ConcurrentHashMap`**.
</details>

---

### E6
<details><summary>Xem đáp án</summary>

**Hướng giải:**
Khi chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`:
- `SimpleHashMap` tự cài đặt dùng `(cap - 1) & hash(key)` và resize `2×` mỗi khi `size > capacity * 0.75`.
- Với `10.000` key chuẩn, dung lượng cuối cùng đạt `16.384` buckets (`threshold = 12.288`) và độ dài chuỗi lớn nhất trên 1 bucket thường chỉ `<= 6` (xác suất vượt `10` gần như bằng `0`).
</details>

---

### E7
<details><summary>Xem đáp án</summary>

**Hướng giải:**
Khi chạy `HashMapPitfallsDemo` đo `put + get` trên `15.000` phần tử:
- `GoodHashKey`: hoàn tất chỉ trong vài mili-giây (`O(1)`).
- `ConstantComparableKey`: chậm hơn `GoodHashKey` nhiều lần do tất cả dồn vào 1 Red-Black Tree ($O(\log n)$ + chi phí cấp phát `TreeNode`).
- `ConstantNonComparableKey`: chậm hơn `GoodHashKey` **hàng trăm đến hàng nghìn lần** vì tạo key mới khi `get()` không phân nhánh bằng `compareTo()` được trên cây tie-break!
</details>

---

### E8
<details><summary>Xem đáp án</summary>

**Hướng giải:**
- Trong kịch bản Mutable Key: thay đổi field sau `put()` làm `get(mutableKey)` trả về `null` dù `map.size()` vẫn là `1`.
- Trong `simulateJava7ResizeCycle()`: tái hiện chính xác 2 con trỏ của Thread 1 và Thread 2 khi head insertion đan xen tạo thành `A.next == B` và `B.next == A`, được phát hiện tức thì bằng Floyd's Tortoise & Hare (`hasCycle == true`).

**Code mẫu Immutable Key chuẩn:** xem [lab/solutions/ImmutableMapKeySolution.java](./lab/solutions/ImmutableMapKeySolution.java).
</details>

---

### E9
<details><summary>Xem đáp án</summary>

<details open><summary>Phân tích Ticket Production: JBoss Java 7 treo 400% CPU + Leak Heap</summary>

**1. Cơ chế gây ra 2 triệu chứng:**
1. **Leak Heap & entry trùng lặp:** Lớp `FeeKey` chỉ override `equals()` mà **quên `hashCode()`** (và còn bị mutate!). Mỗi lần tạo `new FeeKey(p, t, c)` đều có `Object.hashCode()` khác nhau → `FEE_CACHE.get(key)` **luôn miss** và đẩy thêm một entry trùng lặp nữa vào `FEE_CACHE`!
2. **CPU 400% tại `HashMap.getEntry` / `transfer`:** Vì `FEE_CACHE` là `HashMap` tĩnh dùng chung không đồng bộ giữa các HTTP worker threads trên **Java `1.7.0_80`**, khi hàng loạt miss kích hoạt `resize()` đồng thời ở 2+ threads, kỹ thuật **Head Insertion** của Java 7 tạo ra **chu trình `A -> B -> A`** trong bucket! Bất kỳ thread nào gọi `FEE_CACHE.get(...)` vào bucket đó đều sa vào **vòng lặp vô hạn** ăn 100% CPU từng core một cho tới khi cạn sạch cả 4 vCPU!

**2. Code sửa chuẩn cho Java 7 / JBoss AS 7.1.1:**
- Làm `FeeKey` hoàn toàn **immutable** (`final class`, mọi field `private final`, bỏ setter, override chuẩn cả `equals()` lẫn `hashCode()`).
- Dùng **`ConcurrentHashMap`** (hoặc Guava/Caffeine `Cache` có `maximumSize` & TTL để **KHÔNG bao giờ phình vô hạn gây OOM**!).
```java
public final class FeeKey {
    private final String productCode;
    private final int tier;
    private final String currency;
    // constructor + getters (NO setters!) + IDE/Objects equals & hashCode chuẩn
}

// Dùng ConcurrentHashMap + giới hạn kích thước (hoặc Guava CacheBuilder trên Java 7)
private static final ConcurrentMap<FeeKey, BigDecimal> FEE_CACHE = new ConcurrentHashMap<FeeKey, BigDecimal>();
```
- *(Trên Java 21, `public record FeeKey(String productCode, int tier, String currency) {}` tự động là immutable + sinh `equals`/`hashCode` chuẩn trong 1 dòng!)*

</details>
</details>

---

## Self-check (từ README §9) — đáp án gợi ý
<details><summary>Xem</summary>

1. **Đường đi `put(key, val)` Java 8+:** Tính `hash = (h = key.hashCode()) ^ (h >>> 16)` → `idx = (n - 1) & hash`. Nếu trống tạo `Node`; nếu đụng độ duyệt chain kiểm tra `equals()` để ghi đè hoặc nối cuối; nếu `chain >= 8` gọi `treeifyBin()` (chỉ chuyển sang cây Đỏ-Đen nếu `table.length >= 64`, nếu `< 64` thì `resize()`); cuối cùng nếu `++size > threshold` thì `resize()` `2×`.
2. **Tại sao size là $2^k$ & XOR `h >>> 16`:** Size $2^k$ cho phép dùng bitmask `(n - 1) & hash` thay cho phép chia `%` chậm và tách nhanh bucket khi resize qua 1 bit `(e.hash & oldCap)`. XOR 16 bit cao xuống thấp tránh mất entropy của bit cao khi `n` còn nhỏ.
3. **`hashCode()` hằng số (`42`):** Toàn bộ key dồn vào 1 bucket duy nhất. Java 7: `get`/`put` tụt xuống $O(n)$ (`put` N key mất $O(n^2)$). Java 8+: thành Red-Black Tree nếu `table.length >= 64` — `Comparable` đạt $O(\log n)$, còn không `Comparable` bị gãy tìm kiếm nhị phân khi lookup bằng key mới nên vẫn chậm gần $O(n)$!
4. **Java 7 concurrent `resize()` infinite loop:** Java 7 `transfer()` dùng Head Insertion đảo ngược thứ tự `A -> B` thành `B -> A`. 2 thread cùng resize đan xen con trỏ tạo thành vòng tròn `A -> B -> A` → `get()` lặp vô tận ăn `100%` CPU. Java 8 dùng Tail Insertion nên hết vòng lặp, nhưng vẫn mất dữ liệu nếu không dùng `ConcurrentHashMap`.
5. **Pre-size chứa `1.000` key không resize:** Java 19+: `HashMap.newHashMap(1000)`. Java 7–18: `new HashMap<>((int) Math.ceil(1000 / 0.75) + 1)` (vì `new HashMap<>(1000)` cho `capacity = 1024`, `threshold = 768` vẫn bị resize ở phần tử `769`!).
</details>
