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

---

<!-- CURRICULUM:START — sinh tự động từ CURRICULUM, cột Status cập nhật tay -->
## 📚 Danh sách bài

Loại: **Bài học** · Template: [lesson-template.md](../../docs/templates/lesson-template.md) · Quy chuẩn: [CONTENT-GUIDE](../../docs/CONTENT-GUIDE.md)

| ID | Bài | Giai đoạn | Status |
| :--- | :--- | :--- | :---: |
| IAC-01 | [Terraform core: provider, resource, lifecycle](01-terraform-core/README.md) | Năm 2 · Q1 | 🔲 |
| IAC-02 | [State, remote backend & locking](02-state-backend-locking/README.md) | Năm 2 · Q1 | 🔲 |
| IAC-03 | [Thiết kế module](03-modules-design/README.md) | Năm 2 · Q1 | 🔲 |
| IAC-04 | [Tổ chức multi-environment](04-multi-env-layout/README.md) | Năm 2 · Q1 | 🔲 |
| IAC-05 | [Drift, import & refactor](05-drift-import-refactor/README.md) | Năm 2 · Q1 | 🔲 |
| IAC-06 | [Ansible fundamentals](06-ansible-fundamentals/README.md) | Năm 2 · Q1 | 🔲 |
| IAC-07 | [Provisioning vs Configuration management vs Immutable image](07-provisioning-vs-config/README.md) | Năm 2 · Q1 | 🔲 |
| IAC-08 | [Secrets: Vault & AWS Secrets Manager](08-secrets-vault/README.md) | Năm 2 · Q1 | 🔲 |
| IAC-09 | [Kiến trúc tham chiếu AWS Multi-AZ bằng Terraform](09-aws-multi-az-architecture/README.md) | Năm 2 · Q1 | 🔲 |

**Tài liệu gốc của chủ đề:** Terraform: Up & Running · developer.hashicorp.com/terraform · Ansible docs · HashiCorp Vault docs

<!-- CURRICULUM:END -->
