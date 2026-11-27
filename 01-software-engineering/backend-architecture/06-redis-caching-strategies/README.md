---
id: ARCH-06
title: "Redis & caching strategies"
domain: 01-software-engineering/backend-architecture
phase: Y1Q3
kind: lesson
status: todo
prerequisites: [ARCH-05]
related_lessons: []
related_existing: [01-software-engineering/system-design/caching-layered-strategy.md]
last_updated: 2026-11-27
---

# ARCH-06 · Redis & caching strategies

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q3 |
| **Chủ đề** | Backend Architecture (Spring Boot & API) |
| **Tiên quyết** | [ARCH-05](../05-error-model-versioning/README.md) |
| **Đã có trong repo** | [caching-layered-strategy.md](../../system-design/caching-layered-strategy.md) — link tới, không viết lại |

**Phạm vi bắt buộc**
- Redis data structures, single-thread model
- cache-aside, read-through, write-through, write-behind
- TTL
- invalidation
- consistency giữa cache và DB

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Update DB rồi xóa cache hay xóa cache rồi update DB?
- Redis single-thread mà vẫn nhanh vì sao?

**Ý tưởng lab / demo**
- Cache-aside cho API đọc nhiều, đo hit-rate và latency

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
