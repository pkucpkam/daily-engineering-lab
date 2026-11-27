# JVM-01 · Lab — Java 7 → 21: Bridge Cheatsheet

> **Mục tiêu:** sau lab này bạn *nhìn thấy tận mắt* hai bản Java 7 vs Java 21 chạy ra cùng một kết quả, soi được bytecode `invokedynamic` của lambda bằng `javap`, tái hiện tận mắt race condition của `SimpleDateFormat` và đo được khi nào `Stream` chậm hơn hoặc nhanh hơn `for-loop`.
> **Thời lượng:** ~45 phút · **Cấp:** L1/L2

## 0. Yêu cầu môi trường
| Công cụ | Phiên bản | Kiểm tra |
| :--- | :--- | :--- |
| JDK | 21+ | `java -version` |
| Maven | 3.9+ | `mvn -v` |

> Lab chạy 100% offline trên localhost (dùng `com.sun.net.httpserver.HttpServer` tích hợp sẵn trong JDK, không cần Docker hay dịch vụ ngoài). Chạy được trên **Windows (PowerShell / Git Bash) và Linux/macOS**.

## 1. Cấu trúc
```text
lab/
├── README.md
├── pom.xml                                  # Maven project độc lập, Java 21
├── src/
│   ├── main/java/lab/jvm/bridge/
│   │   ├── OrderStatus.java
│   │   ├── Order.java
│   │   ├── OrderReport.java
│   │   ├── OrderReportServiceJava7.java     # Phiên bản Java 7 truyền thống
│   │   ├── OrderReportServiceJava21.java    # Phiên bản Java 21 hiện đại
│   │   ├── BytecodeInspectTarget.java       # Target để chạy javap soi bytecode
│   │   └── BridgeLabRunner.java             # Runner 4 thí nghiệm & benchmark
│   └── test/java/lab/jvm/bridge/
│       └── BridgeComparisonTest.java        # JUnit 5 + AssertJ kiểm chứng tương đương
└── solutions/                               # Lời giải code cho bài tập E6, E7, E8, E9
    ├── E6ShippingSummary.java
    ├── E7CommonPoolStarvationDemo.java
    ├── E8AsyncRateClient.java
    └── E9FixedSettlementJob.java
```

## 2. Chạy

Từ thư mục `lab/`:

**Windows (PowerShell):**
```powershell
mvn -q verify
mvn -q compile exec:java
```

**Linux / macOS / Git Bash:**
```bash
mvn -q verify
mvn -q compile exec:java
```

*(Nếu máy cài nhiều JDK và mặc định đang trỏ về Java 7/8, đặt `JAVA_HOME` tạm thời trong phiên shell trỏ tới JDK 21+ trước khi chạy `mvn`.)*

## 3. Các bước thí nghiệm

### Bước 1 — Đối chiếu kết quả Java 7 vs Java 21 & chạy test
- Làm gì: chạy `mvn -q verify` để thực thi 5 test trong `BridgeComparisonTest`, kiểm chứng bản Java 7 và Java 21 cho ra `OrderReport` giống hệt nhau, cùng xử lý HTTP 503 đúng cách và `DateTimeFormatter` an toàn đa luồng.
- **Dự đoán trước khi chạy** (ghi vào `my-notes.md`):
  1. Giữa `orElse(...)` và `orElseGet(...)` trên một `Optional.of("HIT")`, số lần gọi hàm fallback lần lượt là bao nhiêu?
  2. Với 200 lần parse/format song song trên 8 thread, `SimpleDateFormat` dùng chung bị lỗi khoảng bao nhiêu lần?

### Bước 2 — Chạy benchmark & quan sát 4 thí nghiệm
- Làm gì: chạy `mvn -q compile exec:java` (chạy `BridgeLabRunner`).
- **Dự đoán trước khi chạy** (ghi vào `my-notes.md`):
  1. Với list nhỏ `n = 100` lặp 100.000 lần: `for-loop`, `stream()`, hay `parallelStream()` nhanh nhất, và bản chậm nhất chênh khoảng bao nhiêu lần?
  2. Với 24 tác vụ blocking `sleep(20ms)` chạy `parallelStream()`, mất bao lâu trên máy của bạn và tại sao con số đó phụ thuộc số CPU?
- Quan sát: 4 khối output `[1]` → `[4]` trên console.

