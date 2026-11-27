# DSA-02 · Lab — HashMap internals

> **Mục tiêu:** tự đọc và chạy `SimpleHashMap` tự cài đặt, đo tận mắt hậu quả hiệu năng khi `hashCode()` trả hằng số (`return 42`) với cả key `Comparable` và không `Comparable`, tái hiện rò rỉ Mutable Key, và mô phỏng an toàn cơ chế sinh chu trình `A -> B -> A` khi `resize()` đồng thời ở Java 7.
> **Thời lượng:** ~30 phút · **Cấp:** L2

## 0. Yêu cầu môi trường
| Công cụ | Phiên bản | Kiểm tra |
| :--- | :--- | :--- |
| JDK | 21+ | `java -version` |
| Maven | 3.9+ | `mvn -v` |

## 1. Cấu trúc
```text
lab/
├── README.md
├── pom.xml                                        # Maven project độc lập, Java 21
├── src/
│   ├── main/java/lab/dsa/hashmap/
│   │   ├── SimpleHashMap.java                     # Tự cài HashMap (bitmask, h^(h>>>16), 2x resize, load factor 0.75)
│   │   ├── HashMapPitfallsDemo.java               # Đo Good vs Bad HashCode, Mutable Key Leak & Java 7 Cycle Sim
│   │   └── HashMapLabRunner.java                  # Runner thực nghiệm
│   └── test/java/lab/dsa/hashmap/
│       └── HashMapLabTest.java                    # JUnit 5 kiểm chứng
└── solutions/
    └── ImmutableMapKeySolution.java               # Mẫu Key bất biến bằng record & Java 7 final class chuẩn
```

## 2. Chạy

Từ thư mục `lab/`:
```powershell
mvn -o -B verify
mvn -o -B compile exec:java
```

## 3. Các bước thí nghiệm

### Bước 1 — Chạy `SimpleHashMap`, Benchmark Collision & Java 7 Resize Cycle
- **Dự đoán trước khi chạy** (ghi vào `my-notes.md`):
  1. Với `15.000` phần tử có `hashCode() == 42`: loại nào chậm nhất — `GoodHashKey`, `ConstantComparableKey`, hay `ConstantNonComparableKey`? Chậm hơn khoảng bao nhiêu lần?
  2. Sau khi `put(key, "VIP")` rồi gọi `key.setCode("CHANGED")`, `map.get(key)` trả về gì và `map.size()` bằng bao nhiêu?
- Làm gì: chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`.

## 4. Kết quả tham khảo khi chạy

> **Output thực tế trên máy soạn** *(Windows 11 amd64, OpenJDK 26.0.1 `--release 21`, ngày 2026-11-27)*:
> - `mvn -o -B verify`: **3/3 tests pass**, 0 failures, 0 errors.
> - `SimpleHashMap` (`10.000` keys): `size=10000 | capacity=16384 | resizes=10 | maxBucketChain=5`.
> - Benchmark `put + get` (`8.000` phần tử trên `java.util.HashMap`):
>   - `GoodHashKey (O(1))`: **`1457 us (1.46 ms)`**
>   - `ConstantComparableKey (RB-Tree)`: `6134 us (6.13 ms)`
>   - `ConstantNonComparableKey (Broken!)`: **`199790 us (199.79 ms)`** (**chậm hơn `137.1×`** so với `GoodHashKey`!)
> - `MutableKey Leak`: `Before=DISCOUNT_20 | After=null | map.size()=1`.
> - `Java 7 Concurrent Resize Cycle Simulator`: `Floyd cycle detected (A -> B -> A): true`.

## 5. Giải thích kết quả
- `hashCode()` hằng số ép toàn bộ phần tử vào cùng 1 bucket; với key không `Comparable`, việc lookup bằng key mới không thể điều hướng theo `compareTo()` trên `TreeNode` dẫn tới hiệu năng tụt thê thảm. Còn Head Insertion ở Java 7 là nguyên nhân tạo chu trình vòng tròn khi 2 thread cùng `resize()` (xem [README §3](../README.md)).

## 6. Dọn dẹp
```powershell
mvn -o -B -q clean
```
