# Post-Mortem: [Incident Title]

**Date of Incident:** YYYY-MM-DD  
**Date of Post-Mortem:** YYYY-MM-DD  
**Author:** @username  
**Reviewers:** @teammate1, @teammate2  
**Severity:** SEV-1 | SEV-2 | SEV-3  
**Status:** Draft | In Review | Published

> **Blameless principle:** This document is about systems and processes, not individuals. The goal is learning and prevention — not accountability.

---

## 1. Incident Summary

> One paragraph. Describe what happened, when, how long it lasted, and the high-level impact. A reader should understand the full picture after reading this alone.

_Example: On 2024-01-15 between 14:20–16:45 UTC (~2h25m), the checkout service experienced a 15% error rate on all payment requests due to database connection pool exhaustion. Approximately 12,400 checkout attempts failed, resulting in an estimated $87,000 in lost revenue and a spike in customer support tickets. The root cause was a slow outbound HTTP call to the inventory service being made inside a database transaction, introduced in a deploy at 14:05 UTC._

---

## 2. Timeline

> Chronological log of events. Include detection, escalation, investigation, mitigation, and resolution. Use UTC. Be precise.

| Time (UTC)      | Event                                                                     |
|-----------------|---------------------------------------------------------------------------|
| 14:05           | `v2.4.1` deployed to production (order-service)                           |
| 14:18           | Inventory service config change applied (response time degrades to ~8s)   |
| 14:20           | Error rate begins climbing; DB connection pool hits 90% utilization       |
| 14:22           | `order-service-error-rate-high` alert fires → PagerDuty pages on-call     |
| 14:25           | On-call engineer acknowledges; begins investigation                       |
| 14:35           | Hypothesis: DB pool exhaustion confirmed via `pg_stat_activity`           |
| 14:50           | Root cause identified: HTTP call inside DB transaction + inventory latency |
| 15:00           | Decision made to rollback `v2.4.1` → `v2.4.0`                            |
| 15:10           | Rollback complete; error rate drops from 15% → 0.1%                      |
| 15:20           | Incident declared resolved; monitoring continues                          |
| 16:45           | All metrics stable; incident fully closed                                 |
| 2024-01-17      | Post-mortem meeting held with full team                                   |

---

## 3. Impact

> Quantify everything you can. Vague impact statements ("some users affected") are not acceptable.

### User Impact

| Metric                        | Value                              |
|-------------------------------|------------------------------------|
| Duration                      | 2 hours 25 minutes                 |
| Affected users (estimated)    | ~41,000 unique users               |
| Failed transactions           | ~12,400 checkout attempts          |
| Error rate at peak            | 15% of all checkout requests       |
| Customer support tickets      | +340 tickets vs. daily average     |

### Business Impact

| Metric                        | Value                              |
|-------------------------------|------------------------------------|
| Estimated revenue loss        | ~$87,000                           |
| SLA breach                    | Yes — breached 99.9% monthly SLA   |
| Reputational risk             | Social media mentions spiked 4×    |

### System Impact

| Metric                        | Value                              |
|-------------------------------|------------------------------------|
| Services affected             | order-service, downstream webhooks |
| DB connection pool            | 100% utilized, queue depth 847     |
| p99 latency at peak           | 12s (normal: 180ms)                |

---

## 4. Root Cause

> The single, specific, technical root cause. Then list contributing factors separately.

### Root Cause

_The `v2.4.1` release introduced a synchronous HTTP call to the `inventory-service` inside a PostgreSQL database transaction. When the inventory service began responding slowly (avg 8s) due to an unrelated config change at 14:18 UTC, database connections were held open for the full duration of each HTTP call. Under normal traffic, the default connection pool of 20 connections was exhausted within 2 minutes, causing all subsequent requests to fail immediately with a timeout error._

### Contributing Factors

