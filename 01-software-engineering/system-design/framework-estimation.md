---
id: SD-01
title: "Framework & Back-of-envelope estimation"
domain: 01-software-engineering/system-design
phase: Y1Q4
kind: design
status: todo
prerequisites: []
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# SD-01 · Framework & Back-of-envelope estimation

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](templates/system-design-template.md). Danh mục: [CURRICULUM](../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | System design |
| **Giai đoạn** | Năm 1 · Q4 |
| **Chủ đề** | System Design |

**Phạm vi bắt buộc**
- quy trình 7 bước: requirements → estimation → API → data model → high-level → bottleneck → trade-off
- bảng con số cần nhớ (latency numbers, QPS, storage)

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- 100 triệu DAU, mỗi user 10 request/ngày thì QPS trung bình và đỉnh?
- Lưu 5 năm ảnh thì cần bao nhiêu storage?

**Ý tưởng lab / demo**
- Bảng tính estimation mẫu

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
