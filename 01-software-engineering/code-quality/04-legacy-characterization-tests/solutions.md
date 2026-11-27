# Đáp án · CQ-04 · Legacy code & Characterization tests

## Đáp án Bài tập

### Level 1

#### Bài 1.1 · Module Java 21 mới viết nhưng 0 test có phải Legacy Code?
**Có**, theo định nghĩa của Michael Feathers ("Legacy code = code without tests"). Dù dùng cú pháp hiện đại đến đâu, không có lưới kiểm thử tự động đồng nghĩa mọi thay đổi ở tháng thứ 2 đều phải kiểm chứng thủ công đầy rủi ro, không thể tự tin refactor cấu trúc → mọi nhược điểm của legacy đều y nguyên.

#### Bài 1.2 · Khi code lệch với tài liệu spec cũ (`age > 60` vs `>= 60`)
- Trong bước viết **Characterization Test đầu tiên**, bạn PHẢI gán **`expected = 0.0`** cho `age = 60` — vì Characterization Test phản ánh **hành vi thực tế hiện tại** của code chứ không tự ý sửa spec.
- Ghi chú phát hiện sai lệch này (`// NOTE: Spec states >= 60, prod currently implements > 60`) và xác nhận lại với PO/Nghiệp vụ: nếu xác nhận đó là bug thật cần sửa, tạo riêng 1 bước sửa + đổi expected test thành `0.2` sau khi đã khoá an toàn phần còn lại.

#### Bài 1.3 · Hai điểm phụ thuộc cứng và Seam trong `LegacyMonthlyStatementDao`
1. `DriverManager.getConnection(...)` (kết nối Oracle DB ngoài) → cắt bằng `protected Connection openConnection()` (*Extract & Override Factory Method*).
2. `System.currentTimeMillis()` (trạng thái thời gian bất định) → cắt bằng `protected long currentEpochMillis()`.

#### Bài 1.4 · Ba nhược điểm lớn của Golden Master
1. **Dễ flaky nếu có dữ liệu động** (timestamp, UUID, thứ tự `HashMap` không sắp xếp) nếu chưa cô lập.
2. **Fail khó chẩn đoán** (*Low clarity*): báo sai lệch text ở dòng X nhưng không nói rõ quy tắc nghiệp vụ nào bị vi phạm.
3. **Nhạy cảm với thay đổi định dạng nhỏ**: đổi nhãn text hay dấu phân cách buộc cập nhật hàng loạt snapshot, đồng thời không tự chứng minh hết nhánh `if/else` bên trong nếu thiếu coverage tool đi kèm.

---

### Level 2

#### Bài 2.1 · Giải thích kết quả `StatementCharacterizationTest`
1. Không override `currentEpochMillis()` sẽ làm dòng `STATEMENT|...|ts=...` đổi giá trị theo từng mili-giây chạy test → Golden Master sẽ **fail 100% ở lần chạy thứ hai**.
2. Công thức trong legacy DAO là phép chia số nguyên: `(60_005L * 2) / 100 = 120_010L / 100 = 1_200L` (phần dư `.10` bị cắt cụt do chia nguyên). Nên `net = 60_005 - 1_200 = 58_805`. Characterization Test giúp khoá đúng cái phép làm tròn cụt đặc trưng này của hệ thống tài chính cũ!

#### Bài 2.2 · Thêm Case 4 (`VIP` với `grossCents = 80_000L`)
```java
LegacyMonthlyStatementDao vipLow = stubDao(List.of(new FakeRow("T-LOW", 80_000L, "REFUND")));
String result = vipLow.buildStatement("C-VIP-LOW", "VIP", 11);
assertThat(result).contains("SUMMARY|count=1|net=80000|bonus=0|final=80000");
```

#### Bài 2.3 · Bẻ Seam cho `InitialContext` JNDI của JBoss Java 7
Trích dòng lookup + lấy connection ra thành một method `protected` duy nhất (giữ nguyên toàn bộ JDBC code phía sau):

