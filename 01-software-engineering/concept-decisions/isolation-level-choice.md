---
id: CD-01
title: "Chọn Isolation Level: READ COMMITTED vs REPEATABLE READ vs SERIALIZABLE"
domain: 01-software-engineering/concept-decisions
phase: Y1Q2
kind: decision
status: todo
prerequisites: []
related_lessons: [DB-10, DB-11]
related_existing: []
last_updated: 2026-11-27
---

# CD-01 · Chọn Isolation Level: READ COMMITTED vs REPEATABLE READ vs SERIALIZABLE

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](templates/concept-decision-template.md). Danh mục: [CURRICULUM](../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Concept decision |
| **Giai đoạn** | Năm 1 · Q2 |
| **Chủ đề** | Concept Decisions |
| **Bài học liên quan** | [DB-10](../database/10-isolation-levels-anomalies/README.md), [DB-11](../database/11-write-skew-serializable/README.md) |

**Phạm vi bắt buộc**
- anomaly từng mức
- chi phí retry
- nghiệp vụ phù hợp
- số liệu benchmark

**Ý tưởng lab / demo**
- Benchmark 3 mức trên nghiệp vụ chuyển tiền

**Tài liệu gốc gợi ý**
- DDIA
- PostgreSQL docs
- HikariCP wiki "About Pool Sizing"

**Gói đầu ra**
- [ ] Toàn bộ file này theo [concept-decision-template](templates/concept-decision-template.md)
- [ ] Bảng so sánh + Final Decision Rule; ưu tiên số đo từ lab liên quan

---

<!-- Nội dung bắt đầu từ đây -->
