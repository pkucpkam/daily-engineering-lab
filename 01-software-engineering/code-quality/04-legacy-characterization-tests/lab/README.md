# Lab CQ-04 · Legacy code & Characterization tests

## Mục tiêu

Thực hành bẻ dependency tối thiểu trên một DAO kiểu **Java 7 + JDBC thuần** ([`LegacyMonthlyStatementDao`](src/main/java/lab/cq/legacy/LegacyMonthlyStatementDao.java)) bằng **Extract & Override Factory Method**, dùng `java.lang.reflect.Proxy` giả lập JDBC trong bộ nhớ (không cần database hay framework mock ngoài), và khoá hành vi bằng **Golden Master Test** ([`StatementCharacterizationTest`](src/test/java/lab/cq/legacy/StatementCharacterizationTest.java)).

## Yêu cầu môi trường

- JDK 21+ (đã verify trên OpenJDK 26.0.1 với target `--release 21`)
- Maven 3.9+

## Cấu trúc thư mục

- `pom.xml` — cấu hình Maven (`--release 21`, JUnit 5, AssertJ)
- `src/main/java/lab/cq/legacy/LegacyMonthlyStatementDao.java` — DAO kiểu Java 7 có 2 minimal seams (`openConnection()`, `currentEpochMillis()`)
- `src/test/java/lab/cq/legacy/StatementCharacterizationTest.java` — Golden Master harness dùng JDK `Proxy` stubbing `Connection`/`PreparedStatement`/`ResultSet`
- `solutions/BoundaryQuirkCheck.java` — bài L3 phơi bày biên quirk `100_000` vs `100_001` của logic cũ

## Cách chạy

```powershell
# 1. Chạy Golden Master characterization tests
mvn -o test

# 2. Chạy minh họa biên quirk L3
mvn -o -q compile
java -cp target\classes solutions\BoundaryQuirkCheck.java

# 3. Dọn artifact build
mvn -o -q clean
```

## Kết quả mẫu (đã chạy verify)

- `StatementCharacterizationTest`: `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`
- `BoundaryQuirkCheck.java`:
```text
=== At exact 100,000 net (VIP bonus = 0 due to strict > check) ===
STATEMENT|cust=C-BOUNDARY|month=11|ts=1700000000000
TX:TX-B:REFUND:net=100000
SUMMARY|count=1|net=100000|bonus=0|final=100000
=== At 100,001 net (VIP bonus = 5,000 triggers) ===
STATEMENT|cust=C-BOUNDARY|month=11|ts=1700000000000
TX:TX-B:REFUND:net=100001
SUMMARY|count=1|net=100001|bonus=5000|final=95001
```
