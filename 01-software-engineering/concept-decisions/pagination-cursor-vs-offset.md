# Concept Decision - Pagination: Cursor vs Offset

## 1. Problem Context
Every API or UI that lists data needs pagination. The choice between offset and cursor pagination determines how your system behaves under load — especially when tables grow into millions of rows or when real-time data is being inserted frequently.

---

## 2. Options

### Option A: Offset Pagination
- **How it works:** Uses `LIMIT` and `OFFSET` in SQL. The DB scans and skips the first N rows, then returns the next page.
- **Pros:**
  - Simple to implement and understand
  - Supports random access (jump to page 50 directly)
  - Stateless — no server-side cursor state needed
- **Cons:**
  - Performance degrades with large offsets (`OFFSET 1000000` forces DB to scan 1M rows)
  - Skipped/duplicated rows if data is inserted or deleted between page requests
  - Indexes are largely bypassed for high-offset queries

### Option B: Cursor Pagination
- **How it works:** Uses a stable, encoded pointer (usually the last seen row's ID or timestamp) to fetch the next page. The DB seeks directly to that row and reads forward.
- **Pros:**
  - Consistent O(1) seek performance regardless of page depth
  - Stable — no skipped or duplicated rows during concurrent writes
  - Ideal for infinite scroll and real-time feeds
- **Cons:**
  - No random page access (can't jump to page 50)
  - Slightly more complex to implement
  - Requires a stable, sortable cursor field (e.g., monotonic ID or created_at + id)

---

## 3. Key Differences
- **Performance:** Offset degrades linearly with depth; cursor is constant-time via index seek
- **Scalability:** Offset breaks past ~100K rows in practice; cursor scales to billions
- **Complexity:** Offset is trivial; cursor requires encoding, decoding, and careful sort order design
- **Consistency:** Offset is prone to drift during writes; cursor is stable

---

## 4. Real-world Trade-offs
- **When to choose Offset:**
  - Admin panels with low row counts where random page access matters
  - Reports or exports where the user explicitly navigates to specific pages
  - Internal tools where simplicity outweighs performance
- **When to choose Cursor:**
  - Public APIs serving large datasets (social feeds, product listings)
  - Mobile apps using infinite scroll
  - Any table expected to grow beyond tens of thousands of rows
  - Real-time data where rows are being inserted continuously

---

## 5. Production Scenarios

**Twitter/X Feed:** Uses cursor-based pagination keyed on tweet IDs (snowflake). Fetching "older tweets" passes the last seen tweet ID as cursor. This is stable even when new tweets are inserted at the top.

**Stripe API:** All list endpoints use cursor pagination. The cursor is the object ID of the last item returned. Offset pagination would be unreliable because payments are created continuously.

**E-commerce Search (Elasticsearch):** Uses `search_after` (cursor equivalent) for deep pagination instead of `from/size` (offset equivalent). Elasticsearch explicitly discourages using `from/size` beyond 10,000 results.

**GitHub API:** All paginated endpoints support both, but for automation (iterating all issues/PRs) cursor-based is recommended to avoid drift.

---

## 6. Common Mistakes
- Using offset pagination on tables that will grow large, then scrambling to migrate later
- Using `created_at` alone as a cursor without a secondary sort key (e.g., `id`), causing instability when two rows share the same timestamp
- Exposing raw database IDs or internal state in the cursor — always encode/opaque the cursor
- Forgetting that offset pagination under concurrent writes can skip rows silently (looks like a data bug, not a pagination bug)
- Using cursor pagination for admin UIs where users expect to navigate to page 37 directly

---

## 7. Final Decision Rule (IMPORTANT)

👉 If the dataset is large (>100K rows), has concurrent writes, or is consumed by infinite scroll → use **Cursor**  
👉 If the dataset is small, static, and users need random page access → use **Offset**  

---

## 8. Lesson Learned
Offset feels simple and is fine for prototypes, but the performance cliff at large offsets is real and expensive to fix in production. Cursor pagination requires slightly more upfront design but eliminates a class of scalability and consistency bugs. Default to cursor for any new API endpoint that paginated data which might grow.
