---
id: DSA-06
title: "LRU Cache"
domain: 00-foundations/dsa
phase: Y1Q1
kind: lesson
status: todo
prerequisites: [DSA-05]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# DSA-06 · LRU Cache

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q1 |
| **Chủ đề** | DSA thực dụng |
| **Tiên quyết** | [DSA-05](../05-graph-bfs-dfs/README.md) |

**Phạm vi bắt buộc**
- HashMap + doubly linked list
- LinkedHashMap accessOrder
- thread-safe LRU
- LRU vs LFU

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Làm LRU thread-safe mà không lock toàn bộ?
- Khi nào LRU tệ (scan pattern)?

**Ý tưởng lab / demo**
- Cài LRU O(1) + test đồng thời

**Tài liệu gốc gợi ý**
- CLRS (tham khảo)
- Grokking Algorithms
- NeetCode roadmap

**Gói đầu ra**
- [ ] `README.md` — lý thuyết theo [lesson-template](../../../docs/templates/lesson-template.md)
- [ ] `exercises.md` — bài tập L1/L2/L3 theo [exercises-template](../../../docs/templates/exercises-template.md)
- [ ] `solutions.md` — đáp án theo [solutions-template](../../../docs/templates/solutions-template.md)
- [ ] `lab/` — code demo chạy được + `lab/README.md` theo [lab-template](../../../docs/templates/lab-template.md)
- [ ] _(người học)_ `my-notes.md` — **agent không tạo**

---

<!-- Nội dung bắt đầu từ đây -->
