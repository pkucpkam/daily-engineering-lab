---
id: CTR-08
title: "Pod lifecycle & probes"
domain: 02-devops-and-cloud/containers-k8s
phase: Y2Q3
kind: lesson
status: todo
prerequisites: [CTR-07]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# CTR-08 · Pod lifecycle & probes

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 2 · Q3 |
| **Chủ đề** | Containers & Kubernetes |
| **Tiên quyết** | [CTR-07](../07-k8s-architecture/README.md) |

**Phạm vi bắt buộc**
- phases
- init container
- liveness/readiness/startup probe
- graceful termination (preStop, terminationGracePeriod)

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Liveness probe sai có thể gây sự cố dây chuyền thế nào?
- Vì sao request lỗi khi rolling update dù có readiness?

**Ý tưởng lab / demo**
- App Spring Boot với probe đúng/sai, quan sát khi rollout

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
