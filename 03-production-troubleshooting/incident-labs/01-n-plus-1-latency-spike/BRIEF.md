---
id: INC-01
title: "N+1 query gây latency spike"
domain: 03-production-troubleshooting/incident-labs
phase: Y1Q2
kind: incident
status: todo
prerequisites: []
related_lessons: [DB-06]
related_existing: [01-software-engineering/notes/n-plus-1-query-problem.md]
last_updated: 2026-11-27
---

# INC-01 · N+1 query gây latency spike — BRIEF (dành cho người soạn)

> ⚠️ **SPOILER — người học đừng đọc file này trước khi làm lab.** BRIEF.md chứa hướng root cause, chỉ dành cho người soạn. Đề bài nằm ở `README.md` (agent tạo).

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/incident-lab-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Incident lab |
| **Giai đoạn** | Năm 1 · Q2 |
| **Chủ đề** | Incident Labs (tái hiện sự cố có chủ đích) |
| **Bài học liên quan** | [DB-06](../../../01-software-engineering/database/06-explain-analyze-planner/README.md) |
| **Đã có trong repo** | [n-plus-1-query-problem.md](../../../01-software-engineering/notes/n-plus-1-query-problem.md) — link tới, không viết lại |

**Phạm vi bắt buộc**
- Hibernate lazy loading
- log SQL
- đếm query mỗi request

**Ý tưởng lab / demo**
- API danh sách đơn hàng 50 dòng phát 51 query

**Tài liệu gốc gợi ý**
- Release It!
- Google SRE book
- danluu/post-mortems (GitHub)

**Gói đầu ra**
- [ ] `README.md` — đề bài (ticket, tín hiệu, cách tái hiện) — **không lộ root cause**
- [ ] `hints.md` — gợi ý 3 tầng
- [ ] `solution.md` — root cause & phân tích
- [ ] `evidence/` — log/dump/dashboard mẫu
- [ ] `lab/` — tái hiện ổn định + bản đã sửa
- [ ] _(người học)_ bug-case của riêng mình trong `bug-cases/content/` — **agent không tạo**

---

<!-- Nội dung bắt đầu từ đây -->
