---
id: AI-04
title: "Vector search với pgvector"
domain: 01-software-engineering/ai-engineering
phase: Y1Q4
kind: lesson
status: todo
prerequisites: [AI-03]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# AI-04 · Vector search với pgvector

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q4 |
| **Chủ đề** | AI Engineering |
| **Tiên quyết** | [AI-03](../03-chunking-strategies/README.md) |

**Phạm vi bắt buộc**
- exact vs ANN
- HNSW vs IVFFlat
- tham số ef/lists
- filter kết hợp
- kích thước index

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- HNSW đánh đổi gì giữa recall và tốc độ?
- Lọc theo metadata trước hay sau vector search?

**Ý tưởng lab / demo**
- pgvector + HNSW trên vài nghìn chunk, đo recall@k và latency

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
