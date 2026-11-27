---
id: ARCH-02
title: "Bean lifecycle & proxies (JDK vs CGLIB)"
domain: 01-software-engineering/backend-architecture
phase: Y1Q3
kind: lesson
status: todo
prerequisites: [ARCH-01]
related_lessons: []
related_existing: []
last_updated: 2026-11-27
---

# ARCH-02 · Bean lifecycle & proxies (JDK vs CGLIB)

> 🔲 **Chưa soạn** — file này hiện là *brief*. Người soạn: đọc [CONTENT-GUIDE](../../../docs/CONTENT-GUIDE.md), giữ nguyên khối Brief bên dưới và viết nội dung theo [template](../../../docs/templates/lesson-template.md). Danh mục: [CURRICULUM](../../../CURRICULUM.md).

## 🧭 Brief

| | |
| :--- | :--- |
| **Loại** | Bài học |
| **Giai đoạn** | Năm 1 · Q3 |
| **Chủ đề** | Backend Architecture (Spring Boot & API) |
| **Tiên quyết** | [ARCH-01](../01-spring-boot-autoconfiguration/README.md) |

**Phạm vi bắt buộc**
- lifecycle: instantiate → populate → aware → init → proxy
- BeanPostProcessor
- scope
- JDK dynamic proxy vs CGLIB

**Câu hỏi sâu — nội dung phải giúp trả lời được**
- AOP proxy được tạo ở bước nào?
- Vì sao final class/method không proxy được?

**Ý tưởng lab / demo**
- In log toàn bộ lifecycle của 1 bean; tự viết proxy bằng java.lang.reflect.Proxy

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
