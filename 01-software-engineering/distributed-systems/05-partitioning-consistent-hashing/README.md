---
id: DIST-05
title: "Partitioning & Consistent hashing"
domain: 01-software-engineering/distributed-systems
phase: Y1Q4
kind: lesson
status: todo
prerequisites: [DIST-04]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# DIST-05 · Partitioning & Consistent hashing

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q4 |
| **Chủ đề** | Distributed Systems |
| **Tiên quyết** | [DIST-04](../04-replication-lag/README.md) |

**Phạm vi bắt buộc**
- range vs hash partitioning
- hot partition
- rebalancing
- consistent hashing & virtual nodes
- secondary index trên dữ liệu phân mảnh

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Thêm 1 node vào cluster hash mod N gây ra gì?
- Xử lý hot partition (celebrity key)?

**Ý tưởng lab / demo**
- Cài consistent hashing với virtual nodes, đo phân phối key khi thêm/bớt node

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
