# READ-01 · Lab — Phương pháp đọc code lạ

> **Mục tiêu:** thực hành quy trình **Outside-In (Recon → Đọc Test trước → Trace Call Stack bằng Debugger → Vẽ Sequence Diagram)** trên một mini-backend 8 class mà không cần dùng AI tóm tắt code.
> **Thời lượng:** ~40 phút · **Cấp:** L1/L2

## 0. Yêu cầu môi trường
| Công cụ | Phiên bản | Kiểm tra |
| :--- | :--- | :--- |
| JDK | 21+ | `java -version` |
| Maven | 3.9+ | `mvn -v` |
| IDE (IntelliJ / VS Code) | Bất kỳ có Java Debugger | — |

> Lab chạy 100% offline, không cần Docker hay dịch vụ ngoài. Chạy được trên **Windows (PowerShell / Git Bash) và Linux/macOS**.

## 1. Cấu trúc
```text
lab/
├── README.md
├── pom.xml                                  # Maven project độc lập, Java 21
├── src/
│   ├── main/java/lab/codereading/method/
│   │   ├── OrderApplication.java            # Entry point bootstrap & demo runner
│   │   ├── model/                           # OrderCommand, HttpResponseData
│   │   ├── web/                             # RequestDispatcher, OrderController
│   │   ├── tx/                              # TransactionProxy (mô phỏng @Transactional)
│   │   ├── service/                         # OrderService, PricingPolicy
│   │   ├── repo/                            # OrderRepository (ThreadLocal txBuffer)
│   │   └── gateway/                         # InventoryGateway
│   └── test/java/lab/codereading/method/
│       └── OrderFlowTest.java               # ĐỌC FILE NÀY TRƯỚC!
└── solutions/
    └── E7BypassedProxyCharacterizationTest.java
```

## 2. Chạy

Từ thư mục `lab/`:
```bash
mvn -q verify
mvn -q compile exec:java
```

## 3. Các bước thí nghiệm (bấm đồng hồ 30 phút)

### Bước 1 — Recon & Đọc Test trước (10 phút, chưa mở `src/main`)
1. Mở [pom.xml](./pom.xml) (5 dòng dependencies) và liệt kê cấu trúc package trong `src/main`.
2. Mở `src/test/java/lab/codereading/method/OrderFlowTest.java` và chạy `mvn -q verify`.
3. **Dự đoán trước khi mở code** (ghi vào `my-notes.md`):
   - `PricingPolicy` áp dụng giảm giá `% VIP` trước hay trừ coupon `SAVE50` trước?
   - Trong kịch bản `SKU-OUT`, `OrderService` có kịp gọi `OrderRepository.save()` trước khi lỗi tồn kho không? Nếu có, tại sao `repo.findAllCommitted()` vẫn rỗng?

### Bước 2 — Trace bằng Debugger & nhìn Call Stack (15 phút)
1. Mở `OrderRepository.java`, đặt **1 breakpoint duy nhất** tại dòng đầu tiên của hàm `save(...)`.
2. Chạy `OrderFlowTest#insufficientStockRollsBackStagedOrder` ở chế độ **Debug** trong IDE.
3. Khi debugger dừng ở `save(...)`, đọc **Frames / Call Stack** từ dưới lên và ghi lại thứ tự các class đi qua (từ `OrderFlowTest` → `RequestDispatcher` → ... → `OrderRepository`).
4. Bấm **Step Over** qua từng bước cho tới khi `InventoryGateway.reserve()` ném `IllegalStateException` và xem luồng nhảy vào `catch` ở class nào (`TransactionProxy` rồi `OrderController`).

### Bước 3 — Vẽ Sequence Diagram & đối chiếu đáp án (10 phút)
- Tự vẽ Sequence Diagram cho luồng `POST /api/orders` trước khi xem đáp án bài E6 trong [../solutions.md](../solutions.md).

## 4. Kết quả tham khảo khi chạy

> **Output thực tế trên máy soạn** *(Windows 11 amd64, JDK 26.0.1 `--release 21`, chạy ngày 2026-11-27)*:
> - `mvn verify`: **3/3 tests pass**, 0 failures, 0 errors.
> - `mvn compile exec:java` (`OrderApplication`):
>   ```text
>   [1] Happy path -> HttpResponseData[status=201, traceId=tr-001, body=CREATED:ORD-101, finalAmount=310.00]
>   [2] Out of stock -> HttpResponseData[status=409, traceId=tr-002, body=CONFLICT:Insufficient stock for SKU-OUT: available=0, finalAmount=null]
>   [3] Committed orders in DB -> [SavedOrder[orderId=ORD-101, customerId=CUST-1, sku=SKU-BOOK, quantity=2, total=310.00]]
>   ```

## 5. Giải thích kết quả
- Dòng `OrderRepository.save` thêm dữ liệu vào `txBuffer` (`ThreadLocal`) khi đang nằm trong `TransactionProxy`. Khi `InventoryGateway.reserve()` ném `IllegalStateException`, `TransactionProxy` bắt `RuntimeException`, gọi `rollbackTx()` xóa `txBuffer`, rồi ném tiếp ra `OrderController` để đổi thành HTTP `409`.
- Nếu bỏ qua `TransactionProxy` (bài E7), `txBuffer.get()` bằng `null` khiến `save()` rơi vào auto-commit và để lại dữ liệu rác trong `committed` — minh họa trực tiếp lý do phải đọc xuyên qua lớp Proxy bằng Debugger.

## 6. Biến thể để tự khám phá
1. Copy test trong `solutions/E7BypassedProxyCharacterizationTest.java` vào `src/test/java/lab/codereading/method/` và chạy `mvn -q verify` để chứng kiến tác dụng phụ khi gọi trực tiếp `OrderService`.
2. Đặt Conditional Breakpoint tại `OrderRepository.save` với điều kiện `"SKU-OUT".equals(sku)` và chạy `OrderApplication.main`.

## 7. Dọn dẹp
```bash
mvn -q clean
```
