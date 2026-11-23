# Project 2 — Infrastructure Automation

> **Tự Động Hóa Toàn Diện Hạ Tầng Đám Mây & Quy Trình Triển Khai Không Gián Đoạn**

---

## 🎯 Mục tiêu dự án
Tự động hóa hoàn toàn việc cấp phát hạ tầng đám mây dạng mã (IaC), kiểm thử bảo mật, đóng gói container và triển khai ứng dụng với cơ chế tự động rollback khi có sự cố.

## 🛠️ Tech Stack
* **Cloud Provider:** AWS (VPC, ECS / EKS, RDS PostgreSQL, S3, CloudFront)
* **Infrastructure as Code:** HashiCorp Terraform
* **Container:** Docker (Multi-stage build, distroless / alpine base)
* **CI/CD Orchestration:** GitHub Actions
* **Security & Quality:** Trivy (container scan), tfsec / checkov (IaC security scan)

---

## 📋 Kiến trúc & Tính năng cốt lõi

- [ ] **Modular Terraform Architecture**: Thiết kế modules độc lập (VPC network, Security Groups, Database, Compute) dễ dàng tái sử dụng.
- [ ] **Multi-Environment State Management**: Tách biệt môi trường `dev`, `staging`, `production` với remote state lưu trữ trên S3 và DynamoDB lock table.
- [ ] **Automated CI Pipeline**:
  - Code lint & unit tests.
  - Vulnerability scan cho source code & dependencies.
  - Multi-stage Docker build tối ưu dung lượng và scan CVE bằng Trivy.
  - Tự động tag phiên bản theo Semantic Versioning (`vX.Y.Z`).
- [ ] **Automated Zero-Downtime CD Pipeline**:
  - Blue/Green hoặc Rolling Update deployment.
  - Tích hợp synthetic smoke tests sau khi deploy vào staging/canary.
  - Tự động trigger rollback về phiên bản trước nếu health check hoặc error rate vượt ngưỡng cho phép trong 3 phút đầu.
