---
id: CD-11
title: "Rolling vs Blue-Green vs Canary"
domain: 01-software-engineering/concept-decisions
phase: Y2Q2
kind: decision
status: todo
prerequisites: []
related_lessons: [CICD-07]
related_existing: []
last_updated: 2026-11-27
---

# CD-11 · Rolling vs Blue-Green vs Canary

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](templates/concept-decision-template.md). Danh mục: [CURRICULUM](../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Concept decision |
| **Giai đoạn** | Năm 2 · Q2 |
| **Chủ đề** | Concept Decisions |
| **Bài học liên quan** | [CICD-07](../../02-devops-and-cloud/ci-cd-pipelines/07-deployment-strategies/README.md) |

**Phạm vi bắt buộc**
- chi phí hạ tầng
- rủi ro
- tốc độ rollback
- yêu cầu DB migration

**Tài liệu gốc gợi ý**
- DDIA
- PostgreSQL docs
- HikariCP wiki "About Pool Sizing"

**Gói đầu ra**
- [ ] Toàn bộ file này theo [concept-decision-template](templates/concept-decision-template.md)
- [ ] Bảng so sánh + Final Decision Rule; ưu tiên số đo từ lab liên quan

---

<!-- Nội dung bắt đầu từ đây -->
