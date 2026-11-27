# READ-01 · Bài tập — Phương pháp đọc code lạ

> Làm theo thứ tự. **Tự làm trước, chỉ mở hint khi đã thử ≥ 15 phút.** Đáp án ở [solutions.md](./solutions.md) — chỉ mở sau khi đã có câu trả lời của riêng bạn.
> Ghi thời gian làm từng bài vào `my-notes.md` để theo dõi tiến bộ.

| Cấp | Mục đích | Số lượng | Thời gian gợi ý |
| :--- | :--- | :---: | :---: |
| 🟢 L1 — Recall | Nhớ & hiểu khái niệm | 5 | 20' |
| 🟡 L2 — Apply | Viết code / phân tích tình huống cụ thể | 3 | 75' |
| 🔴 L3 — Deep | Debug, thiết kế, đo đạc, tình huống mở | 1 | 90' |

---

## 🟢 L1 — Recall

### E1. Top-down vs Bottom-up trên dự án thật
Khi mới nhận một repository hoàn toàn lạ, điều gì xảy ra nếu bạn đọc thuần **Bottom-up** (mở các class `util/`, `entity/`, `repository/` trước) trong 3 giờ đầu? Khi nào nên kết hợp Bottom-up vào chiến lược Outside-In?

---

### E2. Tìm Entry Points trong 2 loại kiến trúc
Liệt kê **ít nhất 3 dấu hiệu/file** giúp bạn xác định toàn bộ Entry Points trong vòng 10 phút cho mỗi hệ thống sau:
1. Một ứng dụng Spring Boot 3 hiện đại.
2. Một ứng dụng legacy **Java 7 / JBoss AS 7.1.1**.

---

### E3. Vì sao đọc Test trước & khi nào Test đánh lừa bạn?
1. Đọc test trước giúp rút ngắn việc đọc hiểu code ở 3 điểm nào?
2. Kể 2 trường hợp mà test **có thể khiến bạn hiểu sai** về hành vi thật ở production.

---

### E4. Chọn thao tác Debugger đúng
Bạn đặt breakpoint tại `RequestDispatcher.dispatch` và muốn trace luồng `POST /api/orders`. Cho biết nên dùng thao tác nào (*Step Over*, *Step Into*, *Step Out*, *Conditional Breakpoint*, *Call Stack*) cho từng tình huống:
1. Gặp dòng `UUID.randomUUID().toString()` (không cần biết JDK sinh UUID ra sao).
2. Muốn nhảy qua 999 item hợp lệ trong vòng lặp và chỉ dừng lại đúng item có `sku.equals("SKU-OUT")`.
3. Lỡ bấm nhầm chui sâu vào bên trong `BigDecimal.multiply(...)`.
4. Muốn biết chính xác các lớp filter/proxy nào đã chạy trước khi tới `OrderRepository.save()`.

---

### E5. Bốn tín hiệu "Dừng đọc sâu"
Bạn đang tìm nguyên nhân đơn hàng không giảm tồn kho khi lỗi thanh toán, nhưng đang mất 35 phút đọc hàm helper mã hóa HMAC-SHA256 trong `SignatureUtil.java`. Bạn đã vi phạm những tiêu chí dừng (Stop Criteria) nào? Cách xử lý đúng là gì?

---

## 🟡 L2 — Apply

### E6. Vẽ Sequence Diagram của request `/api/orders` trong `lab/`
**Bối cảnh:** Mở [lab/](./lab/README.md) như một codebase hoàn toàn mới. Áp dụng đúng thứ tự: đọc `pom.xml` → đọc `OrderFlowTest` → trace code từ `RequestDispatcher` xuống `OrderRepository` & `InventoryGateway`.

**Yêu cầu:**
1. Vẽ **1 Sequence Diagram Mermaid** đầy đủ cho luồng `POST /api/orders` bao gồm cả nhánh thành công (`201`), nhánh sai API key (`401`), và nhánh hết tồn kho (`409` kèm `rollbackTx()`).
2. Chỉ rõ trên sơ đồ: **điểm nào bắt đầu và kết thúc Transaction**, và **tại sao bản ghi `ORD-102` trong kịch bản `SKU-OUT` không xuất hiện trong `findAllCommitted()` dù `orderRepository.save()` đã được gọi trước `inventoryGateway.reserve()`**.

**Input / code khởi đầu:** `lab/src/main/java/lab/codereading/method/` và `lab/src/test/java/lab/codereading/method/OrderFlowTest.java`.
**Tiêu chí hoàn thành:**
- [ ] Sơ đồ Mermaid có đủ 6 participant: `Caller`, `RequestDispatcher`, `OrderController`, `TransactionProxy`, `OrderService`, `OrderRepository`, `InventoryGateway` (7 participant tính cả Caller).
- [ ] Phân biệt rõ `alt` block cho `401`, `201 (commitTx)`, `409 (rollbackTx)`.

<details><summary>💡 Hint</summary>

Đọc kỹ `OrderRepository.save()` và `TransactionProxy.executeInTransaction()` — xem `txBuffer` (`ThreadLocal`) giữ danh sách tạm ở đâu trước khi `commitTx()` chuyển vào `committed`.
</details>

---

### E7. Dùng Debugger + Characterization Test để phát hiện tác dụng phụ ẩn
**Bối cảnh:** Trong `OrderRepository.java`, tác giả có viết một cơ chế fallback: nếu `txBuffer.get() == null`, hàm `save()` sẽ tự ghi thẳng vào `committed` (auto-commit). Trong `OrderService`, hiện tại `OrderController` bọc qua `TransactionProxy`.

