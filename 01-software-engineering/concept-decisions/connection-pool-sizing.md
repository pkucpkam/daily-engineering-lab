---
id: CD-02
title: "Pool sizing: nhỏ vs lớn"
domain: 01-software-engineering/concept-decisions
phase: Y1Q2
kind: decision
status: todo
prerequisites: []
related_lessons: [DB-14]
related_existing: []
last_updated: 2026-11-27
---

# CD-02 · Pool sizing: nhỏ vs lớn

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](templates/concept-decision-template.md). Danh mục: [CURRICULUM](../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Concept decision |
| **Giai đoạn** | Năm 1 · Q2 |
| **Chủ đề** | Concept Decisions |
| **Bài học liên quan** | [DB-14](../database/14-connection-pool-sizing/README.md) |

**Phạm vi bắt buộc**
- công thức
- số đo throughput/p99 theo pool size

**Ý tưởng lab / demo**
- Dùng kết quả lab DB-14

**Tài liệu gốc gợi ý**
- DDIA
- PostgreSQL docs
- HikariCP wiki "About Pool Sizing"

**Gói đầu ra**
- [ ] Toàn bộ file này theo [concept-decision-template](templates/concept-decision-template.md)
- [ ] Bảng so sánh + Final Decision Rule; ưu tiên số đo từ lab liên quan

---

<!-- Nội dung bắt đầu từ đây -->
