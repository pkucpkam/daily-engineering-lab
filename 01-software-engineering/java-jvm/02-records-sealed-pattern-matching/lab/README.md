# JVM-02 · Lab — Records, Sealed classes, Pattern matching

> **Mục tiêu:** sau lab này bạn *nhìn thấy tận mắt* `sealed interface` + `record` + `switch` pattern matching chạy trong Java 21, kiểm chứng được compact constructor chống mutate list ngoài, thấy tận mắt `record` chặn dữ liệu giả mạo khi deserialize trong khi POJO bị lọt, và soi attribute `PermittedSubclasses` / `Record` bằng `javap`.
> **Thời lượng:** ~40 phút · **Cấp:** L1/L2

## 0. Yêu cầu môi trường
| Công cụ | Phiên bản | Kiểm tra |
| :--- | :--- | :--- |
| JDK | 21+ | `java -version` |
| Maven | 3.9+ | `mvn -v` |

> Lab chạy 100% offline, không cần Docker hay dịch vụ ngoài. Chạy được trên **Windows (PowerShell / Git Bash) và Linux/macOS**.

## 1. Cấu trúc
```text
lab/
├── README.md
├── pom.xml                                  # Maven project độc lập, Java 21
├── src/
│   ├── main/java/lab/jvm/dop/
│   │   ├── PaymentResult.java               # sealed interface + 3 record + compact constructor
│   │   ├── LegacyPojoAmount.java            # POJO thường để so sánh deserialization
│   │   ├── PaymentResultHandler.java        # switch pattern matching + when + case null
│   │   └── DopLabRunner.java                # Runner 4 thí nghiệm
│   └── test/java/lab/jvm/dop/
│       └── DopComparisonTest.java           # JUnit 5 + AssertJ kiểm chứng invariant & switch
└── solutions/                               # Lời giải E6, E7, E9
    ├── E6NotificationOutcome.java
    ├── E7NotificationRouter.java
    └── E9SafePaymentRouter.java
```

## 2. Chạy

Từ thư mục `lab/`:
```bash
mvn -q verify
mvn -q compile exec:java
```

## 3. Các bước thí nghiệm

### Bước 1 — Chạy test kiểm chứng deserialization & pattern matching
- Làm gì: chạy `mvn -q verify` để chạy `DopComparisonTest`.
- **Dự đoán trước khi chạy** (ghi vào `my-notes.md`):
  1. Nếu sửa 1 `int` trong byte stream đã serialize của `PositiveRecord(10)` và `PositivePojo(10)` thành `-5` rồi gọi `ObjectInputStream.readObject()`, cái nào ném exception và cái nào lọt qua?
  2. Nếu thêm `mutableTags.add("HACKED")` sau khi khởi tạo `PaymentResult.Success`, `success.auditTags()` có thay đổi không?

### Bước 2 — Chạy `DopLabRunner` & thử phá exhaustiveness
- Làm gì: chạy `mvn -q compile exec:java`.
- **Dự đoán trước khi chạy**: nếu bạn thêm tạm `record Cancelled(String by) implements PaymentResult {}` vào `PaymentResult.java` (mà chưa sửa `PaymentResultHandler.java`), `mvn compile` sẽ báo lỗi gì?

### Bước 3 — Soi bytecode bằng `javap`
- Làm gì:
  ```bash
  javap -v -p target/classes/lab/jvm/dop/PaymentResult.class
  javap -v -p target/classes/lab/jvm/dop/PaymentResult$Success.class
  ```
  *(Trên PowerShell, escape dấu `$` bằng backtick: ``PaymentResult`$Success.class``.)*
- Quan sát:
  - Mục `PermittedSubclasses:` trong `PaymentResult.class`.
  - Mục `Record:` và `BootstrapMethods:` (`ObjectMethods.bootstrap`) trong `PaymentResult$Success.class`.

## 4. Kết quả của bạn

| Kiểm tra | Kết quả thực tế trên máy bạn | Ghi chú |
| :--- | :--- | :--- |
| `mvn -q verify` | ? / 4 tests pass | |
| Record deserialization với `-5` | Ném lỗi hay lọt? | |
| POJO deserialization với `-5` | Ném lỗi hay lọt? | |
| `javap` attribute thấy được | `PermittedSubclasses`, `Record`? | |

> **Số tham khảo của người soạn** *(Windows 11 amd64, JDK 26.0.1 với `--release 21`, chạy ngày 2026-11-27)*:
> - `mvn verify`: **4/4 tests pass**, 0 failures, 0 errors.
> - `PositiveRecord(-5)` khi deserialize ném `InvalidObjectException: value must be > 0`; `PositivePojo(-5)` **bỏ qua constructor** và trả về object có `value == -5`.
> - `javap` ghi nhận `PermittedSubclasses: Success, Pending, Failure` và `List.copyOf` trong canonical constructor của `PaymentResult$Success`.

## 5. Giải thích kết quả
- `ObjectInputStream` xử lý `record` bằng cách đọc các component rồi gọi **canonical constructor** (JEP 395), nên mọi kiểm tra invariant trong compact constructor được bảo toàn khi deserialize.
- Ngược lại, POJO truyền thống (và Lombok `@Value`) dùng cơ chế deserialization bypass constructor → dễ tạo object vi phạm invariant nếu không tự viết `readObject`. Xem thêm [README §3.1](../README.md).

## 6. Biến thể để tự khám phá
1. Bỏ dòng `auditTags = ... List.copyOf(auditTags);` trong `PaymentResult.Success` rồi chạy lại `DopComparisonTest` để thấy test `recordCompactConstructorProtectsAgainstExternalListMutation` đỏ ngay lập tức.
2. Thêm `case PaymentResult.Success s -> ...` lên **trước** nhánh có `when` trong `PaymentResultHandler` và chạy `mvn compile` để thấy lỗi dominance của compiler.

## 7. Dọn dẹp
```bash
mvn -q clean
```
