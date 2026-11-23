---
title: "Kiểm tra mã đã tồn tại khi Import Excel — Từ bài toán cụ thể tới nguyên lý tổng quát"
tags: [database, performance, batch-processing, java, sql, patterns]
---

# Kiểm tra mã đã tồn tại khi Import Excel — Từ bài toán cụ thể tới nguyên lý tổng quát

## Mục lục

1. [Đặt vấn đề](#1-đặt-vấn-đề)
2. [Phân tích 4 phương án triển khai](#2-phân-tích-4-phương-án-triển-khai)
3. [So sánh tổng hợp](#3-so-sánh-tổng-hợp)
4. [Khuyến nghị theo quy mô dữ liệu](#4-khuyến-nghị-theo-quy-mô-dữ-liệu)
5. [Kiến trúc đề xuất chi tiết](#5-kiến-trúc-đề-xuất-chi-tiết)
6. [Tổng quát hoá: đây là dạng bài toán gì?](#6-tổng-quát-hoá-đây-là-dạng-bài-toán-gì)
7. [Nguyên lý nền tảng đứng sau các phương án](#7-nguyên-lý-nền-tảng-đứng-sau-các-phương-án)
8. [Các biến thể khác của cùng một lớp bài toán](#8-các-biến-thể-khác-của-cùng-một-lớp-bài-toán)
9. [Checklist ra quyết định](#9-checklist-ra-quyết-định)
10. [Liên hệ với các vấn đề đã gặp trong hệ thống eBAF](#10-liên-hệ-với-các-vấn-đề-đã-gặp-trong-hệ-thống-ebaf)

---

## 1. Đặt vấn đề

### 1.1. Mô tả nghiệp vụ

Hệ thống cho phép người dùng upload một file Excel chứa danh sách mã (Code) cần import. Ví dụ:

| STT | Code |
|-----|------|
| 1 | A001 |
| 2 | A002 |
| 3 | A003 |
| 4 | A004 |
| ... | ... |

File có thể chứa từ **vài trăm** đến **hàng chục nghìn dòng**, tuỳ vào nghiệp vụ (import danh mục sản phẩm, import danh sách mã số thuế, import danh sách khách hàng...).

### 1.2. Yêu cầu chức năng

- Đọc toàn bộ file Excel.
- Với mỗi `Code`, kiểm tra xem đã tồn tại trong Database hay chưa.
- Trả về danh sách các dòng bị lỗi (dòng nào, mã nào, lỗi gì).

Kết quả mong đợi:

| Row | Code | Error |
|-----|------|-------|
| 2 | A002 | Code đã tồn tại |
| 5 | A010 | Code đã tồn tại |

### 1.3. Bản chất thật sự của bài toán

Nhìn thoáng qua đây chỉ là "kiểm tra trùng dữ liệu", nhưng bản chất là:

> **Kiểm tra membership (thành viên) của N phần tử input đối với một tập hợp M phần tử đang nằm trong Database, mà không kéo cả tập M về Application.**

Đây là dạng bài toán cực kỳ phổ biến trong các hệ thống doanh nghiệp (ERP, Core Banking, CRM), và cách giải quyết nó phản ánh rất rõ tư duy thiết kế hệ thống: **để Application xử lý từng phần tử, hay để Database xử lý cả tập hợp?**

---

## 2. Phân tích 4 phương án triển khai

### Phương án 1 — Query từng dòng (Row-by-row query)

**Ý tưởng:** Với mỗi dòng Excel, chạy một câu SQL riêng.

```sql
SELECT * FROM PRODUCT WHERE CODE = ?
```

```java
for (String code : excelCodes) {
    if (exists(code)) {
        // duplicate
    }
}
```

**Flow:**

```
Excel → Đọc từng dòng → SELECT WHERE CODE=? → Có? → Lưu lỗi
```

**Ưu điểm:** Dễ triển khai, code đơn giản, dễ hiểu cho người mới.

**Nhược điểm:** Nếu file có 10.000 dòng → 10.000 query. Mỗi query đều tốn round-trip Application ↔ Database (gửi request, parse SQL, execute, trả kết quả). Đây chính là **hiện tượng N+1 query** ở quy mô lớn — chi phí round-trip network/DB connection lấn át hoàn toàn chi phí xử lý thực tế.

→ **Không phù hợp với dữ liệu lớn.**

---

### Phương án 2 — WHERE IN (gộp thành một câu query)

**Ý tưởng:** Gom toàn bộ Code lại, query một lần bằng `IN`.

```sql
SELECT CODE FROM PRODUCT
WHERE CODE IN ('A001','A002','A003','A004')
```

Sau khi có kết quả, đưa vào `HashSet` để so sánh:

```java
HashSet<String> existedCodes = ...; // từ DB trả về
for (String code : excelCodes) {
    if (existedCodes.contains(code)) {
        // duplicate
    }
}
```

**Flow:**

```
Excel → Read toàn bộ → List<String> → SELECT WHERE IN(...) → HashSet → Compare
```

**Ưu điểm:** Chỉ 1 query duy nhất, nhanh hơn rất nhiều so với query từng dòng, Database tận dụng được Index trên cột CODE.

**Nhược điểm:** Với file rất lớn (50.000 – 200.000 dòng), câu `IN(...)` sẽ quá dài. Nhiều Database (Oracle giới hạn 1000 phần tử trong IN, DB2 cũng có giới hạn độ dài câu lệnh SQL) sẽ từ chối thực thi hoặc suy giảm hiệu năng nghiêm trọng do query plan phải xử lý literal list khổng lồ.

---

### Phương án 3 — Batch IN (chia nhỏ IN thành nhiều batch)

**Ý tưởng:** Không dồn hết vào 1 câu IN, mà chia thành nhiều batch nhỏ (ví dụ 1000 phần tử/batch).

```java
for (List<String> batch : partition(codes, 1000)) {
    List<String> result = dao.find(batch);
    existed.addAll(result);
}
```

**Flow:**

```
Excel → Read → Partition → IN Batch1 / IN Batch2 / ... → HashSet → Compare
```

**Ưu điểm:** Không vượt giới hạn của `IN`. Phù hợp cho dữ liệu vài nghìn đến vài chục nghìn dòng. Không cần tạo thêm bảng nào trong Database.

**Nhược điểm:** Nếu có 100 batch thì vẫn phải chạy 100 query. Mỗi lần Database vẫn phải parse, optimize, execute lại — chi phí round-trip vẫn tồn tại, chỉ giảm chứ không triệt tiêu.

---

### Phương án 4 — Temporary Table + JOIN (khuyến nghị cho dữ liệu lớn)

Đây là mô hình phổ biến trong các hệ thống ERP, Banking, CRM, ETL.

**Ý tưởng cốt lõi:** Không để Java làm việc đối chiếu. Đưa toàn bộ dữ liệu Excel vào một **bảng tạm** (`TEMP_IMPORT`) trong Database, sau đó để chính Database thực hiện JOIN để đối chiếu.

**Flow:**

```
Excel → Read Excel → Batch Insert → TEMP_IMPORT → JOIN PRODUCT → Danh sách lỗi
```

**Thiết kế bảng tạm:**

| Column | Meaning |
|--------|---------|
| ROW_NO | Dòng trong Excel |
| CODE | Mã cần kiểm tra |
| STATUS | OK / ERROR |
| ERROR_MSG | Thông báo lỗi |

**Batch Insert dữ liệu Excel vào bảng tạm** (dùng JDBC batch, ví dụ 1000 record/batch):

```java
PreparedStatement ps = conn.prepareStatement(
    "INSERT INTO TEMP_IMPORT (ROW_NO, CODE) VALUES (?, ?)");
for (Row row : excelRows) {
    ps.setInt(1, row.getRowNo());
    ps.setString(2, row.getCode());
    ps.addBatch();
}
ps.executeBatch();
```

**Đối chiếu bằng JOIN — để Database làm việc nó giỏi nhất:**

```sql
SELECT T.ROW_NO, T.CODE
FROM TEMP_IMPORT T
JOIN PRODUCT P ON T.CODE = P.CODE
```

Database sẽ tự tối ưu JOIN (dùng index, hash join, sort-merge join tuỳ optimizer), Application không cần lặp qua từng Code nữa.

**Kết quả trả về ví dụ:**

| ROW_NO | CODE |
|--------|------|
| 2 | A001 |
| 7 | A050 |

Frontend hiển thị: *"Dòng 2: Code A001 đã tồn tại"*, *"Dòng 7: Code A050 đã tồn tại"*.

#### Global Temporary Table (GTT) — lựa chọn tối ưu nếu Database hỗ trợ

Nếu Database hỗ trợ Global Temporary Table, chỉ cần tạo bảng **một lần duy nhất**; sau đó mỗi Session có dữ liệu riêng biệt, tự động cô lập, không cần `CREATE TABLE` / `DROP TABLE` lặp đi lặp lại mỗi lần import.

```
Application → Insert Temp → Join → Delete → Done
```

**Ưu điểm:**
- Không ảnh hưởng đến Session khác (dữ liệu cô lập theo session).
- Không phải tạo/xoá bảng liên tục.
- Hiệu năng cao, ổn định.
- Dễ mở rộng thêm nhiều rule kiểm tra chỉ bằng cách thêm SQL.

> **Lưu ý khi áp dụng cho DB2:** DB2 hỗ trợ `DECLARE GLOBAL TEMPORARY TABLE`, dữ liệu tự động dọn khi kết thúc transaction/session tuỳ `ON COMMIT DELETE ROWS` hay `ON COMMIT PRESERVE ROWS`. Cần cân nhắc kỹ tham số này vì ảnh hưởng trực tiếp đến việc bảng tạm có còn dữ liệu để JOIN ở bước tiếp theo trong cùng transaction hay không.

#### Mở rộng thêm nhiều Validation chỉ bằng SQL

Một khi dữ liệu đã nằm trong bảng tạm, mọi rule kiểm tra chỉ là một câu SQL mới — không cần sửa code Java:

```sql
-- Kiểm tra Customer tồn tại
SELECT * FROM TEMP_IMPORT T
LEFT JOIN CUSTOMER C ON T.CUSTOMER_ID = C.ID
WHERE C.ID IS NULL

-- Kiểm tra Department tồn tại
SELECT * FROM TEMP_IMPORT T
LEFT JOIN DEPARTMENT D ON T.DEPT_ID = D.ID
WHERE D.ID IS NULL

-- Kiểm tra trùng lặp ngay trong file Excel
SELECT CODE, COUNT(*) FROM TEMP_IMPORT
GROUP BY CODE HAVING COUNT(*) > 1
```

Đây là điểm mạnh cốt lõi của phương án Temp Table: **tách rời hoàn toàn logic validate khỏi code Java**, biến việc "thêm rule mới" thành việc "thêm câu SQL mới" — dễ bảo trì, dễ review, dễ test độc lập.

---

## 3. So sánh tổng hợp

| Tiêu chí | Query từng dòng | WHERE IN | Batch IN | Temp Table + JOIN |
|---|---|---|---|---|
| Số lần Query | N | 1 | N/BATCH | 1 JOIN |
| Round-trip DB | Rất nhiều | Ít | Trung bình | Ít nhất |
| Hiệu năng | Kém | Tốt | Rất tốt | Tốt nhất |
| Dễ triển khai | ★★★★★ | ★★★★★ | ★★★★☆ | ★★★☆☆ |
| Khả năng mở rộng | Kém | Trung bình | Tốt | Rất tốt |
| Kiểm tra nhiều Rule | Khó | Khó | Khá | Rất dễ |
| Import vài trăm dòng | ★★★★★ | ★★★★★ | ★★★★☆ | ★★☆☆☆ |
| Import vài nghìn dòng | ★☆☆☆☆ | ★★★★★ | ★★★★★ | ★★★★☆ |
| Import vài chục nghìn dòng | ☆☆☆☆☆ | ★★☆☆☆ | ★★★★☆ | ★★★★★ |
| Import hàng trăm nghìn dòng | ☆☆☆☆☆ | ☆☆☆☆☆ | ★★★☆☆ | ★★★★★ |

---

## 4. Khuyến nghị theo quy mô dữ liệu

**Dưới 5.000 dòng** → dùng **Batch IN**. Đơn giản, dễ triển khai, hiệu năng đủ tốt, không cần thêm bảng tạm.

**Từ vài chục nghìn dòng trở lên** → dùng **Global Temporary Table + JOIN**. Đây là mô hình phổ biến trong các hệ thống doanh nghiệp như ERP, CRM, Core Banking và ETL vì tận dụng tối đa khả năng xử lý của Database, giảm tối đa số lần giao tiếp Application ↔ Database, đồng thời dễ mở rộng thêm quy tắc kiểm tra chỉ bằng SQL.

Quy tắc kinh nghiệm (rule of thumb): **nếu số lần round-trip tới Database tỉ lệ thuận với kích thước dữ liệu input, đó là dấu hiệu cần chuyển logic xuống Database.**

---

## 5. Kiến trúc đề xuất chi tiết

```
                  Upload Excel
                        │
                        ▼
                 Apache POI Read
                        │
                        ▼
              Batch Insert Temp Table
                        │
                        ▼
            SQL Validation (JOIN/LEFT JOIN)
                        │
                        ▼
              UPDATE STATUS / ERROR_MSG
                        │
                        ▼
           SELECT các dòng bị lỗi trả về Java
                        │
                        ▼
               Response cho Frontend
```

Với kiến trúc này, Java chỉ chịu trách nhiệm ở phần **I/O và điều phối** (đọc file, ghi vào bảng tạm, trả kết quả), còn toàn bộ **logic đối chiếu dữ liệu** được xử lý tập trung trong Database. Điều này giúp hệ thống:

- Dễ bảo trì (logic nghiệp vụ tập trung một chỗ, dạng SQL dễ đọc).
- Dễ mở rộng (thêm rule = thêm SQL, không sửa Java).
- Đạt hiệu năng cao và ổn định khi dữ liệu tăng trưởng theo thời gian.

---

## 6. Tổng quát hoá: đây là dạng bài toán gì?

Nếu bóc tách khỏi ngữ cảnh "Excel" và "Code", bài toán gốc thực chất là:

> **Set Intersection / Membership Check giữa một tập dữ liệu đến từ bên ngoài (external input set) và một tập dữ liệu đang lưu trữ trong hệ thống (persisted set), ở quy mô mà việc lặp qua từng phần tử không còn khả thi.**

Đây là một trong những dạng bài toán nền tảng, xuất hiện lặp đi lặp lại dưới nhiều lớp áo khác nhau trong lập trình backend:

- Kiểm tra trùng khi import dữ liệu (bài toán trong tài liệu này).
- Kiểm tra danh sách ID có hợp lệ (tồn tại trong bảng cha) trước khi insert bản ghi con.
- Đối chiếu 2 tập dữ liệu để tìm phần tử thêm mới / bị xoá / thay đổi (data reconciliation, đồng bộ dữ liệu giữa 2 hệ thống).
- Kiểm tra danh sách tài khoản có nằm trong blacklist (đúng như bài toán FBA01512 — Blacklist mã số thuế mà Phuc đang làm).
- Kiểm tra quyền truy cập của N user vào N resource cùng lúc (authorization batch check).

Tất cả các bài toán trên đều có chung một cấu trúc: **N input × M dữ liệu đã lưu trữ, cần tìm giao/hiệu của 2 tập.**

---

## 7. Nguyên lý nền tảng đứng sau các phương án

### 7.1. Round-trip cost vs Processing cost

Chi phí thực thi một thao tác Database gồm 2 phần:

1. **Round-trip cost** — chi phí cố định cho mỗi lần giao tiếp: network latency, mở connection/statement, parse SQL, lập kế hoạch thực thi (query plan).
2. **Processing cost** — chi phí tỉ lệ với khối lượng dữ liệu thực sự cần xử lý.

Ở Phương án 1 (query từng dòng), **round-trip cost bị nhân lên N lần** trong khi processing cost cho mỗi query là rất nhỏ (tra 1 dòng theo index). Đây là lý do dù mỗi query "nhanh" (vài ms) nhưng tổng thời gian vẫn rất chậm với N lớn — **chi phí cố định x N luôn thắng chi phí biến đổi khi N đủ lớn.**

Đây chính là bản chất của **N+1 query problem** — vấn đề Phuc đã từng ghi chú trong Obsidian khi tìm hiểu về nó ở ngữ cảnh ORM/ORM lazy-loading. Bài toán import Excel này là một biến thể thủ công (manual) của cùng một hiện tượng, không liên quan gì đến ORM cả — chứng tỏ N+1 là một **pattern chống (anti-pattern) mang tính kiến trúc**, không phải lỗi riêng của công nghệ nào.

### 7.2. "Đẩy việc xuống Database" vs "Kéo dữ liệu lên Application"

Có 2 triết lý thiết kế đối lập:

- **Kéo dữ liệu lên (Phương án 1, 2, 3):** Application chủ động lấy dữ liệu từ Database rồi tự xử lý logic so sánh bằng code Java (`HashSet.contains()`).
- **Đẩy việc xuống (Phương án 4):** Application chỉ chuyển giao dữ liệu thô, còn logic so sánh/đối chiếu để Database (vốn được tối ưu chuyên biệt cho việc join, filter, group trên tập hợp lớn) tự xử lý.

Database Engine được xây dựng với hàng chục năm nghiên cứu tối ưu hoá cho đúng việc: xử lý tập hợp lớn (set-based operations) hiệu quả hơn nhiều so với vòng lặp thủ công trong Application, nhờ:

- Index (B-Tree, Hash Index) giúp tra cứu O(log n) hoặc O(1) thay vì so sánh tuần tự.
- Query Optimizer tự chọn thuật toán JOIN tối ưu (nested loop / hash join / sort-merge join) tuỳ vào kích thước dữ liệu thực tế.
- Xử lý set-based thay vì record-based, tận dụng được I/O tuần tự và caching ở tầng Storage Engine.

**Nguyên lý chung:** *Nếu logic xử lý là thao tác trên tập hợp (set-based) — hãy để Database làm; nếu logic là quy tắc nghiệp vụ phức tạp không diễn đạt được bằng SQL — mới nên đưa lên Application.*

### 7.3. Batching như một giải pháp trung gian

Batch IN (Phương án 3) là một ví dụ điển hình của **kỹ thuật Batching** — đánh đổi giữa 2 thái cực (1 query khổng lồ vs N query nhỏ) bằng cách gom nhóm để giảm số round-trip mà vẫn tránh được giới hạn kỹ thuật (giới hạn độ dài SQL, giới hạn số phần tử trong IN).

Batching là kỹ thuật xuất hiện ở rất nhiều tầng khác nhau của hệ thống, không riêng gì SQL:

- JDBC batch insert/update (`addBatch()` / `executeBatch()`).
- Batch HTTP request (gộp nhiều API call thành 1 request).
- Message queue batch consume (đọc N message một lần thay vì từng message).

### 7.4. Temp Table như một "biên dịch" bài toán về đúng bản chất của nó

Việc đưa dữ liệu vào bảng tạm rồi JOIN thực chất là **biến bài toán "so sánh 2 tập dữ liệu" thành đúng ngôn ngữ tự nhiên của Database — set theory**, thay vì cố nhét nó vào ngôn ngữ thủ tục (procedural) của Java bằng vòng lặp. SQL vốn dựa trên đại số quan hệ (relational algebra), nên các phép JOIN, LEFT JOIN, GROUP BY... chính là các phép toán tập hợp (giao, hiệu, gom nhóm) được tối ưu sẵn.

---

## 8. Các biến thể khác của cùng một lớp bài toán

Một khi đã nắm nguyên lý ở mục 7, có thể áp dụng ngay cho các bài toán tương tự:

| Bài toán | Bản chất | Giải pháp tương ứng |
|---|---|---|
| Kiểm tra Code trùng khi import Excel | Membership check | Temp Table + JOIN |
| Validate Foreign Key trước khi insert hàng loạt | Membership check | Temp Table + LEFT JOIN WHERE NULL |
| Đồng bộ dữ liệu giữa 2 hệ thống (tìm bản ghi mới/xoá/sửa) | Set difference (A - B, B - A) | Temp Table + FULL OUTER JOIN hoặc EXCEPT/MINUS |
| Kiểm tra danh sách mã số thuế Blacklist (FBA01512) | Membership check | Batch IN (nếu vài nghìn) hoặc Temp Table (nếu lớn hơn) |
| Phân quyền hàng loạt cho N user × N resource | Cartesian + filter | Temp Table chứa cặp (user, resource) rồi JOIN bảng quyền |
| Kiểm tra tồn kho cho N sản phẩm trong 1 đơn hàng lớn | Batch lookup | Batch IN hoặc Temp Table tuỳ số lượng dòng |

---

## 9. Checklist ra quyết định

Khi gặp bài toán "cần đối chiếu N phần tử input với dữ liệu trong Database", có thể tự hỏi theo thứ tự:

1. **N là bao nhiêu?**
   - Dưới vài trăm → Query từng dòng vẫn chấp nhận được nếu không phải hot path.
   - Vài trăm – vài nghìn → WHERE IN hoặc Batch IN.
   - Vài chục nghìn trở lên → Temp Table + JOIN.

2. **Có cần kiểm tra nhiều rule cùng lúc không?**
   - Có nhiều rule (FK check, duplicate check, business rule...) → ưu tiên Temp Table, vì thêm rule mới chỉ cần thêm SQL.

3. **Database có hỗ trợ Global Temporary Table không?**
   - DB2, Oracle, SQL Server đều hỗ trợ GTT ở các mức độ khác nhau — cần kiểm tra cú pháp và hành vi `ON COMMIT` cụ thể của từng hệ.

4. **Tần suất chức năng này được gọi có cao không?**
   - Nếu là chức năng chạy 1 lần/ngày (batch job) → có thể chấp nhận giải pháp đơn giản hơn (Batch IN) dù chưa tối ưu tuyệt đối.
   - Nếu là chức năng người dùng bấm trực tiếp và chờ kết quả (như FBA01512) → cần tối ưu độ trễ, ưu tiên giảm round-trip.

5. **Có giới hạn kỹ thuật của Database cần lưu ý không?**
   - Số phần tử tối đa trong `IN` (Oracle: 1000, DB2: giới hạn theo độ dài câu SQL).
   - Độ dài tối đa của một câu SQL.

---

## 10. Liên hệ với các vấn đề đã gặp trong hệ thống eBAF

Nguyên lý "đẩy logic xuống Database, tránh round-trip lặp lại" ở tài liệu này liên hệ trực tiếp tới một số vấn đề đã từng phân tích:

- **DB2 `IN` clause batching** ở màn hình FBA01512 (MST Blacklist) — chính là Phương án 3 trong tài liệu này, được áp dụng để tránh lỗi `SQLCODE -101 / SQLSTATE 54001` khi danh sách `TAX_CODES` quá dài.
- **N+1 query problem** đã ghi chú trong Obsidian — là biểu hiện lý thuyết của chính vấn đề mô tả ở mục 7.1, dù bối cảnh ban đầu là ORM lazy-loading còn ở đây là vòng lặp thủ công.
- **`SimpleCache` trong `AMA0_B066`** — cùng một triết lý "giảm round-trip", nhưng theo hướng khác: cache kết quả để tránh phải hỏi lại Database, thay vì gộp nhiều lần hỏi thành một lần hỏi lớn.

Ba kỹ thuật (Batching, Caching, Set-based/Temp Table) đều là các công cụ khác nhau để giải cùng một vấn đề gốc: **giảm số lần giao tiếp giữa hai tầng có chi phí giao tiếp cao (Application ↔ Database), bằng cách tăng lượng công việc hữu ích thực hiện được trong mỗi lần giao tiếp đó.**