# JVM-03 · Bài tập — Class loading & ClassLoader hierarchy

> Làm theo thứ tự. **Tự làm trước, chỉ mở hint khi đã thử ≥ 15 phút.** Đáp án ở [solutions.md](./solutions.md) — chỉ mở sau khi đã có câu trả lời của riêng bạn.
> Ghi thời gian làm từng bài vào `my-notes.md` để theo dõi tiến bộ.

| Cấp | Mục đích | Số lượng | Thời gian gợi ý |
| :--- | :--- | :---: | :---: |
| 🟢 L1 — Recall | Nhớ & hiểu khái niệm | 5 | 20' |
| 🟡 L2 — Apply | Viết code / quan sát ClassLoader | 3 | 75' |
| 🔴 L3 — Deep | Debug sự cố JBoss ClassLoading | 1 | 90' |

---

## 🟢 L1 — Recall

### E1. Giá trị ở pha Preparation vs Initialization
Cho class sau:
```java
class Counter {
    static int a = 10;
    static final int B = 20;
    static { a = a + 5; }
}
```
Ngay sau khi kết thúc bước **Linking -> Preparation** (chưa chạy `<clinit>`), giá trị của `a` và `B` trong bộ nhớ lần lượt là bao nhiêu? Sau khi kết thúc **Initialization** là bao nhiêu?

---

### E2. Thứ tự Parent Delegation & Bảo vệ `java.lang.String`
Nếu một lập trình viên tự tạo class `java.lang.String` chứa thêm method `hack()` và đặt lên `-cp`, khi ứng dụng chạy `new java.lang.String()`, class nào được nạp vào JVM? Cơ chế nào đã ngăn chặn?

---

### E3. Hai bản sao biến `static` trong cùng một JVM
Một hệ thống plugin nạp `SamplePlugin.class` bằng 2 `IsolatingClassLoader` (`loaderA` và `loaderB`). Nếu `SamplePlugin` có trường `public static int count = 0;`, và plugin A tăng `count` lên `5`, thì plugin B đọc `SamplePlugin.count` thấy giá trị là bao nhiêu? Vì sao?

---

### E4. `ClassNotFoundException` vs `NoClassDefFoundError`
Trong production log xuất hiện lỗi:
- Lỗi 1 (lúc 08:00:01): `java.lang.ExceptionInInitializerError` tại `PdfExporter.<clinit>`
- Lỗi 2 (từ 08:00:02 trở đi, 500 lần): `java.lang.NoClassDefFoundError: Could not initialize class com.app.PdfExporter`
Giải thích vì sao Lỗi 2 xảy ra dù file `PdfExporter.class` vẫn có mặt trong WAR, và tại sao Lỗi 2 lại không phải là `ClassNotFoundException`.

---

### E5. JBoss Modules vs Classpath phẳng
Trong một file EAR chứa 2 WAR trên JBoss AS 7.1.1: WAR 1 dùng `jackson-databind-2.12`, WAR 2 dùng `jackson-databind-2.15`. Vì sao trên classpath phẳng `-cp` truyền thống điều này gây xung đột, còn JBoss Modules xử lý được?

---

## 🟡 L2 — Apply

### E6. Quan sát Class loading order bằng JVM flag `-Xlog:class+load=info`
**Bối cảnh:** Dùng [lab/](./lab/README.md) để quan sát trực tiếp thứ tự các class được JVM nạp từ `Bootstrap`, `Platform`, `Application` và 2 `IsolatingClassLoader`.

**Yêu cầu:**
1. Chạy `mvn -q verify` và `mvn -q compile exec:java`.
2. Chạy thẳng `ClassLoadingLabRunner` bằng `java -Xlog:class+load=info -cp target/classes lab.jvm.classloading.ClassLoadingLabRunner` (hoặc lưu ra file) và lọc tìm 2 dòng nạp `lab.jvm.classloading.SamplePlugin`.
3. Ghi lại chứng minh: `SamplePlugin` xuất hiện **nhiều lần** trong log `class+load` gắn với các `source:` / loader khác nhau.

**Tiêu chí hoàn thành:**
- [ ] `mvn -q verify` pass 3/3 tests.
- [ ] Chỉ ra được sự khác biệt về tên `AppClassLoader` khi chạy trực tiếp bằng `java -cp` (`app`) vs khi chạy qua `mvn exec:java` (`ExecJavaClassLoader`).

<details><summary>💡 Hint</summary>

Trên PowerShell:
`mvn -q compile && java -Xlog:class+load=info -cp target/classes lab.jvm.classloading.ClassLoadingLabRunner | Select-String "SamplePlugin"`
</details>

---

### E7. Kiểm chứng cô lập biến `static` giữa 2 `ClassLoader` bằng JUnit 5 test
**Bối cảnh:** Tiếp nối `ClassLoadingTest.java`, ta muốn chứng minh rõ ràng bằng assertion rằng biến `static` (như một static cache hoặc static counter) của cùng một class được cô lập hoàn toàn giữa 2 `ClassLoader`.

**Yêu cầu:**
Viết JUnit 5 test dùng `IsolatingClassLoader` nạp `SamplePlugin` vào `loader1` và `loader2`:
1. Gọi method hoặc truy cập `STATIC_INIT_COUNTER` trên `class1` (dùng reflection `getField("STATIC_INIT_COUNTER").get(null)`) và tăng nó lên thành `10`.
2. Đọc `STATIC_INIT_COUNTER` từ `class2` và assert rằng giá trị của `class2` **vẫn là `1`** (không bị ảnh hưởng bởi `class1`).

