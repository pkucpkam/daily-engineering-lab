# Bài tập · CQ-04 · Legacy code & Characterization tests

Làm bài trước khi mở [solutions.md](solutions.md). Thực hành trên [lab/](lab/README.md).

---

## Level 1 · Hiểu bản chất Legacy & Test khoá hành vi (4 bài)

### Bài 1.1 · Nhận diện "Legacy Code" theo Feathers
Một startup vừa viết module thanh toán bằng Java 21 + Virtual Threads tháng trước, code rất gọn nhưng **chưa viết test tự động nào** vì muốn ra sản phẩm sớm. Theo định nghĩa của Michael Feathers, module này có phải là *Legacy Code* không? Nêu lý do thực tế khi cần sửa tính năng vào tháng thứ 2.

### Bài 1.2 · Đặt câu hỏi đúng cho Characterization Test
Bạn tìm thấy đoạn code tính giảm giá trong codebase 6 năm tuổi:

```java
if (age > 60) rate = 0.2;
```

Tài liệu đặc tả Word cũ lại ghi: *"Khách hàng từ 60 tuổi trở lên (`>= 60`) được giảm 20%"*. Khi viết **Characterization Test** đầu tiên trước khi refactor, bạn gán `expected` cho `age = 60` là `0.2` hay `0.0`? Sau đó xử lý bước tiếp theo thế nào?

### Bài 1.3 · Hai kỹ thuật bẻ Seam cơ bản ở Java
Chỉ ra hai điểm phụ thuộc cứng trong [`LegacyMonthlyStatementDao.java`](lab/src/main/java/lab/cq/legacy/LegacyMonthlyStatementDao.java) ngăn cản chạy unit test ngoại tuyến, và tên kỹ thuật dùng để cắt hai phụ thuộc đó mà không đổi logic hàm `buildStatement`.

### Bài 1.4 · Bốn điểm yếu của Golden Master Test
Vì sao sau khi dùng Golden Master Test để refactor xong một hàm lớn, người ta không giữ hàng chục file snapshot text đó làm bộ unit test lâu dài duy nhất mà thường viết thêm unit test rõ tên nghiệp vụ? Liệt kê ít nhất 3 nhược điểm của Golden Master.

---

## Level 2 · Thực hành bẻ Seam & viết Snapshot JDBC (3 bài)

### Bài 2.1 · Chạy Golden Master trên giả lập JDBC Proxy
Chạy `mvn -o test` trong `04-legacy-characterization-tests/lab/`. Đọc [`StatementCharacterizationTest.java`](lab/src/test/java/lab/cq/legacy/StatementCharacterizationTest.java) và trả lời:
1. Tại sao phải override `currentEpochMillis()` trả về hằng số `1_700_000_000_000L`? Nếu không override chuyện gì xảy ra với assertion `isEqualTo(approvedGoldenMaster)`?
2. Quan sát phép tính `TX-1` (`amount_cents = 60_005L`, `"PURCHASE"`): giải thích vì sao `net` là `58805` chứ không phải `58804` hay `58804.9` (làm tròn số nguyên thế nào?).

### Bài 2.2 · Bổ sung Case 4 vào Golden Master (Nhánh chưa phủ)
Trong `StatementCharacterizationTest`, hiện tại `FakeRow` chỉ test loại `"PURCHASE"` và `"REFUND"` với `tier = "STD"` và `"VIP"`. Hãy thiết kế thêm **Case 4**: `tier = "VIP"` nhưng `grossCents = 80_000L` (dưới ngưỡng 100k) để chắc chắn nhánh VIP không được thưởng bonus cũng được khoá chặt.

<details>
<summary>Gợi ý</summary>

Chỉ cần truyền list gồm 1 `FakeRow("T-LOW", 80_000L, "REFUND")` với `"VIP"` vào `stubDao(...)` và xác nhận dòng `SUMMARY` in ra `bonus=0|final=80000`.

</details>

### Bài 2.3 · Chuyển ngữ cảnh Java 7 / JBoss JNDI lookup
Bạn gặp servlet Java 7 sau ở công ty (dùng `InitialContext` tra `DataSource` của JBoss thay vì `DriverManager`):

```java
DataSource ds = (DataSource) new InitialContext().lookup("java:jboss/datasources/CoreDS");
Connection conn = ds.getConnection();
```

Áp dụng đúng kỹ thuật *Extract & Override Factory Method* (hoặc *Parameterize Method*) để bẻ dependency cho đoạn Servlet/DAO này chạy được dưới JUnit 4/5 mà **không sửa câu SQL hay vòng `ResultSet`**.

---

## Level 3 · Dựng lưới Characterization phát hiện lỗi biên trước khi Refactor (1 bài)

### Bài 3.1 · Chứng minh biên `100_000` vs `100_001` bằng Characterization Test
Nhìn vào dòng điều kiện trong `LegacyMonthlyStatementDao.java`:

```java
if ("VIP".equals(tier) && grossCents > 100_000L)
```

Giả sử Product Owner nghi ngờ hệ thống đang bị khách VIP khiếu nại vì đơn đúng tròn **100.000** lại không nhận được thưởng `5.000`.

**Yêu cầu**:
1. Chạy [`lab/solutions/BoundaryQuirkCheck.java`](lab/solutions/BoundaryQuirkCheck.java) (`mvn -o -q compile` → `java -cp target\classes solutions\BoundaryQuirkCheck.java`) để ghi lại bằng chứng hành vi hiện tại tại `100_000` và `100_001`.
2. Thêm một `@Test` độc lập trong `StatementCharacterizationTest` (tạm thời tại local của bạn rồi xác nhận xanh) khẳng định quy tắc hiện tại là strict `>` (`100_000 -> bonus=0`, `100_001 -> bonus=5000`), mô phỏng bước **"chụp hiện trạng trước khi bàn với PO sửa `>` thành `>=`"**.

**Tiêu chí đạt**: In ra được output `bonus=0|final=100000` ở mốc `100_000` và `bonus=5000|final=95001` ở mốc `100_001`, và chạy lại `mvn -o test` PASS 100%.
