---
id: ARCH-04
title: "REST API design"
domain: 01-software-engineering/backend-architecture
phase: Y1Q3
kind: lesson
status: todo
prerequisites: [ARCH-03]
related_lessons: []
related_existing: [01-software-engineering/notes/idempotency.md, 01-software-engineering/concept-decisions/pagination-cursor-vs-offset.md]
last_updated: 2026-11-27
---

# ARCH-04 · REST API design

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q3 |
| **Chủ đề** | Backend Architecture (Spring Boot & API) |
| **Tiên quyết** | [ARCH-03](../03-transactional-pitfalls/README.md) |
| **Đã có trong repo** | [idempotency.md](../../notes/idempotency.md)<br>[pagination-cursor-vs-offset.md](../../concept-decisions/pagination-cursor-vs-offset.md) — link tới, không viết lại |

**Phạm vi bắt buộc**
- resource modeling
- HTTP semantics, status codes
- idempotency key
- pagination
- filtering
- HATEOAS (biết là đủ)

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- PUT vs PATCH vs POST khi nào?
- Thiết kế API tạo đơn hàng chịu retry?

**Ý tưởng lab / demo**
- API orders có idempotency key + cursor pagination

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
