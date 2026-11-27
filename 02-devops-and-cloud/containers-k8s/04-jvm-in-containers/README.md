---
id: CTR-04
title: "JVM trong container"
domain: 02-devops-and-cloud/containers-k8s
phase: Y1Q3
kind: lesson
status: todo
prerequisites: [CTR-03]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# CTR-04 · JVM trong container

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q3 |
| **Chủ đề** | Containers & Kubernetes |
| **Tiên quyết** | [CTR-03](../03-dockerfile-best-practices/README.md) |

**Phạm vi bắt buộc**
- container awareness
- MaxRAMPercentage
- CPU quota và số GC thread
- OOMKilled vs OutOfMemoryError
- native memory ngoài heap

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Container OOMKilled dù heap chỉ dùng 60% vì sao?
- Giới hạn CPU 0.5 ảnh hưởng JVM thế nào?

**Ý tưởng lab / demo**
- Chạy JVM với memory limit, tái hiện OOMKilled do native memory

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
