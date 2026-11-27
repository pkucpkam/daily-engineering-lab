---
id: ARCH-05
title: "Error model, validation & versioning"
domain: 01-software-engineering/backend-architecture
phase: Y1Q3
kind: lesson
status: todo
prerequisites: [ARCH-04]
related_lessons: []
related_existing: [01-software-engineering/concept-decisions/validate-client-vs-server.md]
last_updated: 2026-11-27
---

# ARCH-05 · Error model, validation & versioning

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q3 |
| **Chủ đề** | Backend Architecture (Spring Boot & API) |
| **Tiên quyết** | [ARCH-04](../04-rest-api-design/README.md) |
| **Đã có trong repo** | [validate-client-vs-server.md](../../concept-decisions/validate-client-vs-server.md) — link tới, không viết lại |

**Phạm vi bắt buộc**
- RFC 9457 Problem Details
- validation lỗi theo field
- versioning (URI, header)
- backward compatibility
- contract-first (OpenAPI)

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Breaking change là gì và tránh thế nào?
- Error response tốt cần những gì để client xử lý?

**Ý tưởng lab / demo**
- ControllerAdvice trả Problem Details + OpenAPI spec

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
