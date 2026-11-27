---
id: ARCH-03
title: "@Transactional: proxy & cạm bẫy"
domain: 01-software-engineering/backend-architecture
phase: Y1Q3
kind: lesson
status: todo
prerequisites: [ARCH-02]
related_lessons: []
related_existing: [01-software-engineering/notes/transaction-boundary.md]
last_updated: 2026-11-27
---

# ARCH-03 · @Transactional: proxy & cạm bẫy

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q3 |
| **Chủ đề** | Backend Architecture (Spring Boot & API) |
| **Tiên quyết** | [ARCH-02](../02-bean-lifecycle-proxies/README.md) |
| **Đã có trong repo** | [transaction-boundary.md](../../notes/transaction-boundary.md) — link tới, không viết lại |

**Phạm vi bắt buộc**
- self-invocation
- propagation (REQUIRED, REQUIRES_NEW)
- rollbackFor checked exception
- readOnly
- transaction + gọi HTTP bên ngoài
- open-in-view

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Vì sao @Transactional gọi nội bộ không hoạt động?
- Gọi API bên ngoài trong transaction gây hậu quả gì?

**Ý tưởng lab / demo**
- Bộ test chứng minh từng cạm bẫy

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
