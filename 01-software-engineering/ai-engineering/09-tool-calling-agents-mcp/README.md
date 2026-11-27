---
id: AI-09
title: "Tool calling, Agents & MCP"
domain: 01-software-engineering/ai-engineering
phase: Y3Q1
kind: lesson
status: todo
prerequisites: [AI-08]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# AI-09 · Tool calling, Agents & MCP

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 3 · Q1 |
| **Chủ đề** | AI Engineering |
| **Tiên quyết** | [AI-08](../08-prompt-injection-guardrails/README.md) |

**Phạm vi bắt buộc**
- tool calling
- workflow vs agent
- MCP server/client
- khi nào KHÔNG dùng agent
- agent cho DevOps (incident triage)

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Khi nào workflow cố định tốt hơn agent?
- MCP giải quyết vấn đề gì?

**Ý tưởng lab / demo**
- MCP server nhỏ đọc metric Prometheus cho agent triage

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
