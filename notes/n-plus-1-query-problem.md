# N+1 Query Problem trong Chuyển đổi Dữ liệu & ORM
**Loại**: #concept  
**Ngày**: 2026-08-02  
**Bối cảnh**: Bài toán hiệu năng thực tế tại module `FBA0_2000` (Xử lý file danh sách Mã số thuế Blacklist) và các màn hình hiển thị danh sách lớn trong eBAF.

---

## 1. Khái niệm Cốt lõi (N+1 Query Problem là gì?)

**N+1 Query Problem** là một anti-pattern về hiệu năng ứng dụng truy cập cơ sở dữ liệu. Hiện tượng này xảy ra khi ứng dụng cần lấy danh sách $N$ bản ghi cùng với các dữ liệu liên quan của chúng, nhưng thay vì dùng **1 câu query gộp (JOIN hoặc WHERE IN)**, mã nguồn lại thực hiện:
- **1 câu query đầu tiên** để lấy danh sách $N$ bản ghi cha.
- **$N$ câu query tiếp theo** nằm trong vòng lặp `for` để lấy dữ liệu con tương ứng với từng bản ghi cha.

Tổng số câu query thực thi tới Cơ sở dữ liệu là:  
$$\text{Total Queries} = 1 + N$$

Nếu $N = 1000$ bản ghi, ứng dụng sẽ gửi **1001 câu SQL** riêng biệt tới Cơ sở dữ liệu!

---

## 2. Bản chất Kỹ thuật & Tác hại Tới Hiệu năng

### 2.1 Chi phí Chi tiết của một câu HTTP / SQL Round-Trip
Nhiều lập trình viên lầm tưởng rằng mỗi câu query SQL đơn giản chỉ mất $1\text{ms}$, nên 1000 câu query chỉ mất $1\text{s}$. Tuy nhiên, chi phí thực tế cho mỗi câu query SQL bao gồm:

```
[App Server] ──(1. App Overhead)──> ──(2. Network Latency)──> [DB Server] 
                                                                   │
[App Server] <──(5. Result Parse)── <──(4. Network Latency)── <──(3. SQL Parse & Exec)
```

1. **Network Round-Trip Latency (Độ trễ mạng)**: Giữa App Server và DB Server (thường mất từ $1\text{ms} - 5\text{ms}$).
2. **Context Switching & Connection Pool Overhead**: Chi phí mượn/trả Connection trong Pool.
3. **DB Parsing & Execution Plan**: DB phải parse, compile và check permission cho từng câu SQL.

=> Với $N = 1000$ và độ trễ mạng $2\text{ms}$ mỗi lượt, tổng thời gian chờ lãng phí riêng cho mạng đã là:  
$$1000 \times 2\text{ms} = 2000\text{ms} = 2 \text{ giây}!$$

---

## 3. Sơ đồ Luồng Thực thi (Mermaid Diagram)

### Sơ đồ 1: Luồng dính lỗi N+1 Query (1 + N Round-Trips)

```mermaid
sequenceDiagram
    autonumber
    participant App as Application Server (Java)
    participant DB as Database (DB2)

    App->>DB: 1. SELECT * FROM customers; (Query 1)
    DB-->>App: Trả về danh sách N = 4 Khách hàng (IDs: 1, 2, 3, 4)

    loop Vòng lặp N lần (for each customer)
        App->>DB: 2. SELECT * FROM contracts WHERE customer_id = 1;
        DB-->>App: Trả về Hợp đồng của Customer 1
        App->>DB: 3. SELECT * FROM contracts WHERE customer_id = 2;
        DB-->>App: Trả về Hợp đồng của Customer 2
        App->>DB: 4. SELECT * FROM contracts WHERE customer_id = 3;
        DB-->>App: Trả về Hợp đồng của Customer 3
        App->>DB: 5. SELECT * FROM contracts WHERE customer_id = 4;
        DB-->>App: Trả về Hợp đồng của Customer 4
    end
    Note over App,DB: Tổng cộng: 1 + 4 = 5 câu Queries!
```

### Sơ đồ 2: Luồng Tối ưu bằng Batch Query / JOIN (Chỉ 1 hoặc 2 Queries)

