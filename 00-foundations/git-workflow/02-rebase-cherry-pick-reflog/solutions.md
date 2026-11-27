# GIT-02 · Đáp án — Rebase, Cherry-pick, Reflog

> ⚠️ **Chỉ mở sau khi đã tự làm.** Đọc đáp án mà chưa tự làm = mất 80% giá trị bài tập.
> Mỗi đáp án được bọc trong `<details>` để tránh lộ khi lướt.

---

### E1
<details><summary>Xem đáp án</summary>

**Đáp án:**
Khi đồng nghiệp A chạy `git pull`, Git thấy lịch sử local của A (`... -> B -> C -> E1 -> E2`) và lịch sử remote bạn vừa force-push (`... -> B' -> C'`) đã **phân kỳ** (cùng thay đổi nhưng khác hoàn toàn commit hash).
- Nếu `pull` kiểu merge: Git tạo ra một merge commit trộn cả `B, C` cũ lẫn `B', C'` mới — làm **nhân đôi toàn bộ commit** trên PR và dễ sinh conflict giả.
- Nếu `pull --rebase`: Git thử replay `B, C, E1, E2` lên `C'`, đồng nghiệp A bị ép giải lại các conflict của những commit mà chính bạn đã rebase.
</details>

---

### E2
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. Chọn **`fixup`** — gộp thay đổi của `"fix missing import"` vào commit phía trên và bỏ luôn commit message phụ đó.
2. Chọn **`reword`** — giữ nguyên nội dung `tree` của commit nhưng mở trình soạn thảo để sửa commit message.
</details>

---

### E3
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. **Đã `commit` → `reset --hard`:** **Cứu được 100% bằng `git reflog`** (tìm `HEAD@{n}` trước dòng `reset: moving to ...` rồi `git reset --hard <hash>`).
2. **Đã `git add`, chưa `commit` → `reset --hard`:** **Không cứu được bằng `git reflog`** (vì chưa tạo commit nào mới nên ref không dịch chuyển), **nhưng vẫn cứu được nội dung file** bằng `git fsck --lost-found` vì `git add` đã tạo `blob` trong `.git/objects/` (tìm các file mới nhất trong `.git/lost-found/other/`).
3. **Chưa `git add` và chưa `commit` → `reset --hard`:** **Git không cứu được**, vì dữ liệu chưa từng được ghi vào `.git/objects/`. Chỉ có thể khôi phục từ Local History của IDE hoặc backup hệ điều hành.
</details>

---

### E4
<details><summary>Xem đáp án</summary>

**Đáp án:**
Một merge commit có **2 parent** (ví dụ Parent 1 là `main`, Parent 2 là `feature`). Vì `cherry-pick` cần tính diff để áp sang nơi khác, Git không biết bạn muốn lấy diff so với Parent 1 (tức toàn bộ thay đổi mà nhánh `feature` mang vào `main`) hay so với Parent 2.
- `-m 1` (`--mainline 1`) chỉ định lấy Parent 1 làm gốc so sánh, tức lấy trọn bộ thay đổi do nhánh được merge vào mang lại.
</details>

---

### E5
<details><summary>Xem đáp án</summary>

**Đáp án:**
- `git push --force` ép remote trỏ sang commit của bạn **bất chấp** trên remote có ai mới push thêm commit từ lúc bạn fetch lần cuối hay không → dễ xóa mất code đồng nghiệp.
- `git push --force-with-lease` kiểm tra trước: ref trên remote phải **khớp đúng với `origin/<branch>` đang lưu trong local của bạn**. Nếu có commit mới từ người khác trên remote chưa được bạn fetch, lệnh push bị từ chối ngay lập tức.
</details>

---

### E6
<details><summary>Xem đáp án</summary>

**Hướng giải:**
1. Chạy `git switch feature-wip` rồi `git reflog`. Tìm dòng có `wip: partial step 5` (ví dụ `7a98e5e`) và chạy `git reset --hard 7a98e5e` → đủ lại 5 commit WIP.
2. Chạy `git rebase -i main`, giữ `reword` (hoặc `pick`) ở commit 1, đổi 4 dòng kế từ `pick` thành `fixup` (hoặc thực hành bằng `git reset --soft main && git commit -m "feat: complete wip feature in one clean commit"` như trong [lab/solutions/verify-recovery-and-cherrypick.ps1](./lab/solutions/verify-recovery-and-cherrypick.ps1)).
</details>

---

### E7
<details><summary>Xem đáp án</summary>

**Hướng giải:**
1. Xem `git log hotfix-source --oneline` để lấy hash của `fix: set explicit socket timeout`.
2. `git switch main && git cherry-pick -x <hash>` → tạo commit mới trên `main` chỉ chứa `timeout.cfg` và message có `(cherry picked from commit <hash>)`.
3. Khi tự tạo conflict giữa `timeout=5000` và `timeout=9000`: sửa phần `<<<<<<< ... >>>>>>>` giữ giá trị mong muốn, chạy `git add timeout.cfg`, rồi `git cherry-pick --continue`.
</details>

---

### E8
<details><summary>Xem đáp án</summary>

