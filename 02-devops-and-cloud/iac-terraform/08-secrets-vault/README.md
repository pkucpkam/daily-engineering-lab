---
id: IAC-08
title: "Secrets: Vault & AWS Secrets Manager"
domain: 02-devops-and-cloud/iac-terraform
phase: Y2Q1
kind: lesson
status: todo
prerequisites: [IAC-07]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# IAC-08 · Secrets: Vault & AWS Secrets Manager

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 2 · Q1 |
| **Chủ đề** | Infrastructure as Code |
| **Tiên quyết** | [IAC-07](../07-provisioning-vs-config/README.md) |

**Phạm vi bắt buộc**
- static vs dynamic secrets
- Vault KV, database secrets engine
- rotation
- secret trong Terraform state

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Secret nằm trong Terraform state thì sao?
- Dynamic DB credentials hoạt động thế nào?

**Ý tưởng lab / demo**
- Vault dynamic PostgreSQL credentials cho app

**Tài liệu gốc gợi ý**
- Terraform: Up & Running
- developer.hashicorp.com/terraform
- Ansible docs
- HashiCorp Vault docs

**Gói đầu ra**
- [ ] `README.md` — lý thuyết theo [lesson-template](../../../docs/templates/lesson-template.md)
- [ ] `exercises.md` — bài tập L1/L2/L3 theo [exercises-template](../../../docs/templates/exercises-template.md)
- [ ] `solutions.md` — đáp án theo [solutions-template](../../../docs/templates/solutions-template.md)
- [ ] `lab/` — code demo chạy được + `lab/README.md` theo [lab-template](../../../docs/templates/lab-template.md)
- [ ] _(người học)_ `my-notes.md` — **agent không tạo**

---

<!-- Nội dung bắt đầu từ đây -->
