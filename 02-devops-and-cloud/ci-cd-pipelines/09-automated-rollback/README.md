---
id: CICD-09
title: "Automated rollback"
domain: 02-devops-and-cloud/ci-cd-pipelines
phase: Y2Q2
kind: lesson
status: todo
prerequisites: [CICD-08]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# CICD-09 · Automated rollback

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 2 · Q2 |
| **Chủ đề** | CI/CD & Delivery |
| **Tiên quyết** | [CICD-08](../08-feature-flags/README.md) |

**Phạm vi bắt buộc**
- health check, error rate, SLO làm tiêu chí
- analysis template
- rollback vs roll-forward

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Rollback tự động dựa vào metric nào?
- Khi nào roll-forward tốt hơn rollback?

**Ý tưởng lab / demo**
- Deploy bản lỗi có chủ đích, rollback tự động theo error rate

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
