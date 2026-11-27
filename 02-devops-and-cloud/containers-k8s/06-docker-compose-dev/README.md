---
id: CTR-06
title: "Docker Compose cho môi trường dev"
domain: 02-devops-and-cloud/containers-k8s
phase: Y1Q3
kind: lesson
status: todo
prerequisites: [CTR-05]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# CTR-06 · Docker Compose cho môi trường dev

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q3 |
| **Chủ đề** | Containers & Kubernetes |
| **Tiên quyết** | [CTR-05](../05-docker-networking/README.md) |

**Phạm vi bắt buộc**
- services, volumes, healthcheck, depends_on condition
- profiles
- env file
- Compose cho Project 1

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- depends_on có đợi DB sẵn sàng không?
- Quản lý dữ liệu volume khi reset môi trường?

**Ý tưởng lab / demo**
- Compose API + PostgreSQL + Redis + Kafka có healthcheck

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
