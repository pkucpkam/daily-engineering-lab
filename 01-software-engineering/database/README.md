# Database Internals (PostgreSQL)

PostgreSQL internals: storage, index, planner, MVCC, isolation, locking, connection pool.

> Mỗi bài là một thư mục: `README.md` (lý thuyết) · `exercises.md` · `solutions.md` · `lab/` · `my-notes.md` (người học tự viết).

---

<!-- CURRICULUM:START — sinh tự động từ CURRICULUM, cột Status cập nhật tay -->
## 📚 Danh sách bài

Loại: **Bài học** · Template: [lesson-template.md](../../docs/templates/lesson-template.md) · Quy chuẩn: [CONTENT-GUIDE](../../docs/CONTENT-GUIDE.md)

| ID | Bài | Giai đoạn | Status |
| :--- | :--- | :--- | :---: |
| DB-01 | [Storage: page, heap tuple, TOAST](01-storage-page-heap-toast/README.md) | Năm 1 · Q2 | 🔲 |
| DB-02 | [WAL, checkpoint & durability](02-wal-durability/README.md) | Năm 1 · Q2 | 🔲 |
| DB-03 | [B-tree index internals](03-btree-index-internals/README.md) | Năm 1 · Q2 | 🔲 |
| DB-04 | [Hash, GIN, GiST, BRIN, partial & expression index](04-index-types/README.md) | Năm 1 · Q2 | 🔲 |
| DB-05 | [Composite & covering index](05-composite-covering-index/README.md) | Năm 1 · Q2 | 🔲 |
| DB-06 | [EXPLAIN ANALYZE & Query planner](06-explain-analyze-planner/README.md) | Năm 1 · Q2 | 🔲 |
| DB-07 | [Statistics & bad plans](07-statistics-bad-plans/README.md) | Năm 1 · Q2 | 🔲 |
| DB-08 | [MVCC: xmin, xmax, snapshot](08-mvcc/README.md) | Năm 1 · Q2 | 🔲 |
| DB-09 | [VACUUM, autovacuum & bloat](09-vacuum-bloat/README.md) | Năm 1 · Q2 | 🔲 |
| DB-10 | [Isolation levels & anomalies](10-isolation-levels-anomalies/README.md) | Năm 1 · Q2 | 🔲 |
| DB-11 | [Write skew & SERIALIZABLE (SSI)](11-write-skew-serializable/README.md) | Năm 1 · Q2 | 🔲 |
| DB-12 | [Row locks, FOR UPDATE, SKIP LOCKED](12-row-locks-skip-locked/README.md) | Năm 1 · Q2 | 🔲 |
| DB-13 | [Advisory locks & lock contention](13-advisory-locks-contention/README.md) | Năm 1 · Q2 | 🔲 |
| DB-14 | [Connection pool: HikariCP sizing & exhaustion](14-connection-pool-sizing/README.md) | Năm 1 · Q2 | 🔲 |
| DB-15 | [Timeout chain: client → app → pool → DB](15-timeout-chain/README.md) | Năm 1 · Q2 | 🔲 |

**Tài liệu gốc của chủ đề:** PostgreSQL Docs · Database Internals (Petrov) · Use The Index, Luke (use-the-index-luke.com) · The Internals of PostgreSQL (interdb.jp)

<!-- CURRICULUM:END -->
