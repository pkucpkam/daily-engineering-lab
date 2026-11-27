---
id: INC-NN
title: "<Tên sự cố>"
kind: incident
status: draft
related_lessons: []
---

# INC-NN · <Tên sự cố>

> Incident lab = **sự cố được tái hiện có chủ đích** để luyện điều tra. Phần đề (README.md) KHÔNG được lộ root cause. Brief của người soạn nằm ở `BRIEF.md` (giữ nguyên, không xóa).

## README.md — Đề bài (cho người học)
### 📟 Ticket / Alert
> Viết như một ticket thật: thời điểm, ai báo, triệu chứng, mức ảnh hưởng.

### 📊 Tín hiệu có sẵn
- Log trích (trong `evidence/`), metric/ảnh dashboard (mô tả hoặc file), thread dump/heap dump nếu cần.

### 🧪 Tái hiện
```bash
cd lab && docker compose up -d && ./scripts/trigger.sh
```

### 🎯 Nhiệm vụ
1. Viết ≥ 3 giả thuyết và cách kiểm chứng.
2. Tìm root cause với bằng chứng.
3. Đề xuất hotfix + proper fix + prevention.
4. Viết bug-case theo [bug-template](../../bug-cases/templates/bug-template.md) vào `../../bug-cases/content/`.

### ⏱️ Timebox: <n> phút. Hết giờ mới mở hint.

## hints.md — Gợi ý theo tầng
- Hint 1 (nhìn vào đâu) → Hint 2 (công cụ nào) → Hint 3 (gần đáp án).

## solution.md — Root cause & phân tích (ẩn trong `<details>`)
- Causal chain, bằng chứng, fix, cách phòng ngừa (test, alert, guardrail), bài học hệ thống.
- Liên kết về bài học lý thuyết liên quan.

## lab/
- Code + docker-compose tái hiện được sự cố **một cách ổn định**, có script `trigger` và (nếu cần) `load` (k6).
- `lab/fixed/` hoặc branch/flag bật bản đã sửa để so sánh trước/sau.
