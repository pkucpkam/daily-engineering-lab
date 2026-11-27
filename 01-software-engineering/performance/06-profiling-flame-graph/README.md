---
id: PERF-06
title: "Profiling: async-profiler & flame graph"
domain: 01-software-engineering/performance
phase: Y1Q2
kind: lesson
status: todo
prerequisites: [PERF-05]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# PERF-06 · Profiling: async-profiler & flame graph

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q2 |
| **Chủ đề** | Performance Optimization |
| **Tiên quyết** | [PERF-05](../05-load-testing/README.md) |

**Phạm vi bắt buộc**
- CPU vs wall-clock vs allocation profiling
- safepoint bias
- đọc flame graph
- profiling trên prod an toàn

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Vì sao profiler cũ (safepoint-biased) chỉ sai chỗ nóng?
- Wall-clock profile giúp tìm gì mà CPU profile không thấy?

**Ý tưởng lab / demo**
- Tìm hot spot trong app có regex và JSON serialization chậm

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
