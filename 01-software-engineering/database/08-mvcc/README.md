---
id: DB-08
title: "MVCC: xmin, xmax, snapshot"
domain: 01-software-engineering/database
phase: Y1Q2
kind: lesson
status: todo
prerequisites: [DB-07]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# DB-08 · MVCC: xmin, xmax, snapshot

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q2 |
| **Chủ đề** | Database Internals (PostgreSQL) |
| **Tiên quyết** | [DB-07](../07-statistics-bad-plans/README.md) |

**Phạm vi bắt buộc**
- tuple versions
- snapshot
- visibility rules
- so sánh MVCC PG vs undo log (MySQL/Oracle)

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Reader có bao giờ block writer trong PG không?
- Snapshot được lấy lúc nào ở READ COMMITTED vs REPEATABLE READ?

**Ý tưởng lab / demo**
- 2 session psql quan sát xmin/xmax khi update đồng thời

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
