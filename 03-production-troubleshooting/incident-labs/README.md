# Incident Labs (tái hiện sự cố có chủ đích)

Sự cố được **tái hiện có chủ đích** để luyện điều tra: đề bài như ticket thật, có lab chạy được, đáp án ẩn. Sau mỗi lab, người học tự viết bug-case vào `../bug-cases/content/`.

> Mỗi bài là một thư mục: `README.md` (lý thuyết) · `exercises.md` · `solutions.md` · `lab/` · `my-notes.md` (người học tự viết).

---

<!-- CURRICULUM:START — sinh tự động từ CURRICULUM, cột Status cập nhật tay -->
## 📚 Danh sách bài

Loại: **Incident lab** · Template: [incident-lab-template.md](../../docs/templates/incident-lab-template.md) · Quy chuẩn: [CONTENT-GUIDE](../../docs/CONTENT-GUIDE.md)

| ID | Bài | Giai đoạn | Status |
| :--- | :--- | :--- | :---: |
| INC-01 | [N+1 query gây latency spike](01-n-plus-1-latency-spike) | Năm 1 · Q2 | 🔲 |
| INC-02 | [Cạn connection pool](02-db-pool-exhaustion) | Năm 1 · Q2 | 🔲 |
| INC-03 | [Deadlock trong database](03-db-deadlock) | Năm 1 · Q2 | 🔲 |
| INC-04 | [Lost update do race condition](04-lost-update-race) | Năm 1 · Q2 | 🔲 |
| INC-05 | [Thread pool starvation](05-thread-pool-starvation) | Năm 1 · Q2 | 🔲 |
| INC-06 | [Query chậm đột ngột do statistics cũ / plan đổi](06-slow-query-stale-stats) | Năm 1 · Q2 | 🔲 |
| INC-07 | [Memory leak trên heap](07-memory-leak-heap) | Năm 1 · Q1 | 🔲 |
| INC-08 | [OOM Metaspace sau nhiều lần redeploy](08-metaspace-leak-redeploy) | Năm 1 · Q1 | 🔲 |
| INC-09 | [@Transactional không có tác dụng (self-invocation)](09-transactional-self-invocation) | Năm 1 · Q3 | 🔲 |
| INC-10 | [Cache stampede làm sập DB](10-cache-stampede) | Năm 1 · Q3 | 🔲 |
| INC-11 | [Retry storm gây cascading failure](11-retry-storm) | Năm 1 · Q3 | 🔲 |
| INC-12 | [CLOSE_WAIT tích tụ do không đóng connection](12-close-wait-leak) | Năm 1 · Q3 | 🔲 |
| INC-13 | [Container OOMKilled dù heap còn trống](13-container-oomkilled) | Năm 1 · Q3 | 🔲 |
| INC-14 | [Kafka consumer rebalance liên tục](14-kafka-rebalance-loop) | Năm 1 · Q4 | 🔲 |
| INC-15 | [Xử lý trùng message (trừ tiền 2 lần)](15-duplicate-message-processing) | Năm 1 · Q4 | 🔲 |
| INC-16 | [CrashLoopBackOff do liveness probe sai](16-k8s-crashloop-probes) | Năm 2 · Q3 | 🔲 |
| INC-17 | [Latency cao do DNS trong Kubernetes (ndots)](17-k8s-dns-latency) | Năm 2 · Q3 | 🔲 |
| INC-18 | [p99 latency cao do CPU throttling](18-cpu-throttling-latency) | Năm 2 · Q3 | 🔲 |

**Tài liệu gốc của chủ đề:** Release It! · Google SRE book · danluu/post-mortems (GitHub)

<!-- CURRICULUM:END -->
