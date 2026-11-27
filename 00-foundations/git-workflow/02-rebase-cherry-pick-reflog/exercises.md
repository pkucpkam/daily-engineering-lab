# GIT-02 · Bài tập — Rebase, Cherry-pick, Reflog

> Làm theo thứ tự. **Tự làm trước, chỉ mở hint khi đã thử ≥ 15 phút.** Đáp án ở [solutions.md](./solutions.md) — chỉ mở sau khi đã có câu trả lời của riêng bạn.
> Ghi thời gian làm từng bài vào `my-notes.md` để theo dõi tiến bộ.

| Cấp | Mục đích | Số lượng | Thời gian gợi ý |
| :--- | :--- | :---: | :---: |
| 🟢 L1 — Recall | Nhớ & hiểu khái niệm | 5 | 20' |
| 🟡 L2 — Apply | Thực hành lệnh cứu dữ liệu, rebase & cherry-pick | 3 | 60' |
| 🔴 L3 — Deep | Giải quyết sự cố lịch sử phân kỳ | 1 | 60' |

---

## 🟢 L1 — Recall

### E1. Vì sao không được rebase branch đã push chung?
Bạn và đồng nghiệp A cùng làm việc trên nhánh `feat/checkout` (đã push lên `origin`). Đồng nghiệp A vừa pull về và tạo thêm 2 commit mới ở local. Bạn chạy `git rebase main` ở máy mình rồi `git push --force`. Chuyện gì xảy ra khi đồng nghiệp A chạy `git pull` sau đó?

---

### E2. `squash` vs `fixup` vs `reword`
Khi mở trình soạn thảo của `git rebase -i HEAD~4`:
1. Bạn có commit thứ 2 là `"fix missing import"` và không muốn giữ dòng message đó trong lịch sử cuối. Chọn `squash` hay `fixup`?
2. Bạn muốn sửa lỗi chính tả trong commit message của commit đầu tiên mà giữ nguyên code. Chọn từ khóa nào?

---

### E3. `reflog` cứu được gì và KHÔNG cứu được gì?
Với 3 trường hợp dưới đây, trường hợp nào **cứu được bằng `git reflog`**, trường hợp nào **không cứu được bằng `reflog`** (và nếu còn cách cứu qua `git fsck` thì nói rõ):
1. Đã `git commit`, sau đó chạy `git reset --hard HEAD~1`.
2. Đã sửa file và `git add`, chưa `git commit`, rồi chạy `git reset --hard`.
3. Đã sửa file nhưng chưa từng `git add` hay `git commit`, rồi chạy `git reset --hard`.

---

### E4. `git cherry-pick` lên merge commit
Nếu bạn chạy `git cherry-pick <merge-commit-hash>` (commit được tạo bởi `git merge --no-ff`), Git báo lỗi `commit ... is a merge but no -m option was given`. Vì sao Git không tự đoán được phải lấy diff nào, và tham số `-m 1` nghĩa là gì?

---

### E5. `--force` vs `--force-with-lease`
Khác biệt an toàn giữa `git push --force` và `git push --force-with-lease` khi đẩy một nhánh PR cá nhân sau khi `rebase` là gì?

---

## 🟡 L2 — Apply

### E6. Cứu 2 commit bị mất sau `git reset --hard HEAD~2` và squash 5 WIP commit
**Bối cảnh:** Chạy script khởi tạo [lab/](./lab/README.md) (`setup-lab.ps1` hoặc `setup-lab.sh`) tạo repo tạm có nhánh `feature-wip` đã bị lỡ tay `git reset --hard HEAD~2` mất 2 commit cuối.

**Yêu cầu:**
1. Chuyển sang `feature-wip`, chạy `git reflog`, tìm đúng hash của commit `wip: partial step 5` và khôi phục `feature-wip` về đủ 5 commit WIP.
2. Dùng `git rebase -i main` (gõ tay trong editor: dòng 1 giữ `pick`, 4 dòng sau đổi thành `fixup` hoặc `squash`, rồi dùng `reword` cho commit đầu) để gộp 5 commit WIP thành **1 commit duy nhất** có message `feat: complete wip feature in one clean commit`.

**Tiêu chí hoàn thành:**
- [ ] `git log --oneline main..feature-wip` chỉ còn đúng 1 commit và file `wip.txt` giữ đủ cả 5 dòng (`wip step 1` .. `wip step 5`).

<details><summary>💡 Hint</summary>

- Sau khi `git switch feature-wip`, xem `git reflog` dòng ngay trước `reset: moving to HEAD~2`.
- Chạy `git reset --hard <hash-step-5>`.
- Chạy `git rebase -i main`: giữ `pick` ở commit 1 (hoặc `reword` để đổi message), sửa `pick` → `fixup` ở 4 commit sau.
</details>

---

### E7. Cherry-pick hotfix và xử lý conflict có kiểm soát
**Bối cảnh:** Tiếp tục trên repo lab của E6, nhánh `hotfix-source` có 2 commit: commit đầu là `feat: unfinished experiment` (không được đưa lên `main`), commit sau là `fix: set explicit socket timeout`.

