---
id: DB-13
title: "Advisory locks & lock contention"
domain: 01-software-engineering/database
phase: Y1Q2
kind: lesson
status: todo
prerequisites: [DB-12]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# DB-13 · Advisory locks & lock contention

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q2 |
| **Chủ đề** | Database Internals (PostgreSQL) |
| **Tiên quyết** | [DB-12](../12-row-locks-skip-locked/README.md) |

**Phạm vi bắt buộc**
- pg_advisory_lock (session vs xact)
- pg_locks, pg_stat_activity
- lock_timeout
- tìm blocking chain

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Tìm query đang block 50 query khác thế nào?
- Advisory lock khác row lock ra sao?

**Ý tưởng lab / demo**
- Tạo chuỗi blocking và truy vết bằng pg_blocking_pids

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
