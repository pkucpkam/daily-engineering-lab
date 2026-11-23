# Concept Decision - SQL vs NoSQL

## 1. Problem Context
Every new system needs a storage layer. The choice between relational (SQL) and non-relational (NoSQL) databases is one of the most consequential early decisions — it shapes how you model data, how you scale, and what kinds of queries you can run efficiently. The wrong choice is expensive to reverse.

---

## 2. Options

### Option A: SQL (Relational Databases — PostgreSQL, MySQL)
- **How it works:** Data is stored in tables with a fixed schema. Relationships are enforced via foreign keys. ACID transactions guarantee consistency. Queries use structured SQL.
- **Pros:**
  - ACID compliance — strong consistency guarantees across multi-row, multi-table operations
  - Relational integrity enforced at the DB level (foreign keys, constraints)
  - Powerful query engine — ad-hoc joins, aggregations, window functions
  - Mature tooling: migrations, ORMs, observability
- **Cons:**
  - Schema changes are painful at scale (ALTER TABLE on a 500M-row table)
  - Vertical scaling is the primary path; horizontal sharding adds significant complexity
  - Less suited for unstructured or highly variable data shapes

### Option B: NoSQL (Document, Key-Value, Wide-Column, Graph — MongoDB, DynamoDB, Cassandra, Redis)
- **How it works:** Data is stored in flexible formats (documents, key-value pairs, wide rows). Schema is often optional or per-document. Designed for horizontal scaling and specific access patterns.
- **Pros:**
  - Horizontal scaling built-in (sharding, replication across nodes)
  - Flexible schema — add fields per document without migrations
  - Optimized for specific access patterns (high read throughput, time-series, graph traversal)
  - Low-latency lookups by primary key
- **Cons:**
  - Eventual consistency by default in many systems — complex to reason about
  - Limited or no multi-document transactions (improving, but still weaker than SQL)
  - Ad-hoc queries and joins are expensive or unsupported
  - Operational complexity grows quickly (managing shards, replication lag, TTLs)

---

## 3. Key Differences
- **Performance:** NoSQL wins for high-throughput, key-based lookups; SQL wins for complex queries with joins
- **Scalability:** NoSQL scales horizontally by design; SQL requires sharding which is complex to operate
- **Complexity:** SQL has predictable behavior; NoSQL pushes complexity to the application layer
- **Consistency:** SQL offers strong ACID consistency; NoSQL often trades consistency for availability (BASE model)
- **Schema flexibility:** NoSQL allows per-document variance; SQL enforces schema uniformity

---

## 4. Real-world Trade-offs
- **When to choose SQL:**
  - Financial data, orders, inventory — anything requiring ACID transactions
  - Complex reporting and analytics with ad-hoc queries
  - Well-understood domain model with clear relationships
  - Startup phase where flexibility to query and explore data matters more than scale
- **When to choose NoSQL:**
  - User activity feeds, event logs, time-series data — high-volume writes with simple access patterns
  - Globally distributed systems needing low-latency local reads
  - Catalog or content with highly variable attributes per record
  - Systems where horizontal scaling is a hard requirement from day one

---

## 5. Production Scenarios

**Stripe (Payments):** Uses PostgreSQL for core financial data. ACID transactions are non-negotiable when debiting one account and crediting another — NoSQL eventual consistency would introduce unacceptable risk.

**Netflix (User Activity):** Uses Cassandra for viewing history and user interaction events. Writes are extremely high-volume, access is primarily by user ID (key-based), and occasional inconsistency in read-your-own-writes is acceptable.

**Instagram (Feed):** Started on PostgreSQL. As scale grew, moved feed storage to Cassandra for horizontal scalability while keeping user and relationship data in PostgreSQL where relational integrity matters.

**MongoDB at Expedia:** Used for hotel content/catalog where each property can have a wildly different set of attributes. A rigid SQL schema would require hundreds of nullable columns.

---

## 6. Common Mistakes
- Choosing NoSQL because it "scales better" without defining what scale problem you actually have today — SQL with proper indexing handles millions of rows easily
- Storing financial or transactional data in an eventually consistent NoSQL store — subtle double-spend or inconsistency bugs appear under load
- Using NoSQL as a document dump and then writing complex application-side joins that the DB would have handled for free
- Ignoring the CAP theorem: picking a NoSQL DB without understanding whether it sacrifices Consistency or Availability during a network partition
- Treating NoSQL schema flexibility as a feature rather than a risk — lack of schema validation leads to messy data that's hard to query or migrate

---

## 7. Final Decision Rule (IMPORTANT)

👉 If you need ACID transactions, complex queries, or relational integrity → use **SQL**  
👉 If you need horizontal scale-out, flexible schema, or a specific access pattern (key lookups, time-series, graph) → use **NoSQL**  
👉 If unsure and in early stage → default to **SQL** — it's easier to migrate to NoSQL later than to retrofit transactions into an eventually consistent store  

---

## 8. Lesson Learned
SQL vs NoSQL is not a binary war — most mature production systems use both. The real skill is knowing which data belongs where. Financial and transactional state belongs in SQL. High-volume event streams, caches, and flexible content belong in NoSQL. Design your storage layer around your access patterns and consistency requirements, not hype or trend.
