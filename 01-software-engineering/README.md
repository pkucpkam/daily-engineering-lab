# 01 — Software Engineering Deep-Dives

Trục kỹ thuật phần mềm tập trung vào việc làm chủ kiến trúc, cơ chế hoạt động tầng sâu của ngôn ngữ và cơ sở dữ liệu, cùng khả năng thiết kế hệ thống phân tán quy mô lớn.

---

## 📂 Danh mục nội dung

1. [system-design/](./system-design/README.md)
   * Các bài thiết kế hệ thống hoàn chỉnh từ yêu cầu, tính toán dung lượng, API design đến trade-offs.
   * Danh sách 30 bài toán phỏng vấn thực tế: [30 Advanced System Design Challenges](./system-design/30%20Advanced%20System%20Design%20Challenges%20(Interview-Level).md).

2. [patterns/](./patterns/README.md)
   * Các mẫu thiết kế phân tán (Distributed Patterns): Circuit Breaker, Saga, Outbox, CQRS, Cache-Aside, Rate Limiter.

3. [concept-decisions/](./concept-decisions/)
   * Phân tích đánh đổi (Trade-off Matrix) giữa các giải pháp:
     * SQL vs NoSQL
     * Pessimistic vs Optimistic Locking
     * Polling vs WebSocket
     * Session vs Cookie vs Token
     * REST vs GraphQL
     * Cursor-based vs Offset-based Pagination

4. [notes/](./notes/README.md)
   * Nghiên cứu chuyên sâu về các vấn đề kỹ thuật:
     * Idempotency trong distributed backend
     * Transaction boundaries và ACID
     * N+1 query problem và tối ưu hóa ORM
     * Phantom read và các mức cô lập (Isolation Levels)

---

## 📚 Bài học theo chủ đề (lý thuyết · bài tập · lab)

| Thư mục | Chủ đề | Giai đoạn chính |
| :--- | :--- | :--- |
| [java-jvm/](./java-jvm/README.md) | Java 7→21, JVM internals, GC, heap dump | Quý 0 – Năm 1 Q1 |
| [code-quality/](./code-quality/README.md) | SOLID, refactoring legacy, testing | Năm 1 Q1 |
| [concurrency/](./concurrency/README.md) | JMM, thread pool, locks, virtual threads | Năm 1 Q2 |
| [database/](./database/README.md) | PostgreSQL internals, index, MVCC, locking, pool | Năm 1 Q2 |
| [performance/](./performance/README.md) | Percentiles, queueing, JMH, load test, profiling | Năm 1 Q2 |
| [backend-architecture/](./backend-architecture/README.md) | Spring internals, API, caching, resilience | Năm 1 Q3 |
| [distributed-systems/](./distributed-systems/README.md) | Replication, consensus, Kafka, Outbox, Saga | Năm 1 Q4 |
| [security/](./security/README.md) | OWASP, OAuth2/OIDC, JWT, threat modeling | Năm 1 Q4 |
| [ai-engineering/](./ai-engineering/README.md) | AI-assisted dev, RAG, LLM production, agents | Năm 1 – Năm 3 |

Danh mục đầy đủ có ID & status: [CURRICULUM.md](../CURRICULUM.md).
