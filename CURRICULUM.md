# CURRICULUM — Danh mục toàn bộ nội dung

> **Nguồn sự thật** cho mọi bài học, bài tập, lab trong repo. Mỗi dòng = 1 đơn vị nội dung có ID cố định.
> Quy chuẩn soạn: [docs/CONTENT-GUIDE.md](docs/CONTENT-GUIDE.md) · Prompt cho agent: [docs/AGENT-PROMPT.md](docs/AGENT-PROMPT.md) · Lộ trình: [ROADMAP.md](ROADMAP.md)

**Status:** 🔲 todo · ✍️ draft (agent soạn xong) · 👀 review (người học đang học) · ✅ done (qua explain-back)

## Tổng quan

| Loại | Số lượng |
| :--- | ---: |
| Bài học | 178 |
| System design | 7 |
| Pattern card | 20 |
| Concept decision | 11 |
| Incident lab | 18 |
| **Tổng** | **234** |

### Tiến độ tổng hợp (cập nhật 2026-11-27)

> **Tổng tiến độ:** `43 / 234` (**18.4%**) đã soạn giáo trình (`✍️ draft`) · `0` đang học (`👀 review`) · `0` hoàn thành explain-back (`✅ done`) · `191` chưa soạn (`🔲 todo`).

#### Theo giai đoạn

| Giai đoạn | Tổng | ✍️ Draft | 👀 Review | ✅ Done | 🔲 Todo | % Đã soạn | Trạng thái |
| :--- | ---: | ---: | ---: | ---: | ---: | ---: | :--- |
| **Quý 0 · Bridge** | 9 | 9 | 0 | 0 | 0 | 100.0% | Đã soạn đủ (`GIT-01..04`, `READ-01..03`, `JVM-01..02`) |
| **Năm 1 · Q1** | 41 | 34 | 0 | 0 | 7 | 82.9% | Đã soạn `JVM-03..10`, `CQ-01..08`, `AI-01`, `LNX-01..15`, `DSA-01..02`; còn `DSA-03..07`, `INC-07..08` |
| **Năm 1 · Q2** | 40 | 0 | 0 | 0 | 40 | 0.0% | Chờ soạn (`CONC`, `DB`, `PERF`, `CD-01..03`, `INC-01..06`) |
| **Năm 1 · Q3** | 46 | 0 | 0 | 0 | 46 | 0.0% | Chờ soạn (`ARCH`, `PAT`, `CD-07..08`, `CTR-01..06`, `CICD-01..02`, `INC-09..13`) |
| **Năm 1 · Q4** | 42 | 0 | 0 | 0 | 42 | 0.0% | Chờ soạn (`DIST`, `SEC`, `AI-02..04`, `SD-01..07`, `CD-04..06`, `AWS-01..08`, `INC-14..15`) |
| **Năm 2 · Q1** | 13 | 0 | 0 | 0 | 13 | 0.0% | Chờ soạn (`AI-05..06`, `CD-09`, `AWS-09`, `IAC-01..09`) |
| **Năm 2 · Q2** | 9 | 0 | 0 | 0 | 9 | 0.0% | Chờ soạn (`AI-07`, `CD-11`, `CICD-03..05`, `CICD-07..10`) |
| **Năm 2 · Q3** | 16 | 0 | 0 | 0 | 16 | 0.0% | Chờ soạn (`CD-10`, `CTR-07..11,13..18`, `CICD-11`, `INC-16..18`) |
| **Năm 2 · Q4** | 15 | 0 | 0 | 0 | 15 | 0.0% | Chờ soạn (`AI-08`, `CTR-12`, `CICD-06`, `OBS-01..12`) |
| **Năm 3 · Q1** | 3 | 0 | 0 | 0 | 3 | 0.0% | Chờ soạn (`SEC-06`, `AI-09`, `CTR-19`) |
| **Tổng cộng** | **234** | **43** | **0** | **0** | **191** | **18.4%** | — |

#### Theo chủ đề (Domain)

| Prefix | Chủ đề | Tổng | ✍️ Draft | 👀 Review | ✅ Done | 🔲 Todo |
| :--- | :--- | ---: | ---: | ---: | ---: | ---: |
| **GIT** | [Git nâng cao](00-foundations/git-workflow/README.md) | 4 | 4 | 0 | 0 | 0 |
| **READ** | [Đọc code không cần AI](00-foundations/code-reading/README.md) | 3 | 3 | 0 | 0 | 0 |
| **DSA** | [DSA thực dụng](00-foundations/dsa/README.md) | 7 | 2 | 0 | 0 | 5 |
| **JVM** | [Java hiện đại & JVM Internals](01-software-engineering/java-jvm/README.md) | 10 | 10 | 0 | 0 | 0 |
| **CQ** | [Code Quality, Refactoring & Testing](01-software-engineering/code-quality/README.md) | 8 | 8 | 0 | 0 | 0 |
| **CONC** | [Concurrency](01-software-engineering/concurrency/README.md) | 10 | 0 | 0 | 0 | 10 |
| **DB** | [Database Internals (PostgreSQL)](01-software-engineering/database/README.md) | 15 | 0 | 0 | 0 | 15 |
| **PERF** | [Performance Optimization](01-software-engineering/performance/README.md) | 6 | 0 | 0 | 0 | 6 |
| **ARCH** | [Backend Architecture](01-software-engineering/backend-architecture/README.md) | 11 | 0 | 0 | 0 | 11 |
| **DIST** | [Distributed Systems](01-software-engineering/distributed-systems/README.md) | 14 | 0 | 0 | 0 | 14 |
| **SEC** | [Application Security](01-software-engineering/security/README.md) | 6 | 0 | 0 | 0 | 6 |
| **AI** | [AI Engineering](01-software-engineering/ai-engineering/README.md) | 9 | 1 | 0 | 0 | 8 |
| **SD** | [System Design](01-software-engineering/system-design/README.md) | 7 | 0 | 0 | 0 | 7 |
| **PAT** | [Patterns](01-software-engineering/patterns/README.md) | 20 | 0 | 0 | 0 | 20 |
| **CD** | [Concept Decisions](01-software-engineering/concept-decisions/README.md) | 11 | 0 | 0 | 0 | 11 |
| **LNX** | [Linux & Networking](02-devops-and-cloud/linux-networking/README.md) | 15 | 15 | 0 | 0 | 0 |
| **CTR** | [Containers & Kubernetes](02-devops-and-cloud/containers-k8s/README.md) | 19 | 0 | 0 | 0 | 19 |
| **AWS** | [AWS Cloud cơ bản](02-devops-and-cloud/aws-cloud/README.md) | 9 | 0 | 0 | 0 | 9 |
| **IAC** | [Infrastructure as Code](02-devops-and-cloud/iac-terraform/README.md) | 9 | 0 | 0 | 0 | 9 |
| **CICD** | [CI/CD & Delivery](02-devops-and-cloud/ci-cd-pipelines/README.md) | 11 | 0 | 0 | 0 | 11 |
| **OBS** | [Observability & SRE](02-devops-and-cloud/observability-sre/README.md) | 12 | 0 | 0 | 0 | 12 |
| **INC** | [Incident Labs](03-production-troubleshooting/incident-labs/README.md) | 18 | 0 | 0 | 0 | 18 |
| **Tổng** | **22 chủ đề** | **234** | **43** | **0** | **0** | **191** |

## 1. Thứ tự học theo giai đoạn

> Trong mỗi giai đoạn, đi theo thứ tự trong bảng. Mỗi tuần ~2 bài học (ngày thường) + 1 lab/incident (cuối tuần).

### Quý 0 · Bridge — 9 mục

| ID | Bài | Loại | Status |
| :--- | :--- | :--- | :---: |
| GIT-01 | [Git Internals: objects, refs, index](00-foundations/git-workflow/01-git-internals/README.md) | Bài học | ✍️ |
| GIT-02 | [Rebase, Cherry-pick, Reflog](00-foundations/git-workflow/02-rebase-cherry-pick-reflog/README.md) | Bài học | ✍️ |
| GIT-03 | [Tìm bug bằng git bisect](00-foundations/git-workflow/03-git-bisect/README.md) | Bài học | ✍️ |
| GIT-04 | [Branching strategy & Conventional Commits](00-foundations/git-workflow/04-branching-conventional-commits/README.md) | Bài học | ✍️ |
| READ-01 | [Phương pháp đọc code lạ](00-foundations/code-reading/01-code-reading-method/README.md) | Bài học | ✍️ |
| READ-02 | [Đọc source HikariCP](00-foundations/code-reading/02-hikaricp-walkthrough/README.md) | Bài học | ✍️ |
| READ-03 | [Đọc source Guava Cache / Caffeine](00-foundations/code-reading/03-guava-cache-walkthrough/README.md) | Bài học | ✍️ |
| JVM-01 | [Java 7 → 21: Bridge Cheatsheet](01-software-engineering/java-jvm/01-java-8-to-21-bridge/README.md) | Bài học | ✍️ |
| JVM-02 | [Records, Sealed classes, Pattern matching](01-software-engineering/java-jvm/02-records-sealed-pattern-matching/README.md) | Bài học | ✍️ |

### Năm 1 · Q1 — 41 mục

