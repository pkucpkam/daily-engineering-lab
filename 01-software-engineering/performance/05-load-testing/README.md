---
id: PERF-05
title: "Load testing với k6 / Gatling"
domain: 01-software-engineering/performance
phase: Y1Q2
kind: lesson
status: todo
prerequisites: [PERF-04]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# PERF-05 · Load testing với k6 / Gatling

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q2 |
| **Chủ đề** | Performance Optimization |
| **Tiên quyết** | [PERF-04](../04-jmh-microbenchmark/README.md) |

**Phạm vi bắt buộc**
- load vs stress vs soak vs spike test
- open vs closed model
- ramp-up
- xác định điểm gãy
- kết hợp với metric server

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Open model khác closed model ra sao và vì sao quan trọng?
- Tìm "knee point" của hệ thống thế nào?

**Ý tưởng lab / demo**
- Kịch bản k6 tăng tải dần lên API mẫu, tìm điểm gãy

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
