---
id: SD-04
title: "Notification System"
domain: 01-software-engineering/system-design
phase: Y1Q4
kind: design
status: todo
prerequisites: []
related_lessons: [DIST-10]
related_existing: []
last_updated: 2026-11-27
---

# SD-04 · Notification System

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](templates/system-design-template.md). Danh mục: [CURRICULUM](../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | System design |
| **Giai đoạn** | Năm 1 · Q4 |
| **Chủ đề** | System Design |
| **Bài học liên quan** | [DIST-10](../distributed-systems/10-delivery-semantics/README.md) |

**Phạm vi bắt buộc**
- multi-channel (email/push/SMS)
- queue
- retry, DLQ
- template
- dedup
- user preferences
- rate limit theo user

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Đảm bảo không gửi trùng thông báo thế nào?
- Xử lý provider SMS bị lỗi?

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
