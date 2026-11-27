---
id: AI-08
title: "Prompt injection & guardrails"
domain: 01-software-engineering/ai-engineering
phase: Y2Q4
kind: lesson
status: todo
prerequisites: [AI-07]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# AI-08 · Prompt injection & guardrails

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 2 · Q4 |
| **Chủ đề** | AI Engineering |
| **Tiên quyết** | [AI-07](../07-llm-production/README.md) |

**Phạm vi bắt buộc**
- direct vs indirect prompt injection
- dữ liệu không đáng tin trong context
- least privilege cho tool
- output validation

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Vì sao không thể "lọc" hết prompt injection?
- Thiết kế quyền cho agent đọc email?

**Ý tưởng lab / demo**
- Demo indirect injection qua tài liệu trong RAG và giảm thiểu

**Tài liệu gốc gợi ý**
- AI Engineering (Chip Huyen)
- Anthropic docs (docs.claude.com)
- pgvector README
- MTEB leaderboard
- Model Context Protocol spec

**Gói đầu ra**
- [ ] `README.md` — lý thuyết theo [lesson-template](../../../docs/templates/lesson-template.md)
- [ ] `exercises.md` — bài tập L1/L2/L3 theo [exercises-template](../../../docs/templates/exercises-template.md)
- [ ] `solutions.md` — đáp án theo [solutions-template](../../../docs/templates/solutions-template.md)
- [ ] `lab/` — code demo chạy được + `lab/README.md` theo [lab-template](../../../docs/templates/lab-template.md)
- [ ] _(người học)_ `my-notes.md` — **agent không tạo**

---

<!-- Nội dung bắt đầu từ đây -->
