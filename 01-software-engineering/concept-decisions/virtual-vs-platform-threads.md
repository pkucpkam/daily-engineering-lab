---
id: CD-03
title: "Virtual Threads vs Platform Thread Pool"
domain: 01-software-engineering/concept-decisions
phase: Y1Q2
kind: decision
status: todo
prerequisites: []
related_lessons: [CONC-08]
related_existing: []
last_updated: 2026-11-27
---

# CD-03 · Virtual Threads vs Platform Thread Pool

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](templates/concept-decision-template.md). Danh mục: [CURRICULUM](../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Concept decision |
| **Giai đoạn** | Năm 1 · Q2 |
| **Chủ đề** | Concept Decisions |
| **Bài học liên quan** | [CONC-08](../concurrency/08-virtual-threads/README.md) |

**Phạm vi bắt buộc**
- IO-bound vs CPU-bound
- pinning
- tài nguyên downstream

**Ý tưởng lab / demo**
- Dùng kết quả lab CONC-08

**Tài liệu gốc gợi ý**
- DDIA
- PostgreSQL docs
- HikariCP wiki "About Pool Sizing"

**Gói đầu ra**
- [ ] Toàn bộ file này theo [concept-decision-template](templates/concept-decision-template.md)
- [ ] Bảng so sánh + Final Decision Rule; ưu tiên số đo từ lab liên quan

---

<!-- Nội dung bắt đầu từ đây -->
