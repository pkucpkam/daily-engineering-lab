# Patterns

This directory contains reusable architectural and operational patterns worth understanding deeply.

## How to Use

Each file in this directory captures one pattern:

- **What it is** — A clear, one-sentence definition
- **When to use it** — The specific problem it solves
- **When NOT to use it** — The traps and misapplications
- **How it works** — The mechanics, with diagrams where helpful
- **Real-world examples** — Where you've seen it in production systems
- **Trade-offs** — What you give up by using it

## Pattern Categories

### Reliability Patterns
- Circuit Breaker
- Retry with Exponential Backoff
- Bulkhead
- Timeout
- Fallback / Graceful Degradation

### Scalability Patterns
- Read Replicas
- CQRS (Command Query Responsibility Segregation)
- Event Sourcing
- Sharding / Partitioning
- Fan-out on Write vs Fan-out on Read

### Data Patterns
- Cache-Aside (Lazy Loading)
- Write-Through Cache
- Write-Behind (Write-Back) Cache
- Saga Pattern (Distributed Transactions)
- Outbox Pattern

### Messaging Patterns
- Competing Consumers
- Dead Letter Queue
- Idempotent Consumer
- Message Deduplication

## Naming Convention

Files should be named in lowercase with hyphens:

```
patterns/
├── circuit-breaker.md
├── cache-aside.md
├── outbox-pattern.md
└── saga-pattern.md
```

---

<!-- CURRICULUM:START — sinh tự động từ CURRICULUM, cột Status cập nhật tay -->
## 📚 Danh sách bài

Loại: **Pattern card** · Template: [pattern-template.md](../../docs/templates/pattern-template.md) · Quy chuẩn: [CONTENT-GUIDE](../../docs/CONTENT-GUIDE.md)

| ID | Bài | Giai đoạn | Status |
| :--- | :--- | :--- | :---: |
| PAT-01 | [Circuit Breaker](circuit-breaker.md) | Năm 1 · Q3 | 🔲 |
| PAT-02 | [Retry with Exponential Backoff & Jitter](retry-exponential-backoff.md) | Năm 1 · Q3 | 🔲 |
| PAT-03 | [Bulkhead](bulkhead.md) | Năm 1 · Q3 | 🔲 |
| PAT-04 | [Timeout](timeout.md) | Năm 1 · Q3 | 🔲 |
| PAT-05 | [Fallback & Graceful Degradation](fallback-graceful-degradation.md) | Năm 1 · Q3 | 🔲 |
| PAT-06 | [Read Replicas](read-replicas.md) | Năm 1 · Q3 | 🔲 |
| PAT-07 | [CQRS](cqrs.md) | Năm 1 · Q3 | 🔲 |
| PAT-08 | [Event Sourcing](event-sourcing.md) | Năm 1 · Q3 | 🔲 |
| PAT-09 | [Sharding / Partitioning](sharding.md) | Năm 1 · Q3 | 🔲 |
| PAT-10 | [Fan-out on Write vs on Read](fan-out-write-vs-read.md) | Năm 1 · Q3 | 🔲 |
| PAT-11 | [Cache-Aside](cache-aside.md) | Năm 1 · Q3 | 🔲 |
| PAT-12 | [Write-Through Cache](write-through-cache.md) | Năm 1 · Q3 | 🔲 |
| PAT-13 | [Write-Behind Cache](write-behind-cache.md) | Năm 1 · Q3 | 🔲 |
| PAT-14 | [Saga](saga.md) | Năm 1 · Q3 | 🔲 |
| PAT-15 | [Transactional Outbox](outbox.md) | Năm 1 · Q3 | 🔲 |
| PAT-16 | [Competing Consumers](competing-consumers.md) | Năm 1 · Q3 | 🔲 |
| PAT-17 | [Dead Letter Queue](dead-letter-queue.md) | Năm 1 · Q3 | 🔲 |
| PAT-18 | [Idempotent Consumer & Message Deduplication](idempotent-consumer.md) | Năm 1 · Q3 | 🔲 |
| PAT-19 | [Strangler Fig](strangler-fig.md) | Năm 1 · Q3 | 🔲 |
| PAT-20 | [Rate Limiter](rate-limiter.md) | Năm 1 · Q3 | 🔲 |

**Tài liệu gốc của chủ đề:** microservices.io · Release It! · Enterprise Integration Patterns · AWS Builders' Library

<!-- CURRICULUM:END -->
