# GIT-04 · Đáp án — Branching strategy & Conventional Commits

> ⚠️ **Chỉ mở sau khi đã tự làm.** Đọc đáp án mà chưa tự làm = mất 80% giá trị bài tập.
> Mỗi đáp án được bọc trong `<details>` để tránh lộ khi lướt.

---

### E1
<details><summary>Xem đáp án</summary>

**Đáp án:**
- **GitFlow hợp lý hơn** khi sản phẩm là phần mềm đóng gói/cài đặt on-premise hoặc app mobile phải hỗ trợ **nhiều phiên bản cũ song song** ngoài thị trường và có chu kỳ phát hành dài theo lịch kiểm duyệt cứng.
- Với **backend / SaaS chạy CI/CD**, nhánh sống dài (long-lived branch) gây tách biệt tích hợp quá lâu → dẫn đến xung đột lớn khi hợp nhất, trì hoãn phát hiện bug tương tác giữa các module và làm chậm chu kỳ phát hành.
</details>

---

### E2
<details><summary>Xem đáp án</summary>

**Đáp án** *(bắt đầu từ `2.4.1`)*:
1. `fix(auth): ...` → tăng `PATCH` → **`2.4.2`**
2. `feat(order): ...` → tăng `MINOR` (reset `PATCH` về `0`) → **`2.5.0`**
3. `refactor(repo): ...` → không phải `feat`/`fix`/breaking → giữ nguyên **`2.5.0`**
4. `feat(api)!: ...` → có dấu `!` (breaking change) → tăng `MAJOR` (reset `MINOR`, `PATCH` về `0`) → **`3.0.0`**
</details>

---

### E3
<details><summary>Xem đáp án</summary>

**Đáp án:**
- **Vì sao kém:** (1) Lặp lại tên file mà `git diff` đã tự cho thấy (*What*); (2) Không theo chuẩn phân loại (`fix:`/`feat:`); (3) Không giải thích *tại sao* sửa và sửa lỗi gì của discount → vô dụng khi điều tra sự cố sau này.
- **Viết lại chuẩn:**
  ```text
  fix(pricing): apply VIP percentage discount before flat coupon

  Previously SAVE50 was subtracted before the 10% VIP rate, undercharging
  VIP orders by 5.00 per order compared to the finance specification.
  Refs: FIN-412
  ```
</details>

---

### E4
<details><summary>Xem đáp án</summary>

**Đáp án:**
Sử dụng **Feature Flag** (cấu hình bật/tắt runtime) kết hợp **Branch by Abstraction**:
- Tách công việc 2 tuần thành 4–6 PR nhỏ (< 300 dòng) merge liên tục vào `main` (ví dụ: PR 1 thêm DB column nullable, PR 2 thêm interface/service mới để flag = `false`, PR 3 thêm unit/integration test, PR 4 bật flag ở staging rồi production).
- Nhờ flag đang tắt (`false`), code đã được tích hợp và kiểm tra CI hằng ngày trên `main` mà hoàn toàn không ảnh hưởng traffic người dùng thật.
</details>

---

### E5
<details><summary>Xem đáp án</summary>

**Đáp án:**
- **PR `1.200 dòng` nguy hiểm hơn:** Reviewer vượt quá khả năng tập trung ngắn hạn, dễ bấm duyệt qua loa, bỏ sót bug logic/race condition, và nếu có sự cố phải `git revert` cả cục 1.200 dòng rất khó.
- **Tách refactoring khỏi feature:** Gửi **PR 1** thuần đổi tên 15 class (không đổi hành vi — reviewer duyệt rất nhanh qua IDE rename diff), đợi merge xong rồi gửi **PR 2** chỉ chứa logic mới (~100 dòng) để reviewer tập trung 100% vào nghiệp vụ.
</details>

---

### E6
<details><summary>Xem đáp án</summary>

**Kết quả kiểm tra thực tế từ `lab/hooks/commit-msg`:**
- `"update code"` → **Từ chối** (exit `1`, thiếu `<type>: `).
- `"WIP: test something"` → **Từ chối** (exit `1`, `WIP` chữ hoa không nằm trong danh sách allowed types).
- Tiêu đề dài 80 ký tự → **Từ chối** (exit `1`, regex `.{1,72}$` giới hạn phần summary tối đa 72 ký tự).
- `"feat(order): add idempotency key"` và `"fix!: drop legacy v1 endpoint"` → **Chấp nhận** (exit `0`).
</details>

---

### E7
<details><summary>Xem đáp án</summary>

**Hướng giải:**
Kiểm tra thêm điều kiện sau khi khớp `PATTERN`: nếu dòng đầu khớp `'^[a-z]+(\([a-z0-9._-]+\))?!:'` (tức có dấu `!`), bắt buộc kiểm tra `grep -Eq '^BREAKING CHANGE: .+' "$MSG_FILE"`; nếu không có thì in thông báo lỗi và `exit 1`.

**File hook tham khảo đầy đủ:** xem [lab/solutions/commit-msg-strict](./lab/solutions/commit-msg-strict).
</details>

---