**Yêu cầu:**
1. Chuyển sang `main`, dùng `git cherry-pick -x <hash>` chỉ lấy đúng commit `fix: set explicit socket timeout` sang `main`.
2. Kiểm tra `git show HEAD` xác nhận có `timeout.cfg`, không có `exp.txt`, và commit message có dòng `(cherry picked from commit ...)`.
3. Tự tạo thêm 1 tình huống conflict nhẹ (sửa `timeout=5000` trên `main` và `timeout=9000` trên 1 nhánh mới), cherry-pick để gặp conflict, giải quyết conflict và hoàn tất bằng `git cherry-pick --continue`.

**Tiêu chí hoàn thành:**
- [ ] `git ls-tree -r main` không chứa `exp.txt`, có `timeout.cfg`.
- [ ] `git log -n 2 main` có thông tin `(cherry picked from commit ...)`.

<details><summary>💡 Hint</summary>

Lấy hash của commit hotfix bằng `git log hotfix-source --oneline`. Khi cherry-pick vào `main`, dùng `git cherry-pick -x <hash>`.
</details>

---

### E8. Thực hành `git reset --soft` vs `--mixed` vs `--hard`
**Bối cảnh:** Trong một repo tạm bất kỳ có 2 commit, tạo commit thứ 3 sửa file `A.txt` thành `"draft"`.

**Yêu cầu:**
1. Chạy `git reset --soft HEAD~1` → chạy `git status` và ghi nhận file `A.txt` đang nằm ở vùng nào (Working Tree hay Staging Index?).
2. Tạo lại commit thứ 3, rồi chạy `git reset --mixed HEAD~1` → chạy `git status` ghi nhận sự khác biệt với `--soft`.

**Tiêu chí hoàn thành:**
- [ ] Phân biệt chính xác đầu ra `git status` của `--soft` (Changes to be committed) vs `--mixed` (Changes not staged for commit).

<details><summary>💡 Hint</summary>

`--soft` giữ nguyên Index (nên sẵn sàng `git commit` lại ngay); `--mixed` xóa Index về khớp với `HEAD~1` nên thay đổi rơi về Working Tree chưa staged.
</details>

---

## 🔴 L3 — Deep

### E9. Giải cứu đồng nghiệp sau một pha `rebase` + `force push` nhầm lên nhánh chung
**Tình huống:**
> Nhóm có nhánh dùng chung `feature/payment-v2`. Sáng nay một thành viên gõ nhầm ở máy mình:
> 1. Trên máy bạn ấy: rebase `feature/payment-v2` lên `main` nhưng trong lúc giải conflict lỡ tay **xóa mất hàm `validateSignature()`** ở một file, rồi bấm `git push --force origin feature/payment-v2`.
> 2. Ở máy đồng nghiệp B (đang mở sẵn từ hôm qua, chưa `git pull`): đồng nghiệp B vừa commit thêm 2 commit mới lên đầu `feature/payment-v2` cũ (vẫn còn hàm `validateSignature()` nguyên vẹn trước khi rebase).
> 3. Giờ trên remote `origin/feature/payment-v2` vừa thiếu hàm `validateSignature()`, vừa không có 2 commit mới của B.

**Yêu cầu:**
1. Lập phương án khôi phục **không mất bất kỳ dòng code nào** của cả hai người (với giả định máy thành viên A vừa chạy `rebase` xong).
2. Chỉ rõ: tại máy A dùng lệnh gì để kiểm tra bản trước khi rebase (bước tìm lại commit trước lúc giải conflict sai); tại máy B dùng lệnh gì (`git rebase --onto` hay `git cherry-pick`) để ghép 2 commit mới của B lên lịch sử sạch mà không kéo theo cả nhánh đã phân kỳ?
3. Đề xuất 2 cấu hình bảo vệ trên Git server (GitHub/GitLab/Bitbucket) và local Git để lỗi này không lặp lại.

**Tiêu chí hoàn thành:**
- [ ] Đưa ra được đường đi phục hồi cho cả máy A (`reflog` trước khi rebase) và máy B (`rebase --onto` hoặc `cherry-pick` đúng range 2 commit của B).
- [ ] Nêu được quy tắc Branch Protection (chặn force-push trên nhánh dùng chung) + `--force-with-lease`.

<details><summary>💡 Hint</summary>

- Trên máy A: `git reflog feature/payment-v2` tìm lại hash gốc ngay trước dòng `rebase (start)` — đó chính là điểm đồng bộ với nhánh cũ của B!
- Hoặc giữ lịch sử mới đã rebase lên `main`: phục hồi `validateSignature()` từ `ORIG_HEAD` / `reflog` trên máy A, sau đó trên máy B chạy `git rebase --onto origin/feature/payment-v2 <hash-trước-2-commit-của-B> feature/payment-v2` để chỉ bứng 2 commit của B sang!
</details>
