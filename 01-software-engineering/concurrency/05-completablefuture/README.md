---
id: CONC-05
title: "CompletableFuture & async composition"
domain: 01-software-engineering/concurrency
phase: Y1Q2
kind: lesson
status: todo
prerequisites: [CONC-04]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# CONC-05 · CompletableFuture & async composition

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q2 |
| **Chủ đề** | Concurrency |
| **Tiên quyết** | [CONC-04](../04-thread-pool-internals/README.md) |

**Phạm vi bắt buộc**
- thenApply/thenCompose/thenCombine
- executor mặc định (ForkJoinPool.commonPool) và bẫy
- exception handling
- timeout
- allOf/anyOf

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Vì sao dùng commonPool cho tác vụ IO là nguy hiểm?
- Exception trong chuỗi CF bị nuốt khi nào?

**Ý tưởng lab / demo**
- Gọi song song 3 "service" giả lập có timeout và fallback

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
