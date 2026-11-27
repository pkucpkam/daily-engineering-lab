---
id: DIST-09
title: "Kafka consumer groups & rebalancing"
domain: 01-software-engineering/distributed-systems
phase: Y1Q4
kind: lesson
status: todo
prerequisites: [DIST-08]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# DIST-09 · Kafka consumer groups & rebalancing

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q4 |
| **Chủ đề** | Distributed Systems |
| **Tiên quyết** | [DIST-08](../08-kafka-architecture/README.md) |

**Phạm vi bắt buộc**
- partition assignment
- commit offset (auto vs manual)
- rebalance và stop-the-world
- cooperative sticky
- max.poll.interval.ms
- consumer lag

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Vì sao consumer bị rebalance liên tục?
- Commit offset trước hay sau xử lý?

**Ý tưởng lab / demo**
- Consumer xử lý chậm gây rebalance loop, sửa bằng cấu hình đúng

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
