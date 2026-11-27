---
id: AI-03
title: "Chunking strategies"
domain: 01-software-engineering/ai-engineering
phase: Y1Q4
kind: lesson
status: todo
prerequisites: [AI-02]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# AI-03 · Chunking strategies

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q4 |
| **Chủ đề** | AI Engineering |
| **Tiên quyết** | [AI-02](../02-embeddings-fundamentals/README.md) |

**Phạm vi bắt buộc**
- fixed-size, recursive, semantic chunking
- overlap
- metadata
- chunk theo cấu trúc tài liệu (heading, bảng)

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Chunk quá nhỏ và quá lớn gây vấn đề gì?
- Chunk tài liệu có bảng biểu thế nào?

**Ý tưởng lab / demo**
- So sánh 3 cách chunk trên chính repo này, đo recall

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
