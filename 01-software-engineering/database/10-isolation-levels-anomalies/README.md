---
id: DB-10
title: "Isolation levels & anomalies"
domain: 01-software-engineering/database
phase: Y1Q2
kind: lesson
status: todo
prerequisites: [DB-09]
related_lessons: []
related_existing: [01-software-engineering/notes/phantom-read.md, 01-software-engineering/notes/transaction-boundary.md]
last_updated: 2026-11-27
---

# DB-10 · Isolation levels & anomalies

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q2 |
| **Chủ đề** | Database Internals (PostgreSQL) |
| **Tiên quyết** | [DB-09](../09-vacuum-bloat/README.md) |
| **Đã có trong repo** | [phantom-read.md](../../notes/phantom-read.md)<br>[transaction-boundary.md](../../notes/transaction-boundary.md) — link tới, không viết lại |

**Phạm vi bắt buộc**
- dirty/non-repeatable/phantom read
- lost update
- isolation levels chuẩn SQL vs thực tế PG
- READ COMMITTED mặc định

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- READ COMMITTED có ngăn lost update không?
- REPEATABLE READ của PG khác chuẩn SQL ở đâu?

**Ý tưởng lab / demo**
- Script tái hiện từng anomaly với 2 session

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
