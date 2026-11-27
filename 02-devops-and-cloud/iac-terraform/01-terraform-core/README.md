---
id: IAC-01
title: "Terraform core: provider, resource, lifecycle"
domain: 02-devops-and-cloud/iac-terraform
phase: Y2Q1
kind: lesson
status: todo
prerequisites: []
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# IAC-01 · Terraform core: provider, resource, lifecycle

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 2 · Q1 |
| **Chủ đề** | Infrastructure as Code |

**Phạm vi bắt buộc**
- HCL
- provider
- resource, data source
- dependency graph
- plan/apply
- lifecycle (create_before_destroy, prevent_destroy)
- count vs for_each

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- count và for_each khác nhau gây hậu quả gì khi xóa phần tử giữa?
- plan an toàn mà apply vẫn lỗi vì sao?

**Ý tưởng lab / demo**
- Dựng VPC + EC2 đơn giản bằng Terraform

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
