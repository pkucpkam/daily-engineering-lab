---
id: PERF-04
title: "Micro-benchmark với JMH"
domain: 01-software-engineering/performance
phase: Y1Q2
kind: lesson
status: todo
prerequisites: [PERF-03]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# PERF-04 · Micro-benchmark với JMH

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q2 |
| **Chủ đề** | Performance Optimization |
| **Tiên quyết** | [PERF-03](../03-littles-law-queueing/README.md) |

**Phạm vi bắt buộc**
- vì sao benchmark tự viết sai (JIT, dead code, warmup)
- @State, @Benchmark, Blackhole
- đọc kết quả
- profilers trong JMH

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Vì sao System.nanoTime() quanh vòng lặp không đáng tin?
- Dead-code elimination phá benchmark thế nào?

**Ý tưởng lab / demo**
- Benchmark String concat / StringBuilder / String.join đúng cách

**Tài liệu gốc gợi ý**
- Systems Performance (Gregg)
- brendangregg.com
- JMH samples
- k6 docs

**Gói đầu ra**
- [ ] `README.md` — lý thuyết theo [lesson-template](../../../docs/templates/lesson-template.md)
- [ ] `exercises.md` — bài tập L1/L2/L3 theo [exercises-template](../../../docs/templates/exercises-template.md)
- [ ] `solutions.md` — đáp án theo [solutions-template](../../../docs/templates/solutions-template.md)
- [ ] `lab/` — code demo chạy được + `lab/README.md` theo [lab-template](../../../docs/templates/lab-template.md)
- [ ] _(người học)_ `my-notes.md` — **agent không tạo**

---

<!-- Nội dung bắt đầu từ đây -->
