# Infrastructure as Code (Terraform)

Khu vực thực hành tự động hóa hạ tầng (IaC) sử dụng HashiCorp Terraform và Cloud Providers (AWS).

---

## 🎯 Nội dung trọng tâm

1. **Terraform Core Fundamentals**:
   * HCL (HashiCorp Configuration Language), Providers, Resources, Data Sources.
   * Terraform lifecycle: `init`, `plan`, `apply`, `destroy`.
   * Quản lý State: Local state vs Remote state (AWS S3 + DynamoDB State Locking).
   * Variables, Outputs, Local values.

2. **Modular Architecture**:
   * Thiết kế reusable modules (VPC module, EKS module, RDS module).
   * Phân tách môi trường (Environments: dev, staging, prod) bằng Directory structure hoặc Workspaces.

3. **Production Best Practices**:
   * Quản lý Secret an toàn (AWS Secrets Manager, KMS, HashiCorp Vault).
   * Drift detection và Terraform CI/CD integration.
