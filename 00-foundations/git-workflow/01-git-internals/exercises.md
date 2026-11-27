# GIT-01 · Bài tập — Git Internals: objects, refs, index

> Làm theo thứ tự. **Tự làm trước, chỉ mở hint khi đã thử ≥ 15 phút.** Đáp án ở [solutions.md](./solutions.md) — chỉ mở sau khi đã có câu trả lời của riêng bạn.
> Ghi thời gian làm từng bài vào `my-notes.md` để theo dõi tiến bộ.

| Cấp | Mục đích | Số lượng | Thời gian gợi ý |
| :--- | :--- | :---: | :---: |
| 🟢 L1 — Recall | Nhớ & hiểu khái niệm | 5 | 20' |
| 🟡 L2 — Apply | Thực hành lệnh Git plumbing & kiểm tra đĩa | 3 | 60' |
| 🔴 L3 — Deep | Điều tra sự cố & cấu trúc nội tại | 1 | 60' |

---

## 🟢 L1 — Recall

### E1. Đổi tên file có tạo ra `blob` mới không?
Bạn có file `a.txt` chứa `"hello\n"` đã commit. Sau đó bạn chạy `git mv a.txt b.txt` và commit tiếp. Số lượng `blob` trong `.git/objects/` có tăng lên không? Đối tượng nào thực sự thay đổi và tạo hash mới?

---

### E2. Tại sao đổi 1 byte làm đổi hash commit?
Nếu bạn sửa 1 chữ cái trong `src/main/java/App.java` (nằm sâu 4 tầng thư mục) rồi commit, có tối thiểu bao nhiêu Git object mới được sinh ra trong `.git/objects/`? Giải thích theo cấu trúc Merkle DAG.

---

### E3. Branch trên đĩa & `detached HEAD`
1. Tạo 50 branch mới bằng `git branch exp-1` ... `git branch exp-50` có làm tăng dung lượng repo đáng kể không? Giải thích bằng cấu trúc file trên đĩa.
2. Nội dung file `.git/HEAD` khác nhau thế nào khi bạn chạy `git switch main` vs `git checkout HEAD~1`?

---

### E4. Index là snapshot hay diff, và 3 stage khi conflict?
1. Bạn sửa `app.properties` lần 1 → `git add app.properties` → sửa thêm dòng mới lần 2 (chưa `git add`) → chạy `git commit -m "update"`. Dòng mới lần 2 có nằm trong commit đó không?
2. Khi `git merge` bị conflict ở file `Order.java`, chạy `git ls-files --stage Order.java` sẽ thấy các stage số `1`, `2`, `3` — mỗi stage đại diện cho phiên bản nào?

---

### E5. Lightweight tag vs Annotated tag
Chạy `git tag v1.0` và `git tag -a v1.1 -m "release 1.1"`: cái nào tạo thêm object trong `.git/objects/`, cái nào chỉ tạo một file ref 41 byte trong `.git/refs/tags/`? Kiểm tra bằng `git cat-file -t`.

---

## 🟡 L2 — Apply

### E6. Dựng tay 1 commit có thư mục con (`src/Main.java`) bằng plumbing commands
**Bối cảnh:** Trong [lab/](./lab/README.md) ta mới dựng commit chỉ có 1 file ở gốc (`hello.txt`). Thực tế commit luôn có thư mục lồng nhau.

**Yêu cầu:**
Trong một thư mục repo tạm mới (`$env:TEMP\git-01-e6`), **không dùng `git add` hay `git commit`**, hãy dùng thuần plumbing commands (`hash-object`, `update-index`, `write-tree`, `commit-tree`, `update-ref`) để tạo 1 commit chứa file `src/Main.java` (nội dung bất kỳ).

**Tiêu chí hoàn thành:**
- [ ] `git ls-tree -r HEAD` in ra `100644 blob <hash>	src/Main.java`.
- [ ] `git cat-file -t <tree-của-src>` trả về `tree` (tức `write-tree` tự động sinh 1 `tree` lồng bên trong `tree` gốc!).
- [ ] Đối chiếu script mẫu ở `lab/solutions/create-subtree-commit.ps1`.

<details><summary>💡 Hint</summary>

`git update-index --add --cacheinfo 100644 <blobHash> src/Main.java` chấp nhận đường dẫn có `/`, sau đó `git write-tree` sẽ tự tạo cả `tree` cho thư mục `src` lẫn `tree` gốc!
</details>

---

