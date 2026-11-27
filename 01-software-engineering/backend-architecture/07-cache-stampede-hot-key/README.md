---
id: ARCH-07
title: "Cache stampede, hot key, penetration, avalanche"
domain: 01-software-engineering/backend-architecture
phase: Y1Q3
kind: lesson
status: todo
prerequisites: [ARCH-06]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# ARCH-07 · Cache stampede, hot key, penetration, avalanche

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q3 |
| **Chủ đề** | Backend Architecture (Spring Boot & API) |
| **Tiên quyết** | [ARCH-06](../06-redis-caching-strategies/README.md) |

**Phạm vi bắt buộc**
- thundering herd
- mutex/single-flight
- probabilistic early expiration
- hot key
- cache penetration (bloom filter, cache null)
- avalanche (TTL jitter)

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- 1 key hết hạn đúng lúc 5.000 req/s thì sao?
- Bloom filter chống penetration thế nào?

**Ý tưởng lab / demo**
- Tái hiện stampede bằng k6, sửa bằng single-flight + jitter

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
