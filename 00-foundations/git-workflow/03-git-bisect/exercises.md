# GIT-03 · Bài tập — Tìm bug bằng git bisect

> Làm theo thứ tự. **Tự làm trước, chỉ mở hint khi đã thử ≥ 15 phút.** Đáp án ở [solutions.md](./solutions.md) — chỉ mở sau khi đã có câu trả lời của riêng bạn.
> Ghi thời gian làm từng bài vào `my-notes.md` để theo dõi tiến bộ.

| Cấp | Mục đích | Số lượng | Thời gian gợi ý |
| :--- | :--- | :---: | :---: |
| 🟢 L1 — Recall | Nhớ & hiểu khái niệm | 5 | 20' |
| 🟡 L2 — Apply | Chạy bisect thật & viết script điều kiện | 3 | 60' |
| 🔴 L3 — Deep | Debug tình huống bisect chỉ sai commit | 1 | 60' |

---

## 🟢 L1 — Recall

### E1. Số bước tối đa trên 1.000 và 10.000 commit
Tính số bước kiểm tra tối đa của `git bisect` trên một dải lịch sử gồm:
1. 1.000 commit.
2. 10.000 commit.
So sánh với việc kiểm tra tuần tự từng commit từ `HEAD` lùi về.

---

### E2. Ý nghĩa của exit code `0`, `1`, `125`, `128` trong `git bisect run`
Trong `git bisect run`, nếu script kiểm tra trả về các mã thoát sau thì Git hành động thế nào?
1. Exit `0`
2. Exit `1`
3. Exit `125`
4. Exit `128`

---

### E3. Vì sao phải đặt script test bên ngoài repo (hoặc không commit vào nhánh)?
Nếu bạn viết file `my-bisect-check.sh`, `git add` & `commit` nó ở `HEAD`, rồi chạy `git bisect start HEAD v1.0` và `git bisect run ./my-bisect-check.sh`, chuyện gì xảy ra ngay ở bước đầu tiên?

---

### E4. `--first-parent` dùng khi nào?
Khi nào bạn nên thêm `--first-parent` vào `git bisect start --first-parent HEAD v1.0`, và khi đã tìm ra một merge commit `M`, làm thế nào để tiếp tục tìm commit cụ thể bên trong nhánh được merge?

---

### E5. Đảo chiều tìm "commit đã sửa lỗi" (`--term-old` / `--term-new`)
Mặc định `git bisect` tìm commit chuyển từ `good` → `bad`. Nếu một tính năng bị hỏng suốt tháng trước nhưng hôm nay trên `HEAD` tự nhiên lại chạy đúng và bạn muốn tìm **commit nào đã vô tình sửa nó**, bạn dùng lệnh `git bisect start` với tùy chọn gì?

---

## 🟡 L2 — Apply

### E6. Dùng `git bisect run` tìm ra commit `#137` trong repo 200 commit
**Bối cảnh:** Chạy script khởi tạo repo 200 commit trong [lab/](./lab/README.md) (`create-200-commits-repo.ps1` hoặc `.sh`).

**Yêu cầu:**
1. Dùng `git bisect start HEAD <first-commit>` và `git bisect run` cùng `bisect-check.ps1` (hoặc `.sh`) để tự động tìm thủ phạm.
2. Ghi lại chuỗi commit iteration mà Git đã nhảy qua ở mỗi bước (`100 → ...`) và chứng minh đúng bằng `git show <found-hash>`.
3. Nhớ chạy `git bisect reset` và kiểm tra `git status` đã thoát `detached HEAD`.

**Tiêu chí hoàn thành:**
- [ ] `git bisect run` tự động kết luận đúng `chore: iteration 137` trong đúng 8 bước.

<details><summary>💡 Hint</summary>

```powershell
$check = (Resolve-Path 00-foundations/git-workflow/03-git-bisect/lab/scripts/bisect-check.ps1).Path
cd $env:TEMP\git-03-bisect-lab
git bisect start HEAD (git rev-list --max-parents=0 HEAD)
git bisect run powershell -NoProfile -ExecutionPolicy Bypass -File $check
git bisect reset
```
</details>

---

### E7. Viết script `bisect-check` xử lý đúng lỗi compile (`exit 125`)
**Bối cảnh:** Trên repo 200 commit vừa tạo (hoặc 1 bản sao), hãy tạo lại hoặc sửa commit `#125` sao cho `pricing.properties` bị xóa mất (giả lập 1 commit trung gian bị hỏng không test được), còn commit `#137` vẫn là commit đổi `vip_discount_percent=5`.