1. **No timeout on outbound HTTP call** — The inventory service client had no configured timeout, making latency unbounded.
2. **Oversized transaction scope** — The HTTP call was placed inside a DB transaction where no DB operations depended on its result.
3. **Undersized connection pool** — The pool limit (20 connections) had not been revisited since traffic grew 3× over 6 months.
4. **No circuit breaker** — There was no mechanism to fail fast when the inventory service was degraded.
5. **No pool utilization alert** — Alerts were only on error rate, not on pool utilization. The problem was already critical before an alert fired.
6. **Insufficient load testing** — The change was not load tested against a degraded dependency scenario.

---

## 5. Resolution

### Immediate Actions Taken

1. **Rollback:** Deployed `v2.4.0` at 15:00 UTC, restoring pre-incident behavior.
2. **Pool increase:** Temporarily increased `DB_POOL_MAX_CONNS` from 20 → 50 to provide headroom.
3. **Inventory service fix:** Inventory team reverted the config change that caused slow responses.

### Why It Worked

Rolling back removed the code path that placed the HTTP call inside the transaction. The connection pool recovered within ~90 seconds of rollback completion as in-flight requests drained.

---

## 6. Prevention Plan

> For each root cause and contributing factor, list a concrete preventive action. Vague actions ("improve monitoring") are not acceptable.

| # | Problem                                     | Action                                                                 | Type          | Owner   | Due Date   |
|---|---------------------------------------------|------------------------------------------------------------------------|---------------|---------|------------|
| 1 | HTTP I/O inside DB transaction              | Add static analysis rule to flag HTTP calls within DB transaction scope| Code / Linter | Backend | 2024-02-01 |
| 2 | No timeout on HTTP client                   | Enforce default 2s timeout on all outbound HTTP clients in service template | Code   | Platform| 2024-01-25 |
| 3 | No circuit breaker                          | Implement circuit breaker on inventory service client (Hystrix / Resilience4j) | Code | Backend | 2024-02-01 |
| 4 | Undersized connection pool                  | Audit and right-size pool limits for all services; add pool utilization alert at 70% | Infra | SRE | 2024-01-22 |
| 5 | Late alert (error rate, not leading signals)| Add alert on DB pool utilization > 70% for 2 min                      | Monitoring    | SRE     | 2024-01-22 |
| 6 | No degraded-dependency load test            | Add chaos engineering test (slow downstream) to pre-deploy checklist   | Testing       | QE      | 2024-02-15 |

---

## 7. Action Items

> Concrete, assigned, time-boxed tasks. Each item must have an owner and a due date.

| Action Item                                               | Owner     | Due Date   | Status      |
|-----------------------------------------------------------|-----------|------------|-------------|
| Add linter rule: HTTP calls inside DB transactions        | @dev1     | 2024-02-01 | In Progress |
| Enforce 2s timeout on all HTTP clients in base template   | @dev2     | 2024-01-25 | Not Started |
| Implement circuit breaker for inventory client            | @dev1     | 2024-02-01 | Not Started |
| Increase + alert on DB pool utilization (all services)    | @sre1     | 2024-01-22 | Done ✅     |
| Add pool utilization dashboard to runbook                 | @sre2     | 2024-01-25 | Not Started |
| Write integration test: slow downstream during checkout   | @qa1      | 2024-02-01 | Not Started |
| Update service template: timeouts, pool size defaults     | @platform1| 2024-01-30 | Not Started |
| Share learnings in engineering all-hands                  | @em1      | 2024-01-25 | Not Started |

---

## 8. What Went Well

> Acknowledge effective responses — this reinforces good practices.

- On-call engineer acknowledged the alert within 3 minutes
- Rollback decision was made quickly and confidently without lengthy approval chains
- Incident channel was well-organized; communication to stakeholders was clear
- DB team was available immediately and provided `pg_stat_activity` query results within 10 minutes

## 9. What Could Have Gone Better

- Alert fired 2 minutes after degradation started — pool utilization alert would have given us earlier warning
- Root cause took 25 minutes to identify — better distributed tracing would have pointed directly to the slow inventory call
- No runbook for "DB connection pool exhausted" scenario — on-call had to improvise
- Post-deploy monitoring window was only 5 minutes; should be 15 minutes minimum for high-risk changes

---

_This post-mortem was reviewed and approved by the team on YYYY-MM-DD. All action items are tracked in [JIRA/Linear/GitHub Issues]._
