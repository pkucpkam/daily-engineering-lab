# System Design: Layered Caching Strategy for High-Read API

**Date:** 2026-04-24
**Author:** @pkucpkam
**Status:** Draft

---

## 1. Problem Statement

> Design a caching strategy for a high-read API (e.g. invoice listing) to reduce latency and database load while maintaining acceptable data freshness.

The system serves frequent read requests from users (mobile/web) for invoice data. Without caching, the database becomes a bottleneck under high traffic. The goal is to introduce multi-layer caching to optimize performance while handling consistency trade-offs.

---

## 2. Requirements

### Functional Requirements

* [x] Users can fetch invoice list by building_id and billing_month
* [x] Users can view updated invoice data after changes
* [x] System should return cached data when possible

### Non-Functional Requirements

| Attribute    | Target                          |
| ------------ | ------------------------------- |
| Availability | 99.9% uptime                    |
| Latency      | p99 < 200ms for read operations |
| Throughput   | 5,000 reads/sec                 |
| Data Volume  | Moderate (invoice-based)        |
| Consistency  | Eventual consistency acceptable |
| Durability   | High (financial data)           |

### Out of Scope

* Real-time push updates (WebSocket)
* Complex distributed cache synchronization across regions
* Full offline support (Service Worker advanced use cases)

---

## 3. Initial Thinking

* Bottleneck likely at **database read throughput**
* Critical failure mode: **stale data after update**
* Trade-off: **freshness vs performance**
* Similar systems:

  * Feed systems (Facebook, Twitter)
  * E-commerce product listing

👉 Key realization:

> The earlier we stop a request, the cheaper it is

---

## 4. Architecture Design

### High-Level Diagram

```
Client
  |
[Service Worker] (optional)
  |
[HTTP Cache / CDN]
  |
[API Gateway]
  |
[Backend Service]
  |
[Redis Cache]
  |
[Database]
```

---

### Component Breakdown

| Component       | Responsibility                     | Technology Choice |
| --------------- | ---------------------------------- | ----------------- |
| Client          | Initiates request                  | Mobile / Web      |
| HTTP Cache      | Avoid unnecessary network calls    | Browser / CDN     |
| Backend Service | Business logic, cache coordination | Node.js / Go      |
| Cache           | Store hot invoice data             | Redis             |
| Database        | Source of truth                    | PostgreSQL        |

---

### API Design (Key Endpoints)

```
GET    /api/v1/invoices?building_id=&billing_month=
GET    /api/v1/invoices/:id
PATCH  /api/v1/invoices/:id
```

---

## 5. Deep Dive

### Database Design

**Schema:**

```sql
CREATE TABLE invoices (
  id              UUID PRIMARY KEY,
  building_id     UUID NOT NULL,
  billing_month   DATE NOT NULL,
  amount          NUMERIC,
  status          TEXT,
  updated_at      TIMESTAMPTZ
);

CREATE INDEX idx_invoices_building_month 
ON invoices(building_id, billing_month);
```

---

### Caching Strategy

| Layer      | What is cached          | TTL  | Invalidation strategy         |
| ---------- | ----------------------- | ---- | ----------------------------- |
| HTTP Cache | GET /invoices response  | 60s  | ETag / Cache-Control          |
| Redis      | invoices by building_id | 120s | Cache Aside + delete on write |
| App-level  | Feature flags / config  | 30s  | Periodic refresh              |

---

### Cache Flow (Cache Aside)

```
1. Request → check Redis
2. Cache hit → return
3. Cache miss → query DB
4. Store in cache
5. Return response
```

---

### Write Flow (Critical)

```
PATCH /invoices/:id

1. Update DB
2. Invalidate cache:
   - Delete key: invoices:building:{id}
```

---

### Queue / Async Processing

* Not required in MVP
* Future:

  * Use queue for cache invalidation events
  * Prevent blocking write requests

---

### Scaling Strategy

| Layer      | Strategy                          |
| ---------- | --------------------------------- |
| API        | Stateless horizontal scaling      |
| DB reads   | Add read replicas if needed       |
| Cache      | Redis with key-based partitioning |
| HTTP Cache | CDN layer for global performance  |

