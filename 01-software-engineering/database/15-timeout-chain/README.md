---
id: DB-15
title: "Timeout chain: client → app → pool → DB"
domain: 01-software-engineering/database
phase: Y1Q2
kind: lesson
status: todo
prerequisites: [DB-14]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# DB-15 · Timeout chain: client → app → pool → DB

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q2 |
| **Chủ đề** | Database Internals (PostgreSQL) |
| **Tiên quyết** | [DB-14](../14-connection-pool-sizing/README.md) |

**Phạm vi bắt buộc**
- các loại timeout (connect, read, statement, lock, idle-in-transaction)
- timeout phải giảm dần theo tầng
- hậu quả khi sai thứ tự

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Client timeout 5s, DB statement timeout 30s thì chuyện gì xảy ra?
- Thiết lập timeout chain hợp lý cho 1 API?

**Ý tưởng lab / demo**
- Cấu hình sai rồi đúng timeout chain, quan sát request "ma" chạy tiếp trên DB

**Tài liệu gốc gợi ý**
- PostgreSQL Docs
- Database Internals (Petrov)
- Use The Index, Luke (use-the-index-luke.com)
- The Internals of PostgreSQL (interdb.jp)

**Gói đầu ra**
- [ ] `README.md` — lý thuyết theo [lesson-template](../../../docs/templates/lesson-template.md)
- [ ] `exercises.md` — bài tập L1/L2/L3 theo [exercises-template](../../../docs/templates/exercises-template.md)
- [ ] `solutions.md` — đáp án theo [solutions-template](../../../docs/templates/solutions-template.md)
- [ ] `lab/` — code demo chạy được + `lab/README.md` theo [lab-template](../../../docs/templates/lab-template.md)
- [ ] _(người học)_ `my-notes.md` — **agent không tạo**

---

<!-- Nội dung bắt đầu từ đây -->
