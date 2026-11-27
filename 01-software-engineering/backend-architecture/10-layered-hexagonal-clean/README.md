---
id: ARCH-10
title: "Layered, Hexagonal, Clean Architecture"
domain: 01-software-engineering/backend-architecture
phase: Y1Q3
kind: lesson
status: todo
prerequisites: [ARCH-09]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# ARCH-10 · Layered, Hexagonal, Clean Architecture

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q3 |
| **Chủ đề** | Backend Architecture (Spring Boot & API) |
| **Tiên quyết** | [ARCH-09](../09-circuit-breaker-bulkhead/README.md) |

**Phạm vi bắt buộc**
- layered và nhược điểm
- ports & adapters
- dependency rule
- domain model vs anemic model
- khi nào kiến trúc này là quá mức

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Hexagonal giúp test dễ hơn ra sao?
- Khi nào layered là đủ?

**Ý tưởng lab / demo**
- Cùng 1 use case viết theo layered và hexagonal, so sánh

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
