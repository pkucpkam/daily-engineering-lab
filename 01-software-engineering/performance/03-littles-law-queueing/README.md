---
id: PERF-03
title: "Little's Law & queueing theory cơ bản"
domain: 01-software-engineering/performance
phase: Y1Q2
kind: lesson
status: todo
prerequisites: [PERF-02]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# PERF-03 · Little's Law & queueing theory cơ bản

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q2 |
| **Chủ đề** | Performance Optimization |
| **Tiên quyết** | [PERF-02](../02-latency-percentiles/README.md) |

**Phạm vi bắt buộc**
- L = λW
- utilization và latency tăng phi tuyến
- hàng đợi ẩn trong hệ thống
- capacity planning thô

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Utilization 90% vs 70% latency khác nhau thế nào?
- 10.000 request đồng thời qua thread pool 200 + DB pool 20 xếp hàng ở đâu?

**Ý tưởng lab / demo**
- Mô phỏng hàng đợi M/M/1, vẽ latency theo utilization

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
