---
id: OBS-06
title: "Distributed tracing với OpenTelemetry"
domain: 02-devops-and-cloud/observability-sre
phase: Y2Q4
kind: lesson
status: todo
prerequisites: [OBS-05]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# OBS-06 · Distributed tracing với OpenTelemetry

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 2 · Q4 |
| **Chủ đề** | Observability & SRE |
| **Tiên quyết** | [OBS-05](../05-structured-logging-loki/README.md) |

**Phạm vi bắt buộc**
- span, trace, context propagation (W3C traceparent)
- auto vs manual instrumentation
- Java agent
- sampling
- Tempo/Jaeger
- trace qua Kafka

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Trace bị đứt giữa 2 service vì sao?
- Head sampling vs tail sampling?

**Ý tưởng lab / demo**
- Trace xuyên 3 service + Kafka với OTel Java agent

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
