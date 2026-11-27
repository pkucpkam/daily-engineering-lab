# Project 2 — Milestones & Task Breakdown

> Hạ tầng AWS cho chính Project 1. Tất cả tài nguyên tạo bằng code; có lệnh teardown và ước tính chi phí.

## Milestone 1 — Multi-env bằng Terraform (Năm 2 · Q1)
| # | Task | Bài học liên quan | Done khi |
| :-- | :--- | :--- | :--- |
| 1.1 | Remote state S3 + locking, bootstrap stack | IAC-02 | 2 người apply không phá state |
| 1.2 | Module VPC 3-tier, NAT, SG | IAC-03, AWS-02 | `terraform plan` sạch ở 3 env |
| 1.3 | RDS Multi-AZ, ALB, ASG (hoặc ECS) | IAC-09, AWS-03..06 | App truy cập được qua HTTPS |
| 1.4 | Layout dev/staging/prod + ADR workspace vs directory | IAC-04, CD-09 | Dựng 1 env bằng 1 lệnh |
| 1.5 | Import tài nguyên ClickOps năm 1 | IAC-05, AWS-08 | Drift = 0 |
| 1.6 | Secrets bằng Vault/Secrets Manager | IAC-08 | Không secret nào trong repo/state plaintext |
| 1.7 | Ansible/Packer cho cấu hình máy (nếu dùng EC2) | IAC-06, IAC-07 | Chạy lại playbook không đổi gì |
| 1.8 | Budget alert + bảng chi phí | AWS-09 | Chi phí/tháng mỗi env có số |

## Milestone 2 — CI/CD + Canary/Blue-Green + Automated rollback (Năm 2 · Q2)
| # | Task | Bài học liên quan | Done khi |
| :-- | :--- | :--- | :--- |
| 2.1 | OIDC GitHub Actions → AWS | CICD-04 | Không có access key dài hạn |
| 2.2 | Pipeline: lint → test → scan → image → sign → deploy | CICD-03, CICD-05, CICD-06 | Pipeline < 10 phút |
| 2.3 | Terraform plan trên PR, apply sau approve | CICD-03 | Plan hiển thị trong PR |
| 2.4 | Canary hoặc blue-green | CICD-07, CD-11 | Demo chuyển traffic dần |
| 2.5 | Rollback tự động theo error rate | CICD-09 | Bản lỗi tự rollback < 5 phút |
| 2.6 | DB migration expand-contract | CICD-10 | Đổi tên cột không downtime |

## Definition of Done
- Sơ đồ hạ tầng, `make up ENV=dev` / `make down ENV=dev`, video/gif demo canary + rollback, bảng chi phí.
