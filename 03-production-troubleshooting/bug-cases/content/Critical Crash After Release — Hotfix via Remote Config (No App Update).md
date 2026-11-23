
# Bug Case: Critical Crash After Release — Hotfix via Remote Config (No App Update)

**Date:** 2026-04-27
**Author:** @pkucpkam
**Severity:** P0 (Critical)
**Status:** Fixed

---

## 1. Symptom

> What was observed? Describe exactly what users or monitors saw — not why, just what.

* **Reported by:** Firebase Crashlytics + User reviews
* **First observed:** 2026-04-27 01:40 UTC
* **Frequency:** Constant (~60% affected users)
* **Affected scope:** Users accessing “New Dashboard Feature”

*Example:*
"App crashes immediately when opening dashboard screen after login."

---

## 2. Environment

| Field              | Value                               |
| ------------------ | ----------------------------------- |
| Service            | mobile-app (Flutter)                |
| Language / Runtime | Dart 3.x / Flutter 3.x              |
| Version / Commit   | v2.0.0 / `d12fa91`                  |
| Environment        | Production                          |
| Region             | Global                              |
| Infrastructure     | Firebase Remote Config, REST API    |
| Recently deployed? | Yes — new dashboard feature rollout |
| Load at time       | Normal                              |

---

## 3. Logs / Signals

### Error Logs

```id="k79h5l"
Fatal Exception: NoSuchMethodError
The getter 'items' was called on null
#0 DashboardView.build (dashboard_view.dart:58)
```

---

### Metrics / Dashboards

* **Crash rate:** 0.5% → 22% (within 10 minutes of release)
* **Feature usage:** 70% users hit dashboard screen
* **Retention:** Dropped sharply

---

### Alerts Triggered

* `crash-rate-critical` (Crashlytics)
* Spike in session drop-off

---

## 4. Hypothesis

| # | Hypothesis                           | How to test / verify                    | Status      |
| - | ------------------------------------ | --------------------------------------- | ----------- |
| 1 | API response missing `items` field   | Inspect API response in production logs | ✅ Confirmed |
| 2 | Backend deployed incompatible schema | Check backend deployment timeline       | ✅ Confirmed |
| 3 | App not handling null response       | Review UI rendering logic               | ✅ Confirmed |
| 4 | Cache returning corrupted data       | Clear cache and retry                   | ❌ Ruled out |

---

## 5. Root Cause Analysis

### Root Cause

*A new dashboard feature depended on a backend field `items`. A backend deployment introduced a scenario where `items = null`. The mobile app assumed it was always non-null and accessed it directly, causing a crash.*

---

### Causal Chain

```id="4i0q6p"
New dashboard feature released
  → Backend returns null for "items" in some cases
    → App assumes non-null and renders UI
      → Null access triggers runtime crash
        → Users opening dashboard experience crash
```

---

### Contributing Factors

* No null-safety handling in UI
* Tight coupling between mobile and backend schema
* No feature flag / kill switch at release time
* No contract validation between app and API

---

## 6. Fix

### 🚨 Immediate Fix (Hotfix WITHOUT app update)

👉 **Disable feature via Remote Config (kill switch)**

```dart
final isDashboardEnabled = remoteConfig.getBool("dashboard_enabled");

if (!isDashboardEnabled) {
  return FallbackScreen();
}
```

### Remote Config Change

```json
{
  "dashboard_enabled": false
}
```

👉 Effect:

* Feature instantly disabled for all users
* Crash rate drops without app update
* No need to wait for store review

---

### Alternative Hotfix Options

#### 1. Backend Fix (Preferred if fast)

```json
{
  "items": []
}
```

→ Ensure API always returns safe default

---

#### 2. Feature Flag Rollback

* Disable only for affected cohort
* Gradually re-enable after fix

---

### Proper Fix (Long-term)

```dart
final items = response.items ?? [];

if (items.isEmpty) {
  return EmptyStateWidget();
}
```

---

### Validation

* [x] Remote config applied → crash rate drops from 22% → 1%
* [x] Backend patched → API always returns safe data
* [x] Re-enabled feature gradually → no regression

---

## 7. Prevention

| Category     | Action                                                   | Owner   | Due Date   |
| ------------ | -------------------------------------------------------- | ------- | ---------- |
| Code         | Enforce null-safe UI rendering                           | Mobile  | 2026-05-01 |
| Architecture | Add feature flag for all risky features                  | Mobile  | 2026-05-02 |
| Backend      | Enforce API contract (never return null for collections) | Backend | 2026-04-30 |
| Testing      | Add contract tests between mobile & backend              | QA      | 2026-05-03 |
| Monitoring   | Alert on crash spike > 5% within 5 minutes               | DevOps  | 2026-04-28 |

---

## 8. Lesson Learned

**Core lesson:**
*If you cannot turn a feature OFF in production instantly, you do not control your system.*

---

**Corollaries:**

* Feature flags are not optional — they are survival tools
* Backend should never break client assumptions silently
* The fastest fix is often not code — it’s configuration
* Always design for **failure containment**, not just correctness

---