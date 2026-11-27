# Bài tập · CQ-02 · Cohesion, Coupling, Deep modules

Làm bài trước khi mở [solutions.md](solutions.md). Thực hành trên [lab/](lab/README.md).

---

## Level 1 · Nhận diện Coupling, Cohesion & Depth (4 bài)

### Bài 1.1 · Phân loại Coupling qua 4 đoạn chữ ký hàm
Gọi đúng tên loại Coupling (*Content*, *Common/Global*, *Control*, *Temporal*, *Stamp*, *Data*) cho 4 trường hợp sau:
1. `void saveUser(HttpServletRequest req)` khi bên trong hàm chỉ đọc mỗi `req.getParameter("email")`.
2. `void renderReport(List<Item> items, boolean asCsv, boolean skipHeader)` với nhánh `if (asCsv) ... else ...`.
3. Class `TaxEngine` đọc và ghi biến static `AppState.EXCHANGE_RATE`.
4. Phải gọi `parser.setEncoding("UTF-8"); parser.parse(raw); parser.validate();` đúng thứ tự mới không ném `NullPointerException`.

### Bài 1.2 · Deep vs Shallow trên chuẩn API quen thuộc
So sánh độ "sâu" (*Module Depth*) giữa Java I/O cổ điển Java 6/7 (`new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8))`) và Java 11+ (`Files.readString(path)`). Vì sao `Files.readString(path)` giảm được *Dependencies* và *Obscurity*?

### Bài 1.3 · Nhận diện *Information Leakage* và *Classitis*
Mở [`ShallowCsvPipeline.java`](lab/src/main/java/lab/cq/modules/shallow/ShallowCsvPipeline.java). Tìm chính xác 2 điểm cho thấy thông tin nội bộ bị rò rỉ giữa các class dù tổng logic nghiệp vụ chưa tới 40 dòng.

### Bài 1.4 · Hiểu chỉ số Instability ($I = \frac{C_e}{C_a + C_e}$)
Module `DomainMoney` không phụ thuộc vào module nào khác ($C_e = 0$), nhưng được 25 service trong hệ thống sử dụng ($C_a = 25$). Tính $I$ của `DomainMoney` và giải thích vì sao module này phải giữ API cực kỳ ổn định.

<details>
<summary>Gợi ý</summary>

$I = 0$ gọi là tối đa ổn định (*Maximally Stable*): nó không phụ thuộc ai nên không bị vỡ lây từ dưới, nhưng vì có tới 25 caller phụ thuộc vào nó (*high Fan-in*) nên đổi signature của nó sẽ tạo blast radius lớn.

</details>

---

## Level 2 · Đánh giá thiết kế & đo lường (3 bài)

### Bài 2.1 · Chạy kiểm chứng lab Deep vs Shallow
Chạy `mvn -o test` trong `02-cohesion-coupling-deep-modules/lab/`. Giải thích:
1. Kết quả đo cho thấy cả `ShallowCsvPipeline` lẫn `InvoiceBatchImporter` trả về kết quả giống hệt nhau — vậy tiêu chí định lượng nào trong [`DeepVsShallowTest.java`](lab/src/test/java/lab/cq/modules/DeepVsShallowTest.java) chứng minh bản Deep giảm phức tạp bề mặt?
2. Nếu nghiệp vụ yêu cầu thêm cột thứ 4 (`taxCode`) vào CSV, bên `ShallowCsvPipeline` phải sửa mấy class, còn bên `InvoiceBatchImporter` phải sửa mấy file?

### Bài 2.2 · Khử *Temporal Coupling* & *Control Coupling*
Cho API legacy sau:

```java
InvoicePdfBuilder builder = new InvoicePdfBuilder();
builder.loadTemplate("default");
builder.bindData(invoice);
byte[] bytes = builder.export(true, false); // boolean 1=compress, boolean 2=watermark
```

Thiết kế lại API của `InvoicePdfBuilder` thành một Deep Module không bị *Temporal Coupling* và không dùng cờ `boolean` mơ hồ (*Control Coupling*).

### Bài 2.3 · Đo Coupling trên Repo Git thực tế
Bạn tiếp quản một codebase JBoss Java 7 chưa có SonarQube hay plugin đo dependency. Viết lệnh `git log` (trên terminal Git Bash / PowerShell) để phát hiện 20 file thường xuyên bị sửa chung commit nhiều nhất (dấu hiệu *Co-change / Change Coupling* & *Shotgun Surgery*).

<details>
<summary>Gợi ý</summary>

Dùng `git log --name-only --pretty=format:` để xem danh sách file trong từng commit và đếm tần suất xuất hiện/đi kèm nhau.

</details>

---

## Level 3 · Thiết kế Deep Module thay thế Shallow Config Caller (1 bài)

### Bài 3.1 · Che giấu logic đọc cấu hình bằng Deep Module
Hiện tại ở dự án legacy, 15 DAO class khác nhau đều gọi `Properties.getProperty("db.pool.max")` rồi **mỗi nơi tự viết lại** đoạn `trim()`, bắt null fallback `"8"`, `Integer.parseInt`, và kiểm tra `<= 0`. Đây vừa là lặp logic, vừa là *Information Leakage* + *Shallow wrapper*.

**Yêu cầu**:
1. Thiết kế (hoặc đối chiếu [`lab/solutions/DeepConfigLoader.java`](lab/solutions/DeepConfigLoader.java)) một class cung cấp method `positiveIntOrDefault(String key, int defaultValue)`.
2. Đẩy toàn bộ xử lý null/blank, `trim()`, parse lỗi số nguyên với thông báo lỗi mang rõ tên `key` và chặn số `<= 0` vào bên trong module đó.
3. Chạy `java solutions\DeepConfigLoader.java` kiểm chứng với `"db.pool.max" -> " 16 "` và `"db.timeout.ms" -> " "` (lấy fallback `3000`).

**Tiêu chí đạt**: Giao diện public chỉ cần 1 method duy nhất dễ đoán (`positiveIntOrDefault`), báo lỗi rõ khi cấu hình sai thay vì nuốt lỗi hay ném `NumberFormatException` vô nghĩa, và chạy ra `pool.max=16` / `timeout.ms=3000`.
