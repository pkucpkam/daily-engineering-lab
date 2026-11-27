# Observability & Site Reliability Engineering (SRE)

Khu vực nghiên cứu và triển khai giám sát hệ thống, đo lường độ tin cậy và tracing phân tán.

---

## 🎯 Nội dung trọng tâm

1. **Ba trụ cột Observability (Three Pillars)**:
   * **Metrics**: Định lượng trạng thái hệ thống theo thời gian (Prometheus, Grafana).
   * **Logs**: Ghi nhận sự kiện chi tiết (Loki, Promtail, ELK stack).
   * **Traces**: Theo dõi hành trình request xuyên qua các microservices (OpenTelemetry, Jaeger, Tempo).

2. **Khung phương pháp đo lường**:
   * **USE Method** (Utilization, Saturation, Errors) cho tài nguyên hạ tầng.
   * **RED Method** (Rate, Errors, Duration) cho service/API endpoints.
   * **Google 4 Golden Signals**: Latency, Traffic, Errors, Saturation.

3. **SRE Practices**:
   * Định nghĩa SLI (Service Level Indicator), SLO (Service Level Objective), SLA (Service Level Agreement).
   * Error Budgets và quy trình đưa ra quyết định release.
   * Alertmanager rules thiết kế đúng mức độ ưu tiên (chống alert fatigue).

---

<!-- CURRICULUM:START — sinh tự động từ CURRICULUM, cột Status cập nhật tay -->
## 📚 Danh sách bài

Loại: **Bài học** · Template: [lesson-template.md](../../docs/templates/lesson-template.md) · Quy chuẩn: [CONTENT-GUIDE](../../docs/CONTENT-GUIDE.md)

| ID | Bài | Giai đoạn | Status |
| :--- | :--- | :--- | :---: |
| OBS-01 | [Observability vs Monitoring & 3 trụ cột](01-three-pillars/README.md) | Năm 2 · Q4 | 🔲 |
| OBS-02 | [Prometheus: pull model & PromQL](02-prometheus-promql/README.md) | Năm 2 · Q4 | 🔲 |
| OBS-03 | [Cardinality & chi phí metric](03-cardinality/README.md) | Năm 2 · Q4 | 🔲 |
| OBS-04 | [Grafana dashboards: RED & USE](04-grafana-red-use/README.md) | Năm 2 · Q4 | 🔲 |
| OBS-05 | [Structured logging & Loki](05-structured-logging-loki/README.md) | Năm 2 · Q4 | 🔲 |
| OBS-06 | [Distributed tracing với OpenTelemetry](06-opentelemetry-tracing/README.md) | Năm 2 · Q4 | 🔲 |
| OBS-07 | [SLI, SLO, SLA & Error budget](07-sli-slo-error-budget/README.md) | Năm 2 · Q4 | 🔲 |
| OBS-08 | [Alerting theo triệu chứng & burn rate](08-symptom-based-alerting/README.md) | Năm 2 · Q4 | 🔲 |
| OBS-09 | [Runbook & on-call](09-runbooks-oncall/README.md) | Năm 2 · Q4 | 🔲 |
| OBS-10 | [Incident management & blameless post-mortem](10-incident-management/README.md) | Năm 2 · Q4 | 🔲 |
| OBS-11 | [Chaos engineering & game day](11-chaos-engineering/README.md) | Năm 2 · Q4 | 🔲 |
| OBS-12 | [Capacity planning & load test quy mô lớn](12-capacity-planning/README.md) | Năm 2 · Q4 | 🔲 |

**Tài liệu gốc của chủ đề:** Site Reliability Engineering & SRE Workbook (sre.google) · Observability Engineering · Prometheus docs · OpenTelemetry docs

<!-- CURRICULUM:END -->
