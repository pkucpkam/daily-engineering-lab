---
id: DB-14
title: "Connection pool: HikariCP sizing & exhaustion"
domain: 01-software-engineering/database
phase: Y1Q2
kind: lesson
status: todo
prerequisites: [DB-13]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# DB-14 · Connection pool: HikariCP sizing & exhaustion

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q2 |
| **Chủ đề** | Database Internals (PostgreSQL) |
| **Tiên quyết** | [DB-13](../13-advisory-locks-contention/README.md) |

**Phạm vi bắt buộc**
- chi phí 1 connection PG (process)
- công thức sizing
- pool exhaustion
- connectionTimeout
- leak detection
- PgBouncer

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Vì sao pool 200 thường chậm hơn pool 20?
- Triệu chứng pool exhaustion nhìn từ app và DB?

**Ý tưởng lab / demo**
- Load test với pool 5/20/100/200, vẽ throughput & p99

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
