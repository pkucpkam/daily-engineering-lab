---
id: DIST-03
title: "Replication: leader-follower, multi-leader, leaderless"
domain: 01-software-engineering/distributed-systems
phase: Y1Q4
kind: lesson
status: todo
prerequisites: [DIST-02]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# DIST-03 · Replication: leader-follower, multi-leader, leaderless

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q4 |
| **Chủ đề** | Distributed Systems |
| **Tiên quyết** | [DIST-02](../02-cap-pacelc/README.md) |

**Phạm vi bắt buộc**
- sync vs async replication
- failover và split brain
- multi-leader conflict
- quorum (N, R, W)
- sloppy quorum

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- R+W>N có đảm bảo đọc mới nhất mọi lúc không?
- Failover tự động có rủi ro gì?

**Ý tưởng lab / demo**
- PostgreSQL primary + replica bằng Docker Compose, quan sát replication lag

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
