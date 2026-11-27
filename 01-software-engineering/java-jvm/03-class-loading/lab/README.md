# JVM-03 · Lab — Class loading & ClassLoader hierarchy

> **Mục tiêu:** tận mắt thấy 2 `ClassLoader` nạp cùng một file `.class` tạo ra 2 `Class<?>` khác nhau (`classA != classB`), ép kiểu chéo ném `ClassCastException`, giao tiếp thành công qua shared interface, và thấy chuỗi `ExceptionInInitializerError` → `NoClassDefFoundError`.
> **Thời lượng:** ~35 phút · **Cấp:** L2

## 0. Yêu cầu môi trường
| Công cụ | Phiên bản | Kiểm tra |
| :--- | :--- | :--- |
| JDK | 21+ | `java -version` |
| Maven | 3.9+ | `mvn -v` |

> Lab chạy 100% offline, không cần Docker. Chạy được trên **Windows (PowerShell / Git Bash) và Linux/macOS**.

## 1. Cấu trúc
```text
lab/
├── README.md
├── pom.xml                                  # Maven project độc lập, Java 21
├── src/
│   ├── main/java/lab/jvm/classloading/
│   │   ├── PluginContract.java              # Interface dùng chung (nạp bởi Parent Loader)
│   │   ├── SamplePlugin.java                # Class mục tiêu được nạp 2 lần độc lập
│   │   ├── IsolatingClassLoader.java        # Custom ClassLoader cô lập
│   │   ├── BrokenStaticInit.java            # Giả lập lỗi trong khối static {}
│   │   └── ClassLoadingLabRunner.java       # Runner 4 thí nghiệm
│   └── test/java/lab/jvm/classloading/
│       └── ClassLoadingTest.java            # JUnit 5 chứng minh isolation & <clinit>
└── solutions/
    └── E7StaticStateIsolationTest.java      # Lời giải E7 kiểm tra cô lập static state
```

## 2. Chạy

Từ thư mục `lab/`:
```bash
mvn -q verify
mvn -q compile exec:java
```

*(Để quan sát log JVM nạp từng class theo thời gian thực)*:
```bash
java -Xlog:class+load=info -cp target/classes lab.jvm.classloading.ClassLoadingLabRunner
```

## 3. Các bước thí nghiệm

### Bước 1 — Chứng minh 2 `Class<?>` tách biệt & `ExceptionInInitializerError`
- **Dự đoán trước khi chạy** (ghi vào `my-notes.md`):
  1. Khi `SamplePlugin` được nạp bởi `plugin-loader-A` và `plugin-loader-B`, `STATIC_INIT_COUNTER` của `instanceB` in ra `1` hay `2`?
  2. Gọi `BrokenStaticInit.ping()` lần 1 và lần 2 ném 2 loại `Throwable` nào?
- Làm gì: chạy `mvn -q verify` và `mvn -q compile exec:java`.

### Bước 2 — Quan sát `-Xlog:class+load=info`
- Làm gì: chạy lệnh `java -Xlog:class+load=info ...` ở §2 và tìm các dòng có `SamplePlugin`.

## 4. Kết quả tham khảo khi chạy

> **Output thực tế trên máy soạn** *(Windows 11 amd64, JDK 26.0.1 `--release 21`, ngày 2026-11-27)*:
> - `mvn verify`: **3/3 tests pass**, 0 failures, 0 errors.
> - `classA == classB`: `false`; cả `instanceA` và `instanceB` đều có `initCount=1` riêng biệt.
> - Ép kiểu `(SamplePlugin) instanceA` ném `ClassCastException: ... unnamed module of loader 'plugin-loader-A' ...`.
> - `BrokenStaticInit.ping()`: lần 1 ném `ExceptionInInitializerError`, lần 2 ném `NoClassDefFoundError: Could not initialize class lab.jvm.classloading.BrokenStaticInit`.

## 5. Giải thích kết quả
- JVM định danh runtime class theo cặp `(Defining ClassLoader, Binary Name)` nên 2 loader khác nhau tạo 2 `Class<?>` và 2 vùng `static` độc lập. Giao tiếp an toàn phải qua `PluginContract` nằm ở parent loader chung (xem [README §3.2](../README.md)).

## 6. Biến thể để tự khám phá
- Copy `solutions/E7StaticStateIsolationTest.java` vào `src/test/java/lab/jvm/classloading/` và chạy `mvn -q verify` để xác nhận sửa `STATIC_INIT_COUNTER` ở `loader1` không ảnh hưởng `loader2`.

## 7. Dọn dẹp
```bash
mvn -q clean
```
