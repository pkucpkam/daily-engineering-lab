---
id: CONC-01
title: "Java Memory Model & happens-before"
domain: 01-software-engineering/concurrency
phase: Y1Q2
kind: lesson
status: todo
prerequisites: []
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# CONC-01 · Java Memory Model & happens-before

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q2 |
| **Chủ đề** | Concurrency |

**Phạm vi bắt buộc**
- visibility, reordering, atomicity
- happens-before rules
- data race
- safe publication

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Vì sao một thread có thể không bao giờ thấy giá trị mới của biến?
- Double-checked locking sai ở đâu nếu thiếu volatile?

**Ý tưởng lab / demo**
- Tái hiện visibility bug với vòng lặp while(!stop) và sửa bằng volatile

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
