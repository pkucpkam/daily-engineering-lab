---
id: PERF-02
title: "Latency percentiles & coordinated omission"
domain: 01-software-engineering/performance
phase: Y1Q2
kind: lesson
status: todo
prerequisites: [PERF-01]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# PERF-02 · Latency percentiles & coordinated omission

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q2 |
| **Chủ đề** | Performance Optimization |
| **Tiên quyết** | [PERF-01](../01-measure-first/README.md) |

**Phạm vi bắt buộc**
- p50/p95/p99/p999
- histogram (HdrHistogram)
- coordinated omission
- tail latency khi fan-out

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Gọi song song 10 service, mỗi cái p99=100ms, p99 tổng thể là bao nhiêu?
- Coordinated omission làm số đo đẹp giả thế nào?

**Ý tưởng lab / demo**
- Đo cùng 1 service bằng 2 load generator (có/không sửa coordinated omission)

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
