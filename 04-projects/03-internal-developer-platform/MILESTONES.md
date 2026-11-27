# Project 3 — Milestones & Task Breakdown

> Nền tảng để developer tự deploy và quan sát service. Có thể chạy hoàn toàn local bằng kind/k3d để không tốn chi phí cloud.

## Milestone 1 — K8s + Argo CD + Observability stack (Năm 2 · Q3 → Năm 3 · Q1)
| # | Task | Bài học liên quan | Done khi |
| :-- | :--- | :--- | :--- |
| 1.1 | Cluster (kind/k3d hoặc EKS) bằng code | CTR-07, IAC-01 | Dựng lại cluster bằng 1 lệnh |
| 1.2 | Ingress + cert-manager (TLS tự động) | CTR-11 | Service mới có HTTPS tự động |
| 1.3 | Argo CD app-of-apps / ApplicationSet | CICD-11 | Thêm service = thêm 1 thư mục |
| 1.4 | Prometheus, Grafana, Loki, Tempo + OTel Collector | OBS-02, OBS-05, OBS-06 | 1 service có đủ metrics/logs/traces |
| 1.5 | NetworkPolicy default deny, RBAC theo team | CTR-12, CTR-14 | Team A không deploy được vào namespace B |
| 1.6 | Policy (Kyverno/OPA): bắt buộc requests/limits, image đã ký | CICD-06 | Manifest vi phạm bị từ chối |

## Milestone 2 — Self-service + Design doc (Năm 3 · Q1)
| # | Task | Bài học liên quan | Done khi |
| :-- | :--- | :--- | :--- |
| 2.1 | Golden path: template service (Helm chart chuẩn + CI + dashboard mặc định) | CTR-15, OBS-04 | Tạo service mới < 15 phút |
| 2.2 | Portal tối giản (Backstage hoặc CLI/GitHub template) | — | Dev không cần kubectl |
| 2.3 | Operator nhỏ cho CRD `App` (tùy chọn) | CTR-19 | `kubectl apply App` tạo đủ tài nguyên |
| 2.4 | Agent triage sự cố dùng MCP đọc metric (tùy chọn) | AI-09 | Demo triage 1 alert |
| 2.5 | Design doc đầy đủ (context, goals/non-goals, alternatives, rollout) | — | Doc trong `adr/` hoặc `docs/` của project |

## Definition of Done
- Demo: developer mới deploy service đầu tiên và xem dashboard của nó mà không cần hỏi ai.
