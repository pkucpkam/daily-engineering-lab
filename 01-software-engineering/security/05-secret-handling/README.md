---
id: SEC-05
title: "Quản lý secret & cấu hình an toàn"
domain: 01-software-engineering/security
phase: Y1Q4
kind: lesson
status: todo
prerequisites: [SEC-04]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# SEC-05 · Quản lý secret & cấu hình an toàn

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q4 |
| **Chủ đề** | Application Security |
| **Tiên quyết** | [SEC-04](../04-injection-xss-csrf/README.md) |

**Phạm vi bắt buộc**
- không commit secret
- env var vs secret manager
- rotation
- git-secrets/gitleaks
- log không lộ dữ liệu nhạy cảm

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Lỡ commit secret lên GitHub phải làm gì?
- Vì sao env var chưa đủ an toàn?

**Ý tưởng lab / demo**
- Cài gitleaks pre-commit, quét repo có secret giả

**Tài liệu gốc gợi ý**
- OWASP Top 10
- OWASP Cheat Sheet Series
- RFC 6749, OIDC Core
- Threat Modeling (Shostack)

**Gói đầu ra**
- [ ] `README.md` — lý thuyết theo [lesson-template](../../../docs/templates/lesson-template.md)
- [ ] `exercises.md` — bài tập L1/L2/L3 theo [exercises-template](../../../docs/templates/exercises-template.md)
- [ ] `solutions.md` — đáp án theo [solutions-template](../../../docs/templates/solutions-template.md)
- [ ] `lab/` — code demo chạy được + `lab/README.md` theo [lab-template](../../../docs/templates/lab-template.md)
- [ ] _(người học)_ `my-notes.md` — **agent không tạo**

---

<!-- Nội dung bắt đầu từ đây -->
