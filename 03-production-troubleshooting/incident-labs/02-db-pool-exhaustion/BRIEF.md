---
id: INC-02
title: "Cạn connection pool"
domain: 03-production-troubleshooting/incident-labs
phase: Y1Q2
kind: incident
status: todo
prerequisites: []
related_lessons: [DB-14, ARCH-03]
related_existing: []
last_updated: 2026-11-27
---

# INC-02 · Cạn connection pool — BRIEF (dành cho người soạn)

> ⚠️ **SPOILER — người học đừng đọc file này trước khi làm lab.** BRIEF.md chứa hướng root cause, chỉ dành cho người soạn. Đề bài nằm ở `README.md` (agent tạo).

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/incident-lab-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Incident lab |
| **Giai đoạn** | Năm 1 · Q2 |
| **Chủ đề** | Incident Labs (tái hiện sự cố có chủ đích) |
| **Bài học liên quan** | [DB-14](../../../01-software-engineering/database/14-connection-pool-sizing/README.md), [ARCH-03](../../../01-software-engineering/backend-architecture/03-transactional-pitfalls/README.md) |

**Phạm vi bắt buộc**
- transaction giữ connection lâu (gọi HTTP trong transaction)
- HikariCP timeout
- thread dump

**Ý tưởng lab / demo**
- Endpoint gọi API ngoài trong @Transactional dưới tải

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
