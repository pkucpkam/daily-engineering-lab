---
id: AI-05
title: "Hybrid search: BM25 + vector"
domain: 01-software-engineering/ai-engineering
phase: Y2Q1
kind: lesson
status: todo
prerequisites: [AI-04]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# AI-05 · Hybrid search: BM25 + vector

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 2 · Q1 |
| **Chủ đề** | AI Engineering |
| **Tiên quyết** | [AI-04](../04-vector-search-pgvector/README.md) |

**Phạm vi bắt buộc**
- BM25 và full-text PG
- khi nào keyword thắng vector
- reciprocal rank fusion
- mã/ID/thuật ngữ

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Vì sao vector search tệ với mã số, tên riêng?
- RRF hoạt động thế nào?

**Ý tưởng lab / demo**
- Hybrid search với RRF, so với từng phương pháp riêng

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