```mermaid
sequenceDiagram
    autonumber
    participant App as Application Server (Java)
    participant DB as Database (DB2)

    App->>DB: 1. SELECT * FROM customers; (Query 1)
    DB-->>App: Trả về N = 4 Khách hàng (IDs: 1, 2, 3, 4)

    Note over App: Gom tất cả IDs thành danh sách: [1, 2, 3, 4]
    
    App->>DB: 2. SELECT * FROM contracts WHERE customer_id IN (1, 2, 3, 4); (Query 2)
    DB-->>App: Trả về TOÀN BỘ Hợp đồng của 4 khách hàng

    Note over App: Group danh sách Hợp đồng theo customer_id trong RAM Java
    Note over App,DB: Tổng cộng: CHỈ CÓ 2 câu Queries! (Giảm 60% thời gian)
```

---

## 4. Phân tích Case Study Thực tế: Module `FBA0_2000` (Mã số thuế Blacklist)

### Bối cảnh Thực tế ở Module `FBA0_2000`
- **Nghiệp vụ**: Người dùng upload file Excel chứa danh sách **10,000 Mã số thuế (MST)** cần thêm vào danh sách đen (Blacklist).
- **Code ban đầu (Dính N+1)**:
  ```java
  List<String> taxCodesFromFile = parseExcelFile(file); // 10,000 dòng
  
  for (String taxCode : taxCodesFromFile) {
      // VỚI MỖI DÒNG, CHẠY 1 CÂU QUERY KIỂM TRA TỒN TẠI TRONG DB2!
      boolean exists = mstBlacklistDao.existsByTaxCode(taxCode); // SELECT COUNT(*) FROM mst_blacklist WHERE tax_code = ?
      if (!exists) {
          mstBlacklistDao.insert(taxCode);
      }
  }
  ```
- **Hậu quả**:
  - Upload file 10,000 dòng mất **hơn 4 phút**!
  - DB2 log báo ngập hàng chục nghìn câu SQL `SELECT COUNT(*)`, CPU DB2 tăng lên 90%.

### Giải pháp Tối ưu hóa (Batch Lookup in Memory):
1. **Bước 1**: Đọc toàn bộ 10,000 MST từ file Excel vào danh sách `List<String> listFromExcel`.
2. **Bước 2**: Query 1 câu duy nhất lấy toàn bộ MST đã tồn tại trong DB thuộc danh sách trên:
   ```sql
   SELECT tax_code FROM mst_blacklist WHERE tax_code IN ('MST1', 'MST2', ..., 'MST1000');
   ```
   *(Chia chunk 1000 phần tử mỗi lần query nếu danh sách quá lớn).*
3. **Bước 3**: Lưu kết quả DB vào `Set<String> existingTaxCodesInDb` trong RAM của Java.
4. **Bước 4**: Lọc các MST chưa tồn tại bằng `Set.contains()` (Độ phức tạp $O(1)$) và thực hiện **Batch Insert**.

=> **Kết quả**: Thời gian xử lý file 10,000 dòng giảm từ **4 phút (240s) xuống còn 3.5 giây** (Cải thiện hiệu năng ~ 70 lần!).

---

## 5. Các Kỹ thuật Khắc phục N+1 trong ORM (Hibernate / JPA)

Nếu ứng dụng sử dụng ORM (Hibernate / Spring Data JPA), tính năng **Lazy Loading** (`FetchType.LAZY`) là nguyên nhân chính tự động kích hoạt N+1 query khi gọi hàm getter (VD: `customer.getContracts()`).

Các cách khắc phục:

### Cách 1: Sử dụng `JOIN FETCH` trong JPQL / HQL
```java
// Dính N+1
@Query("SELECT c FROM Customer c")
List<Customer> findAllCustomers(); 

// ĐÃ FIX: Chỉ chạy 1 câu SQL JOIN duy nhất!
@Query("SELECT c FROM Customer c JOIN FETCH c.contracts")
List<Customer> findAllCustomersWithContracts();
```

### Cách 2: Sử dụng `@EntityGraph` (Spring Data JPA)
```java
@EntityGraph(attributePaths = {"contracts"})
@Query("SELECT c FROM Customer c")
List<Customer> findAllWithContractsEntityGraph();
```

