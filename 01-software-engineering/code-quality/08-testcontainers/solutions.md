# Đáp án · CQ-08 · Integration test với Testcontainers

## Đáp án Bài tập

### Level 1

#### Bài 1.1 · 3 tính năng PostgreSQL trong lab khiến H2 gãy
1. Kiểu dữ liệu `JSONB` kèm toán tử truy xuất **`metadata ->> 'channel'`** và **Index trên biểu thức (`CREATE INDEX ... ON ledger_entry ((metadata ->> 'channel'))`)**.
2. Hàm đúc kiểu **`CAST(? AS JSONB)`** và lọc trường JSON trong mệnh đề `WHERE`.
3. Cú pháp **`INSERT ... ON CONFLICT (external_ref) DO UPDATE ... RETURNING id`**.

#### Bài 1.2 · Random Port & Ryuk Reaper
1. **Random Mapped Port** tránh lỗi `Port already in use` khi lập trình viên đang cài sẵn Postgres local ở cổng `5432`, hoặc khi CI chạy nhiều container/job kiểm thử song song trên cùng host. Luôn đọc cổng động qua `POSTGRES.getJdbcUrl()` / `getMappedPort(5432)`.
2. **`testcontainers/ryuk`** là container giám sát đi kèm: nó giữ kết nối với tiến trình JVM chạy test, ngay khi JVM tắt (kể cả khi dừng đột ngột bằng `Ctrl+C` hay crash), Ryuk sẽ tự động dọn sạch mọi test container, network và volume mồ côi để không rác máy.

#### Bài 1.3 · Bật container reuse `.withReuse(true)`
Ngoài `.withReuse(true)` trong code Java, bạn **bắt buộc** phải tạo/sửa file `~/.testcontainers.properties` trên máy dev cá nhân thêm dòng:
```properties
testcontainers.reuse.enable=true
```
Không nên bật trên ephemeral CI runner vì máy CI sinh ra cho 1 job xong là huỷ luôn (reuse không giúp ích gì, ngược lại nếu CI runner chia sẻ Docker daemon giữa nhiều branch có thể làm lọt dữ liệu cũ giữa các PR!).

#### Bài 1.4 · Testcontainers cho Redis & Kafka
1. **Redis (`GenericContainer<>("redis:7-alpine")`)**: `HashMap` không thực thi được Lua script EVAL, lệnh `SET NX PX` chính xác từng ms hay hành vi eviction LRU/TTL thật.
2. **Kafka (`KafkaContainer` / Redpanda)**: `BlockingQueue` trong RAM không mô phỏng được offset commit, group rebalance, schema serialization, hay DLQ retry thật giữa các broker partition.

---

### Level 2

#### Bài 2.1 · Giải thích kết quả `H2DialectPitfallTest`
1. Vì `V1__create_ledger_table.sql` chứa câu `CREATE INDEX idx_ledger_channel ON ledger_entry ((metadata ->> 'channel'));` — H2 không hỗ trợ functional JSONB `->>` expression index nên văng `SQLException` ngay lập tức ở pha tạo schema.
2. Hàm `upsertAndReturnId` dùng cú pháp đặc hữu của PostgreSQL (`ON CONFLICT ... DO UPDATE ... RETURNING id`) mà H2 parser không hỗ trợ, dù có truyền tham số URL `;MODE=PostgreSQL`.

#### Bài 2.2 · Singleton Container Pattern (`AbstractPostgresIT`)
```java
public abstract class AbstractPostgresIT {
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("ledger_test")
            .withUsername("app")
            .withPassword("app")
            .withReuse(true);

    static final PGSimpleDataSource DATA_SOURCE = new PGSimpleDataSource();

    static {
        POSTGRES.start();
        DATA_SOURCE.setURL(POSTGRES.getJdbcUrl());
        DATA_SOURCE.setUser(POSTGRES.getUsername());
        DATA_SOURCE.setPassword(POSTGRES.getPassword());
        Flyway.configure().dataSource(DATA_SOURCE).locations("classpath:db/migration").load().migrate();
    }
}
```
Khi chạy `mvn verify`, JVM khởi động **1 PostgreSQL container** và chạy Flyway **đúng 1 lần** dù có 20 file `*IT.java`. Mỗi `@BeforeEach` ở lớp con chỉ chạy `TRUNCATE TABLE ledger_entry RESTART IDENTITY CASCADE` mất vài mili-giây!