```java
protected Connection openConnection() throws SQLException {
    try {
        DataSource ds = (DataSource) new InitialContext().lookup("java:jboss/datasources/CoreDS");
        return ds.getConnection();
    } catch (NamingException e) {
        throw new SQLException("JNDI lookup failed", e);
    }
}
```
Ở production chạy y nguyên code JNDI; ở JUnit chỉ cần subclass `@Override protected Connection openConnection()` trả JDBC Proxy hoặc H2!

---

### Level 3

#### Bài 3.1 · Bằng chứng biên `100_000` vs `100_001`
Xem giải pháp ở [`lab/solutions/BoundaryQuirkCheck.java`](lab/solutions/BoundaryQuirkCheck.java). Kết quả chạy thực tế:

```text
=== At exact 100,000 net (VIP bonus = 0 due to strict > check) ===
STATEMENT|cust=C-BOUNDARY|month=11|ts=1700000000000
TX:TX-B:REFUND:net=100000
SUMMARY|count=1|net=100000|bonus=0|final=100000
=== At 100,001 net (VIP bonus = 5,000 triggers) ===
STATEMENT|cust=C-BOUNDARY|month=11|ts=1700000000000
TX:TX-B:REFUND:net=100001
SUMMARY|count=1|net=100001|bonus=5000|final=95001
```

Dữ liệu này là bằng chứng xác thực trình PO/BA: code hiện tại dùng `>` nên khách đặt đúng `100.000` bị mất `bonus=5000`; sau khi PO duyệt sửa nghiệp vụ sang `>= 100_000L`, ta cập nhật test này thành specification test và triển khai an toàn.

---

## Đáp án Câu hỏi tự kiểm tra

<details>
<summary>1. Service viết bằng Java 21 Spring Boot nhưng 0 test có phải Legacy Code?</summary>

**Có**, vì không có lưới test bảo vệ, mọi sửa đổi hoặc refactor đều có nguy cơ tạo regression mà không thể phát hiện tự động — đúng bản chất định nghĩa "code without tests" của Michael Feathers.

</details>

<details>
<summary>2. Thứ tự 3 hành động đầu tiên trước khi refactor class DAO 2.000 dòng?</summary>

(1) Xác định điểm cần sửa & điểm quan sát output (*Inflection Point*); (2) Chỉ **bẻ dependency tối thiểu ở biên** (`Extract & Override` / `Parameterize`) *không chạm* vào logic tính toán; (3) Viết **Characterization / Golden Master test** khoá nguyên hiện trạng trước khi bắt đầu bước refactor đầu tiên.

</details>

<details>
<summary>3. Khác biệt mục đích giữa Unit Test theo spec và Characterization Test?</summary>

Unit test theo spec kiểm tra *"hàm có làm đúng như đặc tả mong đợi không"*. Characterization test hỏi *"hàm thực sự đang hành xử thế nào ngay bây giờ"* để đóng băng mọi hành vi hiện hữu làm rào chắn trước khi sửa cấu trúc.

</details>

<details>
<summary>4. Hai nhược điểm của Golden Master và cách khắc phục?</summary>

(1) **Dễ flaky** khi có timestamp/UUID/HashMap ngẫu nhiên → khắc phục bằng seam cố định Clock/Seed/Sort output; (2) **Mất ngữ nghĩa rõ ràng khi fail** và dễ vỡ bởi thay đổi format nhỏ → khắc phục bằng dùng Golden Master làm lưới đỡ ban đầu, đo coverage để đảm bảo quét đủ nhánh, rồi dần viết unit test rõ ý nghĩa nghiệp vụ thay thế.

</details>

<details>
<summary>5. Khi Characterization Test phát hiện quirk mất 1 đồng đang chạy 5 năm trên prod thì sao?</summary>

**Khoá nguyên quirk đó trong Characterization Test trước** (để không làm thay đổi hành vi ngoài ý muốn khi đang refactor) và báo cáo lại Product/Kế toán xác nhận xem đối tác có phụ thuộc vào cách làm tròn đó không trước khi lập ticket sửa riêng.

</details>
