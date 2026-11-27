# DSA-02 · Bài tập — HashMap internals

> Làm theo thứ tự. **Tự làm trước, chỉ mở hint khi đã thử ≥ 15 phút.** Đáp án ở [solutions.md](./solutions.md) — chỉ mở sau khi đã có câu trả lời của riêng bạn.
> Ghi thời gian làm từng bài vào `my-notes.md` để theo dõi tiến bộ.

| Cấp | Mục đích | Số lượng | Thời gian gợi ý |
| :--- | :--- | :---: | :---: |
| 🟢 L1 — Recall | Nắm chắc hash perturbation, treeify, pre-size, contract, Java 7 cycle | 5 | 20' |
| 🟡 L2 — Apply | Tự cài `SimpleHashMap`, đo `hashCode()` xấu, tái hiện Mutable Key & Java 7 cycle | 3 | 75' |
| 🔴 L3 — Deep | Ticket Production: JBoss Java 7 treo 100% CPU sau khi deploy cache `HashMap` + Leak RAM | 1 | 90' |

---

## 🟢 L1 — Recall

### E1. Vì sao truyền `new HashMap<>(100)` để chứa `100` phần tử vẫn bị `resize()`?
Tính chính xác `capacity` thực sự (`table.length`) và `threshold` khi bạn gọi `new HashMap<>(100)`. Ở phần tử thứ bao nhiêu thì `HashMap` kích hoạt `resize()` lên `256`? Viết lại câu lệnh khởi tạo đúng trên Java 7 và Java 19+.

---

### E2. Vai trò của `h ^ (h >>> 16)` và 2 điều kiện của Treeify trong Java 8
1. Tại sao OpenJDK không lấy thẳng `key.hashCode() & (n - 1)` mà phải XOR thêm `(h >>> 16)`?
2. Khi một bucket vừa có phần tử thứ `8`, `HashMap` Java 8 có **lập tức** biến bucket đó thành Cây Đỏ-Đen (`TreeNode`) không nếu lúc đó `table.length == 32`? Giải thích.

---

### E3. Chuyện gì xảy ra khi `hashCode()` luôn trả về hằng số (`return 1;`)?
So sánh ảnh hưởng về độ phức tạp thời gian của `HashMap` với `10.000` key có `hashCode() == 1` trong 3 trường hợp:
1. Java 7.
2. Java 8+ với key có `implements Comparable` (vd `String`/`GoodComparableBadHash`).
3. Java 8+ với domain key **KHÔNG `implements Comparable`**.

---

### E4. 2 lỗi kinh điển với `equals()` và `hashCode()` trên domain Key
1. Nếu bạn override `equals()` cho class `CustomerKey` nhưng **quên** override `hashCode()`, điều gì xảy ra khi dùng `CustomerKey` làm key trong `HashMap`?
2. Nếu bạn override đầy đủ cả hai, `put(key, val)` thành công, nhưng sau đó code nghiệp vụ gọi `key.setRegion("SOUTH")` (trong khi `region` tham gia tính `hashCode()`), điều gì xảy ra khi gọi `map.get(key)` và `map.size()`?

---

### E5. Cơ chế gây vòng lặp vô hạn khi `resize()` đồng thời ở Java 7 `HashMap`
Giải thích ngắn gọn vai trò của **Head Insertion** trong `HashMap#transfer` của Java 7 khi 2 thread cùng `resize()` trên 1 bucket đang chứa `A -> B -> null`, và giải thích vì sao Java 8 khắc phục được bug vòng lặp này nhưng vẫn **không** an toàn cho đa luồng.

---

## 🟡 L2 — Apply

