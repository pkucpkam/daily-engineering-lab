---
id: CONC-08
title: "Virtual Threads vs Platform Threads"
domain: 01-software-engineering/concurrency
phase: Y1Q2
kind: lesson
status: todo
prerequisites: [CONC-07]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# CONC-08 · Virtual Threads vs Platform Threads

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q2 |
| **Chủ đề** | Concurrency |
| **Tiên quyết** | [CONC-07](../07-locks-reentrant-stamped/README.md) |

**Phạm vi bắt buộc**
- carrier thread, mount/unmount
- pinning (synchronized, native)
- ThreadLocal với virtual thread
- khi virtual thread không giúp gì (CPU-bound, DB pool giới hạn)
- structured concurrency (preview)

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- 10.000 virtual thread gọi DB có pool 20 thì sao?
- Pinning phát hiện bằng cách nào?

**Ý tưởng lab / demo**
- So sánh throughput HTTP server giả lập IO-bound: platform pool 200 vs virtual threads; quan sát pinning bằng -Djdk.tracePinnedThreads

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
