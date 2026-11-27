---
id: AWS-01
title: "IAM & least privilege"
domain: 02-devops-and-cloud/aws-cloud
phase: Y1Q4
kind: lesson
status: todo
prerequisites: []
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# AWS-01 · IAM & least privilege

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q4 |
| **Chủ đề** | AWS Cloud cơ bản |

**Phạm vi bắt buộc**
- user, group, role, policy
- policy evaluation logic
- assume role
- instance profile
- MFA
- không dùng root

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Explicit deny thắng allow thế nào?
- Vì sao app trên EC2 nên dùng role thay access key?

**Ý tưởng lab / demo**
- Viết policy tối thiểu cho app đọc 1 bucket S3, kiểm chứng bằng IAM Policy Simulator

**Tài liệu gốc gợi ý**
- AWS Well-Architected Framework
- AWS docs
- AWS Skill Builder
- Adrian Cantrill courses

**Gói đầu ra**
- [ ] `README.md` — lý thuyết theo [lesson-template](../../../docs/templates/lesson-template.md)
- [ ] `exercises.md` — bài tập L1/L2/L3 theo [exercises-template](../../../docs/templates/exercises-template.md)
- [ ] `solutions.md` — đáp án theo [solutions-template](../../../docs/templates/solutions-template.md)
- [ ] `lab/` — code demo chạy được + `lab/README.md` theo [lab-template](../../../docs/templates/lab-template.md)
- [ ] _(người học)_ `my-notes.md` — **agent không tạo**

---

<!-- Nội dung bắt đầu từ đây -->
