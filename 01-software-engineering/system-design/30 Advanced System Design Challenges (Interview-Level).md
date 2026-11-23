# 🧠 **30 Advanced System Design Challenges (Interview-Level)**

Grouped by domain so you can rotate and not get bored.

---

## 🔹 **1. Core Distributed Systems**

1. Design a Distributed Rate Limiter (multi-region)
2. Design a Global Unique ID Generator (Snowflake-like)
3. Design a Distributed Lock Service
4. Design a Configuration Management System (dynamic config push)
5. Design a Feature Flag System (real-time rollout)

---

## 🔹 **2. High-Scale Read/Write Systems**

6. Design Twitter Timeline (fan-out vs fan-in)
7. Design Instagram Feed
8. Design YouTube Video Streaming System
9. Design a URL Shortener at global scale
10. Design a News Aggregation System (Google News-like)

---

## 🔹 **3. Real-Time Systems**

11. Design a Real-time Chat System (WhatsApp)
12. Design a Live Comment System (YouTube Live)
13. Design a Real-time Multiplayer Game Backend
14. Design a Collaborative Editor (Google Docs)
15. Design a Presence System (online/offline status)

---

## 🔹 **4. Data-Heavy Systems**

16. Design a Log Aggregation System (ELK-like)
17. Design a Metrics & Monitoring System (Prometheus-like)
18. Design a Search Engine (basic Elasticsearch)
19. Design a Recommendation System (YouTube/Netflix)
20. Design a Data Pipeline (ETL system)

---

## 🔹 **5. Reliability & Infrastructure**

21. Design a Distributed Job Scheduler (cron at scale)
22. Design a Backup & Restore System
23. Design a Multi-region Failover System
24. Design a Circuit Breaker System
25. Design a Service Mesh (basic concept level)

---

## 🔹 **6. Payments & Consistency**

26. Design a Payment Processing System (Stripe-like)
27. Design a Wallet System (balance consistency)
28. Design an Order Management System (e-commerce)
29. Design an Inventory System (avoid overselling)
30. Design a Refund & Reconciliation System

---

# 🔥 How to use these (important)

Don’t just “design” — force yourself to answer:

* What breaks at **10x scale**?
* What happens if **Redis goes down**?
* What happens if **network partitions occur**?
* Where is the **single point of failure**?

---

# 🐛 **20 Real-World Production Bug Scenarios**

These are modeled after real incidents (Google, Netflix, Stripe-style).

---

## 🔹 **1. Database & Query Issues**

1. N+1 query causes p99 latency from 100ms → 5s
2. Missing index → full table scan under load
3. Deadlock between two transactions
4. Connection pool exhaustion → API timeouts
5. Slow replica lag causing stale reads

---

## 🔹 **2. Caching Problems**

6. Cache stampede brings down DB
7. Cache inconsistency after update
8. Hot key causing uneven load
9. Cache eviction removes critical data
10. Redis crash → sudden DB overload

---

## 🔹 **3. Concurrency & Data Integrity**

11. Race condition creates duplicate orders
12. Double payment due to retry logic
13. Lost update problem (no locking)
14. Idempotency key not enforced
15. Event processed twice (at-least-once issue)

---

## 🔹 **4. Distributed System Failures**

16. Cascading timeouts across microservices
17. One slow service causes entire request chain failure
18. Message queue backlog grows indefinitely
19. Partial failure in multi-service transaction
20. Clock drift breaks distributed coordination

---

# 🧱 Suggested Long-Term Plan (REALISTIC)

If you’re serious, run this loop:

### Weekly Structure (repeat for ~10–12 weeks)

* **Mon:** New system design (hard problem)
* **Tue:** Bug analysis
* **Wed:** Pattern deep dive
* **Thu:** Improve Monday design
* **Fri:** Post-mortem

---