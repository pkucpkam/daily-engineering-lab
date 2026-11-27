---
id: OBS-12
title: "Capacity planning & load test quy mô lớn"
domain: 02-devops-and-cloud/observability-sre
phase: Y2Q4
kind: lesson
status: todo
prerequisites: [OBS-11]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# OBS-12 · Capacity planning & load test quy mô lớn

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 2 · Q4 |
| **Chủ đề** | Observability & SRE |
| **Tiên quyết** | [OBS-11](../11-chaos-engineering/README.md) |

**Phạm vi bắt buộc**
- dự báo tải
- headroom
- load test trên môi trường giống prod
- kết hợp Little's Law

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Cần bao nhiêu pod cho 5.000 req/s?
- Load test trên staging nhỏ hơn prod có ý nghĩa không?

**Ý tưởng lab / demo**
- Kế hoạch capacity cho Project 1 dựa trên số đo

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
