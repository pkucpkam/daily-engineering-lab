# Bug Case: React Render Bottleneck on Large List (Fixed with Virtualization)

**Date:** 2026-04-23
**Author:** @pkucpkam
**Severity:** P2 (Medium)
**Status:** Fixed

---

## 1. Symptom

> UI becomes noticeably laggy when scrolling a large list. Users experience stuttering and delayed rendering.

* **Reported by:** Self (manual testing)
* **First observed:** 2026-04-23 09:30 UTC
* **Frequency:** Constant (when list size is large)
* **Affected scope:** All users viewing large datasets (~1000+ items)

*Example: "Scrolling a list of ~1500 items causes visible lag. FPS drops significantly. No issue with small datasets (<100 items)."*

---

## 2. Environment

| Field              | Value                           |
| ------------------ | ------------------------------- |
| Service            | frontend-app                    |
| Language / Runtime | React (TypeScript)              |
| Version / Commit   | local dev                       |
| Environment        | Local                           |
| Region             | N/A                             |
| Infrastructure     | Browser (Chrome), DOM rendering |
| Recently deployed? | No                              |
| Load at time       | Rendering ~1500 list items      |

---

## 3. Logs / Signals

> Signals collected via browser devtools and profiling tools.

### Error Logs

*No explicit runtime errors.*

### Metrics / Observations

* **FPS (scrolling):** Drops significantly (visible stutter)
* **DOM nodes:** ~1500+ elements rendered at once
* **React DevTools:**

  * Entire list re-renders on scroll
* **Performance tab:**

  * Long frames (>16ms)
  * Heavy scripting + rendering time

### Alerts Triggered

*None (manual observation).*

---

## 4. Hypothesis

> Potential causes of performance degradation.

| # | Hypothesis                                 | How to test / verify                   | Status      |
| - | ------------------------------------------ | -------------------------------------- | ----------- |
| 1 | Too many DOM nodes rendered simultaneously | Inspect DOM size                       | ✅ Confirmed |
| 2 | Unnecessary re-renders of list items       | Use React DevTools (highlight updates) | ✅ Confirmed |
| 3 | Expensive item rendering logic             | Profile component render time          | ❌ Minor     |
| 4 | Browser resource limitation (CPU/memory)   | Monitor system resources               | ❌ Ruled out |

---

## 5. Root Cause Analysis

### Root Cause

*The application renders the entire dataset (~1500 items) into the DOM at once. This creates excessive DOM nodes and causes heavy rendering work on each scroll event, leading to frame drops and poor UI responsiveness.*

### Causal Chain

```
Large dataset (~1500 items)
  → All items rendered into DOM simultaneously
    → DOM size becomes large
      → Browser must recalculate layout + paint many elements
        → Frame time exceeds 16ms budget
          → FPS drops → UI lag during scroll
```

### Contributing Factors

* No limit on number of rendered items
* No windowing / virtualization applied
* Full list re-render triggered on state updates
* Assumption that rendering cost is negligible

---

## 6. Fix

### Immediate Fix (Mitigation)

* Reduced dataset size manually for testing

---

### Proper Fix

👉 Apply **list virtualization**

```tsx
import { FixedSizeList as List } from 'react-window';

<List
  height={500}
  itemCount={items.length}
  itemSize={50}
  width="100%"
>
  {({ index, style }) => (
    <div style={style}>
      {items[index].name}
    </div>
  )}
</List>
```

### Key Changes

* Only render items within the viewport (~20–30 items)
* Use a fixed container height
* Simulate full list height via virtualization

---

### Validation

* [x] DOM nodes reduced from ~1500 → ~30
* [x] Scrolling becomes smooth (no visible lag)
* [x] Frame time stays within 16ms budget
* [x] No functional regression

---

## 7. Prevention

> Prevent similar performance issues in the future.

| Category   | Action                                                             | Owner    | Due Date |
| ---------- | ------------------------------------------------------------------ | -------- | -------- |
| Code       | Avoid rendering large lists without pagination or virtualization   | Frontend | Ongoing  |
| Code       | Introduce guideline: >100 items → consider virtualization          | Frontend | Ongoing  |
| Testing    | Add performance testing for large datasets                         | Frontend | TBD      |
| Process    | Review UI components for scalability during design phase           | Team     | Ongoing  |
| Monitoring | Use React Profiler in development for performance-critical screens | Frontend | Ongoing  |

---

## 8. Lesson Learned

> Core engineering insight from this issue.

**Core lesson:**
*Rendering cost scales with the number of DOM nodes. Unbounded rendering is equivalent to unbounded queries in backend systems — both will eventually become bottlenecks.*

**Corollaries:**

* Virtualization in frontend ≈ pagination in backend
* Always design UI for worst-case dataset size, not average
* Performance issues are often architectural, not micro-optimizations
* Reducing work (render less) is more effective than optimizing work
