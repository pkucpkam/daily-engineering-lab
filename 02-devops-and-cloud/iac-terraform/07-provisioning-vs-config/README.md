---
id: IAC-07
title: "Provisioning vs Configuration management vs Immutable image"
domain: 02-devops-and-cloud/iac-terraform
phase: Y2Q1
kind: lesson
status: todo
prerequisites: [IAC-06]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# IAC-07 · Provisioning vs Configuration management vs Immutable image

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 2 · Q1 |
| **Chủ đề** | Infrastructure as Code |
| **Tiên quyết** | [IAC-06](../06-ansible-fundamentals/README.md) |

**Phạm vi bắt buộc**
- mutable vs immutable infrastructure
- Packer
- cloud-init
- khi nào cần Ansible trong thế giới container

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Immutable infrastructure giải quyết vấn đề gì?
- Còn cần Ansible khi đã dùng K8s?

**Ý tưởng lab / demo**
- Build AMI bằng Packer

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
