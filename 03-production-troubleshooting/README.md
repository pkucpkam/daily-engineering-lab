# 03 — Production Troubleshooting

Khả năng gỡ lỗi và ứng phó sự cố trong môi trường thực tế là kỹ năng phân biệt rõ rệt nhất giữa một kỹ sư thông thường và một Senior/Staff Engineer.

---

## 📂 Danh mục nội dung

0. [incident-labs/](./incident-labs/README.md)
   * **Sự cố tái hiện có chủ đích** (18 lab): đề bài như ticket thật, lab chạy được, hint & đáp án ẩn.
   * Làm xong mỗi lab → tự viết bug-case vào `bug-cases/content/`.

1. [bug-cases/](./bug-cases/)
   * Phân tích các bug phức tạp theo phương pháp khoa học:
     * **Symptom**: Triệu chứng quan sát được là gì?
     * **Hypothesis**: Giả thuyết nguyên nhân là gì?
     * **Investigation**: Dữ liệu và log chứng minh giả thuyết.
     * **Root Cause**: Nguyên nhân gốc rễ tầng sâu.
     * **Fix & Prevention**: Cách khắc phục và giải pháp phòng tránh triệt để.

2. [post-mortems/](./post-mortems/)
   * Các báo cáo phân tích sự cố (Blameless Post-Mortem):
     * Timeline chi tiết sự kiện
     * Impact (doanh thu, người dùng, SLA)
     * Điểm thất bại kích hoạt (Triggering condition)
     * Action items để gia cố độ tin cậy hệ thống
