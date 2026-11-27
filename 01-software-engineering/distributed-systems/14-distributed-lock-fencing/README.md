---
id: DIST-14
title: "Distributed lock & fencing token"
domain: 01-software-engineering/distributed-systems
phase: Y1Q4
kind: lesson
status: todo
prerequisites: [DIST-13]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# DIST-14 · Distributed lock & fencing token

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q4 |
| **Chủ đề** | Distributed Systems |
| **Tiên quyết** | [DIST-13](../13-cqrs-event-sourcing/README.md) |

**Phạm vi bắt buộc**
- lock bằng Redis SET NX PX
- Redisson
- tranh luận Redlock (Kleppmann vs antirez)
- GC pause làm lock hết hạn
- fencing token
- lock bằng DB/etcd

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Vì sao distributed lock không có fencing token là không an toàn?
- Khi nào nên dùng lock DB thay Redis?

**Ý tưởng lab / demo**
- Mô phỏng GC pause làm 2 client cùng giữ lock, sửa bằng fencing token

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
