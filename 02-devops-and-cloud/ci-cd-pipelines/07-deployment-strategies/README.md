---
id: CICD-07
title: "Deployment strategies"
domain: 02-devops-and-cloud/ci-cd-pipelines
phase: Y2Q2
kind: lesson
status: todo
prerequisites: [CICD-06]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# CICD-07 · Deployment strategies

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 2 · Q2 |
| **Chủ đề** | CI/CD & Delivery |
| **Tiên quyết** | [CICD-06](../06-supply-chain-security/README.md) |

**Phạm vi bắt buộc**
- recreate, rolling, blue-green, canary
- traffic shifting
- tương thích ngược

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Canary cần những điều kiện gì về observability?
- Blue-green với DB dùng chung khó ở đâu?

**Ý tưởng lab / demo**
- Canary với Argo Rollouts hoặc ALB weighted

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
