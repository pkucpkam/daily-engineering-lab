---
id: CTR-19
title: "Operators & CRD"
domain: 02-devops-and-cloud/containers-k8s
phase: Y3Q1
kind: lesson
status: todo
prerequisites: [CTR-18]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# CTR-19 · Operators & CRD

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 3 · Q1 |
| **Chủ đề** | Containers & Kubernetes |
| **Tiên quyết** | [CTR-18](../18-debugging-k8s/README.md) |

**Phạm vi bắt buộc**
- CRD
- controller pattern
- operator SDK/kubebuilder (hoặc Java Operator SDK)
- reconcile idempotent

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Viết controller khác viết script ở điểm nào?
- Reconcile loop phải idempotent vì sao?

**Ý tưởng lab / demo**
- Operator nhỏ tạo Deployment + Service từ CRD "App"

**Tài liệu gốc gợi ý**
- Docker docs
- Kubernetes docs
- Kubernetes in Action
- Kubernetes the Hard Way
- Learnk8s blog

**Gói đầu ra**
- [ ] `README.md` — lý thuyết theo [lesson-template](../../../docs/templates/lesson-template.md)
- [ ] `exercises.md` — bài tập L1/L2/L3 theo [exercises-template](../../../docs/templates/exercises-template.md)
- [ ] `solutions.md` — đáp án theo [solutions-template](../../../docs/templates/solutions-template.md)
- [ ] `lab/` — code demo chạy được + `lab/README.md` theo [lab-template](../../../docs/templates/lab-template.md)
- [ ] _(người học)_ `my-notes.md` — **agent không tạo**

---

<!-- Nội dung bắt đầu từ đây -->
