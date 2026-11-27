# Lab CQ-08 · Integration test với Testcontainers

## Mục tiêu

Minh hoạ trọn vẹn hai mặt của Integration Test với DB:
1. [`H2DialectPitfallTest`](src/test/java/lab/cq/testcontainers/H2DialectPitfallTest.java): chạy được ngay **không cần Docker**, chứng minh H2 (`MODE=PostgreSQL`) sập ngay trước expression index `JSONB` và `ON CONFLICT ... RETURNING` trong [`V1__create_ledger_table.sql`](src/main/resources/db/migration/V1__create_ledger_table.sql) + [`PostgresLedgerRepository`](src/main/java/lab/cq/testcontainers/PostgresLedgerRepository.java).
2. [`PostgresFlywayIntegrationIT`](src/test/java/lab/cq/testcontainers/PostgresFlywayIntegrationIT.java): chạy **PostgreSQL 16 (`postgres:16-alpine`) + Flyway 10.20.1 thật** qua `@Testcontainers` (tự bật khi máy có Docker Daemon qua `@EnabledIf("isDockerAvailable")`) để kiểm thử migration, JSONB upsert và rollback transaction.

## Yêu cầu môi trường

- JDK 21+ (đã verify trên OpenJDK 26.0.1 với target `--release 21`)
- Maven 3.9+ (đã tải sẵn `testcontainers:postgresql:1.20.4`, `flyway-database-postgresql:10.20.1`, `postgresql:42.7.4`, `h2:2.2.224`)
- Docker Desktop / Docker Engine *(tùy chọn: bắt buộc nếu chạy `PostgresFlywayIntegrationIT`, không cần để chạy `H2DialectPitfallTest` & `MigrationDryRunCheck`)*

## Cách chạy

```powershell
# 1. Chạy unit test H2 vs Postgres pitfall + biên dịch kiểm tra PostgresFlywayIntegrationIT (không cần Docker)
mvn -o verify

# 2. Chạy kiểm tra quy ước Flyway migration (L3)
java solutions\MigrationDryRunCheck.java

# 3. Trên máy có Docker hoặc CI: chạy Integration Test thật với PostgreSQL 16
mvn -o test "-Dtest=PostgresFlywayIntegrationIT"

# 4. Dọn artifact build
mvn -o -q clean
```

## Kết quả mẫu (đã chạy trên Windows 11 · OpenJDK 26.0.1 · chưa cài Docker daemon)

- `mvn -o verify`:
  - Biên dịch thành công cả `PostgresLedgerRepository`, `H2DialectPitfallTest` và `PostgresFlywayIntegrationIT`.
  - `H2DialectPitfallTest`: `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0` (chứng minh migration Postgres + `ON CONFLICT ... RETURNING` ném `SQLException` trên H2).
- `MigrationDryRunCheck.java`:
```text
Migration=V1__create_ledger_table.sql | validName=true | primaryKey=true | usesJsonb=true
```
- *(Lưu ý: `PostgresFlywayIntegrationIT` chưa thực thi container thật tại phiên kiểm chứng này do máy hiện tại không có Docker daemon).*
