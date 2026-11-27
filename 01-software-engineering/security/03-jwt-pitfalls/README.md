---
id: SEC-03
title: "JWT pitfalls"
domain: 01-software-engineering/security
phase: Y1Q4
kind: lesson
status: todo
prerequisites: [SEC-02]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# SEC-03 · JWT pitfalls

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q4 |
| **Chủ đề** | Application Security |
| **Tiên quyết** | [SEC-02](../02-authn-authz-oauth2-oidc/README.md) |

**Phạm vi bắt buộc**
- cấu trúc JWT
- alg=none
- HS vs RS
- thu hồi token
- lưu token ở đâu
- thời hạn token

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Làm sao logout khi dùng JWT stateless?
- Lưu JWT trong localStorage nguy hiểm ra sao?

**Ý tưởng lab / demo**
- Tấn công thử alg confusion trên verifier viết sai, sửa đúng

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
