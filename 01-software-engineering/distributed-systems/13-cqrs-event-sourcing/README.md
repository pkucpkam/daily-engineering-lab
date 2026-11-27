---
id: DIST-13
title: "CQRS & Event Sourcing"
domain: 01-software-engineering/distributed-systems
phase: Y1Q4
kind: lesson
status: todo
prerequisites: [DIST-12]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# DIST-13 · CQRS & Event Sourcing

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q4 |
| **Chủ đề** | Distributed Systems |
| **Tiên quyết** | [DIST-12](../12-saga-pattern/README.md) |

**Phạm vi bắt buộc**
- tách read/write model
- projection
- event store
- snapshot
- schema evolution của event
- khi nào KHÔNG dùng

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Event sourcing khó ở đâu khi chạy production lâu dài?
- CQRS có bắt buộc đi kèm event sourcing không?

**Ý tưởng lab / demo**
- Read model projection từ event Kafka cho dashboard đơn hàng

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
