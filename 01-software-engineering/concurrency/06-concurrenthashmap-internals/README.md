---
id: CONC-06
title: "ConcurrentHashMap internals"
domain: 01-software-engineering/concurrency
phase: Y1Q2
kind: lesson
status: todo
prerequisites: [CONC-05]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# CONC-06 · ConcurrentHashMap internals

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q2 |
| **Chủ đề** | Concurrency |
| **Tiên quyết** | [CONC-05](../05-completablefuture/README.md) |

**Phạm vi bắt buộc**
- Java 7 segment vs Java 8+ CAS + synchronized per bin
- computeIfAbsent và bẫy
- size() đếm thế nào
- so với Collections.synchronizedMap

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- computeIfAbsent có thể deadlock khi nào?
- Vì sao CHM không cho null?

**Ý tưởng lab / demo**
- Benchmark CHM vs synchronizedMap; tái hiện bẫy recursive computeIfAbsent

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