### E8
<details><summary>Xem đáp án</summary>

**3 commit message mẫu đạt chuẩn:**
1. ```text
   fix(date): replace shared SimpleDateFormat with DateTimeFormatter

   SimpleDateFormat holds a mutable Calendar field internally and corrupted
   parsed dates under 8+ concurrent threads in end-of-day settlement jobs.
   DateTimeFormatter is immutable and thread-safe.
   ```
2. ```text
   fix(http): enforce 2000ms read timeout on partner HttpURLConnection

   Upstream tax service hung silently during peak traffic, exhausting all
   200 JBoss worker threads because readTimeout defaulted to 0 (infinite).
   ```
3. ```text
   feat(api)!: switch order report response from XML to JSON

   Drop legacy SOAP/XML marshalling to align with the new React portal
   and reduce payload size by ~60%.

   BREAKING CHANGE: /api/orders/report now returns application/json instead of application/xml.
   ```
</details>

---

### E9
<details><summary>Xem đáp án</summary>

<details open><summary>Bộ quy chuẩn Git thực dụng cho team 6 người (Legacy JBoss + Service Java 21)</summary>

1. **Chiến lược Branching:**
   - **Service Java 21 (CI/CD):** áp dụng **Trunk-Based Development**: 1 nhánh chính duy nhất `main` luôn ở trạng thái sẵn sàng deploy. Mọi nhánh công việc (`feat/*`, `fix/*`) chỉ sống tối đa **1–2 ngày** và phải mở PR nhỏ (`<= 300–400 LOC`).
   - **Repo Legacy JBoss AS 7.1.1:** vẫn dùng `main` làm trục tích hợp ngắn ngày, chỉ cắt nhánh `release/vX.Y` ngay sát ngày đóng gói WAR giao khách hàng. Khi có hotfix production, sửa trên `main` trước rồi `git cherry-pick -x <hash>` sang `release/vX.Y` (giữ được lịch sử vá lỗi rõ ràng).

2. **Xử lý task 2 tuần không giữ nhánh dài ngày:**
   - Áp dụng **Feature Flag** cho logic code và **Expand-Contract** cho thay đổi DB (PR 1: thêm cột nullable + dual-write nếu cần; PR 2: chạy logic mới sau flag tắt; PR 3: bật flag kiểm chứng; PR 4: dọn cột cũ sau khi ổn định).

3. **Conventional Commits, PR nhỏ & Chính sách Merge:**
   - Cấu hình hook chung trong repo ở thư mục `.githooks/commit-msg` (copy từ `lab/hooks/commit-msg`, không phụ thuộc Python/Node) và mỗi dev chạy 1 lần: `git config core.hooksPath .githooks`.
   - Chính sách merge đề xuất cho team đang làm quen: bật **Squash & Merge** trên PR kèm check **PR Title theo chuẩn Conventional Commits** — dev vẫn có thể commit WIP thoải mái trên nhánh cá nhân, nhưng khi vào `main` mỗi PR trở thành đúng 1 commit sạch có `feat:/fix:` và mô tả rõ *Tại sao* để tự sinh release notes và phục vụ `git bisect` sau này.

</details>
</details>

---

## Self-check (từ README §9) — đáp án gợi ý
<details><summary>Xem</summary>

1. **GitFlow vs GitHub Flow vs Trunk-Based:** GitFlow duy trì nhiều nhánh dài ngày (`main`, `develop`, `release/*`, `hotfix/*`); GitHub Flow chỉ có `main` + feature PR ngắn/trung bình; Trunk-Based chỉ có `main` (trunk) với nhánh phụ sống rất ngắn (< 1–2 ngày) hợp nhất liên tục.
2. **Vì sao TBD hợp CI/CD & cách làm việc lớn:** TBD ép mọi dev tích hợp vào nhánh chung mỗi ngày, tránh merge hell và đảm bảo test CI chạy trên mã nguồn tích hợp thật. Tính năng lớn được tách thành nhiều PR nhỏ che sau **Feature Flag** / **Branch by Abstraction** hoặc **Expand-Contract** cho DB.
3. **Conventional Commits → SemVer:** `fix:` tăng `PATCH`; `feat:` tăng `MINOR`; `!` hoặc footer `BREAKING CHANGE:` tăng `MAJOR`; các type khác (`chore`, `refactor`, `docs`, `ci`) không tự tăng version.
4. **Diff (What) vs Message (Why) khi điều tra sự cố:** `git diff` đã chỉ rõ dòng nào đổi; khi dùng `git blame`/`git bisect` giữa sự cố, phần *Why* trong body commit cho biết bối cảnh bug cũ, lý do chọn ngưỡng/giải pháp và ticket liên quan — giúp quyết định nhanh có nên revert hay chỉnh thông số mà không phá ngược nghiệp vụ khác.
5. **2 kỹ thuật giữ PR nhỏ:** (1) Tách PR thuần refactor khỏi PR tính năng mới; (2) Chia tính năng lớn thành các lát cắt đứng/ngang dùng interface + Feature Flag (hoặc Expand-Contract với DB).
</details>