| ID | Bài | Loại | Status |
| :--- | :--- | :--- | :---: |
| DSA-01 | [Độ phức tạp & Big-O thực dụng](00-foundations/dsa/01-complexity-analysis/README.md) | Bài học | ✍️ |
| DSA-02 | [HashMap internals](00-foundations/dsa/02-hash-map-internals/README.md) | Bài học | ✍️ |
| DSA-03 | [Cây, BST, B-tree](00-foundations/dsa/03-trees-btree/README.md) | Bài học | 🔲 |
| DSA-04 | [Heap & Priority Queue](00-foundations/dsa/04-heap-priority-queue/README.md) | Bài học | 🔲 |
| DSA-05 | [Graph, BFS, DFS, Topological sort](00-foundations/dsa/05-graph-bfs-dfs/README.md) | Bài học | 🔲 |
| DSA-06 | [LRU Cache](00-foundations/dsa/06-lru-cache/README.md) | Bài học | 🔲 |
| DSA-07 | [Sorting & Binary search thực dụng](00-foundations/dsa/07-sorting-searching/README.md) | Bài học | 🔲 |
| JVM-03 | [Class loading & ClassLoader hierarchy](01-software-engineering/java-jvm/03-class-loading/README.md) | Bài học | ✍️ |
| JVM-04 | [JVM Memory: Heap, Stack, Metaspace, Direct memory](01-software-engineering/java-jvm/04-jvm-memory-areas/README.md) | Bài học | ✍️ |
| JVM-05 | [JIT: Interpreter, C1, C2, tiered compilation](01-software-engineering/java-jvm/05-jit-compilation/README.md) | Bài học | ✍️ |
| JVM-06 | [GC cơ bản & G1](01-software-engineering/java-jvm/06-gc-fundamentals-g1/README.md) | Bài học | ✍️ |
| JVM-07 | [ZGC / Shenandoah & low-latency GC](01-software-engineering/java-jvm/07-zgc-low-latency/README.md) | Bài học | ✍️ |
| JVM-08 | [Đọc GC log](01-software-engineering/java-jvm/08-gc-log-analysis/README.md) | Bài học | ✍️ |
| JVM-09 | [Heap dump, Eclipse MAT, JFR](01-software-engineering/java-jvm/09-heap-dump-mat-jfr/README.md) | Bài học | ✍️ |
| JVM-10 | [Metaspace & ClassLoader leak](01-software-engineering/java-jvm/10-metaspace-classloader-leak/README.md) | Bài học | ✍️ |
| CQ-01 | [SOLID áp dụng thực tế](01-software-engineering/code-quality/01-solid-in-practice/README.md) | Bài học | ✍️ |
| CQ-02 | [Cohesion, Coupling, Deep modules](01-software-engineering/code-quality/02-cohesion-coupling-deep-modules/README.md) | Bài học | ✍️ |
| CQ-03 | [Refactoring catalog & code smells](01-software-engineering/code-quality/03-refactoring-catalog/README.md) | Bài học | ✍️ |
| CQ-04 | [Legacy code & Characterization tests](01-software-engineering/code-quality/04-legacy-characterization-tests/README.md) | Bài học | ✍️ |
| CQ-05 | [Seams & Strangler Fig](01-software-engineering/code-quality/05-seams-strangler/README.md) | Bài học | ✍️ |
| CQ-06 | [JUnit 5 & Mockito](01-software-engineering/code-quality/06-junit5-mockito/README.md) | Bài học | ✍️ |
| CQ-07 | [Test pyramid & khi nào KHÔNG mock](01-software-engineering/code-quality/07-test-pyramid-mocking/README.md) | Bài học | ✍️ |
| CQ-08 | [Integration test với Testcontainers](01-software-engineering/code-quality/08-testcontainers/README.md) | Bài học | ✍️ |
| AI-01 | [AI-assisted development playbook](01-software-engineering/ai-engineering/01-ai-assisted-dev-playbook/README.md) | Bài học | ✍️ |
| LNX-01 | [Process, thread & signal](02-devops-and-cloud/linux-networking/01-processes-signals/README.md) | Bài học | ✍️ |
| LNX-02 | [File descriptor & I/O](02-devops-and-cloud/linux-networking/02-file-descriptors-io/README.md) | Bài học | ✍️ |
| LNX-03 | [Virtual memory, page cache, swap & OOM killer](02-devops-and-cloud/linux-networking/03-memory-pagecache-oom/README.md) | Bài học | ✍️ |
| LNX-04 | [systemd & quản lý service](02-devops-and-cloud/linux-networking/04-systemd-services/README.md) | Bài học | ✍️ |
| LNX-05 | [Công cụ hiệu năng Linux](02-devops-and-cloud/linux-networking/05-linux-perf-tools/README.md) | Bài học | ✍️ |
| LNX-06 | [Shell scripting tự động hóa](02-devops-and-cloud/linux-networking/06-shell-scripting/README.md) | Bài học | ✍️ |
| LNX-07 | [select, poll, epoll & event loop](02-devops-and-cloud/linux-networking/07-epoll-io-multiplexing/README.md) | Bài học | ✍️ |
| LNX-08 | [TCP: handshake, state machine, backlog](02-devops-and-cloud/linux-networking/08-tcp-handshake-states/README.md) | Bài học | ✍️ |
| LNX-09 | [TIME_WAIT, CLOSE_WAIT & socket exhaustion](02-devops-and-cloud/linux-networking/09-time-wait-close-wait/README.md) | Bài học | ✍️ |
| LNX-10 | [HTTP/1.1 vs HTTP/2 vs HTTP/3](02-devops-and-cloud/linux-networking/10-http-versions/README.md) | Bài học | ✍️ |
| LNX-11 | [DNS resolution & troubleshooting](02-devops-and-cloud/linux-networking/11-dns-resolution/README.md) | Bài học | ✍️ |
| LNX-12 | [TLS handshake & certificate chain](02-devops-and-cloud/linux-networking/12-tls-handshake/README.md) | Bài học | ✍️ |
| LNX-13 | [Load balancer L4 vs L7 & Nginx reverse proxy](02-devops-and-cloud/linux-networking/13-load-balancer-nginx/README.md) | Bài học | ✍️ |
| LNX-14 | [Bắt gói tin: tcpdump & Wireshark](02-devops-and-cloud/linux-networking/14-tcpdump-wireshark/README.md) | Bài học | ✍️ |
| LNX-15 | [iptables/nftables & network namespace](02-devops-and-cloud/linux-networking/15-iptables-netns/README.md) | Bài học | ✍️ |
| INC-07 | [Memory leak trên heap](03-production-troubleshooting/incident-labs/07-memory-leak-heap/) | Incident lab | 🔲 |
| INC-08 | [OOM Metaspace sau nhiều lần redeploy](03-production-troubleshooting/incident-labs/08-metaspace-leak-redeploy/) | Incident lab | 🔲 |

### Năm 1 · Q2 — 40 mục

