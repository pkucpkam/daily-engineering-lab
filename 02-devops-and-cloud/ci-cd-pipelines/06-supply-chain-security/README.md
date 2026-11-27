---
id: CICD-06
title: "Supply chain: SBOM, image signing"
domain: 02-devops-and-cloud/ci-cd-pipelines
phase: Y2Q4
kind: lesson
status: todo
prerequisites: [CICD-05]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# CICD-06 · Supply chain: SBOM, image signing

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 2 · Q4 |
| **Chủ đề** | CI/CD & Delivery |
| **Tiên quyết** | [CICD-05](../05-quality-gates/README.md) |

**Phạm vi bắt buộc**
- SBOM (Syft)
- ký image (cosign)
- SLSA
- verify khi deploy (policy)

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- SBOM dùng để làm gì khi có CVE mới?
- Ký image ngăn được tấn công gì?

**Ý tưởng lab / demo**
- Sinh SBOM + ký image + verify bằng Kyverno

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
