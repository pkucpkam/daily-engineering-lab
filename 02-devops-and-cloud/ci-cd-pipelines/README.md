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
