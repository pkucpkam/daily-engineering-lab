---
id: AWS-04
title: "RDS PostgreSQL"
domain: 02-devops-and-cloud/aws-cloud
phase: Y1Q4
kind: lesson
status: todo
prerequisites: [AWS-03]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# AWS-04 · RDS PostgreSQL

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q4 |
| **Chủ đề** | AWS Cloud cơ bản |
| **Tiên quyết** | [AWS-03](../03-ec2-autoscaling/README.md) |

**Phạm vi bắt buộc**
- Multi-AZ vs read replica
- backup & PITR
- parameter group
- performance insights
- connection limit

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Multi-AZ có tăng hiệu năng đọc không?
- Failover RDS mất bao lâu và app cần làm gì?

**Ý tưởng lab / demo**
- RDS + app kết nối, thử failover

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
