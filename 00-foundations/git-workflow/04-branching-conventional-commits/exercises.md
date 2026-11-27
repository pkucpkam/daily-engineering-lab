# GIT-04 · Bài tập — Branching strategy & Conventional Commits

> Làm theo thứ tự. **Tự làm trước, chỉ mở hint khi đã thử ≥ 15 phút.** Đáp án ở [solutions.md](./solutions.md) — chỉ mở sau khi đã có câu trả lời của riêng bạn.
> Ghi thời gian làm từng bài vào `my-notes.md` để theo dõi tiến bộ.

| Cấp | Mục đích | Số lượng | Thời gian gợi ý |
| :--- | :--- | :---: | :---: |
| 🟢 L1 — Recall | Nhớ & hiểu khái niệm | 5 | 20' |
| 🟡 L2 — Apply | Viết & kiểm thử `commit-msg` hook, viết message chuẩn | 3 | 60' |
| 🔴 L3 — Deep | Thiết kế quy trình branching & PR slicing cho team | 1 | 60' |

---

## 🟢 L1 — Recall

### E1. So sánh nhanh 3 mô hình branching
Khi nào **GitFlow** hợp lý hơn **Trunk-Based Development**, và vì sao với dịch vụ backend chạy CI/CD trên cloud/container người ta tránh nhánh sống dài?

---

### E2. Ánh xạ Conventional Commits sang SemVer
Phiên bản hiện tại của thư viện nội bộ là **`2.4.1`**. Xác định số phiên bản mới sau khi hợp nhất các commit sau:
1. `fix(auth): handle expired refresh token without NPE`
2. `feat(order): support partial refund endpoint`
3. `refactor(repo): extract JdbcTemplate helper`
4. `feat(api)!: rename field totalAmount to totalMinorUnits`

---

### E3. Commit message tốt vs commit message vô ích
Giải thích vì sao commit message sau đây là **kém**, và viết lại thành 1 message chuẩn (subject + body giả định bối cảnh):
`update PricingService.java - fix discount`

---

### E4. Feature Flag và Trunk-Based
Một tính năng mới cần 2 tuần mới hoàn chỉnh (phải sửa cả DB schema, service và API). Theo Trunk-Based Development, làm sao merge code mỗi ngày vào `main` mà không làm hỏng hệ thống đang chạy production?

---

### E5. Bốn quy tắc kích thước PR nhỏ
Vì sao PR `1.200 dòng` nguy hiểm hơn 4 PR `300 dòng`, và khi bạn vừa lỡ sửa tên 15 class vừa thêm 1 logic mới thì nên tách PR thế nào?

---

## 🟡 L2 — Apply

### E6. Cài và kiểm thử `commit-msg` hook trong repo tạm
**Bối cảnh:** Chạy script trong [lab/](./lab/README.md) (`setup-hook-lab.ps1` hoặc `.sh`).

**Yêu cầu:**
1. Thử commit với các message sau và ghi lại kết quả chấp nhận / từ chối của hook:
   - `"update code"`
   - `"feat(order): add idempotency key"`
   - `"fix!: drop legacy v1 endpoint"`
   - `"WIP: test something"`
2. Kiểm chứng rằng nếu gõ tiêu đề dài **80 ký tự** (> 72), hook hiện tại từ chối với exit code `1`.

**Tiêu chí hoàn thành:**
- [ ] Hook từ chối `"update code"`, `"WIP: test something"` và tiêu đề > 72 ký tự; chấp nhận `"feat(order): ..."` và `"fix!: ..."`.

<details><summary>💡 Hint</summary>

Regex trong `lab/hooks/commit-msg` dùng `.{1,72}$` sau dấu `: `, nên chuỗi summary dài hơn 72 ký tự hoặc thiếu `<type>: ` đều bị từ chối.
</details>

---

### E7. Nâng cấp `commit-msg` hook bắt buộc có `BREAKING CHANGE:` giải thích nếu dùng `!`
**Bối cảnh:** Trong thực tế nhiều dev gõ `feat(api)!: change payload` nhưng quên ghi footer `BREAKING CHANGE: ...` mô tả rõ field nào bị đổi để người dùng API biết cách nâng cấp.

**Yêu cầu:**
Chỉnh bản copy của hook (hoặc viết mới dựa trên `lab/hooks/commit-msg`) bổ sung quy tắc:
- Nếu dòng đầu có `!:` (breaking change), toàn bộ file `$MSG_FILE` **bắt buộc phải chứa** dòng bắt đầu bằng `BREAKING CHANGE: ` (có nội dung sau dấu hai chấm).
- Kiểm chứng bằng 2 lệnh commit thử: `feat(api)!: change response` (không body → phải bị **từ chối**) vs có footer `BREAKING CHANGE: rename id to orderId` (phải được **chấp nhận**).

