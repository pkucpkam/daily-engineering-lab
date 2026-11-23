# Transaction Boundary trong Framework nội bộ (eBAF) & Spring
**Loại**: #concept  
**Ngày**: 2026-08-02  
**Bối cảnh**: Quản lý giao dịch cơ sở dữ liệu trong eBAF Framework, xử lý batch import `FBA0_2000` (DB2 Error `-803`), và chống lỗi Silent Rollback.

---

## 1. Khái niệm Cốt lõi (Transaction Boundary là gì?)

**Transaction Boundary (Ranh giới Giao dịch)** là vị trí điểm đầu (**Begin**) và điểm kết thúc (**Commit** hoặc **Rollback**) xác định một tập hợp các thao tác truy vấn Cơ sở dữ liệu (Insert, Update, Delete) được gom lại thành **Một đơn vị công việc nguyên tử (Atomic Unit of Work)**.

Theo nguyên lý **ACID**:
- **Tính Nguyên tử (Atomicity)**: Hoặc TOÀN BỘ các câu lệnh SQL trong ranh giới transaction đều thành công (`COMMIT`), hoặc NẾU CÓ BẤT KỲ LỖI NÀO, toàn bộ các thay đổi phải được khôi phục về trạng thái ban đầu (`ROLLBACK`). Không bao giờ có trạng thái "lưu một nửa, hỏng một nửa".

---

## 2. Các Mô hình Quản lý Ranh giới Transaction

### 2.1 Declarative Transaction (Quản lý Tự động / Tuyên bố)
- **Cơ chế**: Dùng Filter / Interceptor / Spring `@Transactional` để bao bọc toàn bộ một HTTP Request hoặc một Service Method.
- **Quy trình**:
  1. Khi bắt đầu vào Service Method -> Framework tự động xin 1 Connection từ Pool và gọi `connection.setAutoCommit(false)` (Bắt đầu Transaction).
  2. Thực thi các hàm DAO/SQL bên trong.
  3. Nếu hàm kết thúc bình thường -> Framework tự động gọi `connection.commit()`.
  4. Nếu có Exception văng ra ngoài -> Framework tự động gọi `connection.rollback()`.

- **Ưu điểm**: Code sạch sẽ, không phải viết try-catch commit/rollback thủ công.
- **Rủi ro**: Nếu không hiểu rõ cách Exception lan truyền (Propagation), ứng dụng dễ mắc lỗi **Silent Rollback (Rollback Âm thầm)** hoặc **Transaction Kéo dài (Long-Running Transaction)**.

---

### 2.2 Programmatic Transaction (Quản lý Thủ công / Bằng Mã nguồn)
- **Cơ chế**: Lập trình viên tự trực tiếp gọi các hàm quản lý Connection/Transaction.
- **Quy trình**:
  ```java
  Connection conn = dataSource.getConnection();
  try {
      conn.setAutoCommit(false); // BẮT ĐẦU TRANSACTION BOUNDARY
      
      // Thao tác 1
      dao1.update(conn, data1);
      // Thao tác 2
      dao2.insert(conn, data2);
      
      conn.commit(); // COMMIT TRANSACTION BOUNDARY
  } catch (SQLException e) {
      conn.rollback(); // ROLLBACK TRANSACTION BOUNDARY
      throw e;
  } finally {
      conn.close();
  }
  ```
- **Phù hợp**: Các tác vụ Batch Processing lớn cần chia nhỏ chunk (VD: cứ 500 dòng commit 1 lần để tránh tràn Undo Log / Transaction Log DB2).

---

## 3. Sơ đồ Luồng Thực thi (Mermaid Diagram)

### Sơ đồ: Ranh giới Transaction & Antipattern Silent Rollback

