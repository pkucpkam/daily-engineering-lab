---
id: CTR-07
title: "Kiến trúc Kubernetes"
domain: 02-devops-and-cloud/containers-k8s
phase: Y2Q3
kind: lesson
status: todo
prerequisites: [CTR-06]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# CTR-07 · Kiến trúc Kubernetes

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 2 · Q3 |
| **Chủ đề** | Containers & Kubernetes |
| **Tiên quyết** | [CTR-06](../06-docker-compose-dev/README.md) |

**Phạm vi bắt buộc**
- API server, etcd, scheduler, controller manager, kubelet, kube-proxy
- reconciliation loop
- declarative

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Điều gì xảy ra từ kubectl apply đến khi container chạy?
- etcd chết thì cluster còn chạy không?

**Ý tưởng lab / demo**
- Dựng cluster bằng kind/k3d, quan sát sự kiện khi apply Deployment

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
