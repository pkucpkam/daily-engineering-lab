---
id: CONC-09
title: "Deadlock, Livelock, Starvation"
domain: 01-software-engineering/concurrency
phase: Y1Q2
kind: lesson
status: todo
prerequisites: [CONC-08]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# CONC-09 · Deadlock, Livelock, Starvation

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q2 |
| **Chủ đề** | Concurrency |
| **Tiên quyết** | [CONC-08](../08-virtual-threads/README.md) |

**Phạm vi bắt buộc**
- 4 điều kiện Coffman
- lock ordering
- tryLock timeout
- livelock
- starvation do fairness

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Phòng deadlock bằng lock ordering như thế nào?
- Livelock khác deadlock ra sao khi quan sát thread dump?

**Ý tưởng lab / demo**
- Tái hiện deadlock chuyển tiền A↔B, phát hiện bằng jstack và sửa

**Tài liệu gốc gợi ý**
- Java Concurrency in Practice
- JSR-133 FAQ
- JEP 444 Virtual Threads
- Doug Lea's concurrency papers

**Gói đầu ra**
- [ ] `README.md` — lý thuyết theo [lesson-template](../../../docs/templates/lesson-template.md)
- [ ] `exercises.md` — bài tập L1/L2/L3 theo [exercises-template](../../../docs/templates/exercises-template.md)
- [ ] `solutions.md` — đáp án theo [solutions-template](../../../docs/templates/solutions-template.md)
- [ ] `lab/` — code demo chạy được + `lab/README.md` theo [lab-template](../../../docs/templates/lab-template.md)
- [ ] _(người học)_ `my-notes.md` — **agent không tạo**

---

<!-- Nội dung bắt đầu từ đây -->
