---
id: DB-06
title: "EXPLAIN ANALYZE & Query planner"
domain: 01-software-engineering/database
phase: Y1Q2
kind: lesson
status: todo
prerequisites: [DB-05]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# DB-06 · EXPLAIN ANALYZE & Query planner

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q2 |
| **Chủ đề** | Database Internals (PostgreSQL) |
| **Tiên quyết** | [DB-05](../05-composite-covering-index/README.md) |

**Phạm vi bắt buộc**
- đọc plan node
- cost vs actual time
- rows estimate vs actual
- join algorithms (nested loop, hash, merge)
- BUFFERS

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Ước lượng rows sai 100 lần dẫn đến gì?
- Khi nào nested loop tốt hơn hash join?

**Ý tưởng lab / demo**
- Bộ 5 query chậm có sẵn, đọc plan và sửa từng cái

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
