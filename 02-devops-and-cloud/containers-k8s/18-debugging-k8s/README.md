---
id: CTR-18
title: "Debug Kubernetes"
domain: 02-devops-and-cloud/containers-k8s
phase: Y2Q3
kind: lesson
status: todo
prerequisites: [CTR-17]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# CTR-18 · Debug Kubernetes

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 2 · Q3 |
| **Chủ đề** | Containers & Kubernetes |
| **Tiên quyết** | [CTR-17](../17-hpa-autoscaling/README.md) |

**Phạm vi bắt buộc**
- CrashLoopBackOff, Pending, ImagePullBackOff, OOMKilled
- kubectl describe/logs/events
- kubectl debug ephemeral container
- DNS issues

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Pod Pending mãi, các bước điều tra?
- Debug container distroless không có shell?

**Ý tưởng lab / demo**
- Bộ 6 manifest hỏng có chủ đích, sửa từng cái

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
