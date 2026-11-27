---
id: CD-04
title: "Distributed lock: Redis vs Database vs etcd"
domain: 01-software-engineering/concept-decisions
phase: Y1Q4
kind: decision
status: todo
prerequisites: []
related_lessons: [DIST-14]
related_existing: []
last_updated: 2026-11-27
---

# CD-04 · Distributed lock: Redis vs Database vs etcd

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](templates/concept-decision-template.md). Danh mục: [CURRICULUM](../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Concept decision |
| **Giai đoạn** | Năm 1 · Q4 |
| **Chủ đề** | Concept Decisions |
| **Bài học liên quan** | [DIST-14](../distributed-systems/14-distributed-lock-fencing/README.md) |

**Phạm vi bắt buộc**
- an toàn, độ trễ, vận hành, fencing

**Tài liệu gốc gợi ý**
- DDIA
- PostgreSQL docs
- HikariCP wiki "About Pool Sizing"

**Gói đầu ra**
- [ ] Toàn bộ file này theo [concept-decision-template](templates/concept-decision-template.md)
- [ ] Bảng so sánh + Final Decision Rule; ưu tiên số đo từ lab liên quan

---

<!-- Nội dung bắt đầu từ đây -->
