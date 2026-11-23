# Project 3 — Internal Developer Platform (IDP)

> **Xây Dựng Nền Tảng Tự Phục Vụ (Self-Service) Cho Kỹ Sư Phần Mềm Trên Kubernetes**

---

## 🎯 Mục tiêu dự án
Xây dựng một nền tảng vận hành chuẩn hóa (Platform Engineering) giúp các nhóm phát triển phần mềm có thể tự đóng gói, triển khai, cấu hình và giám sát ứng dụng của mình mà không cần phụ thuộc thủ công vào đội ngũ Sysadmin/DevOps.

## 🛠️ Tech Stack
* **Container Orchestration:** Kubernetes (K3s / Minikube / EKS)
* **Packaging:** Helm Charts
* **GitOps Delivery:** ArgoCD
* **Ingress & Networking:** Traefik / NGINX Ingress Controller, cert-manager (Let's Encrypt TLS)
* **Observability Suite:** Prometheus, Grafana, Loki (Logs), OpenTelemetry / Tempo (Tracing)

---

## 📋 Kiến trúc & Tính năng cốt lõi

- [ ] **Standardized Microservice Helm Chart**: Tạo template Helm Chart chuẩn chung cho toàn bộ backend services (ConfigMap, Secret, Deployment, Service, Ingress, HPA, PodDisruptionBudget).
- [ ] **GitOps Delivery với ArgoCD**: Thiết lập mô hình declarative: mọi thay đổi cấu hình deployment được quản lý qua Git repo và ArgoCD tự động đồng bộ xuống cụm K8s.
- [ ] **Automated TLS & Ingress Management**: Tự động cấp phát và renew chứng chỉ SSL/TLS thông qua `cert-manager`.
- [ ] **Pre-configured Observability Stack**:
  - Dashboard Grafana chuẩn hóa cho JVM metrics, HTTP requests (RED metrics: Rate, Errors, Duration).
  - Centralized log aggregation với Grafana Loki.
  - Distributed tracing với OpenTelemetry.
- [ ] **Alerting & Notification Rules**: Tự động kích hoạt cảnh báo qua Telegram / Slack khi tỷ lệ lỗi 5xx tăng cao hoặc Pod bị CrashLoopBackOff.
