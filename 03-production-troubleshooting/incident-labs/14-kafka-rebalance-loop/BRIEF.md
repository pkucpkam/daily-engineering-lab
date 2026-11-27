---
id: INC-14
title: "Kafka consumer rebalance liên tục"
domain: 03-production-troubleshooting/incident-labs
phase: Y1Q4
kind: incident
status: todo
prerequisites: []
related_lessons: [DIST-09]
related_existing: []
last_updated: 2026-11-27
---

# INC-14 · Kafka consumer rebalance liên tục — BRIEF (dành cho người soạn)

> ⚠️ **SPOILER — người học đừng đọc file này trước khi làm lab.** BRIEF.md chứa hướng root cause, chỉ dành cho người soạn. Đề bài nằm ở `README.md` (agent tạo).

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/incident-lab-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Incident lab |
| **Giai đoạn** | Năm 1 · Q4 |
| **Chủ đề** | Incident Labs (tái hiện sự cố có chủ đích) |
| **Bài học liên quan** | [DIST-09](../../../01-software-engineering/distributed-systems/09-kafka-consumer-groups/README.md) |

**Phạm vi bắt buộc**
- max.poll.interval.ms
- xử lý chậm
- consumer lag

**Ý tưởng lab / demo**
- Consumer xử lý 1 batch quá lâu

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
