# Đáp án · CQ-02 · Cohesion, Coupling, Deep modules

## Đáp án Bài tập

### Level 1

#### Bài 1.1 · Phân loại Coupling
1. `saveUser(HttpServletRequest req)` chỉ đọc mỗi `email` → **Stamp Coupling** (truyền cả cấu trúc nặng nề thừa dữ liệu).
2. `renderReport(..., boolean asCsv, boolean skipHeader)` → **Control Coupling** (truyền cờ điều khiển luồng rẽ nhánh bên trong).
3. Đọc/ghi biến static `AppState.EXCHANGE_RATE` → **Common / Global Coupling**.
4. Bắt buộc gọi `setEncoding` → `parse` → `validate` đúng thứ tự → **Temporal Coupling**.

#### Bài 1.2 · `BufferedReader` chuỗi vs `Files.readString(path)`
`BufferedReader(InputStreamReader(FileInputStream))` là tập các wrapper mỏng (*Shallow*) buộc developer phải tự nhớ thứ tự lắp ghép 3 lớp Decorator, tự chọn charset và tự viết `try-with-resources` + vòng lặp đọc dòng. `Files.readString(path)` là một **Deep Module**: giao diện ngoài cực nhỏ gọn, mặc định an toàn UTF-8, bên trong tự lo mở stream, buffer, decode ký tự và đóng tài nguyên — xóa sạch *Temporal Coupling* và giảm triệt để *Dependencies / Obscurity*.

#### Bài 1.3 · Information Leakage trong `ShallowCsvPipeline`
1. `RowTokenSplitter` trả ra `List<String[]>` — cấu trúc mảng trần thay vì kiểu có tên miền; `RowSanitizer` và `RowValidatorMapper` đều phải **ngầm nhớ quy ước** `cols[0]` là ID, `cols[1]` là tiền, `cols[2]` là currency (**Connascence of Position** + **Information Leakage**).
2. `BatchImportCoordinator` là một **Pass-through Class** không có logic riêng, tồn tại chỉ để nối 4 class nông bên dưới theo đúng thứ tự (**Temporal Coupling**).

#### Bài 1.4 · Chỉ số Instability $I$
$$I = \frac{C_e}{C_a + C_e} = \frac{0}{25 + 0} = 0$$
$I = 0$ nghĩa là `DomainMoney` **cực kỳ ổn định** về mặt phụ thuộc đi ra (không bị vỡ bởi thư viện nào bên dưới), nhưng vì có tới **25 caller** ($C_a = 25$) lệ thuộc vào nó nên nó rất *khó sửa đổi chữ ký*. Mọi thay đổi ở `DomainMoney` đều có bán kính nổ lớn (*Change Amplification*) — vì thế những module $C_a$ cao phải có giao diện **sâu, tối giản và không rò rỉ chi tiết dễ thay đổi**.

---

### Level 2

#### Bài 2.1 · Đo lường & đánh giá `InvoiceBatchImporter`
1. [`DeepVsShallowTest.java`](lab/src/test/java/lab/cq/modules/DeepVsShallowTest.java) đo trực tiếp số kiểu `public` mà người gọi phải tiếp xúc: bản Shallow phơi ra **`7` public types** (với 4 bước điều phối), còn bản Deep chỉ cần **`3` public types** (1 worker `InvoiceBatchImporter` + 2 DTO records `InvoiceLine`, `ImportResult`).
2. Khi thêm cột `taxCode`:
   - Bên `ShallowCsvPipeline`: phải chạm gần như toàn bộ chuỗi `RowSanitizer` (quy ước mảng `cols[3]`), `ParsedRow`, `RowValidatorMapper`.
   - Bên `InvoiceBatchImporter`: chỉ sửa bên trong **1 file duy nhất** (với phạm vi biến cục bộ ngay trong `parseLine` + record `InvoiceLine`).

#### Bài 2.2 · Khử Temporal + Control Coupling ở `InvoicePdfBuilder`
Gộp thành 1 phương thức tự trị hoặc đối tượng `ExportOptions` rõ nghĩa (kèm overload mặc định an toàn):

```java
public record PdfOptions(boolean compressed, boolean watermarked) {
    public static final PdfOptions STANDARD = new PdfOptions(true, false);
}

public byte[] exportInvoicePdf(Invoice invoice) {
    return exportInvoicePdf(invoice, PdfOptions.STANDARD);
}

public byte[] exportInvoicePdf(Invoice invoice, PdfOptions options) {
    // Nội bộ tự nạp template "default", bind data và render PDF một bước
}
```
Caller giờ không cần nhớ gọi `loadTemplate` trước `bindData`, cũng không phải đoán `export(true, false)` nghĩa là gì!

