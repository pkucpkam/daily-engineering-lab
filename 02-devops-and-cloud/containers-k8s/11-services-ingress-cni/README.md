---
id: CTR-11
title: "Service, Ingress & CNI"
domain: 02-devops-and-cloud/containers-k8s
phase: Y2Q3
kind: lesson
status: todo
prerequisites: [CTR-10]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# CTR-11 · Service, Ingress & CNI

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 2 · Q3 |
| **Chủ đề** | Containers & Kubernetes |
| **Tiên quyết** | [CTR-10](../10-requests-limits-qos/README.md) |

**Phạm vi bắt buộc**
- ClusterIP/NodePort/LoadBalancer
- kube-proxy iptables/IPVS
- DNS nội bộ
- Ingress controller (Nginx)
- CNI (Calico, Cilium)

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Request từ ngoài vào pod đi qua những bước nào?
- Headless service dùng làm gì?

**Ý tưởng lab / demo**
- Ingress Nginx + TLS cho 2 service

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
