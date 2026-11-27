---
id: ARCH-11
title: "Modular monolith vs Microservices"
domain: 01-software-engineering/backend-architecture
phase: Y1Q3
kind: lesson
status: todo
prerequisites: [ARCH-10]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# ARCH-11 · Modular monolith vs Microservices

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q3 |
| **Chủ đề** | Backend Architecture (Spring Boot & API) |
| **Tiên quyết** | [ARCH-10](../10-layered-hexagonal-clean/README.md) |

**Phạm vi bắt buộc**
- module boundaries (bounded context)
- Spring Modulith
- chi phí microservices (network, data, ops)
- khi nào tách service
- distributed monolith

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Dấu hiệu nào cho thấy nên tách service?
- Distributed monolith là gì?

**Ý tưởng lab / demo**
- Modular monolith với Spring Modulith, kiểm tra vi phạm boundary bằng test

**Tài liệu gốc gợi ý**
- Spring Framework Reference
- Release It!
- Microsoft REST API Guidelines
- Resilience4j docs

**Gói đầu ra**
- [ ] `README.md` — lý thuyết theo [lesson-template](../../../docs/templates/lesson-template.md)
- [ ] `exercises.md` — bài tập L1/L2/L3 theo [exercises-template](../../../docs/templates/exercises-template.md)
- [ ] `solutions.md` — đáp án theo [solutions-template](../../../docs/templates/solutions-template.md)
- [ ] `lab/` — code demo chạy được + `lab/README.md` theo [lab-template](../../../docs/templates/lab-template.md)
- [ ] _(người học)_ `my-notes.md` — **agent không tạo**

---

<!-- Nội dung bắt đầu từ đây -->
