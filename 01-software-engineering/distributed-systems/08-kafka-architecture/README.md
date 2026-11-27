---
id: DIST-08
title: "Kafka architecture"
domain: 01-software-engineering/distributed-systems
phase: Y1Q4
kind: lesson
status: todo
prerequisites: [DIST-07]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# DIST-08 · Kafka architecture

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q4 |
| **Chủ đề** | Distributed Systems |
| **Tiên quyết** | [DIST-07](../07-time-clocks-ordering/README.md) |

**Phạm vi bắt buộc**
- topic, partition, segment, offset
- broker, replication, ISR
- acks
- KRaft
- ordering chỉ trong partition
- retention & compaction

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- acks=all + min.insync.replicas=2 đảm bảo gì?
- Vì sao Kafka nhanh dù ghi xuống đĩa?

**Ý tưởng lab / demo**
- Kafka cluster 3 broker bằng Compose, kill broker và quan sát ISR

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
