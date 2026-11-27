# GIT-03 · Đáp án — Tìm bug bằng git bisect

> ⚠️ **Chỉ mở sau khi đã tự làm.** Đọc đáp án mà chưa tự làm = mất 80% giá trị bài tập.
> Mỗi đáp án được bọc trong `<details>` để tránh lộ khi lướt.

---

### E1
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. **1.000 commit:** $\lceil \log_2(1000) \rceil = \mathbf{10}$ bước ($2^{10} = 1024$).
2. **10.000 commit:** $\lceil \log_2(10000) \rceil = \mathbf{14}$ bước ($2^{14} = 16384$).
So với kiểm tra tuần tự (trung bình 500–5.000 bước), `git bisect` giảm số lần build/test hàng trăm lần.
</details>

---

### E2
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. **Exit `0`:** Commit hiện tại là **`good`** → thu hẹp tìm kiếm về nửa sau (gần `bad` hơn).
2. **Exit `1`:** Commit hiện tại là **`bad`** → thu hẹp tìm kiếm về nửa trước (gần `good` hơn).
3. **Exit `125`:** Commit **không kiểm tra được** (`skip`) → Git bỏ qua commit này và thử commit lân cận.
4. **Exit `128` (`> 127`):** `git bisect run` **dừng khẩn cấp (abort)** ngay tại chỗ để tránh đánh dấu sai hàng loạt khi lệnh bị crash/không tìm thấy command.
</details>

---

### E3
<details><summary>Xem đáp án</summary>

**Đáp án:**
Ngay khi `git bisect` checkout về commit trung vị trong quá khứ (nơi file `my-bisect-check.sh` chưa được tạo ra), file script đó **biến mất khỏi thư mục làm việc** → shell báo không tìm thấy `./my-bisect-check.sh` (exit `127` / `> 127` khiến bisect dừng hoặc báo fail sai). Luôn đặt script test ở **ngoài repo** (ví dụ `/tmp` hoặc `$env:TEMP`) hoặc dùng đường dẫn tuyệt đối ngoài working tree đang bisect.
</details>

---

### E4
<details><summary>Xem đáp án</summary>

**Đáp án:**
- Dùng `--first-parent` khi repo merge nhiều feature branch bằng `--no-ff` và các commit nằm bên trong feature branch có thể chưa build xanh từng bước (WIP), hoặc bạn muốn xác định nhanh **PR/merge nào** đưa lỗi vào `main`.
- Khi tìm ra merge commit `M` là *first bad commit*, bạn thu hẹp phạm vi vào đúng nhánh phụ bằng cách bắt đầu phiên bisect mới trên **`M^2` (bad)** và **`M^1` (good)** (hoặc `git bisect start M^2 M^1`, với `M^1` là `main` ngay trước khi merge và `M^2` là tip nhánh được merge vào).
</details>

---

### E5
<details><summary>Xem đáp án</summary>

**Đáp án:**
Dùng tùy chọn đổi tên thuật ngữ của `git bisect`:
```bash
git bisect start --term-old=broken --term-new=fixed HEAD <old-broken-commit>
```
Sau đó đánh dấu `git bisect broken` / `git bisect fixed` thay cho `good`/`bad`.
</details>

---

### E6
<details><summary>Xem đáp án</summary>

**Kết quả thực tế khi chạy trên Windows (`git 2.54.0.windows.1`):**
Chuỗi commit `git bisect run` đã kiểm tra qua 8 bước:
`iteration 100` (good) → `150` (bad) → `125` (good) → `137` (bad) → `131` (good) → `134` (good) → `136` (good) → kết luận:
`34db58801042dfe267161565f02d37325cf5f7b6 is the first bad commit` (`chore: iteration 137`).
*(Xem script chạy tự động tại [lab/solutions/run-bisect-solution.ps1](./lab/solutions/run-bisect-solution.ps1).)*
</details>

---

