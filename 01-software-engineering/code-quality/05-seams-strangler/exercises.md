# Bài tập · CQ-05 · Seams & Strangler Fig

Làm bài trước khi mở [solutions.md](solutions.md). Thực hành trên [lab/](lab/README.md).

---

## Level 1 · Hiểu Seam, Sprout & Wrap (4 bài)

### Bài 1.1 · Xác định Seam và Enabling Point
Trong đoạn code Java sau:

```java
OrderAuditSender sender = isDryRun ? event -> {} : new KafkaOrderAuditSender();
LegacyOrderHandler handler = new LegacyOrderHandler(sender);
handler.submit(order);
```

Hãy chỉ rõ: **Seam** nằm ở đâu, loại Seam gì (*Object Seam* hay *Link Seam*), và **Enabling Point** là dòng code nào?

### Bài 1.2 · Chọn đúng kỹ thuật: Sprout vs Wrap vs Parameterize
Ghép 3 tình huống dưới đây với đúng kỹ thuật (*Sprout Class*, *Wrap Method*, *Extract & Override*):
1. Muốn thêm nghiệp vụ tính điểm thưởng mới tinh (30 dòng rule) vào giữa một Servlet legacy quá dài chưa test nổi phần cũ.
2. Muốn đo latency + bắn audit message **mỗi khi** hàm legacy `approveLoan(...)` chạy xong mà không sửa một chữ nào bên trong `approveLoan(...)`.
3. Cần unit test chính logic bên trong một class DAO cũ đang gọi trực tiếp `DriverManager.getConnection(...)`.

### Bài 1.3 · Nhược điểm của Link Seam nếu lạm dụng
Trong ứng dụng Java EE / Maven, bạn có thể tạo một class cùng tên package ở `src/test/java` để "đè" class thật trong `src/main/java` trên test classpath (**Classpath Link Seam**). Tại sao ta hạn chế dùng Link Seam bằng cách tráo file classpath mà ưu tiên **Object Seam**?

### Bài 1.4 · Sai lầm kinh điển khi làm Strangler Fig
Team dự án đang chuyển dần JBoss sang Spring Boot theo Strangler Fig, nhưng cho cả JBoss lẫn Spring Boot tự ý `INSERT/UPDATE` trực tiếp lên cùng một bảng `ORDERS` ở Oracle DB. Nêu 2 hậu quả nghiêm trọng kiểu này và cách khắc phục (*Data Ownership + ACL*).

---

## Level 2 · Thực hành lab Sprout & Strangler Router (3 bài)

### Bài 2.1 · Chạy lab & chứng minh tính cô lập của Sprout Class
Chạy `mvn -o test` trong `05-seams-strangler/lab/`. Đọc [`TaxExemptionSproutPolicy.java`](lab/src/main/java/lab/cq/seams/TaxExemptionSproutPolicy.java) và [`LegacyInvoiceProcessor.java`](lab/src/main/java/lab/cq/seams/LegacyInvoiceProcessor.java):
1. Nếu sửa ngưỡng trợ cấp vùng xa `REMOTE_ZONE` từ `>= 1_000_000L` lên `>= 2_000_000L`, ta phải chạy test file nào và mất công fake DB hay server không?
2. Chỉ ra vì sao `calculateNetPayable(...)` trong `LegacyInvoiceProcessor` có nguy cơ lỗi *lệch VAT 10% trước/sau miễn giảm* thấp nhất có thể dù ta không viết lại logic cũ?

### Bài 2.2 · Thiết kế Strangler Nginx + Anti-Corruption Layer cho JBoss → Spring Boot
Hệ thống legacy trên JBoss AS 7.1.1 hiện có 3 nhóm URL:
- `/legacy/auth/*` (login session JBoss)
- `/legacy/orders/*` (tạo đơn cũ)
- `/legacy/reports/*` (xuất báo cáo chậm, đang cần tách trước sang Spring Boot 3 / Java 21)

Vẽ hoặc mô tả kiến trúc routing qua Nginx + cách xử lý DB schema khi tách nhóm `/reports` sang Spring Boot trước trong 2 tuần đầu.

### Bài 2.3 · Vì sao Wrap Method thường tốt hơn chèn trực tiếp vào cuối hàm cũ?
Xem hàm `calculateAndAudit(...)` trong `LegacyInvoiceProcessor.java`. Tại sao tác giả giữ nguyên `calculateNetPayable(...)` và viết hàm mới bọc bên ngoài thay vì nhồi `auditSink.record(...)` thẳng vào cuối `calculateNetPayable(...)`?

<details>
<summary>Gợi ý</summary>

Nhớ tính *Temporal / Single Responsibility*: người gọi khác chỉ muốn tính thử nháp số tiền (`calculateNetPayable`) không hề muốn bị ghi bản ghi audit giả; ngoài ra giữ hàm gốc nguyên vẹn giúp zero diff rủi ro lên caller hiện hữu.

</details>

---

## Level 3 · Dựng Strangler Switch có tự động Fallback (1 bài)

### Bài 3.1 · Triển khai Feature Flag & Auto-Fallback bảo vệ ngày cắt chuyển
Khi chạy thử engine Spring Boot/Modern mới qua Strangler Router cho nhóm `PILOT_TENANTS`, nếu dịch vụ mới gặp lỗi (`RuntimeException`, timeout HTTP, bug mới phát hiện), hệ thống không được làm chết giao dịch của khách.

**Yêu cầu**:
1. Đọc và chạy [`lab/solutions/StranglerFallbackSwitch.java`](lab/solutions/StranglerFallbackSwitch.java) (`mvn -o -q compile` → `java -cp target\classes solutions\StranglerFallbackSwitch.java`).
2. Quan sát kết quả của 3 tenant (`TENANT-STD`, `TENANT-PILOT`, `TENANT-ERR`): xác nhận `TENANT-ERR` dù thuộc danh sách pilot vẫn tự động **fallback** an toàn về đúng giá legacy `1100000`.
3. Mở rộng thêm quy tắc: chỉ ghi log cảnh báo fallback và kích hoạt cờ fallback tự động nếu số lượng lỗi vượt ngưỡng (hoặc ghi nhận metric counter), tránh silent failure làm team vận hành không hay biết microservice mới đang chết âm thầm!

**Tiêu chí đạt**: Chạy `StranglerFallbackSwitch.java` in ra `TENANT-PILOT -> 1075000`, dòng cảnh báo `[FALLBACK]`, và `TENANT-ERR -> 1100000` không crash tiến trình.
