# System Design: [Problem Title]

**Date:** YYYY-MM-DD  
**Author:** @username  
**Status:** Draft | In Review | Final

---

## 1. Problem Statement

> What exactly are we building? What problem does it solve? Who uses it?

_Write 2–4 sentences describing the system. Be specific. Avoid vague descriptions like "a scalable platform"._

---

## 2. Requirements

### Functional Requirements

- [ ] Requirement 1 (e.g., Users can upload images up to 10 MB)
- [ ] Requirement 2
- [ ] Requirement 3

### Non-Functional Requirements

| Attribute      | Target                          |
|----------------|---------------------------------|
| Availability   | 99.99% uptime                   |
| Latency        | p99 < 200ms for read operations |
| Throughput     | 10,000 writes/sec               |
| Data Volume    | 1 TB/day ingestion              |
| Consistency    | Eventual / Strong               |
| Durability     | 99.999999999% (11 nines)        |

### Out of Scope

- _List things you are explicitly NOT designing for in this iteration._

---

## 3. Initial Thinking

> Before drawing boxes and arrows — write your first instincts. What's hard about this problem?

- Where is the bottleneck likely to be?
- What's the most critical failure mode?
- What trade-offs will dominate this design?
- What have I seen before that is similar?

_This section is your unfiltered thinking. Don't skip it._

---

## 4. Architecture Design

### High-Level Diagram

```
[Client] --> [API Gateway / Load Balancer]
                     |
          +----------+----------+
          |                     |
     [Service A]           [Service B]
          |                     |
     [Database]            [Cache / Queue]
```

_Replace with an accurate diagram for your system. Use ASCII or attach an image._

### Component Breakdown

| Component       | Responsibility                        | Technology Choice |
|-----------------|---------------------------------------|-------------------|
| API Gateway     | Routing, auth, rate limiting          | Kong / AWS ALB    |
| Service A       | Core business logic                   | Go / Node.js      |
| Database        | Persistent storage                    | PostgreSQL        |
| Cache           | Low-latency reads, session state      | Redis             |
| Message Queue   | Async processing, decoupling          | Kafka / SQS       |

### API Design (Key Endpoints)

```
POST   /api/v1/resource          → Create resource
GET    /api/v1/resource/:id      → Get by ID
PUT    /api/v1/resource/:id      → Update
DELETE /api/v1/resource/:id      → Delete
GET    /api/v1/resource?filter=  → List / search
```

---

## 5. Deep Dive

### Database Design

**Schema:**

```sql
CREATE TABLE resource (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id     UUID NOT NULL REFERENCES users(id),
  payload     JSONB,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_resource_user_id ON resource(user_id);
```

**Sharding / Partitioning strategy:**  
_Partition by `created_at` for time-series data, shard by `user_id` for user-centric data._

### Caching Strategy

| Layer          | What is cached          | TTL    | Invalidation strategy       |
|----------------|-------------------------|--------|-----------------------------|
| CDN            | Static assets           | 24h    | Cache-busting via versioned URLs |
| Redis          | User session, hot reads | 5 min  | Write-through / TTL expiry  |
| App-level      | Config, feature flags   | 60 sec | Background refresh          |

### Queue / Async Processing

- **Why async?** _(e.g., Decouple slow email sending from user-facing API)_
- **Queue:** Kafka / SQS / RabbitMQ
- **Consumer design:** At-least-once delivery; idempotent consumers required
- **Dead letter queue:** Yes — retry 3x, then DLQ with alerting

### Scaling Strategy

| Layer      | Strategy                                                    |
|------------|-------------------------------------------------------------|
| API        | Horizontal scaling behind load balancer; stateless services |
| DB reads   | Read replicas with replica lag monitoring                   |
| DB writes  | Connection pooling (PgBouncer); vertical scaling initially  |
| Cache      | Redis Cluster with consistent hashing                       |
| Storage    | Object storage (S3/GCS) for blobs; DB for metadata only     |

---

## 6. Bottlenecks

> Where will this system break under load? Be honest.

| Bottleneck         | Impact                             | Mitigation                          |
|--------------------|------------------------------------|-------------------------------------|
| DB write throughput| Writes pile up under spike         | Queue-based write buffer + batching |
| Hot keys in cache  | Cache eviction storms              | Key sharding + local in-proc cache  |
| Single region      | Regional outage = full downtime    | Multi-region active-passive failover|

---

## 7. Failure Handling

### Failure Scenarios

| Failure Scenario       | System Behavior                        | Recovery Strategy                  |
|------------------------|----------------------------------------|------------------------------------|
| DB primary goes down   | Writes fail, reads continue via replica| Auto-failover (RDS Multi-AZ / Patroni) |
| Cache miss storm       | All traffic hits DB                    | Circuit breaker + cache warm-up    |
| Consumer lag spikes    | Queue depth grows; latency increases   | Auto-scaling consumers; backpressure|
| Downstream service down| Calls timeout                          | Timeout + retry with exponential backoff + fallback |

### Observability

- **Metrics:** Request rate, error rate, p50/p99 latency (USE/RED methods)
- **Logs:** Structured JSON logs with trace ID correlation
- **Tracing:** Distributed tracing across all services (OpenTelemetry)
- **Alerts:** PagerDuty on error rate > 1%, latency p99 > 500ms

---

## 8. Trade-offs

> Every design choice is a trade-off. Document the ones you made consciously.

| Decision                      | Options Considered              | Chosen Approach        | Why                                      |
|-------------------------------|---------------------------------|------------------------|------------------------------------------|
| SQL vs NoSQL                  | PostgreSQL, DynamoDB, MongoDB   | PostgreSQL             | Strong consistency, complex queries      |
| Sync vs Async processing      | Direct API call vs Kafka        | Kafka                  | Decouple slow consumers from API latency |
| Cache invalidation strategy   | TTL vs event-driven invalidation| TTL + write-through    | Simpler ops; acceptable staleness        |
| Monolith vs Microservices     | Single app vs service mesh      | Modular monolith first | Avoid premature complexity               |

---

## 9. Final Summary

### What I Got Right

- _Design decision 1 and why it's solid_
- _Design decision 2 and why it's solid_

### What I Would Revisit

- _Area I'm uncertain about — e.g., "Not sure if sharding key choice will hold at 10× scale"_
- _Area — e.g., "Cache invalidation on cross-service writes needs a cleaner pattern"_

### Follow-up Questions

- [ ] How does the system behave during a rolling deployment?
- [ ] What's the data migration strategy for schema changes?
- [ ] How do we handle multi-tenancy isolation?
