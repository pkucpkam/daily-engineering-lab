---
id: CTR-03
title: "Dockerfile: multi-stage, distroless, jlink, non-root"
domain: 02-devops-and-cloud/containers-k8s
phase: Y1Q3
kind: lesson
status: todo
prerequisites: [CTR-02]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# CTR-03 · Dockerfile: multi-stage, distroless, jlink, non-root

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q3 |
| **Chủ đề** | Containers & Kubernetes |
| **Tiên quyết** | [CTR-02](../02-overlayfs-image-layers/README.md) |

**Phạm vi bắt buộc**
- multi-stage build
- base image (distroless, alpine, ubi)
- jlink custom runtime
- non-root user
- .dockerignore
- layered jar Spring Boot

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Image Java 600MB giảm xuống thế nào?
- Alpine + Java có vấn đề gì (musl)?

**Ý tưởng lab / demo**
- Tối ưu image Spring Boot từ ~500MB xuống < 150MB, đo kích thước và thời gian build

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