| ID | Bài | Loại | Status |
| :--- | :--- | :--- | :---: |
| CONC-01 | [Java Memory Model & happens-before](01-software-engineering/concurrency/01-jmm-happens-before/README.md) | Bài học | 🔲 |
| CONC-02 | [volatile, synchronized & monitor](01-software-engineering/concurrency/02-volatile-synchronized/README.md) | Bài học | 🔲 |
| CONC-03 | [CAS, Atomic* & LongAdder](01-software-engineering/concurrency/03-cas-atomics/README.md) | Bài học | 🔲 |
| CONC-04 | [Thread Pool Internals](01-software-engineering/concurrency/04-thread-pool-internals/README.md) | Bài học | 🔲 |
| CONC-05 | [CompletableFuture & async composition](01-software-engineering/concurrency/05-completablefuture/README.md) | Bài học | 🔲 |
| CONC-06 | [ConcurrentHashMap internals](01-software-engineering/concurrency/06-concurrenthashmap-internals/README.md) | Bài học | 🔲 |
| CONC-07 | [ReentrantLock, ReadWriteLock, StampedLock](01-software-engineering/concurrency/07-locks-reentrant-stamped/README.md) | Bài học | 🔲 |
| CONC-08 | [Virtual Threads vs Platform Threads](01-software-engineering/concurrency/08-virtual-threads/README.md) | Bài học | 🔲 |
| CONC-09 | [Deadlock, Livelock, Starvation](01-software-engineering/concurrency/09-deadlock-livelock-starvation/README.md) | Bài học | 🔲 |
| CONC-10 | [Đọc Thread dump](01-software-engineering/concurrency/10-thread-dump-analysis/README.md) | Bài học | 🔲 |
| DB-01 | [Storage: page, heap tuple, TOAST](01-software-engineering/database/01-storage-page-heap-toast/README.md) | Bài học | 🔲 |
| DB-02 | [WAL, checkpoint & durability](01-software-engineering/database/02-wal-durability/README.md) | Bài học | 🔲 |
| DB-03 | [B-tree index internals](01-software-engineering/database/03-btree-index-internals/README.md) | Bài học | 🔲 |
| DB-04 | [Hash, GIN, GiST, BRIN, partial & expression index](01-software-engineering/database/04-index-types/README.md) | Bài học | 🔲 |
| DB-05 | [Composite & covering index](01-software-engineering/database/05-composite-covering-index/README.md) | Bài học | 🔲 |
| DB-06 | [EXPLAIN ANALYZE & Query planner](01-software-engineering/database/06-explain-analyze-planner/README.md) | Bài học | 🔲 |
| DB-07 | [Statistics & bad plans](01-software-engineering/database/07-statistics-bad-plans/README.md) | Bài học | 🔲 |
| DB-08 | [MVCC: xmin, xmax, snapshot](01-software-engineering/database/08-mvcc/README.md) | Bài học | 🔲 |
| DB-09 | [VACUUM, autovacuum & bloat](01-software-engineering/database/09-vacuum-bloat/README.md) | Bài học | 🔲 |
| DB-10 | [Isolation levels & anomalies](01-software-engineering/database/10-isolation-levels-anomalies/README.md) | Bài học | 🔲 |
| DB-11 | [Write skew & SERIALIZABLE (SSI)](01-software-engineering/database/11-write-skew-serializable/README.md) | Bài học | 🔲 |
| DB-12 | [Row locks, FOR UPDATE, SKIP LOCKED](01-software-engineering/database/12-row-locks-skip-locked/README.md) | Bài học | 🔲 |
| DB-13 | [Advisory locks & lock contention](01-software-engineering/database/13-advisory-locks-contention/README.md) | Bài học | 🔲 |
| DB-14 | [Connection pool: HikariCP sizing & exhaustion](01-software-engineering/database/14-connection-pool-sizing/README.md) | Bài học | 🔲 |
| DB-15 | [Timeout chain: client → app → pool → DB](01-software-engineering/database/15-timeout-chain/README.md) | Bài học | 🔲 |
| PERF-01 | [Phương pháp: đo trước, tối ưu sau](01-software-engineering/performance/01-measure-first/README.md) | Bài học | 🔲 |
| PERF-02 | [Latency percentiles & coordinated omission](01-software-engineering/performance/02-latency-percentiles/README.md) | Bài học | 🔲 |
| PERF-03 | [Little's Law & queueing theory cơ bản](01-software-engineering/performance/03-littles-law-queueing/README.md) | Bài học | 🔲 |
| PERF-04 | [Micro-benchmark với JMH](01-software-engineering/performance/04-jmh-microbenchmark/README.md) | Bài học | 🔲 |
| PERF-05 | [Load testing với k6 / Gatling](01-software-engineering/performance/05-load-testing/README.md) | Bài học | 🔲 |
| PERF-06 | [Profiling: async-profiler & flame graph](01-software-engineering/performance/06-profiling-flame-graph/README.md) | Bài học | 🔲 |
| CD-01 | [Chọn Isolation Level: READ COMMITTED vs REPEATABLE READ vs SERIALIZABLE](01-software-engineering/concept-decisions/isolation-level-choice.md) | Concept decision | 🔲 |
| CD-02 | [Pool sizing: nhỏ vs lớn](01-software-engineering/concept-decisions/connection-pool-sizing.md) | Concept decision | 🔲 |
| CD-03 | [Virtual Threads vs Platform Thread Pool](01-software-engineering/concept-decisions/virtual-vs-platform-threads.md) | Concept decision | 🔲 |
| INC-01 | [N+1 query gây latency spike](03-production-troubleshooting/incident-labs/01-n-plus-1-latency-spike/) | Incident lab | 🔲 |
| INC-02 | [Cạn connection pool](03-production-troubleshooting/incident-labs/02-db-pool-exhaustion/) | Incident lab | 🔲 |
| INC-03 | [Deadlock trong database](03-production-troubleshooting/incident-labs/03-db-deadlock/) | Incident lab | 🔲 |
| INC-04 | [Lost update do race condition](03-production-troubleshooting/incident-labs/04-lost-update-race/) | Incident lab | 🔲 |
| INC-05 | [Thread pool starvation](03-production-troubleshooting/incident-labs/05-thread-pool-starvation/) | Incident lab | 🔲 |
| INC-06 | [Query chậm đột ngột do statistics cũ / plan đổi](03-production-troubleshooting/incident-labs/06-slow-query-stale-stats/) | Incident lab | 🔲 |

### Năm 1 · Q3 — 46 mục

| ID | Bài | Loại | Status |
| :--- | :--- | :--- | :---: |
| ARCH-01 | [Spring Boot auto-configuration](01-software-engineering/backend-architecture/01-spring-boot-autoconfiguration/README.md) | Bài học | 🔲 |
| ARCH-02 | [Bean lifecycle & proxies (JDK vs CGLIB)](01-software-engineering/backend-architecture/02-bean-lifecycle-proxies/README.md) | Bài học | 🔲 |
| ARCH-03 | [@Transactional: proxy & cạm bẫy](01-software-engineering/backend-architecture/03-transactional-pitfalls/README.md) | Bài học | 🔲 |
| ARCH-04 | [REST API design](01-software-engineering/backend-architecture/04-rest-api-design/README.md) | Bài học | 🔲 |
| ARCH-05 | [Error model, validation & versioning](01-software-engineering/backend-architecture/05-error-model-versioning/README.md) | Bài học | 🔲 |
| ARCH-06 | [Redis & caching strategies](01-software-engineering/backend-architecture/06-redis-caching-strategies/README.md) | Bài học | 🔲 |
| ARCH-07 | [Cache stampede, hot key, penetration, avalanche](01-software-engineering/backend-architecture/07-cache-stampede-hot-key/README.md) | Bài học | 🔲 |
| ARCH-08 | [Timeout, Retry, Backoff & Jitter](01-software-engineering/backend-architecture/08-timeout-retry-backoff/README.md) | Bài học | 🔲 |
| ARCH-09 | [Circuit Breaker, Bulkhead, Rate limiter (Resilience4j)](01-software-engineering/backend-architecture/09-circuit-breaker-bulkhead/README.md) | Bài học | 🔲 |
| ARCH-10 | [Layered, Hexagonal, Clean Architecture](01-software-engineering/backend-architecture/10-layered-hexagonal-clean/README.md) | Bài học | 🔲 |
| ARCH-11 | [Modular monolith vs Microservices](01-software-engineering/backend-architecture/11-modular-monolith-vs-microservices/README.md) | Bài học | 🔲 |
| PAT-01 | [Circuit Breaker](01-software-engineering/patterns/circuit-breaker.md) | Pattern card | 🔲 |
| PAT-02 | [Retry with Exponential Backoff & Jitter](01-software-engineering/patterns/retry-exponential-backoff.md) | Pattern card | 🔲 |
| PAT-03 | [Bulkhead](01-software-engineering/patterns/bulkhead.md) | Pattern card | 🔲 |
| PAT-04 | [Timeout](01-software-engineering/patterns/timeout.md) | Pattern card | 🔲 |
| PAT-05 | [Fallback & Graceful Degradation](01-software-engineering/patterns/fallback-graceful-degradation.md) | Pattern card | 🔲 |
| PAT-06 | [Read Replicas](01-software-engineering/patterns/read-replicas.md) | Pattern card | 🔲 |
| PAT-07 | [CQRS](01-software-engineering/patterns/cqrs.md) | Pattern card | 🔲 |
| PAT-08 | [Event Sourcing](01-software-engineering/patterns/event-sourcing.md) | Pattern card | 🔲 |
| PAT-09 | [Sharding / Partitioning](01-software-engineering/patterns/sharding.md) | Pattern card | 🔲 |
| PAT-10 | [Fan-out on Write vs on Read](01-software-engineering/patterns/fan-out-write-vs-read.md) | Pattern card | 🔲 |
| PAT-11 | [Cache-Aside](01-software-engineering/patterns/cache-aside.md) | Pattern card | 🔲 |
| PAT-12 | [Write-Through Cache](01-software-engineering/patterns/write-through-cache.md) | Pattern card | 🔲 |
| PAT-13 | [Write-Behind Cache](01-software-engineering/patterns/write-behind-cache.md) | Pattern card | 🔲 |
| PAT-14 | [Saga](01-software-engineering/patterns/saga.md) | Pattern card | 🔲 |
| PAT-15 | [Transactional Outbox](01-software-engineering/patterns/outbox.md) | Pattern card | 🔲 |
| PAT-16 | [Competing Consumers](01-software-engineering/patterns/competing-consumers.md) | Pattern card | 🔲 |
| PAT-17 | [Dead Letter Queue](01-software-engineering/patterns/dead-letter-queue.md) | Pattern card | 🔲 |
| PAT-18 | [Idempotent Consumer & Message Deduplication](01-software-engineering/patterns/idempotent-consumer.md) | Pattern card | 🔲 |
| PAT-19 | [Strangler Fig](01-software-engineering/patterns/strangler-fig.md) | Pattern card | 🔲 |
| PAT-20 | [Rate Limiter](01-software-engineering/patterns/rate-limiter.md) | Pattern card | 🔲 |
| CD-07 | [Modular Monolith vs Microservices](01-software-engineering/concept-decisions/monolith-vs-microservices.md) | Concept decision | 🔲 |
| CD-08 | [Jenkins vs GitHub Actions](01-software-engineering/concept-decisions/jenkins-vs-github-actions.md) | Concept decision | 🔲 |
| CTR-01 | [Container internals: namespaces & cgroups](02-devops-and-cloud/containers-k8s/01-namespaces-cgroups/README.md) | Bài học | 🔲 |
| CTR-02 | [Image layers & OverlayFS](02-devops-and-cloud/containers-k8s/02-overlayfs-image-layers/README.md) | Bài học | 🔲 |
| CTR-03 | [Dockerfile: multi-stage, distroless, jlink, non-root](02-devops-and-cloud/containers-k8s/03-dockerfile-best-practices/README.md) | Bài học | 🔲 |
| CTR-04 | [JVM trong container](02-devops-and-cloud/containers-k8s/04-jvm-in-containers/README.md) | Bài học | 🔲 |
| CTR-05 | [Docker networking](02-devops-and-cloud/containers-k8s/05-docker-networking/README.md) | Bài học | 🔲 |
| CTR-06 | [Docker Compose cho môi trường dev](02-devops-and-cloud/containers-k8s/06-docker-compose-dev/README.md) | Bài học | 🔲 |
| CICD-01 | [CI cơ bản với GitHub Actions](02-devops-and-cloud/ci-cd-pipelines/01-ci-github-actions/README.md) | Bài học | 🔲 |
| CICD-02 | [Jenkins pipeline-as-code](02-devops-and-cloud/ci-cd-pipelines/02-jenkins-pipeline/README.md) | Bài học | 🔲 |
| INC-09 | [@Transactional không có tác dụng (self-invocation)](03-production-troubleshooting/incident-labs/09-transactional-self-invocation/) | Incident lab | 🔲 |
| INC-10 | [Cache stampede làm sập DB](03-production-troubleshooting/incident-labs/10-cache-stampede/) | Incident lab | 🔲 |
| INC-11 | [Retry storm gây cascading failure](03-production-troubleshooting/incident-labs/11-retry-storm/) | Incident lab | 🔲 |
| INC-12 | [CLOSE_WAIT tích tụ do không đóng connection](03-production-troubleshooting/incident-labs/12-close-wait-leak/) | Incident lab | 🔲 |
| INC-13 | [Container OOMKilled dù heap còn trống](03-production-troubleshooting/incident-labs/13-container-oomkilled/) | Incident lab | 🔲 |

### Năm 1 · Q4 — 42 mục

| ID | Bài | Loại | Status |
| :--- | :--- | :--- | :---: |
| DIST-01 | [8 Fallacies of Distributed Computing](01-software-engineering/distributed-systems/01-fallacies/README.md) | Bài học | 🔲 |
| DIST-02 | [CAP & PACELC](01-software-engineering/distributed-systems/02-cap-pacelc/README.md) | Bài học | 🔲 |
| DIST-03 | [Replication: leader-follower, multi-leader, leaderless](01-software-engineering/distributed-systems/03-replication-models/README.md) | Bài học | 🔲 |
| DIST-04 | [Replication lag & read-your-writes](01-software-engineering/distributed-systems/04-replication-lag/README.md) | Bài học | 🔲 |
| DIST-05 | [Partitioning & Consistent hashing](01-software-engineering/distributed-systems/05-partitioning-consistent-hashing/README.md) | Bài học | 🔲 |
| DIST-06 | [Consensus & Raft](01-software-engineering/distributed-systems/06-raft-consensus/README.md) | Bài học | 🔲 |
| DIST-07 | [Thời gian, đồng hồ & thứ tự](01-software-engineering/distributed-systems/07-time-clocks-ordering/README.md) | Bài học | 🔲 |
| DIST-08 | [Kafka architecture](01-software-engineering/distributed-systems/08-kafka-architecture/README.md) | Bài học | 🔲 |
| DIST-09 | [Kafka consumer groups & rebalancing](01-software-engineering/distributed-systems/09-kafka-consumer-groups/README.md) | Bài học | 🔲 |
| DIST-10 | [At-most/at-least/exactly-once & idempotent consumer](01-software-engineering/distributed-systems/10-delivery-semantics/README.md) | Bài học | 🔲 |
| DIST-11 | [Transactional Outbox & CDC](01-software-engineering/distributed-systems/11-outbox-pattern/README.md) | Bài học | 🔲 |
| DIST-12 | [Saga: orchestration vs choreography](01-software-engineering/distributed-systems/12-saga-pattern/README.md) | Bài học | 🔲 |
| DIST-13 | [CQRS & Event Sourcing](01-software-engineering/distributed-systems/13-cqrs-event-sourcing/README.md) | Bài học | 🔲 |
| DIST-14 | [Distributed lock & fencing token](01-software-engineering/distributed-systems/14-distributed-lock-fencing/README.md) | Bài học | 🔲 |
| SEC-01 | [OWASP Top 10 cho backend](01-software-engineering/security/01-owasp-top10-backend/README.md) | Bài học | 🔲 |
| SEC-02 | [AuthN/AuthZ: OAuth2 & OIDC](01-software-engineering/security/02-authn-authz-oauth2-oidc/README.md) | Bài học | 🔲 |
| SEC-03 | [JWT pitfalls](01-software-engineering/security/03-jwt-pitfalls/README.md) | Bài học | 🔲 |
| SEC-04 | [Injection, XSS, CSRF](01-software-engineering/security/04-injection-xss-csrf/README.md) | Bài học | 🔲 |
| SEC-05 | [Quản lý secret & cấu hình an toàn](01-software-engineering/security/05-secret-handling/README.md) | Bài học | 🔲 |
| AI-02 | [Embeddings cơ bản](01-software-engineering/ai-engineering/02-embeddings-fundamentals/README.md) | Bài học | 🔲 |
| AI-03 | [Chunking strategies](01-software-engineering/ai-engineering/03-chunking-strategies/README.md) | Bài học | 🔲 |
| AI-04 | [Vector search với pgvector](01-software-engineering/ai-engineering/04-vector-search-pgvector/README.md) | Bài học | 🔲 |
| SD-01 | [Framework & Back-of-envelope estimation](01-software-engineering/system-design/framework-estimation.md) | System design | 🔲 |
| SD-02 | [URL Shortener](01-software-engineering/system-design/url-shortener.md) | System design | 🔲 |
| SD-03 | [Rate Limiter](01-software-engineering/system-design/rate-limiter.md) | System design | 🔲 |
| SD-04 | [Notification System](01-software-engineering/system-design/notification-system.md) | System design | 🔲 |
| SD-05 | [Ticket Booking](01-software-engineering/system-design/ticket-booking.md) | System design | 🔲 |
| SD-06 | [News Feed](01-software-engineering/system-design/news-feed.md) | System design | 🔲 |
| SD-07 | [Payment System](01-software-engineering/system-design/payment-system.md) | System design | 🔲 |
| CD-04 | [Distributed lock: Redis vs Database vs etcd](01-software-engineering/concept-decisions/redis-lock-vs-db-lock.md) | Concept decision | 🔲 |
| CD-05 | [Kafka vs RabbitMQ](01-software-engineering/concept-decisions/kafka-vs-rabbitmq.md) | Concept decision | 🔲 |
| CD-06 | [Saga: Orchestration vs Choreography](01-software-engineering/concept-decisions/saga-orchestration-vs-choreography.md) | Concept decision | 🔲 |
| AWS-01 | [IAM & least privilege](02-devops-and-cloud/aws-cloud/01-iam-least-privilege/README.md) | Bài học | 🔲 |
| AWS-02 | [VPC, subnet, route table, SG, NACL](02-devops-and-cloud/aws-cloud/02-vpc-networking/README.md) | Bài học | 🔲 |
| AWS-03 | [EC2 & Auto Scaling](02-devops-and-cloud/aws-cloud/03-ec2-autoscaling/README.md) | Bài học | 🔲 |
| AWS-04 | [RDS PostgreSQL](02-devops-and-cloud/aws-cloud/04-rds/README.md) | Bài học | 🔲 |
| AWS-05 | [S3](02-devops-and-cloud/aws-cloud/05-s3/README.md) | Bài học | 🔲 |
| AWS-06 | [ALB & Route 53](02-devops-and-cloud/aws-cloud/06-alb-route53/README.md) | Bài học | 🔲 |
| AWS-07 | [CloudWatch: metrics, logs, alarms](02-devops-and-cloud/aws-cloud/07-cloudwatch/README.md) | Bài học | 🔲 |
| AWS-08 | [Deploy Project 1 lên AWS bằng tay (ClickOps)](02-devops-and-cloud/aws-cloud/08-clickops-deploy/README.md) | Bài học | 🔲 |
| INC-14 | [Kafka consumer rebalance liên tục](03-production-troubleshooting/incident-labs/14-kafka-rebalance-loop/) | Incident lab | 🔲 |
| INC-15 | [Xử lý trùng message (trừ tiền 2 lần)](03-production-troubleshooting/incident-labs/15-duplicate-message-processing/) | Incident lab | 🔲 |

### Năm 2 · Q1 — 13 mục

| ID | Bài | Loại | Status |
| :--- | :--- | :--- | :---: |
| AI-05 | [Hybrid search: BM25 + vector](01-software-engineering/ai-engineering/05-hybrid-search-bm25/README.md) | Bài học | 🔲 |
| AI-06 | [Reranking & đánh giá retrieval](01-software-engineering/ai-engineering/06-reranking-retrieval-eval/README.md) | Bài học | 🔲 |
| CD-09 | [Terraform: Workspace vs Directory-per-env](01-software-engineering/concept-decisions/terraform-workspace-vs-directory.md) | Concept decision | 🔲 |
| AWS-09 | [Cost awareness](02-devops-and-cloud/aws-cloud/09-cost-awareness/README.md) | Bài học | 🔲 |
| IAC-01 | [Terraform core: provider, resource, lifecycle](02-devops-and-cloud/iac-terraform/01-terraform-core/README.md) | Bài học | 🔲 |
| IAC-02 | [State, remote backend & locking](02-devops-and-cloud/iac-terraform/02-state-backend-locking/README.md) | Bài học | 🔲 |
| IAC-03 | [Thiết kế module](02-devops-and-cloud/iac-terraform/03-modules-design/README.md) | Bài học | 🔲 |
| IAC-04 | [Tổ chức multi-environment](02-devops-and-cloud/iac-terraform/04-multi-env-layout/README.md) | Bài học | 🔲 |
| IAC-05 | [Drift, import & refactor](02-devops-and-cloud/iac-terraform/05-drift-import-refactor/README.md) | Bài học | 🔲 |
| IAC-06 | [Ansible fundamentals](02-devops-and-cloud/iac-terraform/06-ansible-fundamentals/README.md) | Bài học | 🔲 |
| IAC-07 | [Provisioning vs Configuration management vs Immutable image](02-devops-and-cloud/iac-terraform/07-provisioning-vs-config/README.md) | Bài học | 🔲 |
| IAC-08 | [Secrets: Vault & AWS Secrets Manager](02-devops-and-cloud/iac-terraform/08-secrets-vault/README.md) | Bài học | 🔲 |
| IAC-09 | [Kiến trúc tham chiếu AWS Multi-AZ bằng Terraform](02-devops-and-cloud/iac-terraform/09-aws-multi-az-architecture/README.md) | Bài học | 🔲 |

### Năm 2 · Q2 — 9 mục

| ID | Bài | Loại | Status |
| :--- | :--- | :--- | :---: |
| AI-07 | [LLM trong production: latency, cost, caching](01-software-engineering/ai-engineering/07-llm-production/README.md) | Bài học | 🔲 |
| CD-11 | [Rolling vs Blue-Green vs Canary](01-software-engineering/concept-decisions/deployment-strategy-choice.md) | Concept decision | 🔲 |
| CICD-03 | [GitHub Actions nâng cao](02-devops-and-cloud/ci-cd-pipelines/03-gha-advanced/README.md) | Bài học | 🔲 |
| CICD-04 | [OIDC từ CI tới AWS](02-devops-and-cloud/ci-cd-pipelines/04-oidc-to-aws/README.md) | Bài học | 🔲 |
| CICD-05 | [Quality gates: lint, SAST, dependency scan](02-devops-and-cloud/ci-cd-pipelines/05-quality-gates/README.md) | Bài học | 🔲 |
| CICD-07 | [Deployment strategies](02-devops-and-cloud/ci-cd-pipelines/07-deployment-strategies/README.md) | Bài học | 🔲 |
| CICD-08 | [Feature flags](02-devops-and-cloud/ci-cd-pipelines/08-feature-flags/README.md) | Bài học | 🔲 |
| CICD-09 | [Automated rollback](02-devops-and-cloud/ci-cd-pipelines/09-automated-rollback/README.md) | Bài học | 🔲 |
| CICD-10 | [Database migration an toàn (Flyway, expand-contract)](02-devops-and-cloud/ci-cd-pipelines/10-db-migration-expand-contract/README.md) | Bài học | 🔲 |

### Năm 2 · Q3 — 16 mục

| ID | Bài | Loại | Status |
| :--- | :--- | :--- | :---: |
| CD-10 | [Helm vs Kustomize](01-software-engineering/concept-decisions/helm-vs-kustomize.md) | Concept decision | 🔲 |
| CTR-07 | [Kiến trúc Kubernetes](02-devops-and-cloud/containers-k8s/07-k8s-architecture/README.md) | Bài học | 🔲 |
| CTR-08 | [Pod lifecycle & probes](02-devops-and-cloud/containers-k8s/08-pod-lifecycle-probes/README.md) | Bài học | 🔲 |
| CTR-09 | [Deployment, StatefulSet, DaemonSet, Job](02-devops-and-cloud/containers-k8s/09-workloads/README.md) | Bài học | 🔲 |
| CTR-10 | [Requests, Limits & QoS](02-devops-and-cloud/containers-k8s/10-requests-limits-qos/README.md) | Bài học | 🔲 |
| CTR-11 | [Service, Ingress & CNI](02-devops-and-cloud/containers-k8s/11-services-ingress-cni/README.md) | Bài học | 🔲 |
| CTR-13 | [Storage: PV, PVC, StorageClass](02-devops-and-cloud/containers-k8s/13-storage-pv-pvc/README.md) | Bài học | 🔲 |
| CTR-14 | [ConfigMap, Secret, RBAC, ServiceAccount](02-devops-and-cloud/containers-k8s/14-config-secrets-rbac/README.md) | Bài học | 🔲 |
| CTR-15 | [Helm charts](02-devops-and-cloud/containers-k8s/15-helm/README.md) | Bài học | 🔲 |
| CTR-16 | [Kustomize](02-devops-and-cloud/containers-k8s/16-kustomize/README.md) | Bài học | 🔲 |
| CTR-17 | [HPA, VPA & Cluster Autoscaler](02-devops-and-cloud/containers-k8s/17-hpa-autoscaling/README.md) | Bài học | 🔲 |
| CTR-18 | [Debug Kubernetes](02-devops-and-cloud/containers-k8s/18-debugging-k8s/README.md) | Bài học | 🔲 |
| CICD-11 | [GitOps với Argo CD](02-devops-and-cloud/ci-cd-pipelines/11-gitops-argocd/README.md) | Bài học | 🔲 |
| INC-16 | [CrashLoopBackOff do liveness probe sai](03-production-troubleshooting/incident-labs/16-k8s-crashloop-probes/) | Incident lab | 🔲 |
| INC-17 | [Latency cao do DNS trong Kubernetes (ndots)](03-production-troubleshooting/incident-labs/17-k8s-dns-latency/) | Incident lab | 🔲 |
| INC-18 | [p99 latency cao do CPU throttling](03-production-troubleshooting/incident-labs/18-cpu-throttling-latency/) | Incident lab | 🔲 |

### Năm 2 · Q4 — 15 mục

| ID | Bài | Loại | Status |
| :--- | :--- | :--- | :---: |
| AI-08 | [Prompt injection & guardrails](01-software-engineering/ai-engineering/08-prompt-injection-guardrails/README.md) | Bài học | 🔲 |
| CTR-12 | [NetworkPolicy](02-devops-and-cloud/containers-k8s/12-networkpolicy/README.md) | Bài học | 🔲 |
| CICD-06 | [Supply chain: SBOM, image signing](02-devops-and-cloud/ci-cd-pipelines/06-supply-chain-security/README.md) | Bài học | 🔲 |
| OBS-01 | [Observability vs Monitoring & 3 trụ cột](02-devops-and-cloud/observability-sre/01-three-pillars/README.md) | Bài học | 🔲 |
| OBS-02 | [Prometheus: pull model & PromQL](02-devops-and-cloud/observability-sre/02-prometheus-promql/README.md) | Bài học | 🔲 |
| OBS-03 | [Cardinality & chi phí metric](02-devops-and-cloud/observability-sre/03-cardinality/README.md) | Bài học | 🔲 |
| OBS-04 | [Grafana dashboards: RED & USE](02-devops-and-cloud/observability-sre/04-grafana-red-use/README.md) | Bài học | 🔲 |
| OBS-05 | [Structured logging & Loki](02-devops-and-cloud/observability-sre/05-structured-logging-loki/README.md) | Bài học | 🔲 |
| OBS-06 | [Distributed tracing với OpenTelemetry](02-devops-and-cloud/observability-sre/06-opentelemetry-tracing/README.md) | Bài học | 🔲 |
| OBS-07 | [SLI, SLO, SLA & Error budget](02-devops-and-cloud/observability-sre/07-sli-slo-error-budget/README.md) | Bài học | 🔲 |
| OBS-08 | [Alerting theo triệu chứng & burn rate](02-devops-and-cloud/observability-sre/08-symptom-based-alerting/README.md) | Bài học | 🔲 |
| OBS-09 | [Runbook & on-call](02-devops-and-cloud/observability-sre/09-runbooks-oncall/README.md) | Bài học | 🔲 |
| OBS-10 | [Incident management & blameless post-mortem](02-devops-and-cloud/observability-sre/10-incident-management/README.md) | Bài học | 🔲 |
| OBS-11 | [Chaos engineering & game day](02-devops-and-cloud/observability-sre/11-chaos-engineering/README.md) | Bài học | 🔲 |
| OBS-12 | [Capacity planning & load test quy mô lớn](02-devops-and-cloud/observability-sre/12-capacity-planning/README.md) | Bài học | 🔲 |

### Năm 3 · Q1 — 3 mục

| ID | Bài | Loại | Status |
| :--- | :--- | :--- | :---: |
| SEC-06 | [Threat modeling (STRIDE)](01-software-engineering/security/06-threat-modeling/README.md) | Bài học | 🔲 |
| AI-09 | [Tool calling, Agents & MCP](01-software-engineering/ai-engineering/09-tool-calling-agents-mcp/README.md) | Bài học | 🔲 |
| CTR-19 | [Operators & CRD](02-devops-and-cloud/containers-k8s/19-operators-crd/README.md) | Bài học | 🔲 |

## 2. Theo chủ đề

### GIT — Git nâng cao · [`00-foundations/git-workflow/`](00-foundations/git-workflow/README.md)

| ID | Bài | Giai đoạn | Status |
| :--- | :--- | :--- | :---: |
| GIT-01 | [Git Internals: objects, refs, index](00-foundations/git-workflow/01-git-internals/README.md) | Quý 0 · Bridge | ✍️ |
| GIT-02 | [Rebase, Cherry-pick, Reflog](00-foundations/git-workflow/02-rebase-cherry-pick-reflog/README.md) | Quý 0 · Bridge | ✍️ |
| GIT-03 | [Tìm bug bằng git bisect](00-foundations/git-workflow/03-git-bisect/README.md) | Quý 0 · Bridge | ✍️ |
| GIT-04 | [Branching strategy & Conventional Commits](00-foundations/git-workflow/04-branching-conventional-commits/README.md) | Quý 0 · Bridge | ✍️ |

### READ — Đọc code không cần AI · [`00-foundations/code-reading/`](00-foundations/code-reading/README.md)

| ID | Bài | Giai đoạn | Status |
| :--- | :--- | :--- | :---: |
| READ-01 | [Phương pháp đọc code lạ](00-foundations/code-reading/01-code-reading-method/README.md) | Quý 0 · Bridge | ✍️ |
| READ-02 | [Đọc source HikariCP](00-foundations/code-reading/02-hikaricp-walkthrough/README.md) | Quý 0 · Bridge | ✍️ |
| READ-03 | [Đọc source Guava Cache / Caffeine](00-foundations/code-reading/03-guava-cache-walkthrough/README.md) | Quý 0 · Bridge | ✍️ |

### DSA — DSA thực dụng · [`00-foundations/dsa/`](00-foundations/dsa/README.md)

| ID | Bài | Giai đoạn | Status |
| :--- | :--- | :--- | :---: |
| DSA-01 | [Độ phức tạp & Big-O thực dụng](00-foundations/dsa/01-complexity-analysis/README.md) | Năm 1 · Q1 | ✍️ |
| DSA-02 | [HashMap internals](00-foundations/dsa/02-hash-map-internals/README.md) | Năm 1 · Q1 | ✍️ |
| DSA-03 | [Cây, BST, B-tree](00-foundations/dsa/03-trees-btree/README.md) | Năm 1 · Q1 | 🔲 |
| DSA-04 | [Heap & Priority Queue](00-foundations/dsa/04-heap-priority-queue/README.md) | Năm 1 · Q1 | 🔲 |
| DSA-05 | [Graph, BFS, DFS, Topological sort](00-foundations/dsa/05-graph-bfs-dfs/README.md) | Năm 1 · Q1 | 🔲 |
| DSA-06 | [LRU Cache](00-foundations/dsa/06-lru-cache/README.md) | Năm 1 · Q1 | 🔲 |
| DSA-07 | [Sorting & Binary search thực dụng](00-foundations/dsa/07-sorting-searching/README.md) | Năm 1 · Q1 | 🔲 |

### JVM — Java hiện đại & JVM Internals · [`01-software-engineering/java-jvm/`](01-software-engineering/java-jvm/README.md)

| ID | Bài | Giai đoạn | Status |
| :--- | :--- | :--- | :---: |
| JVM-01 | [Java 7 → 21: Bridge Cheatsheet](01-software-engineering/java-jvm/01-java-8-to-21-bridge/README.md) | Quý 0 · Bridge | ✍️ |
| JVM-02 | [Records, Sealed classes, Pattern matching](01-software-engineering/java-jvm/02-records-sealed-pattern-matching/README.md) | Quý 0 · Bridge | ✍️ |
| JVM-03 | [Class loading & ClassLoader hierarchy](01-software-engineering/java-jvm/03-class-loading/README.md) | Năm 1 · Q1 | ✍️ |
| JVM-04 | [JVM Memory: Heap, Stack, Metaspace, Direct memory](01-software-engineering/java-jvm/04-jvm-memory-areas/README.md) | Năm 1 · Q1 | ✍️ |
| JVM-05 | [JIT: Interpreter, C1, C2, tiered compilation](01-software-engineering/java-jvm/05-jit-compilation/README.md) | Năm 1 · Q1 | ✍️ |
| JVM-06 | [GC cơ bản & G1](01-software-engineering/java-jvm/06-gc-fundamentals-g1/README.md) | Năm 1 · Q1 | ✍️ |
| JVM-07 | [ZGC / Shenandoah & low-latency GC](01-software-engineering/java-jvm/07-zgc-low-latency/README.md) | Năm 1 · Q1 | ✍️ |
| JVM-08 | [Đọc GC log](01-software-engineering/java-jvm/08-gc-log-analysis/README.md) | Năm 1 · Q1 | ✍️ |
| JVM-09 | [Heap dump, Eclipse MAT, JFR](01-software-engineering/java-jvm/09-heap-dump-mat-jfr/README.md) | Năm 1 · Q1 | ✍️ |
| JVM-10 | [Metaspace & ClassLoader leak](01-software-engineering/java-jvm/10-metaspace-classloader-leak/README.md) | Năm 1 · Q1 | ✍️ |

### CQ — Code Quality, Refactoring & Testing · [`01-software-engineering/code-quality/`](01-software-engineering/code-quality/README.md)

| ID | Bài | Giai đoạn | Status |
| :--- | :--- | :--- | :---: |
| CQ-01 | [SOLID áp dụng thực tế](01-software-engineering/code-quality/01-solid-in-practice/README.md) | Năm 1 · Q1 | ✍️ |
| CQ-02 | [Cohesion, Coupling, Deep modules](01-software-engineering/code-quality/02-cohesion-coupling-deep-modules/README.md) | Năm 1 · Q1 | ✍️ |
| CQ-03 | [Refactoring catalog & code smells](01-software-engineering/code-quality/03-refactoring-catalog/README.md) | Năm 1 · Q1 | ✍️ |
| CQ-04 | [Legacy code & Characterization tests](01-software-engineering/code-quality/04-legacy-characterization-tests/README.md) | Năm 1 · Q1 | ✍️ |
| CQ-05 | [Seams & Strangler Fig](01-software-engineering/code-quality/05-seams-strangler/README.md) | Năm 1 · Q1 | ✍️ |
| CQ-06 | [JUnit 5 & Mockito](01-software-engineering/code-quality/06-junit5-mockito/README.md) | Năm 1 · Q1 | ✍️ |
| CQ-07 | [Test pyramid & khi nào KHÔNG mock](01-software-engineering/code-quality/07-test-pyramid-mocking/README.md) | Năm 1 · Q1 | ✍️ |
| CQ-08 | [Integration test với Testcontainers](01-software-engineering/code-quality/08-testcontainers/README.md) | Năm 1 · Q1 | ✍️ |

### CONC — Concurrency · [`01-software-engineering/concurrency/`](01-software-engineering/concurrency/README.md)

| ID | Bài | Giai đoạn | Status |
| :--- | :--- | :--- | :---: |
| CONC-01 | [Java Memory Model & happens-before](01-software-engineering/concurrency/01-jmm-happens-before/README.md) | Năm 1 · Q2 | 🔲 |
| CONC-02 | [volatile, synchronized & monitor](01-software-engineering/concurrency/02-volatile-synchronized/README.md) | Năm 1 · Q2 | 🔲 |
| CONC-03 | [CAS, Atomic* & LongAdder](01-software-engineering/concurrency/03-cas-atomics/README.md) | Năm 1 · Q2 | 🔲 |
| CONC-04 | [Thread Pool Internals](01-software-engineering/concurrency/04-thread-pool-internals/README.md) | Năm 1 · Q2 | 🔲 |
| CONC-05 | [CompletableFuture & async composition](01-software-engineering/concurrency/05-completablefuture/README.md) | Năm 1 · Q2 | 🔲 |
| CONC-06 | [ConcurrentHashMap internals](01-software-engineering/concurrency/06-concurrenthashmap-internals/README.md) | Năm 1 · Q2 | 🔲 |
| CONC-07 | [ReentrantLock, ReadWriteLock, StampedLock](01-software-engineering/concurrency/07-locks-reentrant-stamped/README.md) | Năm 1 · Q2 | 🔲 |
| CONC-08 | [Virtual Threads vs Platform Threads](01-software-engineering/concurrency/08-virtual-threads/README.md) | Năm 1 · Q2 | 🔲 |
| CONC-09 | [Deadlock, Livelock, Starvation](01-software-engineering/concurrency/09-deadlock-livelock-starvation/README.md) | Năm 1 · Q2 | 🔲 |
| CONC-10 | [Đọc Thread dump](01-software-engineering/concurrency/10-thread-dump-analysis/README.md) | Năm 1 · Q2 | 🔲 |

### DB — Database Internals (PostgreSQL) · [`01-software-engineering/database/`](01-software-engineering/database/README.md)

| ID | Bài | Giai đoạn | Status |
| :--- | :--- | :--- | :---: |
| DB-01 | [Storage: page, heap tuple, TOAST](01-software-engineering/database/01-storage-page-heap-toast/README.md) | Năm 1 · Q2 | 🔲 |
| DB-02 | [WAL, checkpoint & durability](01-software-engineering/database/02-wal-durability/README.md) | Năm 1 · Q2 | 🔲 |
| DB-03 | [B-tree index internals](01-software-engineering/database/03-btree-index-internals/README.md) | Năm 1 · Q2 | 🔲 |
| DB-04 | [Hash, GIN, GiST, BRIN, partial & expression index](01-software-engineering/database/04-index-types/README.md) | Năm 1 · Q2 | 🔲 |
| DB-05 | [Composite & covering index](01-software-engineering/database/05-composite-covering-index/README.md) | Năm 1 · Q2 | 🔲 |
| DB-06 | [EXPLAIN ANALYZE & Query planner](01-software-engineering/database/06-explain-analyze-planner/README.md) | Năm 1 · Q2 | 🔲 |
| DB-07 | [Statistics & bad plans](01-software-engineering/database/07-statistics-bad-plans/README.md) | Năm 1 · Q2 | 🔲 |
| DB-08 | [MVCC: xmin, xmax, snapshot](01-software-engineering/database/08-mvcc/README.md) | Năm 1 · Q2 | 🔲 |
| DB-09 | [VACUUM, autovacuum & bloat](01-software-engineering/database/09-vacuum-bloat/README.md) | Năm 1 · Q2 | 🔲 |
| DB-10 | [Isolation levels & anomalies](01-software-engineering/database/10-isolation-levels-anomalies/README.md) | Năm 1 · Q2 | 🔲 |
| DB-11 | [Write skew & SERIALIZABLE (SSI)](01-software-engineering/database/11-write-skew-serializable/README.md) | Năm 1 · Q2 | 🔲 |
| DB-12 | [Row locks, FOR UPDATE, SKIP LOCKED](01-software-engineering/database/12-row-locks-skip-locked/README.md) | Năm 1 · Q2 | 🔲 |
| DB-13 | [Advisory locks & lock contention](01-software-engineering/database/13-advisory-locks-contention/README.md) | Năm 1 · Q2 | 🔲 |
| DB-14 | [Connection pool: HikariCP sizing & exhaustion](01-software-engineering/database/14-connection-pool-sizing/README.md) | Năm 1 · Q2 | 🔲 |
| DB-15 | [Timeout chain: client → app → pool → DB](01-software-engineering/database/15-timeout-chain/README.md) | Năm 1 · Q2 | 🔲 |

### PERF — Performance Optimization · [`01-software-engineering/performance/`](01-software-engineering/performance/README.md)

| ID | Bài | Giai đoạn | Status |
| :--- | :--- | :--- | :---: |
| PERF-01 | [Phương pháp: đo trước, tối ưu sau](01-software-engineering/performance/01-measure-first/README.md) | Năm 1 · Q2 | 🔲 |
| PERF-02 | [Latency percentiles & coordinated omission](01-software-engineering/performance/02-latency-percentiles/README.md) | Năm 1 · Q2 | 🔲 |
| PERF-03 | [Little's Law & queueing theory cơ bản](01-software-engineering/performance/03-littles-law-queueing/README.md) | Năm 1 · Q2 | 🔲 |
| PERF-04 | [Micro-benchmark với JMH](01-software-engineering/performance/04-jmh-microbenchmark/README.md) | Năm 1 · Q2 | 🔲 |
| PERF-05 | [Load testing với k6 / Gatling](01-software-engineering/performance/05-load-testing/README.md) | Năm 1 · Q2 | 🔲 |
| PERF-06 | [Profiling: async-profiler & flame graph](01-software-engineering/performance/06-profiling-flame-graph/README.md) | Năm 1 · Q2 | 🔲 |

### ARCH — Backend Architecture (Spring Boot & API) · [`01-software-engineering/backend-architecture/`](01-software-engineering/backend-architecture/README.md)

| ID | Bài | Giai đoạn | Status |
| :--- | :--- | :--- | :---: |
| ARCH-01 | [Spring Boot auto-configuration](01-software-engineering/backend-architecture/01-spring-boot-autoconfiguration/README.md) | Năm 1 · Q3 | 🔲 |
| ARCH-02 | [Bean lifecycle & proxies (JDK vs CGLIB)](01-software-engineering/backend-architecture/02-bean-lifecycle-proxies/README.md) | Năm 1 · Q3 | 🔲 |
| ARCH-03 | [@Transactional: proxy & cạm bẫy](01-software-engineering/backend-architecture/03-transactional-pitfalls/README.md) | Năm 1 · Q3 | 🔲 |
| ARCH-04 | [REST API design](01-software-engineering/backend-architecture/04-rest-api-design/README.md) | Năm 1 · Q3 | 🔲 |
| ARCH-05 | [Error model, validation & versioning](01-software-engineering/backend-architecture/05-error-model-versioning/README.md) | Năm 1 · Q3 | 🔲 |
| ARCH-06 | [Redis & caching strategies](01-software-engineering/backend-architecture/06-redis-caching-strategies/README.md) | Năm 1 · Q3 | 🔲 |
| ARCH-07 | [Cache stampede, hot key, penetration, avalanche](01-software-engineering/backend-architecture/07-cache-stampede-hot-key/README.md) | Năm 1 · Q3 | 🔲 |
| ARCH-08 | [Timeout, Retry, Backoff & Jitter](01-software-engineering/backend-architecture/08-timeout-retry-backoff/README.md) | Năm 1 · Q3 | 🔲 |
| ARCH-09 | [Circuit Breaker, Bulkhead, Rate limiter (Resilience4j)](01-software-engineering/backend-architecture/09-circuit-breaker-bulkhead/README.md) | Năm 1 · Q3 | 🔲 |
| ARCH-10 | [Layered, Hexagonal, Clean Architecture](01-software-engineering/backend-architecture/10-layered-hexagonal-clean/README.md) | Năm 1 · Q3 | 🔲 |
| ARCH-11 | [Modular monolith vs Microservices](01-software-engineering/backend-architecture/11-modular-monolith-vs-microservices/README.md) | Năm 1 · Q3 | 🔲 |

### DIST — Distributed Systems · [`01-software-engineering/distributed-systems/`](01-software-engineering/distributed-systems/README.md)

| ID | Bài | Giai đoạn | Status |
| :--- | :--- | :--- | :---: |
| DIST-01 | [8 Fallacies of Distributed Computing](01-software-engineering/distributed-systems/01-fallacies/README.md) | Năm 1 · Q4 | 🔲 |
| DIST-02 | [CAP & PACELC](01-software-engineering/distributed-systems/02-cap-pacelc/README.md) | Năm 1 · Q4 | 🔲 |
| DIST-03 | [Replication: leader-follower, multi-leader, leaderless](01-software-engineering/distributed-systems/03-replication-models/README.md) | Năm 1 · Q4 | 🔲 |
| DIST-04 | [Replication lag & read-your-writes](01-software-engineering/distributed-systems/04-replication-lag/README.md) | Năm 1 · Q4 | 🔲 |
| DIST-05 | [Partitioning & Consistent hashing](01-software-engineering/distributed-systems/05-partitioning-consistent-hashing/README.md) | Năm 1 · Q4 | 🔲 |
| DIST-06 | [Consensus & Raft](01-software-engineering/distributed-systems/06-raft-consensus/README.md) | Năm 1 · Q4 | 🔲 |
| DIST-07 | [Thời gian, đồng hồ & thứ tự](01-software-engineering/distributed-systems/07-time-clocks-ordering/README.md) | Năm 1 · Q4 | 🔲 |
| DIST-08 | [Kafka architecture](01-software-engineering/distributed-systems/08-kafka-architecture/README.md) | Năm 1 · Q4 | 🔲 |
| DIST-09 | [Kafka consumer groups & rebalancing](01-software-engineering/distributed-systems/09-kafka-consumer-groups/README.md) | Năm 1 · Q4 | 🔲 |
| DIST-10 | [At-most/at-least/exactly-once & idempotent consumer](01-software-engineering/distributed-systems/10-delivery-semantics/README.md) | Năm 1 · Q4 | 🔲 |
| DIST-11 | [Transactional Outbox & CDC](01-software-engineering/distributed-systems/11-outbox-pattern/README.md) | Năm 1 · Q4 | 🔲 |
| DIST-12 | [Saga: orchestration vs choreography](01-software-engineering/distributed-systems/12-saga-pattern/README.md) | Năm 1 · Q4 | 🔲 |
| DIST-13 | [CQRS & Event Sourcing](01-software-engineering/distributed-systems/13-cqrs-event-sourcing/README.md) | Năm 1 · Q4 | 🔲 |
| DIST-14 | [Distributed lock & fencing token](01-software-engineering/distributed-systems/14-distributed-lock-fencing/README.md) | Năm 1 · Q4 | 🔲 |

### SEC — Application Security · [`01-software-engineering/security/`](01-software-engineering/security/README.md)

| ID | Bài | Giai đoạn | Status |
| :--- | :--- | :--- | :---: |
| SEC-01 | [OWASP Top 10 cho backend](01-software-engineering/security/01-owasp-top10-backend/README.md) | Năm 1 · Q4 | 🔲 |
| SEC-02 | [AuthN/AuthZ: OAuth2 & OIDC](01-software-engineering/security/02-authn-authz-oauth2-oidc/README.md) | Năm 1 · Q4 | 🔲 |
| SEC-03 | [JWT pitfalls](01-software-engineering/security/03-jwt-pitfalls/README.md) | Năm 1 · Q4 | 🔲 |
| SEC-04 | [Injection, XSS, CSRF](01-software-engineering/security/04-injection-xss-csrf/README.md) | Năm 1 · Q4 | 🔲 |
| SEC-05 | [Quản lý secret & cấu hình an toàn](01-software-engineering/security/05-secret-handling/README.md) | Năm 1 · Q4 | 🔲 |
| SEC-06 | [Threat modeling (STRIDE)](01-software-engineering/security/06-threat-modeling/README.md) | Năm 3 · Q1 | 🔲 |

### AI — AI Engineering · [`01-software-engineering/ai-engineering/`](01-software-engineering/ai-engineering/README.md)

| ID | Bài | Giai đoạn | Status |
| :--- | :--- | :--- | :---: |
| AI-01 | [AI-assisted development playbook](01-software-engineering/ai-engineering/01-ai-assisted-dev-playbook/README.md) | Năm 1 · Q1 | ✍️ |
| AI-02 | [Embeddings cơ bản](01-software-engineering/ai-engineering/02-embeddings-fundamentals/README.md) | Năm 1 · Q4 | 🔲 |
| AI-03 | [Chunking strategies](01-software-engineering/ai-engineering/03-chunking-strategies/README.md) | Năm 1 · Q4 | 🔲 |
| AI-04 | [Vector search với pgvector](01-software-engineering/ai-engineering/04-vector-search-pgvector/README.md) | Năm 1 · Q4 | 🔲 |
| AI-05 | [Hybrid search: BM25 + vector](01-software-engineering/ai-engineering/05-hybrid-search-bm25/README.md) | Năm 2 · Q1 | 🔲 |
| AI-06 | [Reranking & đánh giá retrieval](01-software-engineering/ai-engineering/06-reranking-retrieval-eval/README.md) | Năm 2 · Q1 | 🔲 |
| AI-07 | [LLM trong production: latency, cost, caching](01-software-engineering/ai-engineering/07-llm-production/README.md) | Năm 2 · Q2 | 🔲 |
| AI-08 | [Prompt injection & guardrails](01-software-engineering/ai-engineering/08-prompt-injection-guardrails/README.md) | Năm 2 · Q4 | 🔲 |
| AI-09 | [Tool calling, Agents & MCP](01-software-engineering/ai-engineering/09-tool-calling-agents-mcp/README.md) | Năm 3 · Q1 | 🔲 |

### SD — System Design · [`01-software-engineering/system-design/`](01-software-engineering/system-design/README.md)

| ID | Bài | Giai đoạn | Status |
| :--- | :--- | :--- | :---: |
| SD-01 | [Framework & Back-of-envelope estimation](01-software-engineering/system-design/framework-estimation.md) | Năm 1 · Q4 | 🔲 |
| SD-02 | [URL Shortener](01-software-engineering/system-design/url-shortener.md) | Năm 1 · Q4 | 🔲 |
| SD-03 | [Rate Limiter](01-software-engineering/system-design/rate-limiter.md) | Năm 1 · Q4 | 🔲 |
| SD-04 | [Notification System](01-software-engineering/system-design/notification-system.md) | Năm 1 · Q4 | 🔲 |
| SD-05 | [Ticket Booking](01-software-engineering/system-design/ticket-booking.md) | Năm 1 · Q4 | 🔲 |
| SD-06 | [News Feed](01-software-engineering/system-design/news-feed.md) | Năm 1 · Q4 | 🔲 |
| SD-07 | [Payment System](01-software-engineering/system-design/payment-system.md) | Năm 1 · Q4 | 🔲 |

### PAT — Patterns · [`01-software-engineering/patterns/`](01-software-engineering/patterns/README.md)

| ID | Bài | Giai đoạn | Status |
| :--- | :--- | :--- | :---: |
| PAT-01 | [Circuit Breaker](01-software-engineering/patterns/circuit-breaker.md) | Năm 1 · Q3 | 🔲 |
| PAT-02 | [Retry with Exponential Backoff & Jitter](01-software-engineering/patterns/retry-exponential-backoff.md) | Năm 1 · Q3 | 🔲 |
| PAT-03 | [Bulkhead](01-software-engineering/patterns/bulkhead.md) | Năm 1 · Q3 | 🔲 |
| PAT-04 | [Timeout](01-software-engineering/patterns/timeout.md) | Năm 1 · Q3 | 🔲 |
| PAT-05 | [Fallback & Graceful Degradation](01-software-engineering/patterns/fallback-graceful-degradation.md) | Năm 1 · Q3 | 🔲 |
| PAT-06 | [Read Replicas](01-software-engineering/patterns/read-replicas.md) | Năm 1 · Q3 | 🔲 |
| PAT-07 | [CQRS](01-software-engineering/patterns/cqrs.md) | Năm 1 · Q3 | 🔲 |
| PAT-08 | [Event Sourcing](01-software-engineering/patterns/event-sourcing.md) | Năm 1 · Q3 | 🔲 |
| PAT-09 | [Sharding / Partitioning](01-software-engineering/patterns/sharding.md) | Năm 1 · Q3 | 🔲 |
| PAT-10 | [Fan-out on Write vs on Read](01-software-engineering/patterns/fan-out-write-vs-read.md) | Năm 1 · Q3 | 🔲 |
| PAT-11 | [Cache-Aside](01-software-engineering/patterns/cache-aside.md) | Năm 1 · Q3 | 🔲 |
| PAT-12 | [Write-Through Cache](01-software-engineering/patterns/write-through-cache.md) | Năm 1 · Q3 | 🔲 |
| PAT-13 | [Write-Behind Cache](01-software-engineering/patterns/write-behind-cache.md) | Năm 1 · Q3 | 🔲 |
| PAT-14 | [Saga](01-software-engineering/patterns/saga.md) | Năm 1 · Q3 | 🔲 |
| PAT-15 | [Transactional Outbox](01-software-engineering/patterns/outbox.md) | Năm 1 · Q3 | 🔲 |
| PAT-16 | [Competing Consumers](01-software-engineering/patterns/competing-consumers.md) | Năm 1 · Q3 | 🔲 |
| PAT-17 | [Dead Letter Queue](01-software-engineering/patterns/dead-letter-queue.md) | Năm 1 · Q3 | 🔲 |
| PAT-18 | [Idempotent Consumer & Message Deduplication](01-software-engineering/patterns/idempotent-consumer.md) | Năm 1 · Q3 | 🔲 |
| PAT-19 | [Strangler Fig](01-software-engineering/patterns/strangler-fig.md) | Năm 1 · Q3 | 🔲 |
| PAT-20 | [Rate Limiter](01-software-engineering/patterns/rate-limiter.md) | Năm 1 · Q3 | 🔲 |

### CD — Concept Decisions · [`01-software-engineering/concept-decisions/`](01-software-engineering/concept-decisions/README.md)

| ID | Bài | Giai đoạn | Status |
| :--- | :--- | :--- | :---: |
| CD-01 | [Chọn Isolation Level: READ COMMITTED vs REPEATABLE READ vs SERIALIZABLE](01-software-engineering/concept-decisions/isolation-level-choice.md) | Năm 1 · Q2 | 🔲 |
| CD-02 | [Pool sizing: nhỏ vs lớn](01-software-engineering/concept-decisions/connection-pool-sizing.md) | Năm 1 · Q2 | 🔲 |
| CD-03 | [Virtual Threads vs Platform Thread Pool](01-software-engineering/concept-decisions/virtual-vs-platform-threads.md) | Năm 1 · Q2 | 🔲 |
| CD-04 | [Distributed lock: Redis vs Database vs etcd](01-software-engineering/concept-decisions/redis-lock-vs-db-lock.md) | Năm 1 · Q4 | 🔲 |
| CD-05 | [Kafka vs RabbitMQ](01-software-engineering/concept-decisions/kafka-vs-rabbitmq.md) | Năm 1 · Q4 | 🔲 |
| CD-06 | [Saga: Orchestration vs Choreography](01-software-engineering/concept-decisions/saga-orchestration-vs-choreography.md) | Năm 1 · Q4 | 🔲 |
| CD-07 | [Modular Monolith vs Microservices](01-software-engineering/concept-decisions/monolith-vs-microservices.md) | Năm 1 · Q3 | 🔲 |
| CD-08 | [Jenkins vs GitHub Actions](01-software-engineering/concept-decisions/jenkins-vs-github-actions.md) | Năm 1 · Q3 | 🔲 |
| CD-09 | [Terraform: Workspace vs Directory-per-env](01-software-engineering/concept-decisions/terraform-workspace-vs-directory.md) | Năm 2 · Q1 | 🔲 |
| CD-10 | [Helm vs Kustomize](01-software-engineering/concept-decisions/helm-vs-kustomize.md) | Năm 2 · Q3 | 🔲 |
| CD-11 | [Rolling vs Blue-Green vs Canary](01-software-engineering/concept-decisions/deployment-strategy-choice.md) | Năm 2 · Q2 | 🔲 |

### LNX — Linux & Networking · [`02-devops-and-cloud/linux-networking/`](02-devops-and-cloud/linux-networking/README.md)

| ID | Bài | Giai đoạn | Status |
| :--- | :--- | :--- | :---: |
| LNX-01 | [Process, thread & signal](02-devops-and-cloud/linux-networking/01-processes-signals/README.md) | Năm 1 · Q1 | ✍️ |
| LNX-02 | [File descriptor & I/O](02-devops-and-cloud/linux-networking/02-file-descriptors-io/README.md) | Năm 1 · Q1 | ✍️ |
| LNX-03 | [Virtual memory, page cache, swap & OOM killer](02-devops-and-cloud/linux-networking/03-memory-pagecache-oom/README.md) | Năm 1 · Q1 | ✍️ |
| LNX-04 | [systemd & quản lý service](02-devops-and-cloud/linux-networking/04-systemd-services/README.md) | Năm 1 · Q1 | ✍️ |
| LNX-05 | [Công cụ hiệu năng Linux](02-devops-and-cloud/linux-networking/05-linux-perf-tools/README.md) | Năm 1 · Q1 | ✍️ |
| LNX-06 | [Shell scripting tự động hóa](02-devops-and-cloud/linux-networking/06-shell-scripting/README.md) | Năm 1 · Q1 | ✍️ |
| LNX-07 | [select, poll, epoll & event loop](02-devops-and-cloud/linux-networking/07-epoll-io-multiplexing/README.md) | Năm 1 · Q1 | ✍️ |
| LNX-08 | [TCP: handshake, state machine, backlog](02-devops-and-cloud/linux-networking/08-tcp-handshake-states/README.md) | Năm 1 · Q1 | ✍️ |
| LNX-09 | [TIME_WAIT, CLOSE_WAIT & socket exhaustion](02-devops-and-cloud/linux-networking/09-time-wait-close-wait/README.md) | Năm 1 · Q1 | ✍️ |
| LNX-10 | [HTTP/1.1 vs HTTP/2 vs HTTP/3](02-devops-and-cloud/linux-networking/10-http-versions/README.md) | Năm 1 · Q1 | ✍️ |
| LNX-11 | [DNS resolution & troubleshooting](02-devops-and-cloud/linux-networking/11-dns-resolution/README.md) | Năm 1 · Q1 | ✍️ |
| LNX-12 | [TLS handshake & certificate chain](02-devops-and-cloud/linux-networking/12-tls-handshake/README.md) | Năm 1 · Q1 | ✍️ |
| LNX-13 | [Load balancer L4 vs L7 & Nginx reverse proxy](02-devops-and-cloud/linux-networking/13-load-balancer-nginx/README.md) | Năm 1 · Q1 | ✍️ |
| LNX-14 | [Bắt gói tin: tcpdump & Wireshark](02-devops-and-cloud/linux-networking/14-tcpdump-wireshark/README.md) | Năm 1 · Q1 | ✍️ |
| LNX-15 | [iptables/nftables & network namespace](02-devops-and-cloud/linux-networking/15-iptables-netns/README.md) | Năm 1 · Q1 | ✍️ |

### CTR — Containers & Kubernetes · [`02-devops-and-cloud/containers-k8s/`](02-devops-and-cloud/containers-k8s/README.md)

| ID | Bài | Giai đoạn | Status |
| :--- | :--- | :--- | :---: |
| CTR-01 | [Container internals: namespaces & cgroups](02-devops-and-cloud/containers-k8s/01-namespaces-cgroups/README.md) | Năm 1 · Q3 | 🔲 |
| CTR-02 | [Image layers & OverlayFS](02-devops-and-cloud/containers-k8s/02-overlayfs-image-layers/README.md) | Năm 1 · Q3 | 🔲 |
| CTR-03 | [Dockerfile: multi-stage, distroless, jlink, non-root](02-devops-and-cloud/containers-k8s/03-dockerfile-best-practices/README.md) | Năm 1 · Q3 | 🔲 |
| CTR-04 | [JVM trong container](02-devops-and-cloud/containers-k8s/04-jvm-in-containers/README.md) | Năm 1 · Q3 | 🔲 |
| CTR-05 | [Docker networking](02-devops-and-cloud/containers-k8s/05-docker-networking/README.md) | Năm 1 · Q3 | 🔲 |
| CTR-06 | [Docker Compose cho môi trường dev](02-devops-and-cloud/containers-k8s/06-docker-compose-dev/README.md) | Năm 1 · Q3 | 🔲 |
| CTR-07 | [Kiến trúc Kubernetes](02-devops-and-cloud/containers-k8s/07-k8s-architecture/README.md) | Năm 2 · Q3 | 🔲 |
| CTR-08 | [Pod lifecycle & probes](02-devops-and-cloud/containers-k8s/08-pod-lifecycle-probes/README.md) | Năm 2 · Q3 | 🔲 |
| CTR-09 | [Deployment, StatefulSet, DaemonSet, Job](02-devops-and-cloud/containers-k8s/09-workloads/README.md) | Năm 2 · Q3 | 🔲 |
| CTR-10 | [Requests, Limits & QoS](02-devops-and-cloud/containers-k8s/10-requests-limits-qos/README.md) | Năm 2 · Q3 | 🔲 |
| CTR-11 | [Service, Ingress & CNI](02-devops-and-cloud/containers-k8s/11-services-ingress-cni/README.md) | Năm 2 · Q3 | 🔲 |
| CTR-12 | [NetworkPolicy](02-devops-and-cloud/containers-k8s/12-networkpolicy/README.md) | Năm 2 · Q4 | 🔲 |
| CTR-13 | [Storage: PV, PVC, StorageClass](02-devops-and-cloud/containers-k8s/13-storage-pv-pvc/README.md) | Năm 2 · Q3 | 🔲 |
| CTR-14 | [ConfigMap, Secret, RBAC, ServiceAccount](02-devops-and-cloud/containers-k8s/14-config-secrets-rbac/README.md) | Năm 2 · Q3 | 🔲 |
| CTR-15 | [Helm charts](02-devops-and-cloud/containers-k8s/15-helm/README.md) | Năm 2 · Q3 | 🔲 |
| CTR-16 | [Kustomize](02-devops-and-cloud/containers-k8s/16-kustomize/README.md) | Năm 2 · Q3 | 🔲 |
| CTR-17 | [HPA, VPA & Cluster Autoscaler](02-devops-and-cloud/containers-k8s/17-hpa-autoscaling/README.md) | Năm 2 · Q3 | 🔲 |
| CTR-18 | [Debug Kubernetes](02-devops-and-cloud/containers-k8s/18-debugging-k8s/README.md) | Năm 2 · Q3 | 🔲 |
| CTR-19 | [Operators & CRD](02-devops-and-cloud/containers-k8s/19-operators-crd/README.md) | Năm 3 · Q1 | 🔲 |

### AWS — AWS Cloud cơ bản · [`02-devops-and-cloud/aws-cloud/`](02-devops-and-cloud/aws-cloud/README.md)

| ID | Bài | Giai đoạn | Status |
| :--- | :--- | :--- | :---: |
| AWS-01 | [IAM & least privilege](02-devops-and-cloud/aws-cloud/01-iam-least-privilege/README.md) | Năm 1 · Q4 | 🔲 |
| AWS-02 | [VPC, subnet, route table, SG, NACL](02-devops-and-cloud/aws-cloud/02-vpc-networking/README.md) | Năm 1 · Q4 | 🔲 |
| AWS-03 | [EC2 & Auto Scaling](02-devops-and-cloud/aws-cloud/03-ec2-autoscaling/README.md) | Năm 1 · Q4 | 🔲 |
| AWS-04 | [RDS PostgreSQL](02-devops-and-cloud/aws-cloud/04-rds/README.md) | Năm 1 · Q4 | 🔲 |
| AWS-05 | [S3](02-devops-and-cloud/aws-cloud/05-s3/README.md) | Năm 1 · Q4 | 🔲 |
| AWS-06 | [ALB & Route 53](02-devops-and-cloud/aws-cloud/06-alb-route53/README.md) | Năm 1 · Q4 | 🔲 |
| AWS-07 | [CloudWatch: metrics, logs, alarms](02-devops-and-cloud/aws-cloud/07-cloudwatch/README.md) | Năm 1 · Q4 | 🔲 |
| AWS-08 | [Deploy Project 1 lên AWS bằng tay (ClickOps)](02-devops-and-cloud/aws-cloud/08-clickops-deploy/README.md) | Năm 1 · Q4 | 🔲 |
| AWS-09 | [Cost awareness](02-devops-and-cloud/aws-cloud/09-cost-awareness/README.md) | Năm 2 · Q1 | 🔲 |

### IAC — Infrastructure as Code · [`02-devops-and-cloud/iac-terraform/`](02-devops-and-cloud/iac-terraform/README.md)

| ID | Bài | Giai đoạn | Status |
| :--- | :--- | :--- | :---: |
| IAC-01 | [Terraform core: provider, resource, lifecycle](02-devops-and-cloud/iac-terraform/01-terraform-core/README.md) | Năm 2 · Q1 | 🔲 |
| IAC-02 | [State, remote backend & locking](02-devops-and-cloud/iac-terraform/02-state-backend-locking/README.md) | Năm 2 · Q1 | 🔲 |
| IAC-03 | [Thiết kế module](02-devops-and-cloud/iac-terraform/03-modules-design/README.md) | Năm 2 · Q1 | 🔲 |
| IAC-04 | [Tổ chức multi-environment](02-devops-and-cloud/iac-terraform/04-multi-env-layout/README.md) | Năm 2 · Q1 | 🔲 |
| IAC-05 | [Drift, import & refactor](02-devops-and-cloud/iac-terraform/05-drift-import-refactor/README.md) | Năm 2 · Q1 | 🔲 |
| IAC-06 | [Ansible fundamentals](02-devops-and-cloud/iac-terraform/06-ansible-fundamentals/README.md) | Năm 2 · Q1 | 🔲 |
| IAC-07 | [Provisioning vs Configuration management vs Immutable image](02-devops-and-cloud/iac-terraform/07-provisioning-vs-config/README.md) | Năm 2 · Q1 | 🔲 |
| IAC-08 | [Secrets: Vault & AWS Secrets Manager](02-devops-and-cloud/iac-terraform/08-secrets-vault/README.md) | Năm 2 · Q1 | 🔲 |
| IAC-09 | [Kiến trúc tham chiếu AWS Multi-AZ bằng Terraform](02-devops-and-cloud/iac-terraform/09-aws-multi-az-architecture/README.md) | Năm 2 · Q1 | 🔲 |

### CICD — CI/CD & Delivery · [`02-devops-and-cloud/ci-cd-pipelines/`](02-devops-and-cloud/ci-cd-pipelines/README.md)

| ID | Bài | Giai đoạn | Status |
| :--- | :--- | :--- | :---: |
| CICD-01 | [CI cơ bản với GitHub Actions](02-devops-and-cloud/ci-cd-pipelines/01-ci-github-actions/README.md) | Năm 1 · Q3 | 🔲 |
| CICD-02 | [Jenkins pipeline-as-code](02-devops-and-cloud/ci-cd-pipelines/02-jenkins-pipeline/README.md) | Năm 1 · Q3 | 🔲 |
| CICD-03 | [GitHub Actions nâng cao](02-devops-and-cloud/ci-cd-pipelines/03-gha-advanced/README.md) | Năm 2 · Q2 | 🔲 |
| CICD-04 | [OIDC từ CI tới AWS](02-devops-and-cloud/ci-cd-pipelines/04-oidc-to-aws/README.md) | Năm 2 · Q2 | 🔲 |
| CICD-05 | [Quality gates: lint, SAST, dependency scan](02-devops-and-cloud/ci-cd-pipelines/05-quality-gates/README.md) | Năm 2 · Q2 | 🔲 |
| CICD-06 | [Supply chain: SBOM, image signing](02-devops-and-cloud/ci-cd-pipelines/06-supply-chain-security/README.md) | Năm 2 · Q4 | 🔲 |
| CICD-07 | [Deployment strategies](02-devops-and-cloud/ci-cd-pipelines/07-deployment-strategies/README.md) | Năm 2 · Q2 | 🔲 |
| CICD-08 | [Feature flags](02-devops-and-cloud/ci-cd-pipelines/08-feature-flags/README.md) | Năm 2 · Q2 | 🔲 |
| CICD-09 | [Automated rollback](02-devops-and-cloud/ci-cd-pipelines/09-automated-rollback/README.md) | Năm 2 · Q2 | 🔲 |
| CICD-10 | [Database migration an toàn (Flyway, expand-contract)](02-devops-and-cloud/ci-cd-pipelines/10-db-migration-expand-contract/README.md) | Năm 2 · Q2 | 🔲 |
| CICD-11 | [GitOps với Argo CD](02-devops-and-cloud/ci-cd-pipelines/11-gitops-argocd/README.md) | Năm 2 · Q3 | 🔲 |

### OBS — Observability & SRE · [`02-devops-and-cloud/observability-sre/`](02-devops-and-cloud/observability-sre/README.md)

| ID | Bài | Giai đoạn | Status |
| :--- | :--- | :--- | :---: |
| OBS-01 | [Observability vs Monitoring & 3 trụ cột](02-devops-and-cloud/observability-sre/01-three-pillars/README.md) | Năm 2 · Q4 | 🔲 |
| OBS-02 | [Prometheus: pull model & PromQL](02-devops-and-cloud/observability-sre/02-prometheus-promql/README.md) | Năm 2 · Q4 | 🔲 |
| OBS-03 | [Cardinality & chi phí metric](02-devops-and-cloud/observability-sre/03-cardinality/README.md) | Năm 2 · Q4 | 🔲 |
| OBS-04 | [Grafana dashboards: RED & USE](02-devops-and-cloud/observability-sre/04-grafana-red-use/README.md) | Năm 2 · Q4 | 🔲 |
| OBS-05 | [Structured logging & Loki](02-devops-and-cloud/observability-sre/05-structured-logging-loki/README.md) | Năm 2 · Q4 | 🔲 |
| OBS-06 | [Distributed tracing với OpenTelemetry](02-devops-and-cloud/observability-sre/06-opentelemetry-tracing/README.md) | Năm 2 · Q4 | 🔲 |
| OBS-07 | [SLI, SLO, SLA & Error budget](02-devops-and-cloud/observability-sre/07-sli-slo-error-budget/README.md) | Năm 2 · Q4 | 🔲 |
| OBS-08 | [Alerting theo triệu chứng & burn rate](02-devops-and-cloud/observability-sre/08-symptom-based-alerting/README.md) | Năm 2 · Q4 | 🔲 |
| OBS-09 | [Runbook & on-call](02-devops-and-cloud/observability-sre/09-runbooks-oncall/README.md) | Năm 2 · Q4 | 🔲 |
| OBS-10 | [Incident management & blameless post-mortem](02-devops-and-cloud/observability-sre/10-incident-management/README.md) | Năm 2 · Q4 | 🔲 |
| OBS-11 | [Chaos engineering & game day](02-devops-and-cloud/observability-sre/11-chaos-engineering/README.md) | Năm 2 · Q4 | 🔲 |
| OBS-12 | [Capacity planning & load test quy mô lớn](02-devops-and-cloud/observability-sre/12-capacity-planning/README.md) | Năm 2 · Q4 | 🔲 |

### INC — Incident Labs (tái hiện sự cố có chủ đích) · [`03-production-troubleshooting/incident-labs/`](03-production-troubleshooting/incident-labs/README.md)

| ID | Bài | Giai đoạn | Status |
| :--- | :--- | :--- | :---: |
| INC-01 | [N+1 query gây latency spike](03-production-troubleshooting/incident-labs/01-n-plus-1-latency-spike) | Năm 1 · Q2 | 🔲 |
| INC-02 | [Cạn connection pool](03-production-troubleshooting/incident-labs/02-db-pool-exhaustion) | Năm 1 · Q2 | 🔲 |
| INC-03 | [Deadlock trong database](03-production-troubleshooting/incident-labs/03-db-deadlock) | Năm 1 · Q2 | 🔲 |
| INC-04 | [Lost update do race condition](03-production-troubleshooting/incident-labs/04-lost-update-race) | Năm 1 · Q2 | 🔲 |
| INC-05 | [Thread pool starvation](03-production-troubleshooting/incident-labs/05-thread-pool-starvation) | Năm 1 · Q2 | 🔲 |
| INC-06 | [Query chậm đột ngột do statistics cũ / plan đổi](03-production-troubleshooting/incident-labs/06-slow-query-stale-stats) | Năm 1 · Q2 | 🔲 |
| INC-07 | [Memory leak trên heap](03-production-troubleshooting/incident-labs/07-memory-leak-heap) | Năm 1 · Q1 | 🔲 |
| INC-08 | [OOM Metaspace sau nhiều lần redeploy](03-production-troubleshooting/incident-labs/08-metaspace-leak-redeploy) | Năm 1 · Q1 | 🔲 |
| INC-09 | [@Transactional không có tác dụng (self-invocation)](03-production-troubleshooting/incident-labs/09-transactional-self-invocation) | Năm 1 · Q3 | 🔲 |
| INC-10 | [Cache stampede làm sập DB](03-production-troubleshooting/incident-labs/10-cache-stampede) | Năm 1 · Q3 | 🔲 |
| INC-11 | [Retry storm gây cascading failure](03-production-troubleshooting/incident-labs/11-retry-storm) | Năm 1 · Q3 | 🔲 |
| INC-12 | [CLOSE_WAIT tích tụ do không đóng connection](03-production-troubleshooting/incident-labs/12-close-wait-leak) | Năm 1 · Q3 | 🔲 |
| INC-13 | [Container OOMKilled dù heap còn trống](03-production-troubleshooting/incident-labs/13-container-oomkilled) | Năm 1 · Q3 | 🔲 |
| INC-14 | [Kafka consumer rebalance liên tục](03-production-troubleshooting/incident-labs/14-kafka-rebalance-loop) | Năm 1 · Q4 | 🔲 |
| INC-15 | [Xử lý trùng message (trừ tiền 2 lần)](03-production-troubleshooting/incident-labs/15-duplicate-message-processing) | Năm 1 · Q4 | 🔲 |
| INC-16 | [CrashLoopBackOff do liveness probe sai](03-production-troubleshooting/incident-labs/16-k8s-crashloop-probes) | Năm 2 · Q3 | 🔲 |
| INC-17 | [Latency cao do DNS trong Kubernetes (ndots)](03-production-troubleshooting/incident-labs/17-k8s-dns-latency) | Năm 2 · Q3 | 🔲 |
| INC-18 | [p99 latency cao do CPU throttling](03-production-troubleshooting/incident-labs/18-cpu-throttling-latency) | Năm 2 · Q3 | 🔲 |

## 3. Thêm bài mới

1. Chọn domain, lấy ID kế tiếp (ví dụ `CONC-11`).
2. Tạo brief theo đúng format của brief có sẵn (frontmatter + khối 🧭 Brief).
3. Thêm dòng vào cả mục 1 (theo giai đoạn) và mục 2 (theo chủ đề) của file này, và bảng trong README của domain.
