# Lab CQ-03 · Refactoring catalog & code smells

## Mục tiêu

Thực hành **Kata Gilded Rose**: refactor code điều kiện lồng 4 tầng ([`GildedRoseLegacy`](src/main/java/lab/cq/refactor/before/GildedRoseLegacy.java)) sang kiến trúc đa hình sạch ([`GildedRoseRefactored`](src/main/java/lab/cq/refactor/after/GildedRoseRefactored.java)) dưới sự bảo vệ của bộ ma trận **122 tổ hợp × 15 ngày** ([`GildedRoseCharacterizationTest`](src/test/java/lab/cq/refactor/GildedRoseCharacterizationTest.java)), rồi mở rộng an toàn thêm `"Conjured Mana Cake"`.

## Yêu cầu môi trường

- JDK 21+ (đã verify trên OpenJDK 26.0.1 với target `--release 21`)
- Maven 3.9+

## Cấu trúc thư mục

- `pom.xml` — cấu hình Maven (`--release 21`, JUnit 5, AssertJ)
- `src/main/java/lab/cq/refactor/Item.java` — entity mẫu của Gilded Rose
- `src/main/java/lab/cq/refactor/before/GildedRoseLegacy.java` — code lồng ghép `if/else` trước refactor
- `src/main/java/lab/cq/refactor/after/GildedRoseRefactored.java` — bản sau refactor (*Extract Method + Replace Conditional with Polymorphism*)
- `src/test/java/lab/cq/refactor/GildedRoseCharacterizationTest.java` — kiểm chứng khớp 100% suốt 15 ngày giữa `before` và `after`
- `solutions/ConjuredGildedRoseExtension.java` — lời giải L3 cắm thêm `"Conjured Mana Cake"` không sửa code lõi

## Cách chạy

```powershell
# 1. Chạy bộ test bảo vệ 122 tổ hợp x 15 ngày
mvn -o test

# 2. Chạy mở rộng L3 ("Conjured Mana Cake")
mvn -o -q compile
java -cp target\classes solutions\ConjuredGildedRoseExtension.java

# 3. Dọn artifact build
mvn -o -q clean
```

## Kết quả mẫu (đã chạy verify)

- `GildedRoseCharacterizationTest`: `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`
- `ConjuredGildedRoseExtension.java`:
```text
Day 1 -> conjured=[Conjured Mana Cake, 1, 8] | normal=[Elixir of the Mongoose, 1, 9]
Day 2 -> conjured=[Conjured Mana Cake, 0, 6] | normal=[Elixir of the Mongoose, 0, 8]
Day 3 -> conjured=[Conjured Mana Cake, -1, 2] | normal=[Elixir of the Mongoose, -1, 6]
Day 4 -> conjured=[Conjured Mana Cake, -2, 0] | normal=[Elixir of the Mongoose, -2, 4]
```
