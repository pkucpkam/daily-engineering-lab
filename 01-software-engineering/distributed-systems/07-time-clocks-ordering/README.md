---
id: DIST-07
title: "Thời gian, đồng hồ & thứ tự"
domain: 01-software-engineering/distributed-systems
phase: Y1Q4
kind: lesson
status: todo
prerequisites: [DIST-06]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# DIST-07 · Thời gian, đồng hồ & thứ tự

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q4 |
| **Chủ đề** | Distributed Systems |
| **Tiên quyết** | [DIST-06](../06-raft-consensus/README.md) |

**Phạm vi bắt buộc**
- clock skew, NTP
- wall clock vs monotonic clock
- Lamport clock
- vector clock
- hybrid logical clock
- last-write-wins nguy hiểm

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Dùng timestamp để quyết định bản ghi mới nhất sai khi nào?
- Lamport clock đảm bảo gì và không đảm bảo gì?

**Ý tưởng lab / demo**
- Mô phỏng LWW mất dữ liệu do clock skew

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
