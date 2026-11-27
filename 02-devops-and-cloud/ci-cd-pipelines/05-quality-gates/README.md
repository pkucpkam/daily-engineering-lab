---
id: CICD-05
title: "Quality gates: lint, SAST, dependency scan"
domain: 02-devops-and-cloud/ci-cd-pipelines
phase: Y2Q2
kind: lesson
status: todo
prerequisites: [CICD-04]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# CICD-05 · Quality gates: lint, SAST, dependency scan

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 2 · Q2 |
| **Chủ đề** | CI/CD & Delivery |
| **Tiên quyết** | [CICD-04](../04-oidc-to-aws/README.md) |

**Phạm vi bắt buộc**
- Checkstyle/Spotless
- SonarQube
- OWASP dependency-check/Trivy
- fail build theo ngưỡng

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Đặt ngưỡng chặn build thế nào để không bị bỏ qua?
- SAST cho nhiều false positive xử lý ra sao?

**Ý tưởng lab / demo**
- Pipeline có lint + test + Trivy scan

**Tài liệu gốc gợi ý**
- Continuous Delivery (Humble, Farley)
- GitHub Actions docs
- Jenkins docs
- Argo CD docs
- DORA research (dora.dev)

**Gói đầu ra**
- [ ] `README.md` — lý thuyết theo [lesson-template](../../../docs/templates/lesson-template.md)
- [ ] `exercises.md` — bài tập L1/L2/L3 theo [exercises-template](../../../docs/templates/exercises-template.md)
- [ ] `solutions.md` — đáp án theo [solutions-template](../../../docs/templates/solutions-template.md)
- [ ] `lab/` — code demo chạy được + `lab/README.md` theo [lab-template](../../../docs/templates/lab-template.md)
- [ ] _(người học)_ `my-notes.md` — **agent không tạo**

---

<!-- Nội dung bắt đầu từ đây -->
