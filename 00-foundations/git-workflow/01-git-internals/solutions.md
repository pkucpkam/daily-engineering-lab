# GIT-01 · Đáp án — Git Internals: objects, refs, index

> ⚠️ **Chỉ mở sau khi đã tự làm.** Đọc đáp án mà chưa tự làm = mất 80% giá trị bài tập.
> Mỗi đáp án được bọc trong `<details>` để tránh lộ khi lướt.

---

### E1
<details><summary>Xem đáp án</summary>

**Đáp án:**
**Không tăng thêm `blob` nào.** Git chỉ tạo mới 1 `tree` object (cập nhật tên `a.txt` → `b.txt` trỏ vào **cùng hash `blob` cũ**) và 1 `commit` object trỏ tới `tree` mới đó.

**Giải thích:**
`blob` chỉ lưu nội dung file thuần, không lưu tên file. Tên file nằm ở `tree` object.
- **Đáp án sai phổ biến:** Nghĩ `git mv` tạo `blob` mới cho `b.txt` và đánh dấu xóa `a.txt` → Sai; content-addressable storage tự động deduplicate mọi file có cùng byte content.
</details>

---

### E2
<details><summary>Xem đáp án</summary>

**Đáp án:**
Tối thiểu **6 object mới**: 1 `blob` (`App.java`) + 4 `tree` (`java/`, `main/`, `src/`, và root tree) + 1 `commit`.

**Giải thích:**
Theo Merkle DAG, đổi 1 byte ở `App.java` làm đổi hash `blob` → làm đổi nội dung và hash của `tree` chứa nó (`java/`), kéo theo đổi hash các `tree` cha (`main/`, `src/`, root) và cuối cùng đổi dòng `tree <hash>` bên trong `commit` object.
</details>

---

### E3
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. **Gần như không tăng** (~2 KB tổng cộng: `50 × 41 bytes ≈ 2.050 bytes`). Mỗi branch chỉ là 1 file text chứa 40 ký tự SHA-1 + `\n` trong `.git/refs/heads/`, không copy source code hay object nào.
2. Khi chạy `git switch main`: `.git/HEAD` chứa `ref: refs/heads/main`. Khi chạy `git checkout HEAD~1`: `.git/HEAD` chuyển sang chứa **trực tiếp 40 ký tự hex SHA-1** của commit `HEAD~1` (trạng thái `detached HEAD`).
</details>

---

### E4
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. **Không.** `git commit` đóng gói thành `tree` đúng những gì đang nằm trong `.git/index` (lần sửa 1 lúc `git add`), không đọc trực tiếp file đang sửa trên Working Tree (lần sửa 2).
2. `git ls-files --stage` khi conflict hiển thị:
   - **Stage `1`:** common ancestor (base)
   - **Stage `2`:** phiên bản của nhánh hiện tại (`ours` / `HEAD`)
   - **Stage `3`:** phiên bản của nhánh đang merge vào (`theirs`)
</details>

---

### E5
<details><summary>Xem đáp án</summary>

**Đáp án:**
- `git tag v1.0` (lightweight tag) **không** tạo object trong `.git/objects/`; nó chỉ tạo file ref 41 byte `.git/refs/tags/v1.0` trỏ thẳng vào commit (`git cat-file -t v1.0` trả về `commit`).
- `git tag -a v1.1 -m "release 1.1"` (annotated tag) tạo thêm **1 `tag` object thật** trong `.git/objects/` chứa tagger, ngày, message và trỏ tới commit (`git cat-file -t v1.1` trả về `tag`), rồi `.git/refs/tags/v1.1` trỏ tới `tag` object đó.
</details>

---

### E6
<details><summary>Xem đáp án</summary>

**Hướng giải:**
Truyền `src/Main.java` làm đường dẫn cho `git update-index --add --cacheinfo 100644 <blobHash> src/Main.java`. Khi gọi `git write-tree`, Git tự động tách `src` thành một `tree` object con rồi đưa entry `040000 tree <subTreeHash> src` vào `tree` gốc!

**Script tham khảo đầy đủ:** xem [lab/solutions/create-subtree-commit.ps1](./lab/solutions/create-subtree-commit.ps1).
</details>

---

### E7
<details><summary>Xem đáp án</summary>

**Hướng giải:**
1. Trước khi rời `detached HEAD`: `Get-Content .git/HEAD` in trực tiếp 40 ký tự SHA-1 của commit `fix: urgent`.
2. Sau khi `git checkout main`, Git in dòng cảnh báo kèm hash của commit bị bỏ lại (hoặc chạy `git reflog` để lấy lại hash đó).
3. Chạy `git cat-file -p <hash>` thấy `parent` trỏ vào commit đầu của `main` và `tree` chứa phần sửa; chạy `git branch recovered <hash>` để gắn ref 41 byte vào lại commit đó.
</details>