**Yêu cầu:**
Viết **1 JUnit 5 test** chứng minh điều gì xảy ra nếu một developer mới gọi thẳng `orderService.placeOrder(...)` **bỏ qua `TransactionProxy`** (ví dụ gọi trực tiếp từ 1 batch job hoặc controller khác quên bọc proxy) với `SKU-OUT`:
1. Trạng thái DB (`repo.findAllCommitted()`) bị ảnh hưởng ra sao dù `IllegalStateException` bị ném ra?
2. Liên hệ hiện tượng này với cái bẫy thật ở Spring (`@Transactional` self-invocation) và trả lời: khi đọc code tĩnh chỉ nhìn `OrderService.placeOrder()`, vì sao người đọc dễ tưởng hàm này "an toàn transaction"?

**Input / code khởi đầu:** `lab/src/test/java/lab/codereading/method/OrderFlowTest.java`.
**Tiêu chí hoàn thành:**
- [ ] Test chạy pass với `mvn -q verify` (tham khảo mẫu tại `lab/solutions/E7BypassedProxyCharacterizationTest.java`).
- [ ] Giải thích chính xác vì sao `repo.findAllCommitted()` có 1 bản ghi rác dù `placeOrder` ném exception.

<details><summary>💡 Hint</summary>

Nhìn `OrderRepository.save()`: khi `beginTx()` chưa được gọi, `txBuffer.get()` là `null`, nên nhánh `else` chạy `committed.put(...)` ngay lập tức trước khi dòng tiếp theo `inventoryGateway.reserve(...)` ném `IllegalStateException`!
</details>

---

### E8. Lập Open Questions Log khi đọc `PricingPolicy` & `OrderRepository`
**Bối cảnh:** Bạn được giao tiếp nhận codebase trong `lab/` (coi nó như một phần trích xuất từ hệ thống 50k dòng).

**Yêu cầu:**
Đọc kỹ toàn bộ `src/main` và liệt kê **ít nhất 4 câu hỏi mở (Open Questions)** mang tính production/concurrency/edge-case thật mà code hiện tại chưa trả lời rõ (gợi ý: thứ tự tính giảm giá, độ an toàn thread, rò rỉ `ThreadLocal`, thứ tự gọi repo vs gateway). Mỗi mục phải ghi theo format 3 cột: `[File:dòng] Câu hỏi | Giả định tạm | Cách kiểm chứng`.

**Tiêu chí hoàn thành:**
- [ ] Có ≥ 4 câu hỏi thực chất (không hỏi cú pháp), mỗi câu chỉ rõ tên file + giả định + cách kiểm chứng cụ thể.

<details><summary>💡 Hint</summary>

Để ý: (1) `OrderRepository.beginTx()` nếu gọi lồng nhau hoặc quên `remove()` thì `txBuffer` thế nào? (2) `InventoryGateway.reserve()` dùng `available = stock.getOrDefault(...)` rồi `stock.put(sku, available - qty)` có thread-safe dưới 2 request song song không? (3) `PricingPolicy` áp dụng `SAVE50` trước hay sau `% VIP`? (4) Nếu `InventoryGateway` là HTTP call thật ngoài DB, đặt trong Transaction DB gây hậu quả gì?
</details>

---

## 🔴 L3 — Deep

### E9. Kế hoạch 1 ngày đọc codebase lạ 50.000 dòng (JBoss AS 7.1.1 + JDBC)
**Tình huống:**
> Sáng thứ Hai, Tech Lead bàn giao cho bạn một module tính cước & đối soát hóa đơn mới tiếp quản từ vendor cũ:
> - **Quy mô:** ~52.000 dòng Java 7, đóng gói WAR chạy trên JBoss AS 7.1.1, dùng JDBC thuần + Servlet + Quartz cron job + vài EJB `@Stateless`.
> - **Tình trạng:** Không có tài liệu kiến trúc, chỉ có ~15 unit test (chạy nhiều cái bị `@Ignore`), 1 file `pom.xml`, 1 thư mục `sql/` có 22 bảng, và log production tuần trước báo lỗi trùng hóa đơn vào lúc 00:05 hằng ngày.
> - **Mục tiêu sếp giao:** Cuối ngày hôm nay (6,5 tiếng làm việc) phải báo cáo được bức tranh luồng chạy của job 00:05 và 2–3 giả thuyết nghi ngờ trùng hóa đơn.

**Yêu cầu:**
Lập kế hoạch hành động chi tiết theo từng khung giờ trong 6,5 tiếng (Pha 1 → Pha 4):
1. Ở mỗi pha, liệt kê chính xác **bạn mở file nào / chạy lệnh gì / tìm từ khóa nào** (phù hợp với stack Java 7 + JBoss + Quartz + JDBC).
2. Chỉ rõ thời điểm dùng Top-down, thời điểm dùng Bottom-up (từ bảng DB nào), và cách dùng Debugger (hoặc log) khi unit test bị `@Ignore`.
3. Xác định **điểm dừng** (những phần nào chủ động KHÔNG đọc trong ngày đầu tiên) và mẫu **Open Questions Log** gửi lại cuối ngày.

**Tiêu chí hoàn thành:**
- [ ] Tổng thời gian phân bổ khớp ~6,5 tiếng (390 phút), có timebox rõ ràng từng pha.
- [ ] Có lệnh tìm entry point cụ thể cho Servlet / Quartz / EJB / JDBC và chiến lược khoanh vùng job 00:05.

<details><summary>💡 Hint</summary>

- Job chạy lúc `00:05` hằng ngày → grep ngay cron expression `"0 5 0"` hoặc `0 5 0` hoặc `QuartzJobBean` / `implements Job` / `@Schedule`! Đó là đường tắt số 1 thay vì đọc từ `Servlet`.
- Dùng `git log` trên các bảng/DAO liên quan đến `INVOICE` để tìm file vừa bị sửa hoặc hay bị vá lỗi.
</details>
