# Content Guide — Quy chuẩn soạn nội dung

> Tài liệu này dành cho **người/agent soạn giáo trình** trong repo. Đọc hết trước khi viết bất kỳ bài nào.
> Danh mục toàn bộ bài: [CURRICULUM.md](../CURRICULUM.md) · Lộ trình: [ROADMAP.md](../ROADMAP.md)

---

## 1. Phân vai: ai viết gì

| Loại nội dung | Ai viết | Ghi chú |
| :--- | :--- | :--- |
| Lý thuyết (`README.md` của bài), bài tập (`exercises.md`), đáp án (`solutions.md`), lab/demo (`lab/`), pattern card, system design mẫu, incident lab | **Agent soạn giáo trình** | Theo template trong `docs/templates/` |
| `my-notes.md` trong mỗi bài, `weekly-log/`, bug-case/post-mortem **thật**, cột "Hiện tại" của Skill Matrix | **Người học** | Agent **không bao giờ** tạo hay sửa các file này |
| Trạng thái `review` → `done` | **Người học** | Agent chỉ được chuyển `todo` → `draft` |

> Lý do: ROADMAP §2.3 — "viết trước, hỏi sau", "không để AI viết hộ note". Giáo trình có thể do AI soạn, nhưng **hiểu biết** phải do người học tự viết ra.

---

## 2. Các loại nội dung & cấu trúc file

### 2.1 Lesson (bài học) — thư mục `NN-slug/`
```
<domain>/NN-slug/
├── README.md        # Brief (giữ lại) + lý thuyết — docs/templates/lesson-template.md
├── exercises.md     # docs/templates/exercises-template.md
├── solutions.md     # docs/templates/solutions-template.md
├── lab/             # docs/templates/lab-template.md — code chạy được
│   └── README.md
└── my-notes.md      # ❌ agent không tạo — người học tự viết
```

### 2.2 Pattern card — file `patterns/<slug>.md`
Template: `docs/templates/pattern-template.md`. Ngắn (1–3 trang), link sang lesson sâu nếu có.

### 2.3 Concept decision — file `concept-decisions/<slug>.md`
Template: `01-software-engineering/concept-decisions/templates/concept-decision-template.md`. **Bắt buộc có bảng so sánh và "Final Decision Rule"**; ưu tiên số đo từ lab liên quan.

### 2.4 System design — file `system-design/<slug>.md`
Template: `01-software-engineering/system-design/templates/system-design-template.md`. **Bắt buộc có back-of-envelope estimation với số cụ thể**, ≥ 1 sơ đồ Mermaid kiến trúc, mục trade-off. Có thể kèm thư mục `system-design/<slug>-prototype/` nếu brief có ý tưởng prototype.

### 2.5 Incident lab — thư mục `03-production-troubleshooting/incident-labs/NN-slug/`
Template: `docs/templates/incident-lab-template.md`.
```
NN-slug/
├── BRIEF.md      # brief cho người soạn (có sẵn, chứa hướng root cause — giữ nguyên)
├── README.md     # Đề bài: ticket, tín hiệu, cách tái hiện, nhiệm vụ — KHÔNG lộ root cause
├── hints.md      # gợi ý 3 tầng
├── solution.md   # root cause, causal chain, fix, prevention (trong <details>)
├── evidence/     # log, thread dump mẫu, ảnh/mô tả dashboard
└── lab/          # tái hiện ổn định + bản đã sửa (lab/fixed/ hoặc flag)
```

---

## 3. Frontmatter & trạng thái

Mỗi file nội dung bắt đầu bằng YAML frontmatter (đã có sẵn trong brief). Khi soạn xong, cập nhật:
```yaml
status: draft
last_updated: 2026-11-27
```

