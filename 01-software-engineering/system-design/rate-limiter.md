---
id: SD-03
title: "Rate Limiter"
domain: 01-software-engineering/system-design
phase: Y1Q4
kind: design
status: todo
prerequisites: []
related_lessons: [PAT-20]
related_existing: []
last_updated: 2026-11-27
---

# SD-03 · Rate Limiter

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](templates/system-design-template.md). Danh mục: [CURRICULUM](../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | System design |
| **Giai đoạn** | Năm 1 · Q4 |
| **Chủ đề** | System Design |
| **Bài học liên quan** | [PAT-20](../patterns/rate-limiter.md) |

**Phạm vi bắt buộc**
- token bucket, leaky bucket, fixed/sliding window
- distributed bằng Redis + Lua
- multi-region
- header trả về

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Sliding window log vs counter?
- Rate limit phân tán chính xác đến đâu?

**Ý tưởng lab / demo**
- Rate limiter Redis + Lua, test bằng k6

**Tài liệu gốc gợi ý**
- System Design Interview (Alex Xu) vol 1-2
- DDIA
- ByteByteGo
- High Scalability blog

**Gói đầu ra**
- [ ] Toàn bộ file này theo [system-design-template](templates/system-design-template.md)
- [ ] Estimation có số, ≥ 1 sơ đồ Mermaid, mục trade-off
- [ ] _(tùy chọn)_ prototype trong thư mục `<slug>-prototype/` nếu brief có ý tưởng

---

<!-- Nội dung bắt đầu từ đây -->
