# Bug Case: [Short Description of the Bug]

**Date:** YYYY-MM-DD  
**Author:** @username  
**Severity:** P0 (Critical) | P1 (High) | P2 (Medium) | P3 (Low)  
**Status:** Investigating | Root Cause Identified | Fixed | Closed

---

## 1. Symptom

> What was observed? Describe exactly what users or monitors saw — not why, just what.

- **Reported by:** (User / Monitor / Alert / Teammate)
- **First observed:** YYYY-MM-DD HH:MM UTC
- **Frequency:** Constant | Intermittent | One-time
- **Affected scope:** All users | Specific region | Specific user group | Single user

_Example: "API /checkout returns HTTP 500 for ~15% of requests. Users see an error page. No error on staging."_

---

## 2. Environment

| Field              | Value                                   |
|--------------------|-----------------------------------------|
| Service            | e.g., order-service                     |
| Language / Runtime | e.g., Go 1.21 / Node.js 20             |
| Version / Commit   | e.g., v2.4.1 / `abc123f`               |
| Environment        | Production / Staging / Local            |
| Region             | e.g., us-east-1                         |
| Infrastructure     | e.g., EKS, RDS PostgreSQL 15, Redis 7   |
| Recently deployed? | Yes / No — describe change if yes       |
| Load at time       | e.g., 3× normal traffic due to campaign |

---

## 3. Logs / Signals

> Paste the raw signals that led you here. Preserve exact log lines, stack traces, and metric values.

### Error Logs

```
[2024-01-15 14:23:01 UTC] ERROR order-service: failed to acquire DB connection
  context deadline exceeded (client.Timeout exceeded while awaiting headers)
  goroutine 47 [running]:
  main.processOrder(0xc000123400)
    /app/handlers/order.go:87 +0x1a4
```

### Metrics / Dashboards

- **Error rate:** Spiked from 0.1% → 15% at 14:20 UTC
- **DB connection pool:** Utilization at 100%; wait queue depth = 847
- **Latency (p99):** 12s (normal: 180ms)
- **CPU / Memory:** Normal — ruling out resource exhaustion

### Alerts Triggered

- `order-service-error-rate-high` → PagerDuty at 14:22 UTC
- `db-connection-pool-exhausted` → Slack #incidents at 14:23 UTC

---

## 4. Hypothesis

> List your hypotheses from most to least likely. For each one, describe how you would prove or disprove it.

| # | Hypothesis                                    | How to test / verify                            | Status     |
|---|-----------------------------------------------|-------------------------------------------------|------------|
| 1 | DB connection pool exhausted due to slow query | Check `pg_stat_activity` for long-running queries | ✅ Confirmed |
| 2 | Recent deploy introduced a connection leak     | Compare connection count pre/post deploy        | ❌ Ruled out |
| 3 | Downstream service hanging causing held conns  | Check traces for outbound call duration         | ❌ Ruled out |

---

## 5. Root Cause Analysis

> Write the causal chain. Use "because" chains: X happened because Y, because Z, because...

### Root Cause

_A new feature shipped in `v2.4.1` added a synchronous HTTP call to the `inventory-service` inside a DB transaction. The inventory service began returning slow responses (avg 8s) after a config change. This held DB connections open for the duration of each call, exhausting the pool within minutes under normal traffic._

### Causal Chain

```
Inventory service config change → slow responses (avg 8s)
  → DB transactions held open waiting for HTTP response
    → DB connection pool exhausted (100% utilization, queue = 847)
      → New requests fail immediately with "connection timeout"
        → HTTP 500s returned to users (~15% of checkout attempts)
```

### Contributing Factors

- No timeout was set on the outbound HTTP call to inventory service
- DB connection pool size was not scaled after traffic growth (still at default: 20)
- No circuit breaker existed on inventory service calls
- The DB transaction scope was wider than necessary (HTTP call inside txn was wrong)

---

## 6. Fix

### Immediate Fix (Hotfix / Mitigation)

```bash
# Rolled back v2.4.1 → v2.4.0 to stop the bleeding
kubectl set image deployment/order-service app=order-service:v2.4.0

# Increased connection pool size as a temporary buffer
# Set DB_POOL_MAX_CONNS=50 in ConfigMap
kubectl edit configmap order-service-config
```

### Proper Fix

```go
// Before: HTTP call inside DB transaction, no timeout
func processOrder(ctx context.Context, tx *sql.Tx, orderID string) error {
    // ... DB operations ...
    resp, err := http.Get("http://inventory-service/check/" + orderID) // WRONG
    // ... more DB operations ...
}

// After: HTTP call outside transaction, with timeout
func processOrder(ctx context.Context, db *sql.DB, orderID string) error {
    // Step 1: Check inventory BEFORE opening transaction
    ctx, cancel := context.WithTimeout(ctx, 2*time.Second)
    defer cancel()
    available, err := inventoryClient.Check(ctx, orderID)
    if err != nil {
        return fmt.Errorf("inventory check failed: %w", err)
    }

    // Step 2: Only open DB transaction for DB-only operations
    tx, err := db.BeginTx(ctx, nil)
    // ... DB operations only ...
}
```

### Validation

- [ ] Deployed fix to staging — error rate = 0%
- [ ] Load tested at 2× peak traffic — connection pool stays < 40%
- [ ] Deployed to production — monitored for 30 min, all metrics normal

---

## 7. Prevention

> How do we make sure this class of bug can't happen again (or is caught faster)?

| Category        | Action                                                                 | Owner   | Due Date   |
|-----------------|------------------------------------------------------------------------|---------|------------|
| Code            | Add linter rule: flag HTTP calls inside DB transactions                | Backend | 2024-02-01 |
| Code            | Enforce timeouts on all outbound HTTP clients (circuit breaker pattern)| Backend | 2024-01-25 |
| Infrastructure  | Increase DB pool size; add pool utilization alert at 70%               | SRE     | 2024-01-20 |
| Testing         | Add integration test that simulates slow downstream during checkout    | QE      | 2024-02-01 |
| Process         | Require load test for any change touching the DB transaction path      | EM      | 2024-01-30 |

---

## 8. Lesson Learned

> Distill the single most important engineering insight from this bug. Write it so a new engineer on the team would immediately understand.

**Core lesson:** _Never perform I/O (HTTP calls, external service calls, file reads) inside a database transaction. Transactions hold locks and connections for their full duration. Any external latency directly translates to connection starvation under load._

**Corollaries:**
- Always set timeouts on outbound HTTP clients. No call should be unbounded.
- DB connection pool exhaustion is a cascade multiplier — one slow dependency can take down an entire service.
- Monitor connection pool utilization as a leading indicator, not just error rates.