### Cách 3: Sử dụng Cấu hình `@BatchSize` (Hibernate)
Giúp Hibernate tự động gộp các câu Lazy Query thành `WHERE id IN (?, ?, ?, ?)` theo kích thước batch chỉ định.

```java
@Entity
public class Customer {
    @OneToMany(mappedBy = "customer")
    @BatchSize(size = 50) // Gộp 50 request lazy loading thành 1 câu IN (...)
    private List<Contract> contracts;
}
```

---

## 6. Bảng So sánh Các Giải pháp Tối ưu

| Phương pháp | Mô tả | Ưu điểm | Nhược điểm |
|---|---|---|---|
| **SQL `JOIN` / `JOIN FETCH`** | Gộp bảng Cha và Con trong 1 câu SQL duy nhất. | Chỉ mất đúng 1 câu SQL query. | Dữ liệu bảng Cha bị lặp lại nhiều lần nếu bảng Con có nhiều dòng (Cartesian Product). |
| **Batch Query `WHERE IN`** | 1 query lấy Cha + 1 query `IN (...)` lấy toàn bộ Con. | Tránh trùng lặp dữ liệu truyền qua mạng, code kiểm soát tốt. | Cần gom ID và map thủ công trong RAM Java nếu viết SQL thuần. |
| **Hibernate `@BatchSize`** | Gom các câu Lazy query thành từng cụm IN (VD: size 50). | Rất dễ cấu hình, không cần sửa nhiều câu HQL. | Vẫn tốn nhiều hơn 1-2 câu query (nhưng giảm được 95% số query). |
| **Memory Lookup (`Set<String>`)** | Load dữ liệu tham chiếu vào `Set/Map` RAM để tra cứu $O(1)$. | Cực nhanh cho các bài toán Import/Batch Excel (như `FBA0_2000`). | Tốn RAM nếu dung lượng bảng tham chiếu quá lớn (hàng triệu dòng). |

---

## 7. Phương pháp Phát hiện N+1 Query

1. **Bật Log SQL ở môi trường Development**:
   - Trong Spring Boot / Hibernate: `spring.jpa.show-sql=true` và `logging.level.org.hibernate.SQL=DEBUG`.
   - Quan sát nếu thấy 1 câu SQL lặp đi lặp lại hàng chục lần với chỉ tham số ID thay đổi -> **Đã dính N+1**.
2. **Sử dụng Công cụ Giám sát Proxy (p6spy)**:
   - Thư viện `p6spy` log ra chính xác số lượng query và thời gian thực thi của từng HTTP Request.
3. **Thư viện Assertion trong Unit Test (`QuickPerf` / `Hibernate-Assert`)**:
   - Viết unit test khẳng định một hàm API không được vượt quá $X$ câu SQL query.

---

## 8. Cây Quyết định Chọn Giải pháp (Decision Tree)

```
Bạn đang làm chức năng gì?
 ├── 1. Xử lý File Import / Batch Data lớn (VD: FBA0_2000 upload Excel)
 │    └── CHỌN: Batch Memory Lookup (Query IN(...) gom ID + Set/Map Java RAM)
 │         └── Quy tắc: Tuyệt đối KHÔNG đặt câu SELECT DB bên trong vòng lặp đọc dòng Excel!
 └── 2. Màn hình Hiển thị Danh sách trên Web (API Trả về DTO)
      ├── Sử dụng SQL thuần / JDBC (eBAF DAO)
      │    └── CHỌN: Viết SQL JOIN hoặc 2 câu Query (Query 1 lấy Parent, Query 2 SELECT ... WHERE id IN (...))
      └── Sử dụng Spring Data JPA / Hibernate ORM
           ├── Cần lấy luôn danh sách Con kèm theo -> CHỌN: JOIN FETCH hoặc @EntityGraph
           └── Danh sách Con lớn và chỉ cần truy cập thỉnh thoảng -> CHỌN: @BatchSize(size = 100)
```

---

## 9. Tài liệu & Bài viết liên quan
- [[transaction-boundary]] — Giữ Connection Pool hiệu quả khi tối ưu query.
- [[sync-vs-async-batch-processing]] — Tối ưu N+1 trong các tác vụ Batch Report.
- [[../bug-cases/]] — Chi tiết Bug-case thực tế và số liệu benchmark của `FBA0_2000`.