---

## 6. Bottlenecks

| Bottleneck       | Impact               | Mitigation                    |
| ---------------- | -------------------- | ----------------------------- |
| Cache miss spike | DB overload          | Warm-up cache + rate limiting |
| Hot keys         | Uneven load on Redis | Key sharding                  |
| Stale cache      | Incorrect data shown | Aggressive invalidation       |

---

## 7. Failure Handling

### Failure Scenarios

| Failure Scenario      | System Behavior      | Recovery Strategy                |
| --------------------- | -------------------- | -------------------------------- |
| Redis down            | All requests hit DB  | Fallback to DB + circuit breaker |
| Cache not invalidated | Users see stale data | Add strict invalidation logic    |
| HTTP cache too long   | Old data served      | Reduce TTL + ETag validation     |

---

### Observability

* Metrics:

  * Cache hit rate
  * DB query latency
* Logs:

  * Cache miss / hit logs
* Alerts:

  * Cache hit rate drops suddenly
  * DB load spike

---

## 8. Trade-offs

| Decision         | Options Considered           | Chosen Approach           | Why                              |
| ---------------- | ---------------------------- | ------------------------- | -------------------------------- |
| Cache strategy   | Write-through vs Cache Aside | Cache Aside               | Simpler control                  |
| Invalidation     | TTL vs event-driven          | TTL + manual invalidation | Balance simplicity & correctness |
| HTTP cache usage | None vs aggressive           | Moderate (60s TTL)        | Avoid stale UX issues            |

---

## 9. Final Summary

### What I Got Right

* Layered caching reduces load at multiple levels (client → backend)
* Cache Aside keeps system simple and flexible
* Explicit invalidation prevents major consistency issues

---

### What I Would Revisit

* Cache invalidation across multiple services (future scaling)
* Handling cache stampede under high traffic
* Whether Redis cluster is needed at higher scale

---

### Follow-up Questions

* [ ] How to prevent cache stampede effectively?
* [ ] Should we move to event-driven invalidation?
* [ ] How to handle multi-region cache consistency?
* [ ] In what order should caching layers be applied (HTTP Cache, Service Worker, In-memory Cache)? Why?

Chuẩn — câu này đáng để thêm vào vì nó chạm đúng “decision making”, không phải kiến thức tĩnh. Tôi viết lại theo style để ông nhét thẳng vào phần **Follow-up Questions** 👇

---

### Follow-up Questions

* [ ] How to prevent cache stampede effectively?
* [ ] Should we move to event-driven invalidation?
* [ ] How to handle multi-region cache consistency?
* [ ] **In what order should caching layers be applied (HTTP Cache, Service Worker, In-memory Cache)? Why?**

---

### 🧠 Reference Answers

#### ✅ Answer 1 — Theo request flow (recommended)

> Apply caching in the order of request flow:
> **Service Worker → HTTP Cache → Backend (Redis / In-memory)**

**Reasoning:**

* Service Worker: chặn request ngay trên client (không cần network)
* HTTP Cache: tránh gọi backend nếu resource chưa thay đổi
* Backend Cache: giảm tải DB khi request đã tới server

👉 Insight:

> The earlier a request is stopped, the cheaper it is

---

#### ✅ Answer 2 — Theo cost optimization

> Apply cache from cheapest to most expensive layer:

1. Client-side cache (Service Worker)
2. Network-level cache (HTTP / CDN)
3. Server-side cache (Redis)

**Reasoning:**

* Client cache = gần user nhất → gần như free
* HTTP cache = giảm bandwidth + server load
* Redis = tốn memory + cần quản lý invalidation

---

#### ⚖️ Answer 3 — Theo mức độ complexity (practical approach)

> Start with the simplest cache first:

1. HTTP Cache (easy, standardized)
2. Backend Cache (more control, more complexity)
3. Service Worker (advanced, optional)

**Reasoning:**

* HTTP cache: config nhanh, hiệu quả ngay
* Redis: cần design + invalidation
* SW: tăng complexity frontend

--