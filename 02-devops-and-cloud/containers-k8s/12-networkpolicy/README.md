---
id: CTR-12
title: "NetworkPolicy"
domain: 02-devops-and-cloud/containers-k8s
phase: Y2Q4
kind: lesson
status: todo
prerequisites: [CTR-11]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# CTR-12 · NetworkPolicy

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 2 · Q4 |
| **Chủ đề** | Containers & Kubernetes |
| **Tiên quyết** | [CTR-11](../11-services-ingress-cni/README.md) |

**Phạm vi bắt buộc**
- default deny
- ingress/egress rules
- CNI hỗ trợ
- zero-trust trong cluster

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Không có NetworkPolicy thì pod nào gọi được pod nào?
- Cách debug NetworkPolicy chặn nhầm?

**Ý tưởng lab / demo**
- Default deny + mở dần cho 3 service

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
