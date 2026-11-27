---
id: DIST-06
title: "Consensus & Raft"
domain: 01-software-engineering/distributed-systems
phase: Y1Q4
kind: lesson
status: todo
prerequisites: [DIST-05]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# DIST-06 · Consensus & Raft

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q4 |
| **Chủ đề** | Distributed Systems |
| **Tiên quyết** | [DIST-05](../05-partitioning-consistent-hashing/README.md) |

**Phạm vi bắt buộc**
- vấn đề consensus
- leader election, log replication, term
- safety
- etcd dùng Raft
- FLP (mức khái niệm)

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Vì sao cluster Raft cần số node lẻ?
- Điều gì xảy ra khi leader bị partition khỏi đa số?

**Ý tưởng lab / demo**
- Chạy cluster etcd 3 node, kill leader và quan sát bầu lại

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
