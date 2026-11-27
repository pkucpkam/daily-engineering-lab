# JVM-03 · Đáp án — Class loading & ClassLoader hierarchy

> ⚠️ **Chỉ mở sau sau khi đã tự làm.** Đọc đáp án mà chưa tự làm = mất 80% giá trị bài tập.
> Mỗi đáp án được bọc trong `<details>` để tránh lộ khi lướt.

---

### E1
<details><summary>Xem đáp án</summary>

**Đáp án:**
- Sau bước **Preparation**: `a = 0` (giá trị mặc định kiểu `int`), `B = 20` (vì `static final int B = 20` là hằng số compile-time có `ConstantValue` attribute nên được gán ngay lúc Preparation).
- Sau bước **Initialization (`<clinit>`)**: `a = 15` (`10` rồi `+ 5`), `B = 20`.

**Giải thích:**
**Đáp án sai phổ biến:** Nghĩ `a = 10` ngay ở pha Preparation → Sai, giá trị `10` được compiler đưa vào phương thức `<clinit>`, chỉ chạy ở pha Initialization.
</details>

---

### E2
<details><summary>Xem đáp án</summary>

**Đáp án:**
`java.lang.String` chuẩn của **Bootstrap ClassLoader** vẫn được nạp. Cơ chế **Parent Delegation** (ủy quyền cho cha trước) khiến `AppClassLoader` hỏi `PlatformClassLoader` → hỏi `BootstrapClassLoader`, và `BootstrapClassLoader` tìm thấy `java.lang.String` chuẩn ngay lập tức nên `AppClassLoader` không bao giờ tự nạp file giả mạo trên `-cp`. (Ngoài ra, JVM còn kiểm tra gói `java.*` qua Security/Module restrictions không cho custom loader tự định nghĩa class trong package `java.*`.)
</details>

---

### E3
<details><summary>Xem đáp án</summary>

**Đáp án:**
Plugin B đọc `SamplePlugin.count` vẫn thấy **`0`**. Vì mỗi `ClassLoader` (`loaderA` và `loaderB`) tạo một thực thể `Class<?>` riêng biệt trong Metaspace kèm vùng lưu trữ biến `static` độc lập cho riêng loader đó.
</details>

---

### E4
<details><summary>Xem đáp án</summary>

**Đáp án:**
`PdfExporter.class` vẫn nằm trong WAR nhưng khối `static { ... }` của nó đã ném exception ở lần chạm đầu lúc `08:00:01` (`ExceptionInInitializerError`). Theo JVMS §5.5, JVM đánh dấu class `PdfExporter` ở trạng thái *erroneous* (hỏng khởi tạo), nên mọi lần truy cập tiếp theo từ `08:00:02` đều bị JVM từ chối ngay bằng `NoClassDefFoundError: Could not initialize class com.app.PdfExporter`. Đây không phải `ClassNotFoundException` vì `ClassNotFoundException` chỉ ném ra khi nạp động bằng tên chuỗi mà không tìm thấy file `.class`.
</details>

---

### E5
<details><summary>Xem đáp án</summary>

**Đáp án:**
Trên classpath phẳng `-cp`, mọi class đều được nạp bởi một `AppClassLoader` duy nhất theo thứ tự xuất hiện của JAR — JAR đứng trước che khuất hoàn toàn JAR đứng sau (nếu WAR 1 cần API mới của 2.15 hoặc ngược lại sẽ nổ `NoSuchMethodError`). Ngược lại, **JBoss Modules** cấp cho mỗi module/WAR một `ModuleClassLoader` riêng dựa trên đồ thị phụ thuộc (`module.xml`) nên WAR 1 và WAR 2 hoàn toàn có thể nạp 2 phiên bản Jackson riêng biệt mà không thấy và không đè lên nhau.
</details>

---

### E6
<details><summary>Xem đáp án</summary>

**Hướng giải:**
1. Chạy `mvn -q verify` (3/3 tests pass) và `mvn -q compile exec:java`.
2. Khi chạy trực tiếp `java -Xlog:class+load=info -cp target/classes lab.jvm.classloading.ClassLoadingLabRunner`:
   - Dòng 1: `SamplePlugin` được nạp bởi `app` (`AppClassLoader`).
   - Dòng 2 & 3: `SamplePlugin` được nạp lại bởi `plugin-loader-A` và `plugin-loader-B` (với `source: __JVM_DefineClass__`).
   - Tên `AppClassLoader` khi chạy qua `java -cp` là `app`, còn khi chạy qua `mvn exec:java` là `ExecJavaClassLoader` (vì plugin Maven tạo URLClassLoader riêng bọc bên dưới `app`).
</details>

---

### E7
<details><summary>Xem đáp án</summary>

**Hướng giải:**
Vì `AtomicInteger` thuộc `java.util.concurrent.atomic` (nạp bởi Bootstrap ClassLoader), cả `loader1`, `loader2` và test đều dùng chung một kiểu `AtomicInteger`. Dùng reflection đọc field `STATIC_INIT_COUNTER` từ `class1` rồi `.set(10)`, sau đó đọc từ `class2` sẽ thấy vẫn giữ nguyên giá trị `1`.

**Code tham khảo đầy đủ:** xem [lab/solutions/E7StaticStateIsolationTest.java](./lab/solutions/E7StaticStateIsolationTest.java).
</details>

---

