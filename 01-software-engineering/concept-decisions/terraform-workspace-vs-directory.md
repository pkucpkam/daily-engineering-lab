---
id: CD-09
title: "Terraform: Workspace vs Directory-per-env"
domain: 01-software-engineering/concept-decisions
phase: Y2Q1
kind: decision
status: todo
prerequisites: []
related_lessons: [IAC-04]
related_existing: []
last_updated: 2026-11-27
---

# CD-09 · Terraform: Workspace vs Directory-per-env

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](templates/concept-decision-template.md). Danh mục: [CURRICULUM](../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Concept decision |
| **Giai đoạn** | Năm 2 · Q1 |
| **Chủ đề** | Concept Decisions |
| **Bài học liên quan** | [IAC-04](../../02-devops-and-cloud/iac-terraform/04-multi-env-layout/README.md) |

**Phạm vi bắt buộc**
- cô lập state
- khác biệt giữa env
- rủi ro thao tác nhầm

**Tài liệu gốc gợi ý**
- DDIA
- PostgreSQL docs
- HikariCP wiki "About Pool Sizing"

**Gói đầu ra**
- [ ] Toàn bộ file này theo [concept-decision-template](templates/concept-decision-template.md)
- [ ] Bảng so sánh + Final Decision Rule; ưu tiên số đo từ lab liên quan

---

<!-- Nội dung bắt đầu từ đây -->
