---
id: OBS-07
title: "SLI, SLO, SLA & Error budget"
domain: 02-devops-and-cloud/observability-sre
phase: Y2Q4
kind: lesson
status: todo
prerequisites: [OBS-06]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# OBS-07 · SLI, SLO, SLA & Error budget

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 2 · Q4 |
| **Chủ đề** | Observability & SRE |
| **Tiên quyết** | [OBS-06](../06-opentelemetry-tracing/README.md) |

**Phạm vi bắt buộc**
- chọn SLI đúng
- SLO window
- error budget và chính sách release
- burn rate

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- SLO 99.9% cho phép bao nhiêu phút lỗi/tháng?
- Hết error budget thì team làm gì?

**Ý tưởng lab / demo**
- Định nghĩa SLO cho API Project 1 bằng PromQL

**Tài liệu gốc gợi ý**
- Site Reliability Engineering & SRE Workbook (sre.google)
- Observability Engineering
- Prometheus docs
- OpenTelemetry docs

**Gói đầu ra**
- [ ] `README.md` — lý thuyết theo [lesson-template](../../../docs/templates/lesson-template.md)
- [ ] `exercises.md` — bài tập L1/L2/L3 theo [exercises-template](../../../docs/templates/exercises-template.md)
- [ ] `solutions.md` — đáp án theo [solutions-template](../../../docs/templates/solutions-template.md)
- [ ] `lab/` — code demo chạy được + `lab/README.md` theo [lab-template](../../../docs/templates/lab-template.md)
- [ ] _(người học)_ `my-notes.md` — **agent không tạo**

---

<!-- Nội dung bắt đầu từ đây -->