#### Bài 2.3 · Chạy Testcontainers trong CI nội bộ không có Internet
1. Pull và push các Docker image cần thiết (`postgres:16-alpine`, `testcontainers/ryuk:0.11.0` hoặc bản phù hợp) lên **Private Container Registry nội bộ** của công ty (Harbor / Nexus / Artifactory).
2. Truyền biến môi trường cho job CI runner:
```text
TESTCONTAINERS_HUB_IMAGE_NAME_PREFIX=registry.company.local/dockerhub-mirror/
```
Testcontainers sẽ tự ghép prefix vào mọi ảnh (kể cả ảnh nội bộ như Ryuk) để tải qua mạng LAN nội bộ không cần ra Internet!

---

### Level 3

#### Bài 3.1 · Kiểm chứng Dry-Run & Khẳng định Rollback Transaction
1. Chạy `java solutions\MigrationDryRunCheck.java` trong `08-testcontainers/lab/` ra kết quả thực tế:
```text
Migration=V1__create_ledger_table.sql | validName=true | primaryKey=true | usesJsonb=true
```
2. Lý do phải assert `assertThat(repository.findRefsByChannel("WEB")).isEmpty()` sau `assertThatThrownBy`:
   - Nếu hàm `insertBatchAtomically` quên gọi `conn.setAutoCommit(false)` (hoặc quên `conn.rollback()` trong `catch`), thì dòng thứ 2 (`-5L`) **vẫn ném `SQLException` (`CHECK` constraint violation)** làm `assertThatThrownBy` PASS, **nhưng dòng thứ nhất `"PG-ATOMIC-1"` đã bị autocommit lưu chình ình vào DB**!
   - Nhờ truy vấn đọc lại DB sau khi văng lỗi và khẳng định bảng trống (`isEmpty()`), test mới thật sự chứng minh giao dịch đã rollback toàn vẹn (All-or-Nothing).

---

## Đáp án Câu hỏi tự kiểm tra

<details>
<summary>1. Ba điểm khác biệt khiến H2 MODE=PostgreSQL không thay thế được PostgreSQL thật?</summary>

(1) DDL nâng cao: `JSONB`, kiểu mảng `TEXT[]`, expression index `((metadata ->> 'channel'))`; (2) DML đặc thù: `INSERT ... ON CONFLICT ... DO UPDATE ... RETURNING`, `ILIKE`, CTE/window edge cases; (3) Cơ chế locking MVCC (`FOR UPDATE SKIP LOCKED`, `SERIALIZABLE` conflict) và kiểm chứng script Flyway thật.

</details>

<details>
<summary>2. Cách tăng tốc bộ test 25 file *IT.java dùng Testcontainers?</summary>

Dùng **Singleton Container** (`static { POSTGRES.start(); }`) chia sẻ chung 1 container cho cả suite, dùng **`TRUNCATE`** dọn bảng giữa các test thay vì tạo mới container hay chạy lại Flyway, dùng image **`alpine`** nhẹ, và bật **`.withReuse(true)`** + `testcontainers.reuse.enable=true` ở máy dev local.

</details>

<details>
<summary>3. Kiểm tra rollback transaction: sao bắt SQLException thôi là chưa đủ?</summary>

Vì khi quên tắt autocommit (`setAutoCommit(false)`) hoặc quên gọi `rollback()`, câu INSERT sai thứ 2 vẫn văng `SQLException` bình thường, nhưng câu INSERT hợp lệ đứng trước đã bị ghi chết xuống DB! Bắt buộc phải query lại DB sau exception để xác nhận dòng đầu không tồn tại.

</details>

<details>
<summary>4. Cấu hình Testcontainers trong mạng LAN/CI không có Internet?</summary>

Mirror các image dùng trong test (kể cả `testcontainers/ryuk` và `postgres:16-alpine`) vào Private Registry nội bộ rồi cấu hình biến môi trường `TESTCONTAINERS_HUB_IMAGE_NAME_PREFIX=<registry-url>/` (hoặc cấu hình trong `~/.testcontainers.properties`).

</details>

<details>
<summary>5. Tại sao Testcontainers ép dùng Random Host Port?</summary>

Để tránh xung đột cổng (`5432` đã bị Postgres local hoặc một job CI song song khác chiếm sẵn), đảm bảo nhiều test runner/container chạy song song hoàn toàn cô lập và truyền cổng động qua `POSTGRES.getJdbcUrl()` / `@DynamicPropertySource`.

</details>
