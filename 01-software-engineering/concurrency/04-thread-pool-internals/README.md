---
id: CONC-04
title: "Thread Pool Internals"
domain: 01-software-engineering/concurrency
phase: Y1Q2
kind: lesson
status: todo
prerequisites: [CONC-03]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# CONC-04 · Thread Pool Internals

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q2 |
| **Chủ đề** | Concurrency |
| **Tiên quyết** | [CONC-03](../03-cas-atomics/README.md) |

**Phạm vi bắt buộc**
- ThreadPoolExecutor: core/max/queue/keepAlive
- thứ tự tạo thread vs xếp hàng
- rejection policies
- Executors factory nguy hiểm (unbounded queue)
- sizing CPU-bound vs IO-bound

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Vì sao core=10,max=100 với LinkedBlockingQueue không bao giờ lên quá 10 thread?
- Pool nên lớn bao nhiêu cho tác vụ gọi DB?

**Ý tưởng lab / demo**
- Visualize trạng thái pool (active, queue size) khi bắn tải tăng dần

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
