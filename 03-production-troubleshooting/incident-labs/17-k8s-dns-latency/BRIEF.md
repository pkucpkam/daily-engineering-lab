---
id: INC-17
title: "Latency cao do DNS trong Kubernetes (ndots)"
domain: 03-production-troubleshooting/incident-labs
phase: Y2Q3
kind: incident
status: todo
prerequisites: []
related_lessons: [LNX-11]
related_existing: []
last_updated: 2026-11-27
---

# INC-17 · Latency cao do DNS trong Kubernetes (ndots) — BRIEF (dành cho người soạn)

> ⚠️ **SPOILER — người học đừng đọc file này trước khi làm lab.** BRIEF.md chứa hướng root cause, chỉ dành cho người soạn. Đề bài nằm ở `README.md` (agent tạo).

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/incident-lab-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Incident lab |
| **Giai đoạn** | Năm 2 · Q3 |
| **Chủ đề** | Incident Labs (tái hiện sự cố có chủ đích) |
| **Bài học liên quan** | [LNX-11](../../../02-devops-and-cloud/linux-networking/11-dns-resolution/README.md) |

**Phạm vi bắt buộc**
- ndots:5
- search domains
- conntrack

**Ý tưởng lab / demo**
- Gọi domain bên ngoài từ pod, đo thời gian resolve

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