```mermaid
sequenceDiagram
    autonumber
    actor User as User / UI Client
    participant Controller as Controller Layer
    participant Service as Service Layer (@Transactional)
    participant DAO as DAO Layer
    participant DB as Database (DB2)

    User->>Controller: POST /batchUpload (100 dòng dữ liệu)
    Controller->>Service: processBatchData()
    Note over Service,DB: Framework BẮT ĐẦU Transaction (Begin TX)

    Service->>DAO: insertLine(Dòng 1..99)
    DAO->>DB: SQL INSERT (Tạm lưu vào Transaction Log)
    
    Service->>DAO: insertLine(Dòng 100 - Bị trùng Unique Key!)
    DAO->>DB: SQL INSERT -> DB2 Trả lỗi -803 (Duplicate Key)
    
    Note over DAO: DAO nuốt Exception!<br/>try { insert() } catch(Exception e) { log.error("Lỗi dòng 100"); }
    DAO-->>Service: Trả về thành công (Không throw exception lên Service)
    
    Service-->>Controller: Hoàn thành hàm processBatchData()
    Note over Service,DB: Framework thấy hàm chạy xong êm đẹp,<br/>gọi COMMIT TX!

    alt Nếu DB2 cho phép
        DB-->>Service: Commit 99 dòng, bỏ dòng 100 (Dữ liệu bị sai lệch!)
    else Nếu Spring Transaction Manager đánh dấu Rollback-Only
        Note over DB: UnexpectedRollbackException!<br/>Spring rollback sạch cả 100 dòng!
        Service-->>Controller: Lỗi 500
        Controller-->>User: Báo lỗi chung chung (User không biết dòng nào hỏng)
    end
```

---

## 4. Phân tích Case Study Thực tế: Module `FBA0_2000` (Lỗi DB2 `-803` Silent Rollback)

### Bối cảnh Bug Thực tế tại `FBA0_2000`
- **Mô tả nghiệp vụ**: Import 1,000 mã số thuế từ file Excel vào bảng `MST_BLACKLIST`.
- **Cấu hình Transaction**: Toàn bộ hàm `importExcel()` được bọc trong 1 Transaction duy nhất theo Request.
- **Diễn biến sự cố**:
  1. File Excel chứa 1,000 dòng. Dòng thứ 850 bị lặp lại một MST đã có trong DB2.
  2. DB2 trả về mã lỗi **`SQLCODE: -803, SQLSTATE: 23505`** (*Duplicate key value specified for unique index*).
  3. Đoạn code tầng DAO viết:
     ```java
     try {
         insertSingleMst(mst);
     } catch (Exception e) {
         log.error("Không insert được MST: " + mst); // NUỐT EXCEPTION!
     }
     ```
  4. **Hậu quả nghiêm trọng**:
     - Vì DAO đã `catch` và nuốt exception, tầng Service không biết có lỗi.
     - Cuối request, Framework cố gắng `COMMIT`. Tuy nhiên, kết nối DB2 đã bị đánh dấu `Rollback-Only` ngay từ thời điểm xảy ra lỗi `-803`.
     - Spring văng ra lỗi `UnexpectedRollbackException`. **Toàn bộ 999 dòng hợp lệ trước đó bị ROLLBACK SẠCH SẼ**.
     - UI hiển thị không rõ ràng khiến người dùng ngỡ là đã lưu thành công 999 dòng, nhưng kiểm tra DB thì hoàn toàn **trống rỗng**!

---

## 5. Các Mức độ Lan truyền Transaction (Spring Transaction Propagation)

Khi Service A gọi Service B, ranh giới Transaction được xử lý theo các mode phổ biến:

| Propagation Mode | Cơ chế hoạt động | Trường hợp sử dụng |
|---|---|---|
| **`REQUIRED`** *(Mặc định)* | Sử dụng lại Transaction hiện tại. Nếu chưa có, tạo mới. | Phù hợp cho 90% các nghiệp vụ CRUD thông thường. |
| **`REQUIRES_NEW`** | **Luôn tạo một Transaction mới hoàn toàn**, tạm dừng Transaction cũ. | Dùng cho tác vụ độc lập: Ghi Log Audit DB, Gửi Notification (Dù nghiệp vụ chính có rollback thì log audit vẫn phải được commit!). |
| **`NESTED`** | Tạo một **Savepoint** trong Transaction hiện tại. | Cho phép rollback từng phần (Rollback về Savepoint) mà không làm hỏng toàn bộ Transaction cha. |

---

## 6. Quy tắc Vàng để Thiết kế Transaction Boundary Chuẩn

### Quy tắc 1: Tuyệt đối KHÔNG nuốt Exception trong Ranh giới Transaction
Nếu `catch` Exception ở tầng dưới, bắt buộc phải:
- **Hoặc re-throw Exception đó lên**: `throw new BusinessException(e);`
- **Hoặc đánh dấu Rollback thủ công**: `TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();`