### E6. Tự cài `SimpleHashMap<K, V>` & Kiểm chứng phân bố Bucket + Resize `2×`
**Bối cảnh:** Dùng [lab/](./lab/README.md) chạy `HashMapLabTest` và `HashMapLabRunner`.
**Yêu cầu:**
1. Đọc `lab/src/main/java/lab/dsa/hashmap/SimpleHashMap.java` (cài đặt đầy đủ `hash`, index bitmask `(cap - 1) & h`, chaining `Node`, `resize` `2×` khi `size > threshold`, `get`/`put`/`remove`).
2. Chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`.
3. Kiểm chứng với `10.000` key chuẩn: `SimpleHashMap` tự động resize lên `16.384` buckets và giữ `maxBucketChainLength <= 10`!

**Tiêu chí hoàn thành:**
- [ ] `mvn -o -B verify` pass 100%.
- [ ] Hiểu rõ từng dòng hàm `resize()` và tính chỉ số `(cap - 1) & hash` trong `SimpleHashMap`.

<details><summary>💡 Hint</summary>

Để ý hàm `rehashIndex`: sau khi mảng mới tăng gấp đôi lên `newCap`, mọi `Node` cũ được gài vào bucket mới tại `(newCap - 1) & node.hash`.
</details>

---

### E7. Đo hiệu năng `java.util.HashMap` khi `hashCode()` tốt vs hằng số (`return 42`)
**Bối cảnh:** Mở `lab/src/main/java/lab/dsa/hashmap/HashMapPitfallsDemo.java`.
**Yêu cầu:**
1. Đo thời gian `put + get` `15.000` phần tử vào `HashMap` với 3 loại key:
   - `GoodHashKey` (hash phân bố đều)
   - `ConstantComparableKey` (`hashCode() == 42`, có `Comparable`)
   - `ConstantNonComparableKey` (`hashCode() == 42`, **không** `Comparable`)
2. Ghi lại bảng so sánh ms và tỷ lệ chậm đi giữa 3 loại key.

**Tiêu chí hoàn thành:**
- [ ] Chứng minh thực nghiệm `ConstantNonComparableKey` chậm hơn `GoodHashKey` hàng trăm lần và chậm hơn nhiều so với `ConstantComparableKey`.

<details><summary>💡 Hint</summary>

Trong `HashMap.java` của OpenJDK, khi key `Comparable` bị dồn chung 1 bucket, `TreeNode` vẫn dùng `compareTo()` phân chia nhị phân cân bằng gọn gàng trong $O(\log n)$; còn khi key không `Comparable`, `tieBreakOrder` lúc lookup key mới không so khớp được bằng `identityHashCode` của instance cũ → buộc phải quét đệ quy cả cây con trái và phải!
</details>

---

### E8. Kiểm chứng Mutable Key Leak & Chu trình Java 7 Head-Insertion Simulator
**Bối cảnh:** Tiếp tục chạy `HashMapPitfallsDemo.java` và đối chiếu `lab/solutions/ImmutableMapKeySolution.java`.
**Yêu cầu:**
1. Chạy kịch bản Mutable Key: `put(mutableKey, "VIP")`, sau đó đổi field `mutableKey.setCode("CHANGED")` → kiểm chứng `map.get(mutableKey) == null` trong khi `map.size() == 1`!
2. Chạy `simulateJava7ResizeCycle()` trong `HashMapPitfallsDemo`: mô phỏng đúng 4 bước xen kẽ giữa Thread 1 & Thread 2 khi `transfer()` dùng head insertion trên `A -> B`, sau đó kiểm chứng bằng **thuật toán Floyd (`slow` / `fast` pointer)** rằng chuỗi kết quả đã dính **cycle (`hasCycle == true`)**!

**Tiêu chí hoàn thành:**
- [ ] Pass test chứng minh Mutable Key bị thất lạc trong map và mô phỏng Java 7 sinh ra chu trình thực sự phát hiện được bởi Floyd.

<details><summary>💡 Hint</summary>

Nếu một danh sách liên kết không có vòng lặp, thuật toán 2 con trỏ `slow = slow.next`, `fast = fast.next.next` sẽ luôn gặp `null`; ngược lại nếu có cycle `A -> B -> A`, `slow` và `fast` chắc chắn đụng nhau (`slow == fast`)!
</details>

---

## 🔴 L3 — Deep

### E9. Ticket Production: JBoss Java 7 bất ngờ ăn `100%` CPU toàn bộ core + Heap Leak dần mỗi ngày
**Tình huống:**
> Một ứng dụng legacy **JBoss AS 7.1.1 (Java `1.7.0_80`, 4 vCPU)** triển khai một bộ cache tự viết cho bảng tra cứu phí:
> ```java
> public class FeeLookupService {
>     private static final Map<FeeKey, BigDecimal> FEE_CACHE = new HashMap<FeeKey, BigDecimal>();
>
>     public BigDecimal getOrLoad(FeeKey key) {
>         BigDecimal val = FEE_CACHE.get(key);
>         if (val == null) {
>             val = queryDb(key);
>             FEE_CACHE.put(key, val);
>         }
>         return val;
>     }
> }
> ```
> Ngoài ra, lớp `FeeKey` có 3 field `productCode`, `tier`, `currency` (trong đó dev chỉ override `equals()` nhưng quên `hashCode()`, và một chỗ khác lại mutate `key.setTier(...)` trước khi tái sử dụng object `FeeKey`!).
> Sau 4 giờ chạy cao điểm sáng thứ Hai:
> - CPU server nhảy vọt lên **400% (max 4/4 cores)**, toàn bộ HTTP request đứng im!
> - Chạy `kill -3 <PID>` lấy Thread Dump thấy hàng chục worker thread `RUNNABLE` đứng mãi tại:
>   `at java.util.HashMap.getEntry(HashMap.java:465)` / `at java.util.HashMap.transfer(HashMap.java:595)`.
> - Heap dump cho thấy `FEE_CACHE` phình lên hàng trăm nghìn entry trùng lặp cùng bộ `(productCode, tier, currency)`.

**Yêu cầu:**
1. Giải thích **cơ chế bên trong** gây ra cả 2 triệu chứng: **CPU 400% tại `HashMap.getEntry`** VÀ **Entry trùng lặp phình Heap**.
2. Viết lại thiết kế & code đúng chuẩn cho **Java 7 / JBoss AS 7.1.1** (và bản tương đương Java 21 dùng `record`) giải quyết triệt để mọi lỗi trên (gồm thread-safety, `equals`/`hashCode`, immutability, và giới hạn kích thước cache tránh `OutOfMemoryError`).

**Tiêu chí hoàn thành:**
- [ ] Chỉ ra đủ 3 nguyên nhân: `HashMap` không thread-safe trên Java 7 tạo cycle lúc `transfer()`, thiếu `hashCode()` nên mỗi `new FeeKey(...)` là 1 entry mới làm phình Heap, và mutate key gây thất lạc bucket.
- [ ] Đưa ra code `ConcurrentHashMap` + immutable `FeeKey` chuẩn `equals`/`hashCode` + kiểm soát dung lượng cache.

<details><summary>💡 Hint</summary>

1. Quên override `hashCode()` làm `FEE_CACHE.get(new FeeKey("A", 1, "VND"))` KHÔNG BAO GIỜ hit cache vì mỗi instance mới nhận `Object.hashCode()` khác nhau → mỗi request đều `miss` và gọi `FEE_CACHE.put(...)` nhét thêm 1 bản sao nữa vào `FEE_CACHE`, vừa leak Heap vừa nhanh chóng kích hoạt `resize()` liên tục!
2. Đúng lúc nhiều thread cùng đẩy `FEE_CACHE` vượt `threshold` và gọi `resize()`/`transfer()` đồng thời trên **Java 7**, head-insertion tạo chu trình vòng tròn trong bucket → toàn bộ 4 core CPU sa vào vòng lặp vô hạn tại `HashMap.getEntry`!
</details>