| Status | Ý nghĩa | Ai đặt |
| :--- | :--- | :--- |
| `todo` 🔲 | Mới có brief | (khởi tạo) |
| `draft` ✍️ | Agent đã soạn đủ gói đầu ra, đã tự kiểm tra | Agent |
| `review` 👀 | Người học đang đọc / làm bài | Người học |
| `done` ✅ | Người học qua explain-back + làm xong bài tập L2 | Người học |

Sau khi soạn, **cập nhật cột Status trong [CURRICULUM.md](../CURRICULUM.md)** (đổi 🔲 → ✍️ ở đúng dòng ID).

---

## 4. Quality bar — tiêu chuẩn chất lượng

### 4.1 Nội dung
1. **Đúng trước, hay sau.** Mọi khẳng định kỹ thuật không hiển nhiên phải có nguồn gốc (docs chính thức, JEP, RFC, source code, paper, sách trong ROADMAP §8). Không chắc → kiểm chứng bằng lab hoặc ghi rõ "cần kiểm chứng".
2. **Cơ chế, không phải định nghĩa.** Phần "Cơ chế bên trong" là trọng tâm (≥ 40% độ dài lý thuyết). Trả lời được *tại sao* và *chuyện gì xảy ra bên dưới*.
3. **Định lượng mọi thứ.** Không viết "nhanh hơn", "nhiều request" — viết `~3× throughput`, `5.000 req/s`, `p99 từ 800ms xuống 45ms` (và nói rõ đo trên máy nào).
4. **Trả lời hết "Câu hỏi sâu" trong brief.** Đây là tiêu chí nghiệm thu chính của bài.
5. **Có ví dụ sai và đúng.** Người học nhớ lâu nhờ thấy cái sai trước.
6. **Liên hệ legacy khi hợp lý.** Người học làm việc với Java 7 / JBoss AS 7 / JDBC thuần, máy công ty không có Python, không gọi được dịch vụ bên ngoài. Thêm mục "Legacy corner" khi bài có liên quan.
7. **Không chép.** Không copy nguyên văn đoạn dài từ sách/bài viết; diễn đạt lại và dẫn nguồn.

### 4.2 Bài tập
- Tối thiểu: 4 bài L1 + 3 bài L2 + 1 bài L3. Challenge tùy chọn.
- Bài L2/L3 phải có **tiêu chí hoàn thành kiểm chứng được** (test pass, số đo, output cụ thể).
- Bài L3 nên mô phỏng tình huống thật: ticket, log, code có bug.
- Đáp án giải thích **cả lý do các đáp án sai phổ biến là sai**.

### 4.3 Lab / Demo
- **Phải chạy được** từ một thư mục sạch theo đúng README của lab. Nếu môi trường soạn cho phép, agent **chạy thật** và ghi lại số đo tham khảo + cấu hình máy. Nếu không chạy được, ghi rõ `> ⚠️ Chưa chạy kiểm chứng trong môi trường soạn` ở đầu lab README.
- Mỗi lab **độc lập**: có `pom.xml` riêng (không dùng parent pom chung), không phụ thuộc lab khác.
- Ưu tiên chạy được bằng **Docker Compose + JDK 21 + Maven** — chạy được trên Windows.
- Lab có bước "**Dự đoán trước khi chạy**" — người học ghi dự đoán rồi mới chạy.
- Không cần tài khoản cloud trả phí, trừ domain `aws-cloud/` và `iac-terraform/`: khi đó bắt buộc có mục **chi phí ước tính** và **lệnh teardown**, ưu tiên free tier / LocalStack khi được.

---

## 5. Quy ước viết

### 5.1 Ngôn ngữ & giọng văn
- Viết **tiếng Việt**, giữ **thuật ngữ kỹ thuật tiếng Anh** (thread pool, heap, isolation level, ...). Lần đầu xuất hiện có thể kèm giải thích ngắn.
- Giọng văn: thẳng, kỹ thuật, như senior giải thích cho đồng nghiệp. Không văn mẫu, không emoji tràn lan (emoji chỉ dùng ở tiêu đề mục như template).
- Câu ngắn. Mỗi đoạn một ý.

