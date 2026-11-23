# Kế hoạch & Lộ trình Phát triển Kỹ thuật (Engineering Roadmap)

> Tài liệu kim chỉ nam định hướng học tập và rèn luyện: Đi sâu bản chất Software Engineering & mở rộng năng lực Cloud-Native / DevOps.  
> Trở về trang chủ: [README.md](file:///d:/pkucpkam/repo-git/daily-engineering-lab/README.md)

---

## 1. Trụ cột Kỹ thuật Cốt lõi

### Năng lực nền tảng
- **System Design**
- **Distributed Systems**
- **Database Internals**
- **Performance Optimization**
- **Security**
- **Software Architecture**
- **Production Troubleshooting**

### Phân bổ chủ đề & Nội dung
| Chủ đề | Nội dung chi tiết |
| :--- | :--- |
| **SE** | System Design, Architecture, Distributed Systems |
| **Backend** | Java, JVM, Concurrency, Performance |
| **Database** | Index, Query Optimization, Transaction |
| **DevOps** | Linux, Docker, CI/CD, AWS, Terraform, K8s |
| **AI** | AI-assisted development, RAG, Agent |

---

## 2. Roadmap 3 Năm

> Tui sẽ không chia đều thời gian học tất cả công nghệ. Phải có thứ tự ưu tiên.

### Năm 1 — Xây nền tảng kỹ thuật (SE First)
- **Tỷ trọng:** 70% SE / 30% DevOps
- **Nội dung:**
  - Java nâng cao, JVM, concurrency.
  - System Design, Distributed Systems.
  - Database internals, performance tuning.
  - Linux, Networking.
  - Docker, GitHub Actions, Jenkins.
  - AWS cơ bản.
- **Kết quả cần đạt:** Tự thiết kế, phát triển và deploy một ứng dụng hoàn chỉnh.

### Năm 2 — Mở rộng Infrastructure (DevOps Growth)
- **Tỷ trọng:** 50% SE / 50% DevOps
- **Nội dung:**
  - Terraform, Ansible.
  - Kubernetes, Helm.
  - CI/CD nâng cao.
  - Prometheus, Grafana, OpenTelemetry.
  - Security, observability.
  - Reliability Engineering.
- **Kết quả cần đạt:** Có thể thiết kế hạ tầng, tự động hóa deployment và vận hành ứng dụng production.

### Năm 3 — Định vị chuyên môn (Specialization)
- **Định hướng:** Chọn một nhánh chính:
  - Platform Engineering.
  - SRE (Site Reliability Engineering).
  - Senior Backend / Software Architect có năng lực Cloud Native.
- **Kết quả cần đạt:** Có khả năng thiết kế giải pháp ở cấp độ hệ thống và đảm nhận trách nhiệm kỹ thuật lớn hơn.

---

## 3. Những Năng lực Nên Đầu tư Để Ít Bị AI Cạnh Tranh

Tui xếp theo mức độ ưu tiên:

| Năng lực | Mức độ ưu tiên |
| :--- | :--- |
| **System Design** | Rất cao |
| **Distributed Systems** | Rất cao |
| **Debugging & Troubleshooting** | Rất cao |
| **Coding** | Rất cao |
| **Sử dụng AI hiệu quả** | Rất cao |
| **Database & Performance** | Cao |
| **Cloud Infrastructure** | Cao |
| **Security & Reliability** | Cao |

> [!TIP]
> **Tư duy cốt lõi:** Đừng hiểu *Master SE* là phải học thật nhiều framework.

Ví dụ, thay vì chỉ học Spring Boot, hãy hiểu sâu bản chất:
- JVM quản lý memory thế nào?
- Thread pool hoạt động ra sao?
- Khi 10.000 request đồng thời thì hệ thống xảy ra chuyện gì?
- Database connection pool bị cạn thì xử lý thế nào?
- Làm sao thiết kế hệ thống chịu lỗi?
- Khi deploy phiên bản mới bị lỗi thì rollback ra sao?

*Đây là những kiến thức có giá trị lâu dài hơn việc biết thêm một framework mới.*

---

## 4. Bộ Project Nên Xây Dựng Trong 2 Năm

> Tui muốn ông có 3 project đủ chiều sâu thay vì 10 project tutorial.

### [Project 1 — Distributed Backend System](file:///d:/pkucpkam/repo-git/daily-engineering-lab/04-projects/01-distributed-backend-system/README.md)
- **Tech Stack:** Java, Spring Boot, PostgreSQL, Redis, Kafka.
- **Mục tiêu:** Chứng minh năng lực thiết kế hệ thống, concurrency, database và xử lý distributed transactions.

### [Project 2 — Infrastructure Automation](file:///d:/pkucpkam/repo-git/daily-engineering-lab/04-projects/02-infrastructure-automation/README.md)
- **Tech Stack:** AWS, Terraform, Docker, GitHub Actions.
- **Mục tiêu:** Tự động tạo hạ tầng, build, deploy và rollback.

### [Project 3 — Internal Developer Platform](file:///d:/pkucpkam/repo-git/daily-engineering-lab/04-projects/03-internal-developer-platform/README.md)
- **Tech Stack:** Kubernetes, Helm, ArgoCD, Prometheus, Grafana.
- **Mục tiêu:** Xây dựng nền tảng để developer có thể deploy và theo dõi ứng dụng dễ dàng.

---

## 5. Tài liệu Học Nên Theo

Một số nguồn có thể dùng xuyên suốt:
- **Software Design & Architecture Roadmap**
- **DevOps Roadmap**
- **System Design Roadmap**
- **CNCF Training**
- **Terraform Tutorials**
- **Kubernetes Tutorials**
