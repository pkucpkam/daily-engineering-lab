---
id: DB-02
title: "WAL, checkpoint & durability"
domain: 01-software-engineering/database
phase: Y1Q2
kind: lesson
status: todo
prerequisites: [DB-01]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# DB-02 · WAL, checkpoint & durability

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q2 |
| **Chủ đề** | Database Internals (PostgreSQL) |
| **Tiên quyết** | [DB-01](../01-storage-page-heap-toast/README.md) |

**Phạm vi bắt buộc**
- write-ahead logging
- fsync
- checkpoint
- synchronous_commit
- crash recovery
- WAL cho replication

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- COMMIT trả về thì dữ liệu đã nằm trên đĩa chưa?
- synchronous_commit=off đánh đổi gì?

**Ý tưởng lab / demo**
- Đo TPS với synchronous_commit on/off bằng pgbench

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
