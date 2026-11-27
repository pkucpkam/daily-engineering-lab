# Bài tập · CQ-08 · Integration test với Testcontainers

Làm bài trước khi mở [solutions.md](solutions.md). Thực hành với [lab/](lab/README.md).

---

## Level 1 · Hiểu lý do & Cơ chế Testcontainers (4 bài)

### Bài 1.1 · Tại sao H2 `MODE=PostgreSQL` là cái bẫy?
Nhìn lại file migration [`V1__create_ledger_table.sql`](lab/src/main/resources/db/migration/V1__create_ledger_table.sql) và [`PostgresLedgerRepository.java`](lab/src/main/java/lab/cq/testcontainers/PostgresLedgerRepository.java). Chỉ ra **3 cú pháp/tính năng Postgres** ngay trong 2 file đó khiến H2 không chạy đúng hoặc báo lỗi dù đã bật `MODE=PostgreSQL`.

### Bài 1.2 · Hiểu kiến trúc Random Mapped Port & Ryuk
1. Tại sao Testcontainers mở port **ngẫu nhiên** trên máy host (ví dụ `58123 -> 5432`) thay vì bind thẳng cổng mặc định `5432`?
2. Container phụ có tên `testcontainers/ryuk` xuất hiện mỗi khi chạy test làm nhiệm vụ gì?

### Bài 1.3 · Hai yếu tố bắt buộc để `.withReuse(true)` hoạt động
Bạn đã thêm `.withReuse(true)` vào `PostgreSQLContainer` nhưng khi chạy lại test thấy container vẫn bị xoá tạo mới từ đầu. Cần thêm cấu hình gì ở máy dev (và **vì sao không nên bật `reuse`** trên GitHub Actions runner)?

### Bài 1.4 · Kể các công cụ anh em trong Testcontainers ngoài DB quan hệ
Với hai tình huống sau, nếu dùng In-Memory HashMap thay thế thì rủi ro gì xảy ra và giải pháp Testcontainers tương ứng là gì?
1. Kiểm tra script Lua atomic rate-limiter và TTL expire key.
2. Kiểm tra consumer Kafka xử lý rebalance, dead-letter topic khi JSON hỏng.

---

## Level 2 · Thực hành lab H2 vs PostgreSQL + Flyway (3 bài)

### Bài 2.1 · Chạy bài kiểm chứng bẫy H2 (`H2DialectPitfallTest`)
Chạy `mvn -o verify` trong `08-testcontainers/lab/`. Đọc [`H2DialectPitfallTest.java`](lab/src/test/java/lab/cq/testcontainers/H2DialectPitfallTest.java) và trả lời:
1. Tại sao lệnh `st.execute(migrationSql)` văng `SQLException` ngay lập tức trên H2 `MODE=PostgreSQL`?
2. Vì sao ngay cả khi ta tự tạo bảng đơn giản hơn trên H2, hàm `upsertAndReturnId` của `PostgresLedgerRepository` vẫn nổ `SQLException`?

### Bài 2.2 · Tối ưu hóa từ `@Container` theo class sang *Singleton Container*
Trong [`PostgresFlywayIntegrationIT.java`](lab/src/test/java/lab/cq/testcontainers/PostgresFlywayIntegrationIT.java), hiện tại `POSTGRES` được gắn `@Container` ở cấp 1 class test. Nếu dự án nở lên **20 class `*IT.java`** (tất cả đều dùng Postgres), hãy viết lại khung lớp nền `AbstractPostgresIT` sử dụng **Singleton Container Pattern** (khởi động đúng 1 lần cho toàn bộ Maven test suite) kèm cơ chế làm sạch bảng giữa các test.

<details>
<summary>Gợi ý</summary>

Bỏ `@Testcontainers` / `@Container`, dùng khối `static { POSTGRES.start(); }` trong lớp cha `AbstractPostgresIT` và giữ Flyway chạy 1 lần; các test con kế thừa và gọi `TRUNCATE TABLE ledger_entry RESTART IDENTITY` trong `@BeforeEach`.

</details>

### Bài 2.3 · Chuyển ngữ cảnh Air-gapped CI (Không Internet) & Legacy Oracle
Công ty bạn dùng GitLab Runner nội bộ bị ngắt Internet và muốn chạy Testcontainers cho service mới (Spring Boot + PostgreSQL) hoặc test Docker Oracle cho dự án Java 7 cũ thông qua một test module JDK 21 tách riêng. Bạn cần chuẩn bị 2 bước hạ tầng nào để Testcontainers kéo được ảnh `postgres:16-alpine` và `testcontainers/ryuk` mà không cần Internet?

---

## Level 3 · Kiểm chứng quy ước Migration & Thực thi PostgreSQL thật (1 bài)

### Bài 3.1 · Kiểm tra Dry-Run Migration & Kịch bản Transaction Rollback
**Yêu cầu**:
1. Đọc và chạy [`lab/solutions/MigrationDryRunCheck.java`](lab/solutions/MigrationDryRunCheck.java) trong thư mục `08-testcontainers/lab/`: `java solutions\MigrationDryRunCheck.java`.
2. Đọc test `executesUpsertReturningJsonbQueryAndTransactionRollbackOnRealPostgres()` trong [`PostgresFlywayIntegrationIT.java`](lab/src/test/java/lab/cq/testcontainers/PostgresFlywayIntegrationIT.java) và giải thích: tại sao chỉ bắt `assertThatThrownBy(() -> repository.insertBatchAtomically(...))` là **chưa đủ** để chứng minh transaction hoạt động đúng, mà bắt buộc phải có dòng `assertThat(repository.findRefsByChannel("WEB")).isEmpty()` ngay phía sau?
3. *(Trên máy có Docker Desktop hoặc CI runner)* Chạy `mvn verify -Dit.test=PostgresFlywayIntegrationIT` để quan sát PostgreSQL 16 container + Flyway 10 chạy thật từ đầu đến cuối.

**Tiêu chí đạt**: `mvn -o verify` chạy pass `H2DialectPitfallTest`, `MigrationDryRunCheck.java` in `validName=true | primaryKey=true | usesJsonb=true`, và nắm vững khẳng định double-check rollback DB.
