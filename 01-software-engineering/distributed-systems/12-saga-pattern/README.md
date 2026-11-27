---
id: DIST-12
title: "Saga: orchestration vs choreography"
domain: 01-software-engineering/distributed-systems
phase: Y1Q4
kind: lesson
status: todo
prerequisites: [DIST-11]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# DIST-12 · Saga: orchestration vs choreography

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q4 |
| **Chủ đề** | Distributed Systems |
| **Tiên quyết** | [DIST-11](../11-outbox-pattern/README.md) |

**Phạm vi bắt buộc**
- distributed transaction không dùng 2PC
- compensating transaction
- orchestration vs choreography
- semantic lock
- idempotency trong saga
- trạng thái saga

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Bước bồi hoàn thất bại thì làm gì?
- Vì sao 2PC ít được dùng trong microservices?

**Ý tưởng lab / demo**
- Saga Order → Inventory → Payment có lỗi ở bước Payment

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