**Đáp án:**
- `git reset --soft HEAD~1`: dời nhánh về `HEAD~1`, giữ nguyên cả Index lẫn Working Tree → `git status` báo `Changes to be committed: modified: A.txt` (sẵn sàng `git commit` lại ngay).
- `git reset --mixed HEAD~1`: dời nhánh và reset Index về khớp `HEAD~1`, giữ Working Tree → `git status` báo `Changes not staged for commit: modified: A.txt` (cần `git add` lại trước khi commit).
</details>

---

### E9
<details><summary>Xem đáp án</summary>

<details open><summary>Kế hoạch cứu lịch sử an toàn cho cả máy A và máy B</summary>

1. **Bước 1 — Khôi phục hàm `validateSignature()` bị mất trên máy A:**
   - Trên máy A, chạy `git reflog feature/payment-v2` tìm hash ngay trước dòng `rebase (start)` (hoặc kiểm tra `ORIG_HEAD`, Git luôn lưu vị trí trước khi rebase vào `.git/ORIG_HEAD`!).
   - Đặt nhánh đánh dấu ngay: `git branch backup/pre-rebase <pre-rebase-hash>`.
   - Nếu muốn giữ lịch sử đã rebase sạch lên `main`, tạo 1 commit vá lại hàm `validateSignature()` từ bản backup bằng `git checkout backup/pre-rebase -- path/to/File.java` rồi commit và `git push --force-with-lease` (hoặc quay hẳn về `<pre-rebase-hash>` nếu team thống nhất bỏ rebase để ghép code B bằng merge).

2. **Bước 2 — Ghép 2 commit mới của đồng nghiệp B mà KHÔNG kéo theo lịch sử cũ đã phân kỳ:**
   - Giả sử remote đã chốt sạch ở nhánh mới `origin/feature/payment-v2` (đã phục hồi `validateSignature()`).
   - Trên máy B: chạy `git fetch origin`.
   - Không chạy `git pull`! Xem `git log --oneline -n 5` để xác định hash commit **ngay trước** 2 commit của B (chính là hash trùng với `backup/pre-rebase` cũ, gọi là `<old-base>`).
   - Chạy:
     ```bash
     git rebase --onto origin/feature/payment-v2 <old-base> feature/payment-v2
     ```
     *(Hoặc đơn giản và dễ kiểm soát nhất nếu chỉ có 2 commit: `git switch -c feat-b origin/feature/payment-v2 && git cherry-pick <b1> <b2>`.)*
   - Khi đó chỉ đúng 2 commit của B được áp lên lịch sử mới, không kéo theo bất kỳ commit trùng lặp nào.

3. **Bước 3 — Guardrail phòng ngừa:**
   - Trên GitHub/GitLab: bật **Branch Protection** cho mọi nhánh dùng chung (`main`, `release/*`, `feature/payment-v2` nếu nhiều người chung) với quy tắc **cấm force-push** (`Allow force pushes = false`).
   - Trên local mọi thành viên: cấu hình alias hoặc thói quen luôn dùng `git push --force-with-lease`, và tạo nhánh backup nhẹ (`git branch backup/...`) trước khi chạy `rebase -i`.

</details>
</details>

---

## Self-check (từ README §9) — đáp án gợi ý
<details><summary>Xem</summary>

1. **`rebase` vs `cherry-pick` & hash mới:** Cả hai đều lấy diff của commit cũ áp lên một base mới để sinh commit object mới. Vì `commit` mới có `parent` mới (và `committer` timestamp mới) nên hash SHA-1 của commit luôn khác commit nguồn.
2. **Golden Rule của `rebase`:** Không rebase commit đã push lên nhánh có người khác dùng chung. Nếu làm vậy rồi force-push, người khác pull về sẽ đối mặt lịch sử phân kỳ (cùng nội dung nhưng khác hash) dẫn tới duplicate commits và conflict giả.
3. **`squash` / `fixup` / `reword`:** `squash` gộp thay đổi vào commit trên và giữ/cho sửa gộp message; `fixup` gộp thay đổi vào commit trên và vứt bỏ message của nó; `reword` giữ nguyên nội dung, chỉ sửa message.
4. **Khôi phục `reset --hard` & giới hạn:** Chạy `git reflog` tìm hash `HEAD@{n}` ngay trước `reset`, kiểm tra bằng `git show`, rồi `git reset --hard <hash>` (hoặc `switch -c`). `reflog` không cứu được thay đổi **chưa từng commit**: nếu đã `git add` thì tìm `blob` qua `git fsck --lost-found`; nếu chưa cả `git add` thì Git không còn dữ liệu trong `.git/objects/`.
5. **`--force` vs `--force-with-lease` & 3 chế độ `reset`:** `--force-with-lease` chỉ cho phép ghi đè nếu ref trên remote khớp với local đã fetch (tránh xóa nhầm commit người khác). `reset --soft` chỉ dời ref (giữ Index & Working Tree); `--mixed` dời ref + reset Index (giữ Working Tree); `--hard` dời ref + reset cả Index lẫn Working Tree.
</details>
