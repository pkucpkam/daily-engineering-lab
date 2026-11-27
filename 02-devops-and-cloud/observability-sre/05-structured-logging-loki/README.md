---
id: OBS-05
title: "Structured logging & Loki"
domain: 02-devops-and-cloud/observability-sre
phase: Y2Q4
kind: lesson
status: todo
prerequisites: [OBS-04]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# OBS-05 · Structured logging & Loki

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 2 · Q4 |
| **Chủ đề** | Observability & SRE |
| **Tiên quyết** | [OBS-04](../04-grafana-red-use/README.md) |

**Phạm vi bắt buộc**
- JSON log
- correlation/trace id trong MDC
- log level
- không log dữ liệu nhạy cảm
- Loki labels & LogQL

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Vì sao log text tự do khó điều tra?
- Label Loki khác field trong log thế nào?

**Ý tưởng lab / demo**
- Logback JSON + Loki + truy vấn theo trace id

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
