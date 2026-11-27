---
id: AI-06
title: "Reranking & đánh giá retrieval"
domain: 01-software-engineering/ai-engineering
phase: Y2Q1
kind: lesson
status: todo
prerequisites: [AI-05]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# AI-06 · Reranking & đánh giá retrieval

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 2 · Q1 |
| **Chủ đề** | AI Engineering |
| **Tiên quyết** | [AI-05](../05-hybrid-search-bm25/README.md) |

**Phạm vi bắt buộc**
- cross-encoder reranker
- bộ câu hỏi đánh giá
- recall@k, MRR, nDCG
- LLM-as-judge và hạn chế

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Không có bộ đánh giá thì cải tiến RAG có ý nghĩa không?
- Reranker tốn chi phí gì?

**Ý tưởng lab / demo**
- Tạo bộ 30 câu hỏi vàng cho repo, đo trước/sau reranking

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
