---
id: DB-12
title: "Row locks, FOR UPDATE, SKIP LOCKED"
domain: 01-software-engineering/database
phase: Y1Q2
kind: lesson
status: todo
prerequisites: [DB-11]
related_lessons: []
related_existing: [01-software-engineering/concept-decisions/pessimistic-vs-optimistic-lock.md]
last_updated: 2026-11-27
---

# DB-12 · Row locks, FOR UPDATE, SKIP LOCKED

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q2 |
| **Chủ đề** | Database Internals (PostgreSQL) |
| **Tiên quyết** | [DB-11](../11-write-skew-serializable/README.md) |
| **Đã có trong repo** | [pessimistic-vs-optimistic-lock.md](../../concept-decisions/pessimistic-vs-optimistic-lock.md) — link tới, không viết lại |

**Phạm vi bắt buộc**
- các mức row lock
- FOR UPDATE / FOR SHARE / NOWAIT / SKIP LOCKED
- lock queue
- job queue bằng SKIP LOCKED

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Làm job queue trên PostgreSQL cho nhiều worker ra sao?
- FOR UPDATE trên query có JOIN khóa những gì?

**Ý tưởng lab / demo**
- Job queue nhiều worker bằng SKIP LOCKED, đo throughput

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
