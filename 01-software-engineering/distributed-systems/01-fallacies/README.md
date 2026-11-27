---
id: DIST-01
title: "8 Fallacies of Distributed Computing"
domain: 01-software-engineering/distributed-systems
phase: Y1Q4
kind: lesson
status: todo
prerequisites: []
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# DIST-01 · 8 Fallacies of Distributed Computing

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q4 |
| **Chủ đề** | Distributed Systems |

**Phạm vi bắt buộc**
- từng fallacy với sự cố thực tế
- partial failure
- network partition

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Partial failure khác lỗi trong 1 process thế nào?
- Fallacy nào gây nhiều sự cố nhất trong hệ thống bạn từng làm?

**Ý tưởng lab / demo**
- Dùng Toxiproxy tiêm latency/packet loss vào lời gọi giữa 2 service

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