### Quy tắc 2: Phân đoạn Transaction cho các Tác vụ Batch lớn (Chunked Transaction)
Đối với file 10,000 dòng, không bọc cả 10,000 dòng vào 1 Transaction. Phải chia thành các Chunk (VD: 500 dòng/chunk):

```java
public void processLargeBatch(List<DataRow> rows) {
    List<List<DataRow>> chunks = Lists.partition(rows, 500);
    
    for (List<DataRow> chunk : chunks) {
        try {
            // Mỗi chunk chạy trong 1 Transaction riêng biệt (REQUIRES_NEW)
            batchService.processSingleChunk(chunk); 
        } catch (Exception e) {
            log.error("Chunk bị lỗi, chỉ rollback 500 dòng này: ", e);
            // Các chunk trước đó vẫn được lưu an toàn!
        }
    }
}
```

### Quy tắc 3: Giữ Ranh giới Transaction Ngắn nhất Có thể
- **KHÔNG** gọi API bên thứ ba (RestTemplate/FeignClient), **KHÔNG** đọc/ghi file I/O, và **KHÔNG** tính toán nặng bên trong một Transaction DB đang mở.
- Mở Transaction -> Cập nhật DB nhanh -> Commit và Đóng Transaction ngay lập tức để trả Connection về Pool.

---

## 7. Bảng So sánh: Single TX vs Chunked TX vs Independent TX

| Tiêu chí | Single Transaction (1 TX cho cả Batch) | Chunked Transaction (Mỗi 500 dòng 1 TX) | Independent Log TX (`REQUIRES_NEW`) |
|---|---|---|---|
| **Phạm vi Rollback** | Rollback toàn bộ 100% dữ liệu nếu có 1 lỗi nhỏ | Chỉ rollback đúng 500 dòng thuộc Chunk bị lỗi | Không bị rollback theo nghiệp vụ chính |
| **Dung lượng Transaction Log DB** | Cực lớn (Dễ gây tràn Undo Log/Redo Log DB2) | Nhỏ, an toàn cho DB engine | Cực nhỏ |
| **Tốc độ thực thi** | Nhanh hơn một chút (Chỉ commit 1 lần) | Tương đương (Thêm vài lần commit) | Tách biệt |
| **Độ phức tạp xử lý UI** | Người dùng phải sửa lỗi và upload lại từ đầu | Có thể báo rõ: "Lưu thành công 950 dòng, 50 dòng bị lỗi" | Trả về kết quả bình thường |

---

## 8. Cây Quyết định Chọn Ranh giới Transaction (Decision Tree)

```
Nghiệp vụ đang phát triển là gì?
 ├── 1. Ghi Log Audit / Log Lỗi vào Database
 │    └── CHỌN: @Transactional(propagation = Propagation.REQUIRES_NEW)
 │         └── Mục đích: Đảm bảo Log luôn được lưu kể cả khi nghiệp vụ chính bị Rollback.
 ├── 2. Xử lý Giao dịch Web Online thông thường (Tạo đơn hàng, Đổi mật khẩu)
 │    └── CHỌN: @Transactional(propagation = Propagation.REQUIRED)
 │         └── Quy tắc: Đảm bảo Exception văng ra ngoài Controller để Spring tự Rollback.
 └── 3. Xử lý File Import / Batch lớn (VD: FBA0_2000 upload Excel)
      └── CHỌN: Chunked Transaction Pattern
           ├── Chia nhỏ danh sách thành các Chunk 500 - 1000 bản ghi.
           ├── Mỗi Chunk gọi một hàm bọc @Transactional(propagation = Propagation.REQUIRES_NEW).
           └── Thu thập danh sách các bản ghi lỗi của từng Chunk để trả về báo cáo chi tiết cho UI.
```

---

## 9. Tài liệu & Bài viết liên quan
- [[sync-vs-async-batch-processing]] — Kết hợp Chunked Transaction với Async Worker.
- [[pessimistic-vs-optimistic-lock]] — Quản lý Lock bên trong ranh giới Transaction.
- [[../bug-cases/]] — Phân tích chi tiết Bug-case `fba0-2000-db2-803-silent-rollback`.