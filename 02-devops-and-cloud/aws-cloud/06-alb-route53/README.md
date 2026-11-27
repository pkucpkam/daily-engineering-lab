---
id: AWS-06
title: "ALB & Route 53"
domain: 02-devops-and-cloud/aws-cloud
phase: Y1Q4
kind: lesson
status: todo
prerequisites: [AWS-05]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# AWS-06 · ALB & Route 53

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q4 |
| **Chủ đề** | AWS Cloud cơ bản |
| **Tiên quyết** | [AWS-05](../05-s3/README.md) |

**Phạm vi bắt buộc**
- listener, target group, health check
- path-based routing
- TLS với ACM
- Route 53 records & routing policies

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Health check ALB khác readiness probe ở đâu?
- Weighted routing dùng cho canary thế nào?

**Ý tưởng lab / demo**
- ALB trước ASG với HTTPS

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