#### Bài 2.3 · Lệnh Git soi Co-change Coupling không cần tool ngoài
```bash
# Xem top 20 file hay bị sửa nhất trong lịch sử gần đây (hotspots)
git log --n=100 --name-only --pretty=format: | sort | uniq -c | sort -nr | head -n 20

# Hoặc trên PowerShell Windows không có uniq/head:
git log -n 100 --name-only --pretty=format:"" | Where-Object { $_ -ne "" } | Group-Object | Sort-Object Count -Descending | Select-Object -First 20 Count,Name
```
Kết hợp xem nội dung từng commit lớn (`git log -n 20 --stat`), bạn thấy ngay cụm file nào cứ 1 tính năng nhỏ lại phải sửa chung một chùm (*Temporal / Logical Coupling*).

---

### Level 3

#### Bài 3.1 · `DeepConfigLoader`
Xem code hoàn chỉnh tại [`lab/solutions/DeepConfigLoader.java`](lab/solutions/DeepConfigLoader.java).

Khi chạy `java solutions\DeepConfigLoader.java`:
```text
pool.max=16
timeout.ms=3000
```
Điểm đạt thiết kế **Deep Module**: caller chỉ gọi đúng 1 hàm `positiveIntOrDefault(key, defaultValue)`. Toàn bộ edge-case (`null`, khoảng trắng thừa `" 16 "`, chuỗi rỗng `" "` dùng fallback `3000`, giá trị âm hoặc không phải số ném `IllegalArgumentException` kèm đúng tên key `db.pool.max`) được che giấu trọn vẹn bên trong module.

---

## Đáp án Câu hỏi tự kiểm tra

<details>
<summary>1. Hai thành phần tạo nên Complexity theo Ousterhout?</summary>

**`Complexity = Dependencies + Obscurity`**.
- Ví dụ *Dependencies*: Sửa 1 cột CSV buộc sửa cả 4 class `Splitter`, `Sanitizer`, `Validator`, `DTO`.
- Ví dụ *Obscurity*: Caller phải đọc ruột hàm mới biết tham số thứ 2 kiểu `String` thực ra là định dạng `"yyyyMMdd"`, hoặc phải gọi `init()` trước `run()`.

</details>

<details>
<summary>2. Vì sao tách Step1Class, Step2Class, Step3Class truyền String[] qua lại tăng complexity?</summary>

Vì mỗi đường cắt đều tạo thêm một interface công khai cần học và bảo trì, rò rỉ quy ước mảng `String[]` qua biên giới giữa 3 class (*Information Leakage*), đồng thời sinh *Temporal Coupling* ép caller phải gọi đúng thứ tự — tức tăng cả *Dependencies* lẫn *Obscurity*.

</details>

<details>
<summary>3. Thứ tự nguy hiểm từ cao xuống thấp của các loại coupling?</summary>

**Common/Global Coupling** (biến static/shared mutable toàn cục) → **Control Coupling** (truyền cờ điều khiển luồng) → **Stamp Coupling** (truyền cả object lớn dù dùng 1 field) → **Data Coupling** (truyền đúng tham số nhỏ gọn tối thiểu cần dùng).

</details>

<details>
<summary>4. Dấu hiệu trên `git log` chứng tỏ hai module bị dính chặt nhau?</summary>

Hai hoặc nhiều file không nằm cùng tính năng nhỏ nhưng thường xuyên xuất hiện **chung trong cùng một commit** qua nhiều tháng (*Change Amplification / Co-change Coupling*), ví dụ cứ đổi trường dữ liệu ở module A là luôn có diff đi kèm ở B, C, D.

</details>

<details>
<summary>5. Tại sao `positiveIntOrDefault(key, defaultValue)` sâu hơn `String getRaw(key)`?</summary>

Vì `getRaw(key)` chỉ pass-through thẳng ra ngoài (nông), đẩy toàn bộ gánh nặng xử lý `null`, `blank`, `trim()`, `Integer.parseInt`, kiểm tra `> 0` sang hàng chục caller (*Change Amplification*). `positiveIntOrDefault` gom trọn toàn bộ phức tạp đó vào sau 1 chữ ký duy nhất rõ nghĩa.

</details>
