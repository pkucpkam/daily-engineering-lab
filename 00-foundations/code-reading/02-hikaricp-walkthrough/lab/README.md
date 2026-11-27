# READ-02 · Lab — Đọc source HikariCP

> **Mục tiêu:** quan sát trực tiếp luồng `HikariPool.getConnection()` khi pool cạn (`pool size = 2`, `5` thread), thấy log stack trace thật của `ProxyLeakTask`, đo sự khác biệt của `FastList`, và soi bytecode `ConcurrentBag` / `FastList` bằng `javap`.
> **Thời lượng:** ~45 phút · **Cấp:** L2

## 0. Yêu cầu môi trường
| Công cụ | Phiên bản | Kiểm tra |
| :--- | :--- | :--- |
| JDK | 21+ | `java -version` |
| Maven | 3.9+ | `mvn -v` |

> Lab dùng HikariCP `5.1.0` + H2 in-memory `2.2.224` + SLF4J Simple `1.7.36`, chạy hoàn toàn cục bộ không cần Docker. Chạy được trên **Windows (PowerShell / Git Bash) và Linux/macOS**.

## 1. Cấu trúc
```text
lab/
├── README.md
├── pom.xml                                  # HikariCP 5.1.0 + H2 + JUnit 5 + AssertJ
├── src/
│   ├── main/java/lab/codereading/hikari/
│   │   └── HikariWalkthroughRunner.java     # 3 thí nghiệm: Contention, Leak, FastList
│   └── test/java/lab/codereading/hikari/
│       └── HikariWalkthroughTest.java       # Test exhaustion, ConcurrentBag ThreadLocal, FastList
└── solutions/
    └── E7LeakAndMxBeanTest.java             # Lời giải E7 kiểm tra HikariPoolMXBean
```

## 2. Chạy

Từ thư mục `lab/`:
```bash
mvn -q verify
mvn -q compile exec:java
```

## 3. Các bước thí nghiệm

### Bước 1 — Chạy test chứng minh `ConcurrentBag` & pool exhaustion
- Làm gì: chạy `mvn -q verify`.
- **Dự đoán trước khi chạy** (ghi vào `my-notes.md`):
  1. Trong test `concurrentBagReusesThreadLocalEntryBeforeScanningSharedList`, nếu Thread 1 mượn 1 entry trong số 2 entry, trả lại (`requite`), rồi mượn tiếp ngay trên cùng Thread 1, nó có lấy đúng lại entry ban đầu không?
  2. Giá trị `connectionTimeout` tối thiểu mà HikariCP cho phép cấu hình là bao nhiêu ms?

### Bước 2 — Chạy walkthrough & debug step-through
- Làm gì: chạy `mvn -q compile exec:java`, sau đó đặt breakpoint ở `ds.getConnection()` trong `HikariWalkthroughRunner` và bước vào `HikariPool.getConnection(long)` + `ConcurrentBag.borrow(long, TimeUnit)`.
- **Dự đoán trước khi chạy**:
  1. Với `maximumPoolSize = 2`, 5 worker thread cùng giữ `150ms`, `connectionTimeout = 350ms`: bao nhiêu thread thành công và bao nhiêu thread timeout?
  2. Khi bật `leakDetectionThreshold = 2000ms` và giữ connection `2200ms` rồi mới đóng, ngoài dòng `WARN`, HikariCP có in thêm dòng `INFO` nào sau khi đóng không?

### Bước 3 — Soi bytecode `FastList` & `ConcurrentBag`
- Làm gì: chạy `javap -c -p` trên `HikariCP-5.1.0.jar` trong `~/.m2/repository/com/zaxxer/HikariCP/5.1.0/HikariCP-5.1.0.jar`:
  ```bash
  javap -c -p -cp ~/.m2/repository/com/zaxxer/HikariCP/5.1.0/HikariCP-5.1.0.jar com.zaxxer.hikari.util.FastList
  ```
- Quan sát: `FastList.remove(Object)` duyệt ngược từ `size - 1` về `0` và so sánh bằng `if_acmpne` (`==`) thay vì `.equals(...)`.

## 4. Kết quả của bạn

| Thí nghiệm | Kết quả trên máy bạn | Ghi chú |
| :--- | :--- | :--- |
| `[1]` Contention (5 threads, pool=2) | succeeded = ?, timedOut = ? | |
| `[2]` Leak detection (2000ms vs 2200ms) | Có thấy `WARN` + stack trace + `unleaked`? | |
| `[3]` 100k × 32 LIFO close | `ArrayList`: ? ms \| `FastList`: ? ms | |

> **Số tham khảo của người soạn** *(Windows 11 amd64, 16 CPUs, JDK 26.0.1 `--release 21`, HikariCP 5.1.0, ngày 2026-11-27)*:
> - `mvn verify`: **4/4 tests pass**, 0 failures, 0 errors.
> - `[1]` `succeeded=4, timedOut=1` (`Worker-5 TIMED OUT after 363 ms: ... timed out after 361ms (total=2, active=2, idle=0, waiting=0)`).
> - `[2]` Log `WARN com.zaxxer.hikari.pool.ProxyLeakTask - Connection leak detection triggered ... Apparent connection leak detected` kèm stack trace tại `HikariWalkthroughRunner.java:94`, sau đó log `INFO ... was returned to the pool (unleaked)`.
> - `[3]` `100k x 32 LIFO statement close -> ArrayList: 251 ms | FastList: 34 ms` (`~7,4×` nhanh hơn).

## 5. Giải thích kết quả
- Do `ConcurrentBag` dùng `ThreadLocal` fast-path + CAS từng phần tử + `SynchronousQueue` fair handoff, các thread tranh chấp chỉ bị chặn đúng lúc pool hết sạch connection.
- `FastList` quét từ đuôi về đầu và kiểm tra tham chiếu `==` thay vì quét từ đầu và dịch chuyển mảng như `ArrayList` — phù hợp đặc thù đóng `Statement`/`ResultSet` theo thứ tự LIFO. Chi tiết xem [README §3](../README.md).

## 6. Biến thể để tự khám phá
1. Tăng `connectionTimeout` ở `[1]` lên `500ms` và chạy lại để thấy cả `5/5` worker thành công.
2. Đặt `leakDetectionThreshold = 0` rồi chạy lại `[2]` — quan sát không có log `ProxyLeakTask` nào xuất hiện.

## 7. Dọn dẹp
```bash
mvn -q clean
```