### E7. Giải cứu commit bị bỏ rơi trong `detached HEAD`
**Bối cảnh:** Trong một repo tạm, bạn cố ý tạo 2 commit trên `main`, rồi chạy `git checkout HEAD~1` (vào `detached HEAD`), sửa file và tạo 1 commit `fix: urgent`, rồi chạy `git checkout main` (bỏ lại commit đó phía sau).

**Yêu cầu:**
1. Dùng `git cat-file -p` và `Get-Content .git/HEAD` (hoặc `cat .git/HEAD`) để quan sát trạng thái `detached HEAD` trước khi rời đi.
2. Sau khi quay về `main` (lúc `git log` không còn thấy `fix: urgent`), tìm lại hash của commit `fix: urgent` (qua `git reflog` hoặc cảnh báo trước đó) và kiểm chứng bằng `git cat-file -p <hash>` rằng dữ liệu không hề bị xóa, rồi gắn một branch `recovered` vào đó.

**Tiêu chí hoàn thành:**
- [ ] Branch `recovered` chứa commit `fix: urgent` với parent là commit đầu tiên của `main`.

<details><summary>💡 Hint</summary>

`git branch recovered <lost-commit-hash>` tạo lại file 41 byte `.git/refs/heads/recovered` trỏ thẳng tới commit đó.
</details>

---

### E8. Soi stage `1 / 2 / 3` trong Index khi Merge Conflict
**Bối cảnh:** Tạo 1 repo tạm với `config.txt` chứa `timeout=10`, chia 2 nhánh: nhánh A sửa thành `timeout=20`, nhánh B sửa thành `timeout=30`.

**Yêu cầu:**
Merge nhánh B vào nhánh A để gây conflict, sau đó **trước khi sửa file**:
1. Chạy `git ls-files --stage config.txt` và in nội dung của cả 3 blob hash bằng `git cat-file -p <hash>`.
2. Chỉ ra hash nào tương ứng base (`10`), ours (`20`), theirs (`30`).

**Tiêu chí hoàn thành:**
- [ ] Ghi đúng mapping stage `1` / `2` / `3` với giá trị `10` / `20` / `30`.

<details><summary>💡 Hint</summary>

Stage `1` = common ancestor (base), Stage `2` = nhánh hiện tại (`ours`), Stage `3` = nhánh đang merge vào (`theirs`).
</details>

---

## 🔴 L3 — Deep

### E9. Điều tra repo phình dung lượng bất thường (`.git/` nặng bất thường sau khi đã xóa file)
**Tình huống:**
> Repo dự án legacy Java của công ty có thư mục làm việc chỉ nặng `12 MB`, nhưng thư mục `.git/` nặng tới `480 MB` khiến mỗi lần CI clone mất gần 2 phút. Khi kiểm tra `HEAD` hiện tại (`git ls-tree -r HEAD`) hoàn toàn không thấy file nào lớn hơn `200 KB`. Một đồng nghiệp bảo *"chắc tại mấy trăm cái branch cũ chưa xóa nên `.git/` mới phình to vậy"*.

**Yêu cầu:**
1. Phản biện nhận định của đồng nghiệp dựa trên cơ chế lưu trữ **branch refs** trên đĩa.
2. Viết chính xác chuỗi lệnh Git plumbing (`git rev-list`, `git cat-file --batch-check`) để tìm **top 5 object nặng nhất** đang nằm ẩn trong lịch sử `.git/objects/` (chạy được trên PowerShell Windows không cần Python).
3. Nếu phát hiện một file `backup-prod.zip` `420 MB` ở commit cách đây 8 tháng, giải thích điều gì phải thay đổi trong DAG để dung lượng `.git/` giảm thật sự, và rủi ro gì khi làm điều đó trên repo cả team đang dùng chung?

**Tiêu chí hoàn thành:**
- [ ] Giải thích đúng 500 branch ≈ ~20 KB (không thể gây 480 MB).
- [ ] Chuỗi lệnh tìm top blob lớn nhất không phụ thuộc Python/Linux-only tool, chạy được trên Windows PowerShell.

<details><summary>💡 Hint</summary>

```powershell
git rev-list --objects --all | git cat-file --batch-check="%(objecttype) %(objectsize) %(objectname) %(rest)" | Where-Object { $_ -like "blob *" } | Sort-Object { [long]($_ -split ' ')[1] } -Descending | Select-Object -First 5
```
Muốn xóa `420 MB` thật sự khỏi lịch sử, phải rewrite lại toàn bộ `tree` và `commit` phía sau commit chứa file đó (ví dụ bằng `git filter-repo`), làm đổi toàn bộ commit hash từ điểm đó trở về `HEAD` → mọi thành viên phải re-clone hoặc reset theo nhánh mới.
</details>
