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