### E8
<details><summary>Xem đáp án</summary>

**Đáp án:**
Dòng `assertThat(Probe.clinitRan).isFalse();` biên dịch thành lệnh bytecode **`getstatic Probe.clinitRan`**. Theo JVMS §5.5, đọc một `static` field không phải compile-time constant là một **active use** — chính lệnh `getstatic` đó buộc JVM chạy `<clinit>` của `Probe` ngay trước khi truyền giá trị vào `assertThat(...)`, làm `Probe.clinitRan` chuyển sang `true`!

**Cách viết đúng:** đặt biến `AtomicBoolean` quan sát bên ngoài class `Probe` (xem `ClassLoadingTest#linkingPreparationRunsBeforeStaticInitialization`).
</details>

---

### E9
<details><summary>Xem đáp án</summary>

<details open><summary>Phân tích ClassLoader trên JBoss AS 7.1.1 & hướng xử lý</summary>

1. **Nguyên nhân `ClassCastException: ObjectMapper cannot be cast to ObjectMapper`:**
   - Theo JVMS §5.3, danh tính class là `(ClassLoader, BinaryName)`.
   - Instance `ObjectMapper` lấy từ shared module/JNDI của JBoss được tạo bởi `ModuleClassLoader` của **server module**; còn code bên trong `order-portal.war` lại nạp `ObjectMapper` từ `WEB-INF/lib/jackson-databind-2.9.8.jar` bằng **`ModuleClassLoader` của WAR**. Hai `ClassLoader` khác nhau tạo ra 2 `Class<?>` khác nhau → ép kiểu thất bại!

2. **2 hướng khắc phục:**
   - **Hướng A (dùng chung bản của JBoss):** Đổi dependency Jackson trong `pom.xml` của WAR thành `<scope>provided</scope>` và khai báo phụ thuộc module trong `MANIFEST.MF` (`Dependencies: com.fasterxml.jackson.core.jackson-databind`). *Ưu:* gọn WAR, tương thích trực tiếp với object từ JBoss shared module. *Nhược:* bị khóa theo version cũ của JBoss.
   - **Hướng B (cô lập hoàn toàn dùng bản riêng trong WAR):** Giữ JAR 2.9.8 trong `WEB-INF/lib/`, nhưng **không** nhận trực tiếp instance `ObjectMapper` tạo từ JBoss server module, đồng thời thêm `WEB-INF/jboss-deployment-structure.xml` chặn JBoss inject module Jackson của server vào WAR:
     ```xml
     <jboss-deployment-structure>
       <deployment>
         <exclusions>
           <module name="com.fasterxml.jackson.core.jackson-databind"/>
         </exclusions>
       </deployment>
     </jboss-deployment-structure>
     ```

3. **Điều tra `NoClassDefFoundError: Could not initialize class TaxLookupCache` sau 3 giờ:**
   - Vì EJB timer chạy lần đầu ở giờ thứ 3 mới là lần *active use* đầu tiên của `TaxLookupCache`!
   - Bước 1: Grep ngược trong `server.log` tại thời điểm đúng 3 giờ sau startup (ngay trước dòng `NoClassDefFoundError` đầu tiên) để tìm dòng `ExceptionInInitializerError` + `Caused by: ...`.
   - Bước 2: Đọc khối `static { ... }` của `TaxLookupCache` xem nó có gọi DB/JNDI/file config bị thiếu lúc runtime không.
   - Bước 3: Chuyển logic khởi tạo có thể lỗi ra khỏi `static {}` vào phương thức `@PostConstruct` / factory có log rõ ràng và retry được.

</details>
</details>

---

## Self-check (từ README §9) — đáp án gợi ý
<details><summary>Xem</summary>

1. **3 pha & giá trị `static int count = 5;`:** Loading (đọc byte → `defineClass` tạo `Class<?>`), Linking (Verification → Preparation cấp phát & gán default → Resolution), Initialization (`<clinit>`). Ở Preparation, `count = 0`; sau Initialization, `count = 5`.
2. **2 loader cùng binary name:** Danh tính runtime là `(Defining ClassLoader, Binary Name)`. Hai loader tạo 2 `Class<?>` riêng trong Metaspace kèm bộ biến `static` riêng → `checkcast` thấy khác loader nên ném `ClassCastException`.
3. **Parent Delegation & TCCL:** Child hỏi Parent (`App → Platform → Bootstrap`) trước khi tự nạp. Vì `DriverManager` ở Platform không nhìn xuống được driver JAR ở App/WAR, nó dùng `Thread.currentThread().getContextClassLoader()` để gọi ngược xuống nạp class của ứng dụng.
4. **JBoss Modules vs Classpath phẳng:** `-cp` phẳng tìm tuyến tính 1 namespace duy nhất (dễ đụng version/che khuất JAR); JBoss Modules cấp `ModuleClassLoader` riêng cho từng module/WAR theo đồ thị phụ thuộc tường minh (`module.xml`), cô lập thư viện giữa các deployment.
5. **`ClassNotFoundException` vs `NoClassDefFoundError`:** `ClassNotFoundException` là checked exception khi nạp động (`Class.forName`) không thấy file `.class`; `NoClassDefFoundError` là `Error` khi thiếu class lúc linking/runtime hoặc khi `<clinit>` của class đó từng ném `ExceptionInInitializerError` trước đó.
</details>
