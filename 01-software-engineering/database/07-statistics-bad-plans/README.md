---
id: DB-07
title: "Statistics & bad plans"
domain: 01-software-engineering/database
phase: Y1Q2
kind: lesson
status: todo
prerequisites: [DB-06]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# DB-07 · Statistics & bad plans

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q2 |
| **Chủ đề** | Database Internals (PostgreSQL) |
| **Tiên quyết** | [DB-06](../06-explain-analyze-planner/README.md) |

**Phạm vi bắt buộc**
- ANALYZE, pg_stats, histogram, MCV
- extended statistics cho cột tương quan
- plan thay đổi đột ngột trên prod
- parameter sniffing/generic plan

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Query nhanh trên dev chậm 100× trên prod: 5 giả thuyết?
- Prepared statement có thể chọn plan tệ khi nào?

**Ý tưởng lab / demo**
- Tạo dữ liệu lệch (skew), quan sát plan đổi sau ANALYZE

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
