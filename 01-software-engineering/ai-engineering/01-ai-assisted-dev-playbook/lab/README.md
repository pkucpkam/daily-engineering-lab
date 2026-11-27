# AI-01 · Lab — Săn lỗi tinh vi trong code AI sinh

> **Mục tiêu:** tận mắt thấy 3 đoạn code AI sinh nhìn rất gọn vẫn vượt giới hạn rate limit dưới tải đa luồng, rò rỉ JDBC resource & nổ `ArithmeticException`, retry sai HTTP 400 + nuốt interrupt, và phân biệt **Version Drift** vs **Hallucinated API**.
> **Thời lượng:** ~35 phút · **Cấp:** L2

## 0. Yêu cầu môi trường
| Công cụ | Phiên bản | Kiểm tra |
| :--- | :--- | :--- |
| JDK | 21+ | `java -version` |
| Maven | 3.9+ | `mvn -v` |

> Lab chạy 100% offline, không cần Docker hay API key LLM bên ngoài. Chạy được trên **Windows (PowerShell) và Linux/macOS**.

## 1. Cấu trúc
```text
lab/
├── README.md
├── pom.xml                                  # Maven project độc lập, Java 21
├── src/
│   ├── main/java/lab/ai/playbook/
│   │   ├── buggy/
│   │   │   ├── OrderRateLimiterBuggy.java   # Race condition check-then-act + không evict key
│   │   │   ├── InvoiceJdbcHelperBuggy.java  # SQL injection + leak resource + BigDecimal divide
│   │   │   └── PaymentClientBuggy.java      # SimpleDateFormat static + retry 4xx + nuốt interrupt
│   │   ├── ApiVerifier.java                 # Phân loại API: Java 7 vs Version Drift vs Bịa
│   │   └── PlaybookLabRunner.java           # Runner chạy 4 demo
│   └── test/java/lab/ai/playbook/
│       └── AiBugReproductionTest.java       # JUnit 5 tái hiện chính xác từng lỗi AI
└── solutions/
    ├── OrderRateLimiterFixed.java           # Lời giải E6
    ├── InvoiceJdbcHelperFixed.java          # Lời giải E7 (JDBC)
    └── PaymentClientFixed.java              # Lời giải E7 (Client)
```

## 2. Chạy

Từ thư mục `lab/`:
```powershell
mvn -o -B verify
mvn -o -B compile exec:java
```

## 3. Các bước thí nghiệm

### Bước 1 — Tự đọc code trước khi chạy (áp dụng "Viết trước, hỏi sau")
- **Dự đoán trước khi chạy** (ghi vào `my-notes.md`):
  1. Mở `OrderRateLimiterBuggy.java` và đoán: với `maxRequests = 10` và `16` thread gọi cùng 1 `userId`, số request lọt qua có chắc chắn `<= 10` không?
  2. Trong `InvoiceJdbcHelperBuggy.fetchAverageAmount`, nếu `amounts` có 3 hóa đơn `[10.00, 10.00, 10.01]`, exception nào xảy ra và `conn.isClosed()` bằng `true` hay `false`?
  3. Trong `ApiVerifier.defaultProbes()`, method nào tồn tại ở JDK 21 hiện tại nhưng là **Version Drift** với Java 7, và method nào là **Hallucinated API** hoàn toàn?

### Bước 2 — Chạy kiểm chứng tự động
- Chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`.

## 4. Kết quả tham khảo khi chạy

> **Output thực tế trên máy soạn** *(Windows 11 amd64, OpenJDK 26.0.1 `--release 21`, ngày 2026-11-27)*:
> - `mvn -o -B verify`: **4/4 tests pass**, 0 failures, 0 errors.
> - `PlaybookLabRunner`:
>   - `[1] OrderRateLimiterBuggy (max=10, 16 threads)`: `max allowed in 5 rounds = 13` (vượt trần 10!).
>   - `[2] InvoiceJdbcHelperBuggy SQLi payload`: `SELECT amount FROM invoices WHERE customer_id = '' OR '1'='1'`; chia 3 ném `ArithmeticException: Non-terminating decimal expansion; no exact representable decimal result.` và làm `statementClosed=false`, `connectionClosed=false`.
>   - `[3] PaymentClientBuggy`: retry `400 Bad Request` đủ `3` lần và nuốt `InterruptedException` (`isInterrupted() == false`).
>   - `[4] API Verification (target Java 7)`: `trim` → `AVAILABLE_SINCE_JAVA_7`; `isBlank`/`of`/`orElseThrow`/`ofVirtual` → `VERSION_DRIFT_NEWER_JDK`; `HttpClient.Builder#retryPolicy` → `HALLUCINATED_NON_EXISTENT`.

## 5. Giải thích kết quả
- Các đoạn code do AI sinh thường sạch về cú pháp nên qua mặt người đọc lướt, nhưng sập ngay dưới **tranh chấp đa luồng**, **dữ liệu biên (chia không hết, `null`)**, **lệch phiên bản JDK mục tiêu** và **luồng xử lý lỗi**. Xem chi tiết cơ chế tại [README §3](../README.md).

## 6. Biến thể để tự khám phá
- Chép `solutions/OrderRateLimiterFixed.java` vào `src/main/java/...` và viết thêm test chứng minh với `32` thread × `20` vòng, số request lọt qua luôn bằng đúng `maxRequests`.

## 7. Dọn dẹp
```powershell
mvn -o -B -q clean
```
