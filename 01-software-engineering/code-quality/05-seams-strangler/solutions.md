# Đáp án · CQ-05 · Seams & Strangler Fig

## Đáp án Bài tập

### Level 1

#### Bài 1.1 · Seam & Enabling Point
- **Seam**: Lời gọi `sender.send(...)` bên trong `LegacyOrderHandler.submit(...)` (**Object Seam** — hành vi thay đổi dựa vào instance `OrderAuditSender` được tiêm vào).
- **Enabling Point**: Dòng `LegacyOrderHandler handler = new LegacyOrderHandler(sender);` (nơi chọn `isDryRun ? ... : new KafkaOrderAuditSender()` mà không cần sửa thân hàm `submit`).

#### Bài 1.2 · Ghép kỹ thuật
1. Thêm 30 dòng rule mới sạch giữa Servlet dài → **Sprout Class** (viết rule mới + unit test độc lập rồi gọi 1 dòng từ Servlet).
2. Đo latency + audit trước/sau hàm `approveLoan(...)` mà không đụng ruột hàm → **Wrap Method**.
3. Test chính ruột DAO cũ dính `DriverManager` → **Extract & Override Factory Method** (tạo Object Seam cho connection).

#### Bài 1.3 · Nhược điểm của Classpath Link Seam
Link Seam bằng tráo file trên classpath/build không nhìn thấy được ngay trong mã nguồn khi đọc bằng IDE (*Obscurity* cao), dễ gây nhầm lẫn giữa các developer mới, khó chuyển đổi hai impl ngay trong cùng một suite test (chỉ một class thắng trên classpath tại 1 lần chạy JVM).

#### Bài 1.4 · Hai hệ thống cùng sửa một bảng DB khi Strangler
- **Hậu quả**: (1) Xung đột business validation (JBoss kiểm tra invariant A trong code Java rồi mới save, Spring Boot thiếu hoặc kiểm tra khác → dữ liệu rác lọt vào DB); (2) Không biết code bên nào chịu trách nhiệm rollback/lock hay ai gây lệch dữ liệu khi debug production.
- **Khắc phục**: Phân định rành mạch **Single Writer / Owner** cho từng bảng; bên kia nếu cần đọc dữ liệu cũ thì đọc view/ACL API hoặc đồng bộ sự kiện qua Change Data Capture / Outbox chứ không tự ý update trực tiếp vào bảng của bên kia.

---

### Level 2

#### Bài 2.1 · Cô lập Sprout Class
1. Chỉ cần mở `TaxExemptionSproutPolicy` sửa đúng hằng số `1_000_000L -> 2_000_000L` và chạy `SeamsStranglerTest` (chạy dưới 100 ms, hoàn toàn không phụ thuộc DB/JBoss).
2. Vì mọi logic VAT `0.10` cũ nằm nguyên trong `calculateNetPayable`, chỗ sửa vào code cũ **chỉ là 1 dòng** gọi `sproutPolicy.evaluate(regionCode, gross, nonProfit)`, không chạm vào biến tính thuế hay vòng đời cũ.

#### Bài 2.2 · Kiến trúc Strangler Nginx + ACL cho `/legacy/reports/*`
1. **Routing Edge (Nginx)**: giữ `/legacy/auth/*` và `/legacy/orders/*` trỏ về `jboss:8080`; riêng `/legacy/reports/*` trỏ sang `springboot-reports:8081`.
2. **Auth Seam**: Nginx hoặc JBoss phát JWT / Shared Redis Session Token để Spring Boot nhận diện được user đang đăng nhập mà không cần viết lại module auth trước.
3. **Database / ACL**: Spring Boot ở giai đoạn đầu đọc từ **Read-Replica / Read-Only SQL View** (hoặc gọi REST/ACL qua JBoss) để render báo cáo mà tuyệt đối không sửa schema hay khóa bảng giao dịch của JBoss.

