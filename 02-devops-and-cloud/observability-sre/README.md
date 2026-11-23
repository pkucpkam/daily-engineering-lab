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
