# Project 1 — Milestones & Task Breakdown

> Bối cảnh nghiệp vụ đề xuất: **hệ thống đặt hàng có giới hạn tồn kho (flash sale)** — đủ để luyện concurrency, cache, outbox, saga. Người học có thể đổi domain nhưng giữ nguyên yêu cầu kỹ thuật.
> Mỗi task có ID bài học liên quan — học bài trước, làm task sau.

## Milestone 1 — API + PostgreSQL + Redis + CI (Năm 1 · Q3)
| # | Task | Bài học liên quan | Done khi |
| :-- | :--- | :--- | :--- |
| 1.1 | Khởi tạo Spring Boot 3 + Java 21, cấu trúc modular monolith (order, inventory, payment) | ARCH-01, ARCH-10, ARCH-11 | Test kiến trúc (boundary) pass |
| 1.2 | Schema + Flyway migration; Testcontainers cho repository test | CQ-08, CICD-10 | `mvn verify` xanh |
| 1.3 | API tạo đơn có idempotency key, error model Problem Details, cursor pagination | ARCH-04, ARCH-05 | Gửi trùng request không tạo 2 đơn |
| 1.4 | Trừ tồn kho đúng dưới tranh chấp — so sánh optimistic / pessimistic / Redis lock | DB-12, DIST-14, CD-01 | Benchmark 3 chiến lược có số |
| 1.5 | Cache-aside cho danh mục sản phẩm, chống stampede | ARCH-06, ARCH-07 | Hit-rate & p99 trước/sau |
| 1.6 | Rate limiter Redis + Lua | SD-03, PAT-20 | k6 chứng minh giới hạn đúng |
| 1.7 | Docker Compose (API + PG + Redis), Dockerfile tối ưu | CTR-03, CTR-04, CTR-06 | Image < 200MB, `docker compose up` chạy |
| 1.8 | CI GitHub Actions: build → test → image | CICD-01 | Badge xanh |
| 1.9 | Load test baseline + bảng số đo trong README | PERF-02, PERF-05 | Bảng throughput/p99 |

## Milestone 2 — Kafka + Outbox + Saga + Fault scenarios (Năm 1 · Q4)
| # | Task | Bài học liên quan | Done khi |
| :-- | :--- | :--- | :--- |
| 2.1 | Kafka (KRaft) trong Compose, topic design, partition key | DIST-08, DIST-09 | ADR về partition key |
| 2.2 | Transactional outbox (polling publisher), có thể nâng lên Debezium | DIST-11 | Kill app giữa chừng không mất event |
| 2.3 | Saga orchestration Order → Inventory → Payment + compensation | DIST-12, CD-06 | Test lỗi ở mỗi bước |
| 2.4 | Idempotent consumer + DLQ | DIST-10, PAT-17 | Gửi trùng message không trừ tiền 2 lần |
| 2.5 | Resilience: timeout, retry+jitter, circuit breaker, bulkhead | ARCH-08, ARCH-09 | Downstream chậm không kéo sập service |
| 2.6 | Fault scenarios: kill consumer, DB chậm (Toxiproxy), broker down | DIST-01, INC-14, INC-15 | Báo cáo kịch bản + số đo |
| 2.7 | OpenTelemetry trace xuyên HTTP + Kafka | OBS-06 | Trace đầy đủ 1 đơn hàng |

## Milestone 3 — Kubernetes + GitOps + SLO (Năm 2 · Q3–Q4)
| # | Task | Bài học liên quan | Done khi |
| :-- | :--- | :--- | :--- |
| 3.1 | Helm chart, probes, requests/limits | CTR-08, CTR-10, CTR-15 | Rolling update không lỗi request |
| 3.2 | Argo CD quản lý deploy | CICD-11 | Đổi Git → cluster tự cập nhật |
| 3.3 | Prometheus + Grafana RED dashboard | OBS-02, OBS-04 | Dashboard as code |
| 3.4 | SLO + burn-rate alert + runbook | OBS-07, OBS-08, OBS-09 | Alert kích hoạt đúng trong game day |
| 3.5 | Game day + post-mortem | OBS-11, OBS-10 | 1 post-mortem trong `03-production-troubleshooting/post-mortems/` |

## Definition of Done của project
- README có: sơ đồ kiến trúc, cách chạy 1 lệnh, bảng số đo, danh sách ADR, trade-off chính, chi phí vận hành ước tính.