#### Bài 2.3 · Lý do bọc bằng Wrap Method
Giữ `calculateNetPayable` làm pure query function giúp các caller khác (như màn hình "Xem trước giá giỏ hàng") gọi tính thử nháp mà **không vô tình bắn log audit**. Đồng thời code cũ không bị trộn thêm side-effect I/O, còn caller thật cần tính tiền kèm audit chỉ việc gọi hàm wrapper `calculateAndAudit`.

---

### Level 3

#### Bài 3.1 · `StranglerFallbackSwitch`
Xem code tại [`lab/solutions/StranglerFallbackSwitch.java`](lab/solutions/StranglerFallbackSwitch.java). Kết quả chạy thực tế:

```text
TENANT-STD   -> 1100000
TENANT-PILOT -> 1075000
[FALLBACK] Reverting TENANT-ERR to legacy due to: New microservice timeout
TENANT-ERR   -> 1100000
```

Lưu ý vận hành thật (*mục 3*): trong khối `catch` fallback, luôn phải ghi log ERROR kèm `tenantId` / stacktrace **và** tăng metric cảnh báo (`strangler_fallback_total++`) để Alertmanager báo ngay thay vì để tính năng mới âm thầm fallback 100% về legacy suốt nhiều tuần!

---

## Đáp án Câu hỏi tự kiểm tra

<details>
<summary>1. Seam và Enabling Point trong `new LegacyInvoiceProcessor(captured::add)`?</summary>

- **Seam**: lời gọi `auditSink.record(...)` bên trong `calculateAndAudit` (**Object Seam**).
- **Enabling Point**: biểu thức truyền `captured::add` vào constructor `new LegacyInvoiceProcessor(captured::add)` (nơi quyết định hành vi của seam lúc test mà không đụng ruột hàm).

</details>

<details>
<summary>2. Vì sao Sprout Class tốt hơn nhét thẳng rule mới vào hàm 600 dòng chưa test?</summary>

Vì nếu viết thẳng vào hàm cũ, code mới cũng không thể unit test độc lập được (bị cột chung dependency với 600 dòng cũ) và diff rộng dễ làm hỏng logic cũ. Đưa sang Sprout Class cho phép phủ 100% unit test cho logic mới và tối thiểu hoá số dòng đụng vào code legacy.

</details>

<details>
<summary>3. Khác biệt mục đích giữa Sprout Method và Wrap Method?</summary>

- **Sprout Method/Class**: dùng khi cần chèn thêm một mảnh nghiệp vụ *mới phát sinh vào bên trong* một quy trình cũ.
- **Wrap Method**: dùng khi muốn *bọc thêm* hành vi xảy ra ngay trước hoặc ngay sau toàn bộ hàm cũ (logging, audit, metrics, decoration) mà không sửa bất kỳ dòng nào bên trong hàm cũ.

</details>

<details>
<summary>4. Anti-Corruption Layer (ACL) trong Strangler Fig từ JBoss sang Spring Boot giải quyết gì?</summary>

ACL đóng vai trò bộ chuyển đổi tại biên giới giữa service Spring Boot sạch sẽ với hệ thống JBoss/Oracle cũ: nó dịch schema cột viết tắt lộn xộn, mã lỗi lạ hay XML cũ sang Domain Model sạch của Spring Boot, ngăn không cho thiết kế chắp vá của legacy "lây lan" vào codebase mới.

</details>

<details>
<summary>5. Tại sao Strangler theo tenant/canary + công tắc fallback an toàn hơn cắt 100% ngay?</summary>

Vì dù test giả lập kỹ đến đâu, dữ liệu production thực tế luôn có dị biệt. Chuyển từng lát nhỏ cho một nhóm `pilot tenant` giúp giới hạn bán kính sự cố nếu lệch số liệu, và công tắc fallback tự động trả request lỗi về JBoss giúp zero-downtime cho người dùng cuối trong lúc chờ vá lỗi ở service mới.

</details>
