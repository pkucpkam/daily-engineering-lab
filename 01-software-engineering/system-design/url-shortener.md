---
id: SD-02
title: "URL Shortener"
domain: 01-software-engineering/system-design
phase: Y1Q4
kind: design
status: todo
prerequisites: []
related_lessons: [ARCH-06]
related_existing: []
last_updated: 2026-11-27
---

# SD-02 · URL Shortener

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](templates/system-design-template.md). Danh mục: [CURRICULUM](../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | System design |
| **Giai đoạn** | Năm 1 · Q4 |
| **Chủ đề** | System Design |
| **Bài học liên quan** | [ARCH-06](../backend-architecture/06-redis-caching-strategies/README.md) |

**Phạm vi bắt buộc**
- ID generation (base62, counter, hash)
- redirect 301 vs 302
- read-heavy caching
- analytics

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Sinh ID không trùng giữa nhiều instance?
- 301 vs 302 ảnh hưởng analytics ra sao?

**Ý tưởng lab / demo**
- Prototype bằng Spring Boot + Redis

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
