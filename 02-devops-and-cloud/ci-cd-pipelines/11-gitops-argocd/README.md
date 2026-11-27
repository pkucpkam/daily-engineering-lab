---
id: CICD-11
title: "GitOps với Argo CD"
domain: 02-devops-and-cloud/ci-cd-pipelines
phase: Y2Q3
kind: lesson
status: todo
prerequisites: [CICD-10]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# CICD-11 · GitOps với Argo CD

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 2 · Q3 |
| **Chủ đề** | CI/CD & Delivery |
| **Tiên quyết** | [CICD-10](../10-db-migration-expand-contract/README.md) |

**Phạm vi bắt buộc**
- nguyên lý GitOps
- Application, sync policy, self-heal, prune
- ApplicationSet
- app-of-apps
- promote giữa môi trường

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Ai đó kubectl edit trực tiếp thì Argo CD làm gì?
- Lưu secret trong GitOps thế nào?

**Ý tưởng lab / demo**
- Argo CD quản lý Project 1 trên kind

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