### Bước 3 — Soi bytecode bằng `javap`
- Làm gì: sau khi `mvn -q compile`, chạy lệnh sau:
  ```bash
  javap -c -p target/classes/lab/jvm/bridge/BytecodeInspectTarget.class
  ```
- **Dự đoán trước khi chạy**: compiler có sinh file `BytecodeInspectTarget$2.class` cho lambda giống anonymous class `BytecodeInspectTarget$1.class` không?
- Quan sát:
  - Tìm lệnh `new lab/jvm/bridge/BytecodeInspectTarget$1` trong `sortWithAnonymousClass`.
  - Tìm lệnh `invokedynamic` và method `private static lambda$...` trong `sortWithNonCapturingLambda` và `capturingLambda`.
  - Kiểm tra `var label` (`astore_2`) và text block (`ldc`) trong `varAndTextBlockDemo`.

## 4. Kết quả của bạn

| Thí nghiệm | `for-loop` | `stream()` | `parallelStream()` | Ghi chú |
| :--- | ---: | ---: | ---: | :--- |
| List nhỏ (`n = 100`, 100k lần) | ms | ms | ms | |
| List lớn CPU (`n = 1.000.000`) | ms | ms | ms | |
| Blocking I/O (`24 × 20ms`) | — | ms | ms | `commonPool` parallelism = ? |

> **Số tham khảo của người soạn** *(Windows 11 amd64, 16 logical CPUs, `ForkJoinPool.commonPool` parallelism = 15, JDK 26.0.1 với `--release 21`, chạy ngày 2026-11-27)*:
> - `mvn verify`: **5/5 tests pass**, 0 failures, 0 errors.
> - List nhỏ (`n = 100`, 100k runs): `for-loop: 6 ms` | `stream: 10 ms` | `parallelStream: 1.026 ms`.
> - List lớn CPU (`n = 1.000.000`): `for-loop: 8 ms` | `stream: 10 ms` | `parallelStream: 2 ms`.
> - Blocking I/O (`24 × 20ms`): `stream: 496 ms` | `parallelStream: 59 ms` (chiếm trọn 15 worker threads của `commonPool`).
> - `Optional`: `orElse` gọi fallback **1** lần dù value có sẵn; `orElseGet` gọi **0** lần.
> - `SimpleDateFormat` dùng chung trên 8 thread: **54 / 200** lần sai/exception; `DateTimeFormatter`: **0 / 200**.
>
> *Số tuyệt đối trên máy bạn có thể khác tùy CPU và số core, điều cần nắm là xu hướng giữa các chế độ.*

## 5. Giải thích kết quả
- Với `n = 100`, chi phí chia `Spliterator` và đồng bộ `ForkJoinPool` khiến `parallelStream()` chậm hơn `for-loop` tới ~171× (1.026ms vs 6ms). Chỉ khi `n` lớn và mỗi phần tử tốn CPU đủ nặng (như thí nghiệm `n = 1M`), `parallelStream()` mới phát huy lợi thế đa nhân.
- Dù trong thí nghiệm đơn lẻ `parallelStream()` chạy 24 task sleep 20ms chỉ mất 59ms, việc làm này **rất nguy hiểm trên production** vì chiếm hết worker của `ForkJoinPool.commonPool()` (xem thêm bài tập E7 và [README §3.2](../README.md)).
- `SimpleDateFormat` lưu trạng thái phân tích vào biến `Calendar` mutable bên trong nên hỏng dữ liệu ngay khi chạy đa luồng — đúng như phân tích ở [README §3.4](../README.md).

## 6. Biến thể để tự khám phá
1. Đổi `ArrayList` trong `buildIntegers` thành `LinkedList` ở thí nghiệm `n = 1.000.000` và đo lại `parallelStream()` để thấy tác động của `Spliterator` chia không đều.
2. Trong `BytecodeInspectTarget`, thử sửa lambda `capturingLambda` để đọc thêm biến `int count = 0; count++;` xem compiler báo lỗi gì.
3. Chạy thử `solutions/E7CommonPoolStarvationDemo.java` để chứng kiến `CompletableFuture.supplyAsync()` bị treo ~450ms khi `parallelStream()` chạy blocking sleep song song.

## 7. Dọn dẹp
```bash
mvn -q clean
```
