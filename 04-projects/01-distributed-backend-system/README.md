# Project 1 — Distributed Backend System

> **Hệ thống Backend Phân tán Chịu tải Cao & Đảm bảo Tính Nhất quán Dữ liệu**

---

## 🎯 Mục tiêu dự án
Chứng minh năng lực thiết kế kiến trúc hệ thống phân tán, xử lý tải cao (high throughput), concurrency, quản trị transaction liên dịch vụ và phục hồi lỗi.

## 🛠️ Tech Stack
* **Ngôn ngữ & Framework:** Java 21 LTS, Spring Boot 3, Spring Cloud
* **Database:** PostgreSQL (Partitioning, Indexing tuning)
* **Caching & In-Memory:** Redis (Distributed Lock with Redisson, Cache-Aside)
* **Message Broker:** Apache Kafka (Event-driven, Consumer groups, Idempotent Producer)
* **Testing:** Testcontainers, JMeter / k6 (Load & Stress testing)

---

## 📋 Kiến trúc & Tính năng cốt lõi

- [ ] **High-Throughput API & Rate Limiting**: Triển khai Token Bucket / Sliding Window Rate Limiter bằng Redis & Lua scripts.
- [ ] **Caching Layer**: Chiến lược Cache-Aside, phòng chống Cache Stampede / Thundering Herd bằng Mutex Lock và Probabilistic Early Expiration.
- [ ] **Transactional Outbox Pattern**: Đảm bảo atomicity giữa việc ghi database và gửi message ra Kafka (tránh mất message hoặc gửi message rác khi DB rollback).
- [ ] **Saga Pattern (Orchestration-based)**: Xử lý distributed transaction cho chuỗi nghiệp vụ (Order → Inventory → Payment) có kèm bồi hoàn (Compensating Transactions).
- [ ] **Distributed Concurrency Control**: So sánh và áp dụng Optimistic Locking (versioning) vs Pessimistic Locking vs Redisson Distributed Locks cho bài toán tranh chấp số dư / tồn kho.
- [ ] **Idempotent API Keys**: Thiết kế cơ chế khóa key idempotency ở API Gateway/Service layer tránh duplicate charges/requests.

---

📋 Kế hoạch chi tiết: [MILESTONES.md](./MILESTONES.md) · 🧾 Quyết định kiến trúc: [adr/](./adr/README.md)
