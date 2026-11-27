---
id: CONC-10
title: "Đọc Thread dump"
domain: 01-software-engineering/concurrency
phase: Y1Q2
kind: lesson
status: todo
prerequisites: [CONC-09]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# CONC-10 · Đọc Thread dump

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q2 |
| **Chủ đề** | Concurrency |
| **Tiên quyết** | [CONC-09](../09-deadlock-livelock-starvation/README.md) |

**Phạm vi bắt buộc**
- jstack/jcmd Thread.print
- trạng thái thread (RUNNABLE, BLOCKED, WAITING, TIMED_WAITING)
- tìm lock owner
- nhiều dump cách nhau vài giây
- fastthread.io

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- 200 thread WAITING trên HikariPool nghĩa là gì?
- Vì sao cần lấy 3 dump cách nhau?

**Ý tưởng lab / demo**
- Kịch bản thread pool starvation, chẩn đoán chỉ bằng thread dump

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
