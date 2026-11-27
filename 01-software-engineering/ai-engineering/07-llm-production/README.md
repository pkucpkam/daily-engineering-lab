---
id: AI-07
title: "LLM trong production: latency, cost, caching"
domain: 01-software-engineering/ai-engineering
phase: Y2Q2
kind: lesson
status: todo
prerequisites: [AI-06]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# AI-07 · LLM trong production: latency, cost, caching

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 2 · Q2 |
| **Chủ đề** | AI Engineering |
| **Tiên quyết** | [AI-06](../06-reranking-retrieval-eval/README.md) |

**Phạm vi bắt buộc**
- token và chi phí
- streaming
- prompt caching
- batching
- timeout & fallback
- observability cho LLM call

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Ước lượng chi phí 1 triệu request/tháng?
- Đo chất lượng output LLM trong production thế nào?

**Ý tưởng lab / demo**
- Thêm metric latency/token/cost cho RAG service

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