### 5.2 Định dạng
- Markdown chuẩn GitHub. Sơ đồ dùng **Mermaid** (render được trên GitHub).
- Link nội bộ dùng **đường dẫn tương đối** (`../03-cas-atomics/README.md`), **không** dùng `file:///`.
- Tham chiếu chéo bằng ID: `[CONC-04](../04-thread-pool-internals/README.md)`.
- Code block luôn ghi ngôn ngữ (```java, ```sql, ```bash, ```yaml).

### 5.3 Code
- Java 21, Maven, package `lab.<domain>.<topic>` (ví dụ `lab.concurrency.threadpool`).
- Không dùng Lombok trong lab (để người học thấy rõ code).
- Test bằng JUnit 5 + AssertJ; integration test dùng Testcontainers.
- Comment giải thích **tại sao**, không giải thích *cái gì* hiển nhiên.
- Port mặc định khi cần, tránh đụng nhau giữa các lab: app `8080`, PostgreSQL `5432`, Redis `6379`, Kafka `9092`, Prometheus `9090`, Grafana `3000`. Nếu lab cần chạy song song nhiều thứ, ghi rõ.

### 5.4 Độ dài tham khảo
| File | Độ dài |
| :--- | :--- |
| Lesson README (lý thuyết) | 1.500–4.000 từ |
| exercises.md | 8–12 bài |
| Pattern card | 600–1.500 từ |
| Concept decision | 1.500–3.000 từ |
| System design | 2.000–4.000 từ |

---

## 6. Definition of Done (agent tự kiểm tra trước khi đặt `draft`)

- [ ] Giữ nguyên khối **🧭 Brief**, tick các deliverable đã làm.
- [ ] Đủ các mục của template tương ứng; không còn placeholder `...` / `<...>`.
- [ ] Trả lời được **tất cả** "Câu hỏi sâu" trong brief (có thể đánh dấu ở mục nào trả lời câu nào).
- [ ] ≥ 1 sơ đồ Mermaid hợp lệ.
- [ ] ≥ 3 cạm bẫy production.
- [ ] 5 câu self-check, đáp án nằm trong solutions.md.
- [ ] exercises.md đủ số lượng tối thiểu; solutions.md có đáp án cho mọi bài.
- [ ] Lab: build được (`mvn -q verify` hoặc tương đương), README của lab chạy theo được từng bước; đã chạy thật hoặc có cảnh báo "chưa kiểm chứng".
- [ ] Mọi link tương đối hoạt động; ID tham chiếu tồn tại trong CURRICULUM.md.
- [ ] Có mục Tài liệu tham khảo với link nguồn gốc.
- [ ] Frontmatter: `status: draft`, `last_updated` đúng ngày.
- [ ] CURRICULUM.md: dòng ID tương ứng đổi 🔲 → ✍️.
- [ ] Không tạo/sửa `my-notes.md`, `weekly-log/`, bug-case/post-mortem thật.

---

## 7. Những điều KHÔNG được làm
- Không đổi tên, di chuyển hay xóa file/thư mục có sẵn (kể cả brief). Cần thay đổi cấu trúc → ghi đề xuất vào báo cáo cuối.
- Không đổi ID bài. Muốn thêm bài mới → thêm dòng vào CURRICULUM.md với ID tiếp theo của domain và tạo brief theo format có sẵn, ghi rõ trong báo cáo.
- Không commit secret, token, dữ liệu thật của công ty.
- Không viết nội dung đánh giá năng lực người học.
- Không xóa nội dung cũ đã được viết trong `notes/`, `concept-decisions/` — chỉ link tới.

---

## 8. Commit
- Conventional commits: `docs(conc-04): add thread pool internals lesson, exercises and lab`
- Mỗi bài 1 commit (hoặc 1 commit cho cả batch nếu được yêu cầu).
