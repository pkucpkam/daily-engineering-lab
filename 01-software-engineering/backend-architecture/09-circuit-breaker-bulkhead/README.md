---
id: ARCH-09
title: "Circuit Breaker, Bulkhead, Rate limiter (Resilience4j)"
domain: 01-software-engineering/backend-architecture
phase: Y1Q3
kind: lesson
status: todo
prerequisites: [ARCH-08]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# ARCH-09 · Circuit Breaker, Bulkhead, Rate limiter (Resilience4j)

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q3 |
| **Chủ đề** | Backend Architecture (Spring Boot & API) |
| **Tiên quyết** | [ARCH-08](../08-timeout-retry-backoff/README.md) |

**Phạm vi bắt buộc**
- trạng thái closed/open/half-open
- sliding window
- bulkhead thread-pool vs semaphore
- fallback
- graceful degradation

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Circuit breaker cấu hình sai gây flapping thế nào?
- Bulkhead cứu hệ thống khỏi cascading failure ra sao?

**Ý tưởng lab / demo**
- Service phụ thuộc chậm, so sánh có/không circuit breaker + bulkhead

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