**Yêu cầu:**
1. Giải thích điều gì xảy ra nếu trong `bisect-check`, khi thiếu file `pricing.properties` bạn viết `exit 1` thay vì `exit 125`.
2. Kiểm chứng với `bisect-check.ps1` (đã viết sẵn `exit 125` khi thiếu file) bằng `git bisect skip` / chạy `git bisect run` — hoặc viết một script mới cho bài toán kiểm tra tồn tại key + giá trị nguyên dương trong file config bất kỳ có xử lý `exit 125` khi file thiếu/sai format build.

**Tiêu chí hoàn thành:**
- [ ] Script trả về `0` khi hợp lệ, `1` khi sai nghiệp vụ, và `125` khi thiếu artifact/lỗi build.
- [ ] Đối chiếu tham khảo tại `lab/solutions/run-bisect-solution.ps1`.

<details><summary>💡 Hint</summary>

Nhìn `bisect-check.ps1`: `if (-not (Test-Path $file)) { exit 125 }` đảm bảo nếu một commit trung gian bị thiếu file build/config, `git bisect` tự skip commit đó thay vì kết luận nhầm đó là commit gây bug.
</details>

---

### E8. Thực hành `git bisect --first-parent` trên lịch sử có merge commit
**Bối cảnh:** Tạo 1 repo tạm gồm `main` (commit `M0`), tách nhánh `feat/discount` có 3 commit (commit 1 đổi `discount=5` — gây bug, commit 2 + 3 thêm doc), rồi `git switch main && git merge --no-ff feat/discount` tạo merge commit `M1`, sau đó thêm 2 commit bình thường `M2`, `M3` trên `main`.

**Yêu cầu:**
Chạy `git bisect start --first-parent HEAD M0` và đánh dấu `good` / `bad` thủ công. Quan sát:
1. Git có nhảy vào 3 commit bên trong nhánh `feat/discount` không, hay dừng lại báo `M1` là first bad commit?
2. Sau đó `git bisect reset` rồi chạy `git bisect start M1^2 M0` để chỉ ra đúng commit 1 bên trong nhánh phụ.

**Tiêu chí hoàn thành:**
- [ ] Thấy được sự khác biệt giữa `--first-parent` (chỉ ra `M1`) và bisect đầy đủ trên nhánh con (chỉ ra commit 1 trong `feat/discount`).

<details><summary>💡 Hint</summary>

`M1^2` là parent thứ 2 của merge commit `M1` — tức là đầu nhánh `feat/discount` trước khi merge vào `main`.
</details>

---

## 🔴 L3 — Deep

### E9. Sự cố: `git bisect run` kết luận nhầm một commit vô tội!
**Tình huống:**
> Một đồng nghiệp báo cáo: *"Anh dùng `git bisect run` trên 300 commit để tìm chỗ hỏng tính thuế VAT của module Java. Nó chạy xong báo thủ phạm là commit `4f8a12c` (`docs: update README.md`)! Rõ ràng sửa README không thể làm sai code Java được!"*
>
> Bạn kiểm tra script của anh ấy:
> ```bash
> #!/usr/bin/env bash
> mvn test-compile -DskipTests
> java -cp target/classes com.app.VatSmokeTest
> ```
> Ngoài ra, `VatSmokeTest` trong project có lúc gọi tới một file cấu hình tạm ở `target/test-cache.dat` và đọc ngày hiện tại hệ thống.

**Yêu cầu:**
1. Liệt kê **3 nguyên nhân kỹ thuật khả dĩ nhất** từ script và môi trường trên giải thích vì sao `git bisect run` lại kết luận sai vào commit `docs: update README.md`.
2. Viết lại script kiểm tra đúng chuẩn (đảm bảo clean build, xử lý `exit 125`, loại bỏ state dư thừa giữa các bước) để `git bisect run` không bao giờ chỉ nhầm vào commit vô tội.

**Tiêu chí hoàn thành:**
- [ ] Chỉ ra đủ 3 nguyên nhân: (a) `mvn test-compile` lỗi trả `1` không có `|| exit 125` (hoặc compile lỗi nhưng không thoát sớm vẫn chạy `target/classes` cũ còn sót!), (b) thiếu `clean` giữ bytecode/cache cũ trong `target/`, (c) test không đơn điệu (flaky phụ thuộc ngày/state cũ) hoặc ở commit đó có một thay đổi khác làm fail trước khi chạy tới commit thật.
- [ ] Script sửa lại có `mvn clean test-compile ... || exit 125`.

<details><summary>💡 Hint</summary>

Để ý dòng `mvn test-compile -DskipTests` trong script KHÔNG có `set -e` và KHÔNG có `|| exit 125`! Nếu ở một commit cũ `mvn test-compile` thất bại, script vẫn đi tiếp xuống dòng `java -cp target/classes ...` và **chạy lại file `.class` cũ còn sót trong `target/`** của bước trước (hoặc nếu `VatSmokeTest` mới được thêm sau `docs: update README.md`, ở các commit trước `README.md` nó chưa tồn tại → `ClassNotFoundException` exit 1)!
</details>