---

### E8
<details><summary>Xem đáp án</summary>

**Hướng giải:**
Sau khi merge xung đột file `config.txt`:
- `git ls-files --stage config.txt` in ra 3 dòng có stage lần lượt `1`, `2`, `3`.
- Dùng `git cat-file -p <hash-stage-1>` → `timeout=10` (base); `<hash-stage-2>` → `timeout=20` (ours — nhánh A đang đứng); `<hash-stage-3>` → `timeout=30` (theirs — nhánh B merge vào).
</details>

---

### E9
<details><summary>Xem đáp án</summary>

<details open><summary>Phân tích sự cố & giải pháp làm sạch lịch sử</summary>

1. **Phản biện nhận định về branch cũ:**
   - Sai hoàn toàn. Mỗi branch trên đĩa chỉ là 1 file ref 41 byte (hoặc 1 dòng trong `.git/packed-refs`). Kể cả 1.000 branch cũ cũng chỉ tốn ~40 KB, không thể làm `.git/` nặng `480 MB`. Nguyên nhân nằm trong `.git/objects/` (thường là các `blob` lớn từng được commit trong quá khứ).

2. **Lệnh PowerShell tìm top 5 blob nặng nhất trong lịch sử:**
   ```powershell
   git rev-list --objects --all |
     git cat-file --batch-check="%(objecttype) %(objectsize) %(objectname) %(rest)" |
     Where-Object { $_ -like "blob *" } |
     Sort-Object { [long]($_ -split ' ')[1] } -Descending |
     Select-Object -First 5
   ```

3. **Cơ chế giảm dung lượng & rủi ro khi sửa lịch sử:**
   - Vì mọi `commit` và `tree` là Merkle DAG bất biến, xóa `backup-prod.zip` ở commit mới không chạm tới `blob` cũ `420 MB` đang được các `tree` lịch sử tham chiếu.
   - Muốn loại bỏ thật sự, phải dùng `git filter-repo --path backup-prod.zip --invert-paths` (hoặc `--strip-blobs-bigger-than 50M`) để **viết lại toàn bộ `tree` và `commit` từ commit đó trở về sau**, rồi chạy `git reflog expire` + `git gc --prune=now` (trên server Git cũng phải ép prune).
   - **Rủi ro:** Toàn bộ commit hash từ điểm cắt trở về `HEAD` đều thay đổi — đòi hỏi force-push và toàn bộ thành viên + CI cache phải **re-clone** (nếu ai đó push lại nhánh cũ chưa lọc, `420 MB` sẽ quay lại repo).

</details>
</details>

---

## Self-check (từ README §9) — đáp án gợi ý
<details><summary>Xem</summary>

1. **4 loại Git object:** `blob` (nội dung file thuần), `tree` (bảng mục lục thư mục: mode + type + hash + **tên file**), `commit` (`tree` gốc + `parent` + author/committer + message), `tag` (annotated tag có metadata trỏ tới commit). Tên file được lưu ở **`tree`**, timestamp của commit được lưu ở **`commit`**.
2. **Đổi 1 byte đổi toàn bộ hash:** Do cấu trúc Merkle DAG: nội dung file đổi → hash `blob` đổi → dòng tham chiếu trong `tree` đổi → hash `tree` các tầng cha đổi dây chuyền → dòng `tree <hash>` trong `commit` đổi → hash `commit` đổi → các commit con trỏ `parent <hash>` đổi theo.
3. **Branch & `HEAD` trên đĩa:** Branch là file text 41 byte `.git/refs/heads/<name>` chứa 40 hex chars + `\n` (hoặc gộp trong `.git/packed-refs`). `.git/HEAD` chứa `ref: refs/heads/<name>` khi ở trên branch, và chứa trực tiếp 40 hex chars hash khi ở `detached HEAD`.
4. **Bên dưới `git add` & conflict:** `git add` tạo `blob` trong `.git/objects/` và cập nhật đường dẫn + hash vào `.git/index` ở stage `0`. Khi conflict, Index chứa 3 entry cùng đường dẫn ở stage `1` (base), `2` (ours), `3` (theirs).
5. **Secret xóa ở commit sau vẫn còn:** Vì commit cũ vẫn lưu nguyên `tree` trỏ tới `blob` chứa secret trong `.git/objects/`; mọi người clone repo vẫn lấy về đủ object lịch sử đó.
</details>
