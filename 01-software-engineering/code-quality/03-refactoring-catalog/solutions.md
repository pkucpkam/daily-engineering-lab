# Đáp án · CQ-03 · Refactoring catalog & code smells

## Đáp án Bài tập

### Level 1

#### Bài 1.1 · Gọi tên Smell & Catalog tương ứng
1. Sửa 1 loại mới phải chạm 5 class khác nhau → **Shotgun Surgery** → Phép chữa: **Move Method / Move Field** (hoặc Inline/gom vào một Deep Module hoặc Strategy theo từng kênh thông báo).
2. `OrderController` tự tính hộ từ chuỗi getter của `Customer` → **Feature Envy** → Phép chữa: **Move Method** (đưa hàm `calculatePoints()` về bên trong `Customer`).
3. Bốn tham số đi thành chùm khắp 3 hàm → **Data Clumps** (+ *Long Parameter List*) → Phép chữa: **Introduce Parameter Object** (`record ReportWindow(LocalDate startDate, LocalDate endDate, String timezone, boolean includeWeekend)`).
4. `long amount` + `String currency` đi rời nhau → **Primitive Obsession** → Phép chữa: **Replace Primitive with Object** (`record Money(long amount, Currency currency)` kiểm tra khác currency khi cộng).

#### Bài 1.2 · 3 rủi ro lớn nhất của Big Bang Rewrite
1. **Bỏ sót nghiệp vụ ngầm / edge-case lịch sử**: Code cũ chứa những ngoại lệ được vá qua 5 năm mà không ai nhớ hết; bản viết lại dễ tái tạo đúng những bug đã từng được sửa trong quá khứ.
2. **Mục tiêu di động (*Moving Target*)**: Trong 3 tuần/3 tháng team rewrite bản v2, đội khác vẫn phải vá hotfix và thêm tính năng lên v1 → v2 mãi mãi chạy đuổi theo v1 không khớp nhau nổi.
3. **Đóng băng giá trị & rủi ro ngày cắt chuyển (Cutover risk)**: Không kiểm chứng được từng phần trên đường đi; đến lúc chuyển 100% traffic sang v2 mới phát hiện lệch dữ liệu hàng loạt.

#### Bài 1.3 · Quy tắc "Hai chiếc mũ"
Khi vừa refactor vừa sửa hành vi/bug trong cùng một bước:
- Nếu test cũ đỏ, bạn **không biết** nó đỏ vì mình làm gãy cấu trúc hay vì mình vừa sửa hành vi chủ ý!
- Khi tách 2 mũ rõ ràng: ở bước Refactor, mọi test cũ PHẢI xanh nguyên vẹn (được phép `git reset` nếu lỡ sai không thương tiếc); xong rồi mới chuyển mũ viết test cho bugfix/feature.

#### Bài 1.4 · 3 tình huống KHÔNG nên refactor
1. **Dead/Retiring code**: Module sắp được cho nghỉ hưu trong vài tuần tới (chi phí refactor lớn hơn giá trị sử dụng còn lại).
2. **Code ổn định lâu năm ít đụng tới**: Tuy hơi rườm rà nhưng chạy đúng nhiều năm và roadmap không yêu cầu mở rộng hay sửa vào file đó.
3. **Khủng hoảng trước deadline / Production outage không có test**: Khi chưa có Characterization Test bảo vệ, ưu tiên vá điểm nhỏ tối thiểu hoặc bọc rào bên ngoài để bảo vệ khách hàng trước, lên lịch viết test + refactor sau.

---

### Level 2

#### Bài 2.1 · Tại sao Gilded Rose test tới 122 tổ hợp x 15 ngày?
1. Gilded Rose có nhiều điều kiện biên giao thoa nhau giữa `sellIn` (`11`, `6`, `0`, `-1`), `quality` (`0`, `1`, `48`, `49`, `50`, `80`) và sự biến đổi tích luỹ qua nhiều ngày liên tiếp (vượt qua mốc `11 -> 10` rồi `6 -> 5` rồi `0 -> -1`). Test ít case rời rạc rất dễ bỏ sót bug cộng dồn quá `50` hoặc tụt dưới `0`.
2. Trên [`GildedRoseRefactored.java`](lab/src/main/java/lab/cq/refactor/after/GildedRoseRefactored.java):
   - Khi `sellIn` còn `11`: chỉ `incrementQuality(item, 1)` (`+1`).
   - Khi `sellIn < 11` (từ ngày `sellIn = 10` đến `6`): thêm 1 lần `+1` (tổng `+2`).
   - Khi `sellIn < 6` (từ `sellIn = 5` đến `1`): thêm lần nữa `+1` (tổng `+3`).
   - Sau đó giảm `item.sellIn--`; nếu thành `< 0` (hết buổi diễn) thì đặt ngay `item.quality = 0`. Mỗi bước tăng đều gọi `incrementQuality` nên đảm bảo không bao giờ vượt trần `50`.

