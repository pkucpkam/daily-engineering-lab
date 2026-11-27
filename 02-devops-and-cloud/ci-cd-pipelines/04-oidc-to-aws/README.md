---
id: CICD-04
title: "OIDC từ CI tới AWS"
domain: 02-devops-and-cloud/ci-cd-pipelines
phase: Y2Q2
kind: lesson
status: todo
prerequisites: [CICD-03]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# CICD-04 · OIDC từ CI tới AWS

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 2 · Q2 |
| **Chủ đề** | CI/CD & Delivery |
| **Tiên quyết** | [CICD-03](../03-gha-advanced/README.md) |

**Phạm vi bắt buộc**
- vì sao không dùng access key dài hạn
- OIDC trust
- giới hạn theo repo/branch

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Rủi ro của access key trong secrets CI?
- Giới hạn chỉ branch main được deploy prod thế nào?

**Ý tưởng lab / demo**
- GitHub Actions assume role AWS bằng OIDC

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
