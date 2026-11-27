---
id: SEC-02
title: "AuthN/AuthZ: OAuth2 & OIDC"
domain: 01-software-engineering/security
phase: Y1Q4
kind: lesson
status: todo
prerequisites: [SEC-01]
related_lessons: []
related_existing: [01-software-engineering/concept-decisions/session-vs-cookie-vs-token.md]
last_updated: 2026-11-27
---

# SEC-02 · AuthN/AuthZ: OAuth2 & OIDC

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q4 |
| **Chủ đề** | Application Security |
| **Tiên quyết** | [SEC-01](../01-owasp-top10-backend/README.md) |
| **Đã có trong repo** | [session-vs-cookie-vs-token.md](../../concept-decisions/session-vs-cookie-vs-token.md) — link tới, không viết lại |

**Phạm vi bắt buộc**
- authentication vs authorization
- OAuth2 flows (authorization code + PKCE, client credentials)
- OIDC id_token
- RBAC vs ABAC
- Spring Security resource server

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Vì sao implicit flow bị loại bỏ?
- Access token vs refresh token vs id token?

**Ý tưởng lab / demo**
- Keycloak bằng Docker + Spring resource server bảo vệ API

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