#### Bài 2.2 · Bản tương đương Java 7 cho `GildedRoseRefactored`
```java
public interface ItemUpdater {
    void update(Item item);
}
// Khởi tạo map một lần bằng HashMap trong Java 7:
Map<String, ItemUpdater> updaters = new HashMap<String, ItemUpdater>();
updaters.put("Aged Brie", new ItemUpdater() {
    @Override public void update(Item item) {
        incrementQuality(item, 1);
        item.sellIn--;
        if (item.sellIn < 0) incrementQuality(item, 1);
    }
});
// Hoặc tách thành các class rõ tên (AgedBrieUpdater, BackstagePassUpdater)
```

#### Bài 2.3 · Introduce Parameter Object cho `findLogs`
```java
public record AuditLogFilter(String actorId, String actionType, LocalDate start, LocalDate end) {
    public AuditLogFilter {
        if (start != null && end != null && start.isAfter(end)) {
            throw new IllegalArgumentException("start date must be <= end date");
        }
    }
}

public record PageSpec(int page, int size) {
    public PageSpec {
        if (page < 0 || size <= 0 || size > 500) {
            throw new IllegalArgumentException("Invalid page specification");
        }
    }
}

public List<AuditLog> findLogs(AuditLogFilter filter, PageSpec pageSpec) { ... }
```

---

### Level 3

#### Bài 3.1 · Mở rộng `"Conjured Mana Cake"`
Xem đầy đủ tại [`lab/solutions/ConjuredGildedRoseExtension.java`](lab/solutions/ConjuredGildedRoseExtension.java).

Kết quả chạy thực tế `java -cp target\classes solutions\ConjuredGildedRoseExtension.java`:
```text
Day 1 -> conjured=[Conjured Mana Cake, 1, 8] | normal=[Elixir of the Mongoose, 1, 9]
Day 2 -> conjured=[Conjured Mana Cake, 0, 6] | normal=[Elixir of the Mongoose, 0, 8]
Day 3 -> conjured=[Conjured Mana Cake, -1, 2] | normal=[Elixir of the Mongoose, -1, 6]
Day 4 -> conjured=[Conjured Mana Cake, -2, 0] | normal=[Elixir of the Mongoose, -2, 4]
```
`decrementQuality(item, 2)` chặn đáy an toàn ở `0` (Day 4 từ `2 - 4 -> 0`, không xuống `-2`), đồng thời không sửa bất kỳ dòng nào của `GildedRoseRefactored` nên 122 tổ hợp cũ giữ nguyên xanh 100%.

---

## Đáp án Câu hỏi tự kiểm tra

<details>
<summary>1. Phân biệt Divergent Change vs Shotgun Surgery và cách giải quyết từng cái?</summary>

- **Divergent Change**: *1 class* bị sửa vì nhiều nguyên nhân/Actor khác nhau (giống God Class) → Chữa bằng **Extract Class** tách mỗi Actor ra 1 class riêng.
- **Shotgun Surgery**: *1 thay đổi nhỏ* buộc phải sửa lắt nhắt khắp *nhiều class* → Chữa bằng **Move Method / Move Field** hoặc **Inline Class** gom chúng về cùng một chỗ gắn kết.

</details>

<details>
<summary>2. Tại sao Kent Beck khuyên chỉ đội đúng "một chiếc mũ" tại một thời điểm?</summary>

Để luôn phân biệt rõ ranh giới: khi refactor, mọi test phải giữ nguyên trạng thái xanh (bảo toàn hành vi); khi thêm tính năng hay sửa bug, lúc đó mới thay đổi hành vi mong đợi. Trộn cả hai trong một lần sửa dễ làm mất tín hiệu test và khó rollback khi phát sinh lỗi.

</details>

<details>
<summary>3. Quy trình an toàn để refactor hàm 400 dòng hiện chưa có unit test?</summary>

(1) Đừng sửa logic hàm ngay; (2) Chỉ bẻ dependency tối thiểu ở biên (chi tiết ở CQ-04) để gọi được hàm từ test harness; (3) Viết **Characterization / Golden Master test** khoá 100% đầu ra hiện tại; (4) Chạy vòng lặp Extract Method -> Test xanh từng bước nhỏ rồi mới thêm khuyến mãi mới.

</details>

<details>
<summary>4. Vì sao biến đổi tay chuỗi if lồng nhau trong Gilded Rose rất dễ sai ở trường hợp Backstage passes?</summary>

Vì rule của `"Backstage passes"` nằm phân tán ở cả 3 tầng `if` trên lẫn `if (item.sellIn < 0)` bên dưới (nếu `sellIn` vừa trừ 1 xuống `< 0` thì xóa sạch về `0` thay vì `+1/+2/+3` tiếp!). Thiếu ma trận bao quát `sellIn = 0` rất dễ đặt `item.sellIn--` sai thứ tự so với điều kiện `< 11` / `< 6`.

</details>

<details>
<summary>5. Ba tình huống thực tế KHÔNG nên refactor?</summary>

(1) Đoạn code sắp bị xóa/thay bỏ hoàn toàn; (2) Code ổn định lâu năm không có kế hoạch thay đổi hoặc mở rộng; (3) Lúc đang gấp hotfix production mà chưa có test bảo vệ để kiểm chứng hành vi cũ.

</details>