**Input / code khởi đầu:** `lab/src/test/java/lab/jvm/classloading/ClassLoadingTest.java`.
**Tiêu chí hoàn thành:**
- [ ] Test khẳng định được `counter1.get() == 10` và `counter2.get() == 1` (lời giải mẫu tại `lab/solutions/E7StaticStateIsolationTest.java`).

<details><summary>💡 Hint</summary>

Lấy field bằng `AtomicInteger c1 = (AtomicInteger) class1.getField("STATIC_INIT_COUNTER").get(null);` vì `AtomicInteger` thuộc JDK (Bootstrap ClassLoader) nên kiểu `AtomicInteger` là giống nhau ở cả hai bên!
</details>

---

### E8. Thực hành chuẩn `Class.forName(..., false, loader)` (không kích hoạt `<clinit>`)
**Bối cảnh:** Xem lại bài học rút ra từ `ClassLoadingTest#linkingPreparationRunsBeforeStaticInitialization`.

**Yêu cầu:**
Giải thích vì sao đoạn code kiểm tra bên dưới **SẼ FAIL** (in `true` thay vì `false`):
```java
Class<?> c = Class.forName(Probe.class.getName(), false, loader);
assertThat(Probe.clinitRan).isFalse();
```
Và viết lại cơ chế kiểm tra đúng chuẩn JVMS §5.5 (như đặt biến quan sát bên ngoài class `Probe`).

**Tiêu chí hoàn thành:**
- [ ] Giải thích chính xác lệnh bytecode `getstatic` trên `Probe.clinitRan` là *active use* ép JVM chạy `<clinit>`.

<details><summary>💡 Hint</summary>

Đặt cờ `AtomicBoolean` ở class test bên ngoài chứ không đặt làm `static` field trên chính `Probe`.
</details>

---

## 🔴 L3 — Deep

### E9. Sự cố Production trên JBoss AS 7.1.1: `ClassCastException: X cannot be cast to X` & `NoClassDefFoundError`
**Tình huống:**
> Team deploy `order-portal.war` lên **JBoss AS 7.1.1** (trước đó chạy bình thường trên Tomcat local của dev):
> 1. Ngay khi gọi API xuất JSON, hệ thống nổ:
>    `java.lang.ClassCastException: com.fasterxml.jackson.databind.ObjectMapper cannot be cast to com.fasterxml.jackson.databind.ObjectMapper`
>    tại một helper nội bộ nhận `ObjectMapper` từ JNDI/Shared Module của JBoss.
> 2. Trong `WEB-INF/lib/` của `order-portal.war` có đóng sẵn `jackson-databind-2.9.8.jar`, trong khi JBoss AS 7.1.1 có module `org.codehaus.jackson` / `com.fasterxml.jackson` được gắn ở `global-modules` trong `standalone.xml`.
> 3. Ngoài ra, một EJB timer chạy nền vừa ném `NoClassDefFoundError: Could not initialize class com.company.TaxLookupCache` sau khi app đã chạy được 3 giờ.

**Yêu cầu:**
1. Giải thích **cơ chế ClassLoader** gây ra lỗi `ClassCastException: X cannot be cast to X` giữa `order-portal.war` và module dùng chung của JBoss.
2. Đề xuất **2 hướng khắc phục** cho `order-portal.war` trên JBoss AS 7.1.1 (`scope provided` vs `jboss-deployment-structure.xml` exclusion), và chỉ rõ ưu/nhược điểm từng hướng.
3. Nêu quy trình 3 bước điều tra lỗi `NoClassDefFoundError: Could not initialize class TaxLookupCache` sau 3 giờ chạy (lần tìm gốc lỗi ở đâu trong log JBoss `server.log`?).

**Tiêu chí hoàn thành:**
- [ ] Giải thích đúng cặp `(ClassLoader, BinaryName)` gây lỗi ép kiểu giữa `ModuleClassLoader` của server và `ModuleClassLoader` của WAR.
- [ ] Có mẫu XML `jboss-deployment-structure.xml` chuẩn xác cho JBoss AS 7.1.1 để loại bỏ xung đột module.

<details><summary>💡 Hint</summary>

1. Nếu JBoss server truyền một instance `ObjectMapper` (nạp bởi server module loader) vào code WAR đang có `jackson-databind-2.9.8.jar` tự nạp trong `WEB-INF/lib/` → 2 `ClassLoader` khác nhau cùng định nghĩa `ObjectMapper` → ép kiểu nổ `ClassCastException`.
2. Khắc phục: hoặc bỏ JAR khỏi WAR (`<scope>provided</scope>` nếu dùng đúng bản JBoss), hoặc giữ bản mới trong WAR và loại module Jackson mặc định của JBoss bằng `WEB-INF/jboss-deployment-structure.xml` (`<exclusions><module name="..."/></exclusions>`).
3. Với `TaxLookupCache`: EJB timer chạy lần đầu ở giờ thứ 3 mới chạm vào `TaxLookupCache` → tìm log `ExceptionInInitializerError` ngay trước dòng `NoClassDefFoundError` đầu tiên!
</details>
