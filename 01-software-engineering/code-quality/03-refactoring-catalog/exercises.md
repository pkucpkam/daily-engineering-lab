# Bài tập · CQ-03 · Refactoring catalog & code smells

Làm bài trước khi mở [solutions.md](solutions.md). Thực hành trên [lab/](lab/README.md).

---

## Level 1 · Nhận diện Code Smell & Catalog (4 bài)

### Bài 1.1 · Gọi tên đúng Code Smell & Phép Refactor tương ứng
Với mỗi đoạn mô tả dưới đây, chọn đúng tên **Code Smell** (*Long Method*, *Primitive Obsession*, *Feature Envy*, *Data Clumps*, *Shotgun Surgery*, *Divergent Change*) và **1 kỹ thuật Catalog** thích hợp:
1. Mỗi khi thêm một loại thông báo mới (`Zalo`), phải vào sửa 5 class khác nhau (`NotificationValidator`, `NotificationFormatter`, `AuditLogger`, `QuotaCounter`, `RetryPolicy`).
2. Trong hàm `OrderController`, ta thấy 7 dòng liên tiếp chỉ toàn gọi `order.getCustomer().getTier()`, `order.getCustomer().getJoinedYear()`, `order.getCustomer().getPoints()` để tự tính điểm thưởng hộ cho `Customer`.
3. Ba hàm khác nhau trong `ReportService` đều nhận 4 tham số giống hệt nhau: `(LocalDate startDate, LocalDate endDate, String timezone, boolean includeWeekend)`.
4. Dùng `long amount` và `String currency` tách rời khắp 15 API khiến một chỗ vô tình cộng VND với USD mà compiler không báo lỗi.

### Bài 1.2 · Refactor vs Rewrite
Đồng nghiệp đề xuất: *"Code tính cước phí cũ bẩn lắm rồi, đừng refactor từng chút nữa, mình tạo package `v2` code mới hoàn toàn trong 3 tuần rồi cắt sang 1 lần"*. Nêu 3 rủi ro lớn nhất của cách **Big Bang Rewrite** này so với **Refactoring từng bước nhỏ**.

### Bài 1.3 · Hai chiếc mũ của Kent Beck
Tại sao trong khi tái cấu trúc hàm legacy dài, người ta nghiêm cấm việc vừa gộp/tách hàm vừa tranh thủ "sửa luôn một cái bug nhỏ vừa phát hiện"?

### Bài 1.4 · Khi nào KHÔNG nên refactor?
Đưa ra **3 tình huống thực tế** trong dự án công ty mà việc từ chối refactor lại là quyết định kỹ thuật đúng đắn hơn.

---

## Level 2 · Thực hành đọc & kiểm chứng Kata Gilded Rose (3 bài)

### Bài 2.1 · Chạy ma trận Characterization Test bảo vệ Gilded Rose
Chạy `mvn -o test` trong `03-refactoring-catalog/lab/`. Đọc [`GildedRoseCharacterizationTest.java`](lab/src/test/java/lab/cq/refactor/GildedRoseCharacterizationTest.java) và trả lời:
1. Tại sao bộ test lại quét tới **122 tổ hợp** qua **15 ngày liên tiếp** thay vì viết 4 test case đơn giản cho mỗi loại item?
2. Thử quan sát chuyện gì xảy ra với `"Backstage passes to a TAFKAL80ETC concert"` ở 3 mốc `sellIn = 11`, `sellIn = 6`, và `sellIn = 0` (chỉ ra dòng code tương ứng trong [`GildedRoseRefactored.java`](lab/src/main/java/lab/cq/refactor/after/GildedRoseRefactored.java)).

### Bài 2.2 · Khử *Switch Statement* sang Đa hình trên Java 7
Trong [`GildedRoseRefactored.java`](lab/src/main/java/lab/cq/refactor/after/GildedRoseRefactored.java), bài lab dùng Java 21 lambda + `Map<String, ItemUpdater>`. Nếu bạn phải làm đúng bài refactor **Replace Conditional with Polymorphism** này trên codebase **Java 7** (không có lambda và `Map.of`), cấu trúc class/map sẽ viết lại thế nào? Viết phác thảo ngắn (10–15 dòng).

### Bài 2.3 · Áp dụng Introduce Parameter Object vào Legacy Search API
Cho method legacy Java sau đang bị nhiều caller truyền nhầm thứ tự `start`/`end` và `fromEmail`/`toEmail`:

```java
public List<AuditLog> findLogs(String actorId, String actionType,
                               LocalDate start, LocalDate end,
                               int page, int size);
```

Áp dụng **Introduce Parameter Object** để giảm số tham số xuống còn 1–2 object sạch, bảo vệ invariant `start <= end` và `size` hợp lệ ngay tại constructor.

<details>
<summary>Gợi ý</summary>

Tách `PageSpec(int page, int size)` và `AuditLogFilter(String actorId, String actionType, LocalDate start, LocalDate end)` kèm compact constructor kiểm tra `!start.isAfter(end)` và `size > 0 && size <= 500`.

</details>

---

## Level 3 · Mở rộng tính năng mới trên nền đã Refactor (1 bài)

### Bài 3.1 · Thêm loại vật phẩm `"Conjured Mana Cake"` đúng mũ Add Feature
Đặc tả mới của game Gilded Rose:
- Vật phẩm `"Conjured Mana Cake"` bị giảm `quality` **nhanh gấp đôi** vật phẩm bình thường (giảm `2` mỗi ngày trước khi hết hạn; giảm `4` mỗi ngày sau khi `sellIn < 0`, không bao giờ tụt dưới `0`).

**Yêu cầu**:
1. Cài đặt (hoặc đối chiếu [`lab/solutions/ConjuredGildedRoseExtension.java`](lab/solutions/ConjuredGildedRoseExtension.java)) chiến lược `ItemUpdater` cho `"Conjured Mana Cake"` và truyền qua map `customUpdaters` của `GildedRoseRefactored` mà **không sửa file `GildedRoseRefactored.java`**.
2. Chạy `java -cp target\classes solutions\ConjuredGildedRoseExtension.java` với initial `(sellIn = 2, quality = 10)` qua 4 ngày liên tiếp và đối chiếu từng ngày với vật phẩm thường `(sellIn = 2, quality = 10)`.
3. Chạy lại `mvn -o test` để chắc chắn toàn bộ 122 tổ hợp cũ vẫn xanh 100%.

**Tiêu chí đạt**: Day 1..4 in đúng chuỗi giảm `10 -> 8 -> 6 -> 2 -> 0` cho Conjured và `10 -> 9 -> 8 -> 6 -> 4` cho normal, không sửa `GildedRoseRefactored.java` và regression test giữ nguyên 100%.
