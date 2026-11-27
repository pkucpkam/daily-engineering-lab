---
id: SD-05
title: "Ticket Booking"
domain: 01-software-engineering/system-design
phase: Y1Q4
kind: design
status: todo
prerequisites: []
related_lessons: [DB-12]
related_existing: []
last_updated: 2026-11-27
---

# SD-05 · Ticket Booking

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](templates/system-design-template.md). Danh mục: [CURRICULUM](../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | System design |
| **Giai đoạn** | Năm 1 · Q4 |
| **Chủ đề** | System Design |
| **Bài học liên quan** | [DB-12](../database/12-row-locks-skip-locked/README.md) |

**Phạm vi bắt buộc**
- concurrency trên ghế
- giữ chỗ tạm (hold + TTL)
- locking strategy
- flash sale
- thanh toán

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- 50.000 người tranh 1.000 vé cùng lúc?
- Giữ chỗ hết hạn xử lý thế nào?

**Ý tưởng lab / demo**
- Lab benchmark 3 chiến lược locking (gắn với lab bắt buộc Q2)

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