**Tiêu chí hoàn thành:**
- [ ] Đối chiếu với lời giải mẫu tại `lab/solutions/commit-msg-strict` (chạy hoàn toàn bằng POSIX `sh` + `grep`).

<details><summary>💡 Hint</summary>

Kiểm tra nếu `printf '%s\n' "$FIRST_LINE" | grep -Eq '^[a-z]+(\([a-z0-9._-]+\))?!:'` thì kiểm tiếp `grep -Eq '^BREAKING CHANGE: .+' "$MSG_FILE"`.
</details>

---

### E8. Viết lại 3 commit message chuẩn cho sự cố thật trong bài trước
**Bối cảnh:** Lấy 3 thay đổi từ các bài đã học (`JVM-01`, `READ-02`, `GIT-02`):
1. Đổi `SimpleDateFormat` static sang `DateTimeFormatter` để hết lỗi đa luồng.
2. Đặt `setReadTimeout(2000)` cho `HttpURLConnection` vì đối tác từng treo socket làm nghẽn JBoss thread pool.
3. Nâng cấp DTO API đổi kiểu trả về từ XML sang JSON (breaking change).

**Yêu cầu:**
Viết đủ 3 commit message theo chuẩn Conventional Commits (subject `<= 72` ký tự + body trả lời *Tại sao* + footer khi cần).

**Tiêu chí hoàn thành:**
- [ ] Cả 3 message đều vượt qua regex của `commit-msg` hook (và message 3 vượt qua cả hook nâng cao ở E7).

<details><summary>💡 Hint</summary>

Nhớ: dòng 1 là `<type>(<scope>)[!]: <summary>`, dòng 2 để trống, dòng 3 trở đi giải thích bối cảnh/nguyên nhân gốc và lý do chọn giải pháp.
</details>

---

## 🔴 L3 — Deep

### E9. Thiết kế quy trình Branching & Release cho team chuyển từ Legacy JBoss sang CI/CD
**Tình huống:**
> Team của bạn (6 Java backend dev) đang bảo trì hệ thống legacy **Java 7 / JBoss AS 7.1.1** đồng thời xây dựng service **Java 21 / Spring Boot 3** mới (dự kiến deploy Docker lên cloud mỗi tuần 2–3 lần).
> Hiện tại cả team đang dùng kiểu tự phát:
> - Ai cũng mở nhánh `dev_<tên>` sống 2–3 tuần, sửa hàng chục file rồi cuối tuần thứ 3 mới ghép vào nhánh `test` chung → tuần nào cũng mất cả ngày thứ Sáu để giải conflict.
> - Commit message toàn kiểu `"sua lai"`, `"fix"`, `"ok"`; không biết phiên bản nào đang chạy ở prod có những thay đổi gì.

**Yêu cầu:**
Đề xuất bộ quy chuẩn Git mới gọn nhẹ nhưng áp dụng được ngay cho **cả 2 repo** (legacy JBoss và service mới):
1. Chọn chiến lược branching cụ thể (tên nhánh, tuổi thọ tối đa của nhánh phụ, quy tắc tạo nhánh release nếu bản legacy vẫn phải giữ bản vá cho khách hàng cũ).
2. Cách xử lý tính năng lớn mất 2 tuần trên repo Java 21 mà **không giữ branch quá 2 ngày** (nêu rõ ví dụ triển khai Feature Flag / Expand-Contract khi đụng DB).
3. Quy tắc Conventional Commits + cấu hình `core.hooksPath` + nguyên tắc PR nhỏ + chính sách merge (`Squash & Merge` vs `Rebase` vs `Merge`) để team 6 người áp dụng được ngay tuần tới mà không bị quá tải thủ tục.

**Tiêu chí hoàn thành:**
- [ ] Có chính sách xử lý nhánh cụ thể cho cả repo deploy liên tục (TBD) lẫn nhánh hotfix bản legacy (`cherry-pick -x`).
- [ ] Giải thích được cách tách PR khi đổi DB schema (tránh breaking deploy giữa lúc rolling update).

<details><summary>💡 Hint</summary>

- Khi đụng DB ở service deploy liên tục: áp dụng **Expand-Contract** (`CICD-10`): PR 1 thêm cột mới (nullable) + code đọc/ghi tương thích cả cũ lẫn mới; PR 2 backfill dữ liệu; PR 3 xóa cột cũ. Như vậy mọi PR đều nhỏ và merge được ngay vào `main` mà không vỡ bản đang chạy!
- Với team 6 người mới tập Conventional Commits: cho phép dev commit thoải mái WIP ở local, nhưng bật **Squash & Merge** trên PR với **PR Title** bắt buộc chuẩn Conventional Commits (hoặc cài `core.hooksPath` nhẹ nhàng trên local).
</details>
