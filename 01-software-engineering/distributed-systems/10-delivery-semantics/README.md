---
id: DIST-10
title: "At-most/at-least/exactly-once & idempotent consumer"
domain: 01-software-engineering/distributed-systems
phase: Y1Q4
kind: lesson
status: todo
prerequisites: [DIST-09]
related_lessons: []
related_existing: [01-software-engineering/notes/idempotency.md]
last_updated: 2026-11-27
---

# DIST-10 · At-most/at-least/exactly-once & idempotent consumer

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q4 |
| **Chủ đề** | Distributed Systems |
| **Tiên quyết** | [DIST-09](../09-kafka-consumer-groups/README.md) |
| **Đã có trong repo** | [idempotency.md](../../notes/idempotency.md) — link tới, không viết lại |

**Phạm vi bắt buộc**
- delivery semantics
- idempotent producer
- Kafka transactions
- exactly-once thực tế = at-least-once + idempotent
- dedup table

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Exactly-once end-to-end có thật không?
- Thiết kế consumer idempotent cho nghiệp vụ trừ tiền?

**Ý tưởng lab / demo**
- Consumer xử lý trùng message, sửa bằng dedup key trong DB

**Tài liệu gốc gợi ý**
- Designing Data-Intensive Applications
- Kafka: The Definitive Guide
- Raft paper (raft.github.io)
- Jepsen analyses
- microservices.io

**Gói đầu ra**
- [ ] `README.md` — lý thuyết theo [lesson-template](../../../docs/templates/lesson-template.md)
- [ ] `exercises.md` — bài tập L1/L2/L3 theo [exercises-template](../../../docs/templates/exercises-template.md)
- [ ] `solutions.md` — đáp án theo [solutions-template](../../../docs/templates/solutions-template.md)
- [ ] `lab/` — code demo chạy được + `lab/README.md` theo [lab-template](../../../docs/templates/lab-template.md)
- [ ] _(người học)_ `my-notes.md` — **agent không tạo**

---

<!-- Nội dung bắt đầu từ đây -->
