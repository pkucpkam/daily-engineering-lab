# DSA-01 · Lab — Độ phức tạp & Big-O thực dụng

> **Mục tiêu:** tự tay đo trên máy thật sự chênh lệch tốc độ giữa `int[]`, `ArrayList<Integer>`, và fragmented `LinkedList<Integer>` ở $n = 1.000.000$ (kể cả so với $O(n \log n)$ `Arrays.sort`), và đếm chính xác số lần `grow()` + tổng phần tử phải copy khi không pre-size `ArrayList`.
> **Thời lượng:** ~30 phút · **Cấp:** L2

## 0. Yêu cầu môi trường
| Công cụ | Phiên bản | Kiểm tra |
| :--- | :--- | :--- |
| JDK | 21+ | `java -version` |
| Maven | 3.9+ | `mvn -v` |

> *(Ghi chú: Không cần Docker; chạy hoàn toàn thuần Java 21 offline trên cả Windows và Linux. Thay vì JMH rời — không có sẵn trong Maven local offline — harness `CacheLocalityBenchmark` tự thực hiện các vòng Warmup cho C2 JIT và giữ Blackhole checksum để tránh Dead-Code Elimination).*

## 1. Cấu trúc
```text
lab/
├── README.md
├── pom.xml                                        # Maven project độc lập, Java 21
├── src/
│   ├── main/java/lab/dsa/complexity/
│   │   ├── CacheLocalityBenchmark.java            # Benchmark int[] vs ArrayList vs Fragmented LinkedList vs Arrays.sort
│   │   └── ComplexityLabRunner.java               # Runner in bảng số đo & chi phí resize ArrayList
│   └── test/java/lab/dsa/complexity/
│       └── ComplexityLabTest.java                 # JUnit 5 kiểm chứng
└── solutions/
    └── PreSizedCollectionsSolution.java           # Lời giải E7 & E8 (HashSet + pre-sized ArrayList)
```

## 2. Chạy

Từ thư mục `lab/`:
```powershell
mvn -o -B verify
mvn -o -B compile exec:java
```

## 3. Các bước thí nghiệm

### Bước 1 — Benchmark `1.000.000` phần tử & Đo chi phí Resize `ArrayList`
- **Dự đoán trước khi chạy** (ghi vào `my-notes.md`):
  1. Ở $n = 1.000.000$, `LinkedList` phân mảnh trên Heap chậm hơn `int[]` khoảng bao nhiêu lần? Liệu duyệt $O(n)$ trên `LinkedList` có chậm ngang hoặc chậm hơn $O(n \log n)$ `Arrays.sort(int[])` không?
  2. Thêm `1.000.000` phần tử vào `new ArrayList<>()` (tăng `1.5×` từ `10`) tốn bao nhiêu lần `grow()` và copy tổng cộng bao nhiêu phần tử?
- Làm gì: chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`.

## 4. Kết quả tham khảo khi chạy

> **Output thực tế trên máy soạn** *(Windows 11 amd64, OpenJDK 26.0.1 `--release 21`, ngày 2026-11-27)*:
> - `mvn -o -B verify`: **2/2 tests pass**, 0 failures, 0 errors.
> - `ComplexityLabRunner` (`n = 1.000.000` phần tử sau JIT Warmup):
>   - `int[] sum O(n)`: **`120 us (0.12 ms)`**
>   - `ArrayList sum O(n)`: `1665 us (1.67 ms)`
>   - `LinkedList sum O(n)` (phân mảnh Heap): **`17254 us (17.25 ms)`** (**chậm hơn `143.8×`** so với `int[]`!)
>   - `Arrays.sort(int[]) O(n log n)`: `55519 us (55.52 ms)`
> - `ArrayList` resize (`n = 1.000.000`, `1.5×` từ `cap = 10`): `29` lần `grow()`, copy tổng **`2.430.972` phần tử (`2.43 * n`)**, `default vs pre-sized = 5675 us vs 4051 us`.
> - `PreSizedCollectionsSolution` (`40.000 × 15.000`): `Naive O(n*m) = 243 ms` vs `Fast HashSet O(n+m) = 4 ms`.

## 5. Giải thích kết quả
- CPU Cache Line (`64 bytes`) + Hardware Prefetcher giúp `int[]` chạy ở tốc độ L1/L2 cache (~1 ns), trong khi `LinkedList` phân mảnh trên Heap gây Pointer Chasing & L3 Cache Miss (~60–100 ns). Với `ArrayList.add()`, tổng copy tăng tuyến tính $O(n)$ nên chi phí trung bình mỗi lần là $O(1)$ amortized (xem [README §3](../README.md)).

## 6. Dọn dẹp
```powershell
mvn -o -B -q clean
```
