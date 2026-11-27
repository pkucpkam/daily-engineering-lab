---
id: ARCH-01
title: "Spring Boot auto-configuration"
domain: 01-software-engineering/backend-architecture
phase: Y1Q3
kind: lesson
status: todo
prerequisites: []
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# ARCH-01 · Spring Boot auto-configuration

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q3 |
| **Chủ đề** | Backend Architecture (Spring Boot & API) |

**Phạm vi bắt buộc**
- @Conditional*
- spring.factories / AutoConfiguration.imports
- starter
- debug=true condition report
- tự viết starter

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- Spring Boot quyết định tạo DataSource như thế nào?
- Override 1 bean auto-config đúng cách?

**Ý tưởng lab / demo**
- Tự viết 1 starter nhỏ có @ConditionalOnProperty

**Tài liệu gốc gợi ý**
- Spring Framework Reference
- Release It!
- Microsoft REST API Guidelines
- Resilience4j docs

**Gói đầu ra**
- [ ] `README.md` — lý thuyết theo [lesson-template](../../../docs/templates/lesson-template.md)
- [ ] `exercises.md` — bài tập L1/L2/L3 theo [exercises-template](../../../docs/templates/exercises-template.md)
- [ ] `solutions.md` — đáp án theo [solutions-template](../../../docs/templates/solutions-template.md)
- [ ] `lab/` — code demo chạy được + `lab/README.md` theo [lab-template](../../../docs/templates/lab-template.md)
- [ ] _(người học)_ `my-notes.md` — **agent không tạo**

---

<!-- Nội dung bắt đầu từ đây -->
