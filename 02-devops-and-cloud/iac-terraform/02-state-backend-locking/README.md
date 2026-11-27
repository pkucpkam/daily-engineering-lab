---
id: IAC-02
title: "State, remote backend & locking"
domain: 02-devops-and-cloud/iac-terraform
phase: Y2Q1
kind: lesson
status: todo
prerequisites: [IAC-01]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# IAC-02 · State, remote backend & locking

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 2 · Q1 |
| **Chủ đề** | Infrastructure as Code |
| **Tiên quyết** | [IAC-01](../01-terraform-core/README.md) |

**Phạm vi bắt buộc**
- state là gì và vì sao nhạy cảm
- S3 backend + locking
- state mv/rm
- import

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Hai người apply cùng lúc thì sao?
- State bị lệch thực tế xử lý thế nào?

**Ý tưởng lab / demo**
- Remote state S3 + lock, thử apply đồng thời

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
