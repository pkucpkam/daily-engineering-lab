
# 🧠 **8-Week Backend Engineering Practice Roadmap**

A structured, progressive roadmap to develop **senior-level backend thinking** through daily practice.

---

# **Week 1 — Core System Design (Foundations)**

**Goal:** Learn how to clarify requirements and design end-to-end systems.

**Monday — System Design**

* Design a URL Shortener (e.g., Bitly)
* Focus:

  * Functional vs non-functional requirements
  * Read/write ratio
  * Basic schema design

**Tuesday — Bug Analysis**

* N+1 query causing API latency spike
* Focus:

  * ORM behavior
  * Query optimization

**Wednesday — Pattern Deep Dive**

* Caching Strategies:

  * Cache-aside
  * Write-through
  * Write-back

**Thursday — Design Improvement**

* Improve URL Shortener:

  * Add caching
  * Optimize for read-heavy traffic

**Friday — Post-Mortem**

* Slow database queries causing partial downtime

---

# **Week 2 — Scaling & Bottlenecks**

**Goal:** Identify and reason about system bottlenecks.

**Monday — System Design**

* Design a Rate Limiter

  * Token Bucket
  * Sliding Window

**Tuesday — Bug Analysis**

* API latency spike due to exhausted DB connection pool

**Wednesday — Pattern Deep Dive**

* Load Balancing:

  * Round-robin
  * Least connections

**Thursday — Design Improvement**

* Distributed Rate Limiter using Redis

**Friday — Post-Mortem**

* Traffic spike causing service degradation

---

# **Week 3 — Data & Storage**

**Goal:** Make correct data storage decisions and understand trade-offs.

**Monday — System Design**

* Design a File Storage System (mini S3)

**Tuesday — Bug Analysis**

* Disk full leading to service crash

**Wednesday — Pattern Deep Dive**

* SQL vs NoSQL
* Indexing strategies

**Thursday — Design Improvement**

* Add CDN
* Storage tiering optimization

**Friday — Post-Mortem**

* Data loss due to backup failure

---

# **Week 4 — Async Processing & Messaging**

**Goal:** Understand queues and eventual consistency.

**Monday — System Design**

* Design a Notification System (email/push/SMS)

**Tuesday — Bug Analysis**

* Stuck job queue

**Wednesday — Pattern Deep Dive**

* Message Queues:

  * Kafka / RabbitMQ
  * At-least-once vs exactly-once delivery

**Thursday — Design Improvement**

* Retry strategy
* Dead Letter Queue (DLQ)

**Friday — Post-Mortem**

* Queue overload causing delayed notifications

---

# **Week 5 — Consistency & Concurrency**

**Goal:** Prevent race conditions and data corruption.

**Monday — System Design**

* Design a Distributed Lock system (e.g., Redis-based)

**Tuesday — Bug Analysis**

* Race condition causing duplicate records

**Wednesday — Pattern Deep Dive**

* Optimistic vs Pessimistic Locking

**Thursday — Design Improvement**

* Idempotency design
* Retry-safe APIs

**Friday — Post-Mortem**

* Double payment issue in production

---

# **Week 6 — High Availability & Resilience**

**Goal:** Build systems that survive failures.

**Monday — System Design**

* Design a Highly Available API system

**Tuesday — Bug Analysis**

* Cascading failures due to timeouts

**Wednesday — Pattern Deep Dive**

* Circuit Breaker
* Retry with exponential backoff

**Thursday — Design Improvement**

* Graceful degradation
* Fallback strategies

**Friday — Post-Mortem**

* Third-party service outage impact

---

# **Week 7 — Observability & Debugging**

**Goal:** Learn how to “see” and understand your system.

**Monday — System Design**

* Design a logging & monitoring system

**Tuesday — Bug Analysis**

* Unable to trace a request across services

**Wednesday — Pattern Deep Dive**

* Metrics (Prometheus)
* Distributed tracing (OpenTelemetry)

**Thursday — Design Improvement**

* Alerting strategies
* SLO/SLI definition

**Friday — Post-Mortem**

* Incident with insufficient logs/metrics

---

# **Week 8 — Real-World Systems (Advanced)**

**Goal:** Think like a senior engineer under real constraints.

**Monday — System Design**

* Design an E-commerce Checkout System

**Tuesday — Bug Analysis**

* Payment succeeded but order creation failed

**Wednesday — Pattern Deep Dive**

* Saga Pattern (distributed transactions)

**Thursday — Design Improvement**

* Compensation logic
* Rollback strategies

**Friday — Post-Mortem**

* Inconsistent order states across services

---