### E7
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. Nếu trả về `exit 1` khi thiếu `pricing.properties` (lỗi môi trường/commit thiếu file, không phải lỗi logic giảm giá), `git bisect run` sẽ đánh dấu commit đó là **`bad`** và bỏ qua toàn bộ nửa lịch sử phía sau — trả về sai thủ phạm!
2. Đặt `exit 125` cho mọi lỗi thiếu file, lỗi compile, hoặc môi trường test không dựng được giúp `git bisect` chỉ `skip` commit đó và tiếp tục tìm đúng commit `#137`.
</details>

---

### E8
<details><summary>Xem đáp án</summary>

**Hướng giải:**
- Với `git bisect start --first-parent HEAD M0`: Git chỉ kiểm tra các commit nằm trên xương sống `parent 1` (`M0`, `M1`, `M2`, `M3`) và kết luận chính xác merge commit `M1` là first bad commit (không rơi vào 3 commit bên trong nhánh phụ).
- Sau `git bisect reset`, chạy `git bisect start M1^2 M0` (hoặc `M1^1`) sẽ tìm ra đúng commit 1 trong `feat/discount`.
</details>

---

### E9
<details><summary>Xem đáp án</summary>

<details open><summary>3 nguyên nhân làm `git bisect run` chỉ nhầm vào commit `docs: update README.md`</summary>

1. **Không có `clean` & không kiểm tra exit code của `mvn test-compile`:**
   - Script không có `set -e` cũng không có `|| exit 125`. Khi gặp commit cũ biên dịch lỗi, script vẫn chạy tiếp `java -cp target/classes com.app.VatSmokeTest` — thực chất đang chạy lại file `.class` cũ còn sót trong `target/` của bước trước! Hoặc khi `VatSmokeTest` mới xuất hiện ở gần đây, ở commit trước đó `VatSmokeTest` chưa tồn tại nên `java` ném `NoClassDefFoundError` (exit `1`), làm bisect đánh dấu `bad` nhầm chỗ!
2. **Rác state dùng chung (`target/test-cache.dat`) & phụ thuộc ngày hệ thống:**
   - File `target/test-cache.dat` không bị xóa giữa các lần checkout làm kết quả bước sau phụ thuộc bước trước (vi phạm tính độc lập & đơn điệu).
3. **Thiếu điều kiện tiền đề cho test repro:**
   - Nếu `VatSmokeTest` mới được thêm vào codebase sau commit gây bug, bạn phải giữ file test repro ở **ngoài repo** và tự biên dịch nó trong script, nếu không mọi commit cũ hơn ngày tạo `VatSmokeTest` đều fail vì thiếu class!

**Script sửa lại chuẩn xác:**
```bash
#!/usr/bin/env bash
# Đặt ngoài repo, truyền đường dẫn file repro test nằm ngoài repo
rm -rf target/
mvn -q clean test-compile || exit 125
javac -cp target/classes -d target/repro /tmp/VatSmokeTest.java || exit 125
java -cp target/classes:target/repro VatSmokeTest
```

</details>
</details>

---

## Self-check (từ README §9) — đáp án gợi ý
<details><summary>Xem</summary>

1. **Số bước tối đa:** Theo công thức $\lceil \log_2(N) \rceil$: 1.000 commit tối đa **10 bước** ($2^{10}=1024$); 1.000.000 commit tối đa **20 bước**.
2. **3 nhóm exit code:** `0` = `good`; `1..127` (trừ `125`) = `bad`; `125` = `skip` (không kiểm tra được); `> 127` làm `bisect run` dừng khẩn cấp.
3. **4 điều kiện để `bisect run` đúng:** (1) Lỗi đơn điệu trong khoảng `[good..bad]` (không flaky); (2) Script độc lập & clean build lại mỗi bước; (3) Trả `125` nếu không biên dịch được; (4) Không để file rác làm bẩn working tree hoặc đặt script bên ngoài repo.
4. **Với merge commit:** Dùng `git bisect start --first-parent <bad> <good>` để chỉ bước theo nhánh chính (tránh rơi vào commit WIP gãy build trên nhánh con); khi tìm ra merge commit `M`, mở bisect con trên `M^1..M^2` nếu cần.
5. **Không để script trong repo đang bisect:** Vì khi `bisect` nhảy lùi về commit cũ hơn thời điểm bạn tạo script, file script biến mất khỏi đĩa khiến lệnh chạy thất bại.
</details>
