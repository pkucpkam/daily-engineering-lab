---
id: CTR-05
title: "Docker networking"
domain: 02-devops-and-cloud/containers-k8s
phase: Y1Q3
kind: lesson
status: todo
prerequisites: [CTR-04]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# CTR-05 · Docker networking

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q3 |
| **Chủ đề** | Containers & Kubernetes |
| **Tiên quyết** | [CTR-04](../04-jvm-in-containers/README.md) |

**Phạm vi bắt buộc**
- bridge, host, none, overlay
- DNS nội bộ Compose
- publish port
- network giữa các compose project

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Container A gọi container B qua localhost được không?
- Overlay network dùng khi nào?

**Ý tưởng lab / demo**
- Compose 3 service, debug kết nối giữa chúng

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
