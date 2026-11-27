# CI/CD & Delivery Pipelines

Khu vực thiết kế quy trình tích hợp liên tục (Continuous Integration) và phân phối liên tục (Continuous Delivery / Deployment).

---

## 🎯 Nội dung trọng tâm

1. **Continuous Integration (CI)**:
   * Automated linting, code formatting, static code analysis (SonarQube).
   * Unit test & Integration test execution với database testcontainers.
   * Container image build, vulnerability scanning (Trivy), push lên Container Registry (ECR / DockerHub).

2. **Continuous Delivery (CD)**:
   * Phân biệt Continuous Delivery vs Continuous Deployment.
   * Deployment Strategies: Recreate, Rolling Update, Blue/Green, Canary.
   * Automated rollback khi health check thất bại.

3. **GitOps with ArgoCD**:
   * Nguyên lý GitOps: Git là Single Source of Truth cho trạng thái cụm K8s.
   * Sync policies, automated self-healing, application sets.

---

<!-- CURRICULUM:START — sinh tự động từ CURRICULUM, cột Status cập nhật tay -->
## 📚 Danh sách bài

Loại: **Bài học** · Template: [lesson-template.md](../../docs/templates/lesson-template.md) · Quy chuẩn: [CONTENT-GUIDE](../../docs/CONTENT-GUIDE.md)

| ID | Bài | Giai đoạn | Status |
| :--- | :--- | :--- | :---: |
| CICD-01 | [CI cơ bản với GitHub Actions](01-ci-github-actions/README.md) | Năm 1 · Q3 | 🔲 |
| CICD-02 | [Jenkins pipeline-as-code](02-jenkins-pipeline/README.md) | Năm 1 · Q3 | 🔲 |
| CICD-03 | [GitHub Actions nâng cao](03-gha-advanced/README.md) | Năm 2 · Q2 | 🔲 |
| CICD-04 | [OIDC từ CI tới AWS](04-oidc-to-aws/README.md) | Năm 2 · Q2 | 🔲 |
| CICD-05 | [Quality gates: lint, SAST, dependency scan](05-quality-gates/README.md) | Năm 2 · Q2 | 🔲 |
| CICD-06 | [Supply chain: SBOM, image signing](06-supply-chain-security/README.md) | Năm 2 · Q4 | 🔲 |
| CICD-07 | [Deployment strategies](07-deployment-strategies/README.md) | Năm 2 · Q2 | 🔲 |
| CICD-08 | [Feature flags](08-feature-flags/README.md) | Năm 2 · Q2 | 🔲 |
| CICD-09 | [Automated rollback](09-automated-rollback/README.md) | Năm 2 · Q2 | 🔲 |
| CICD-10 | [Database migration an toàn (Flyway, expand-contract)](10-db-migration-expand-contract/README.md) | Năm 2 · Q2 | 🔲 |
| CICD-11 | [GitOps với Argo CD](11-gitops-argocd/README.md) | Năm 2 · Q3 | 🔲 |

**Tài liệu gốc của chủ đề:** Continuous Delivery (Humble, Farley) · GitHub Actions docs · Jenkins docs · Argo CD docs · DORA research (dora.dev)

<!-- CURRICULUM:END -->
