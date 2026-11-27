---
id: XXX-NN
title: "<Tên bài>"
domain: <domain>
phase: <Q0 | Y1-Q1 ...>
kind: lesson
status: draft          # todo → draft (agent) → review (người học) → done (người học)
prerequisites: []
related_existing: []
last_updated: YYYY-MM-DD
---

# XXX-NN · <Tên bài>

> **TL;DR** — 3–5 câu. Nếu người đọc chỉ đọc đúng đoạn này, họ cần nhớ điều gì?

<!-- Giữ nguyên khối "🧭 Brief" từ file gốc ở đây, đánh dấu [x] các deliverable đã xong. -->

---

## 1. Vấn đề: tại sao phải biết cái này?
- Một tình huống production **cụ thể, có số** (req/s, latency, kích thước dữ liệu) mà thiếu kiến thức này sẽ gây sự cố.
- Nếu không hiểu bài này, bạn sẽ mắc lỗi gì?

## 2. Mô hình tư duy (mental model)
- Một ẩn dụ hoặc hình ảnh đơn giản, sau đó chỉ ra **chỗ ẩn dụ không còn đúng**.
- Ít nhất 1 sơ đồ Mermaid (flowchart / sequence / state).

## 3. Cơ chế bên trong (trọng tâm của bài)
- Đi từng bước: dữ liệu nằm ở đâu, ai gọi ai, theo thứ tự nào, cái gì bị khóa/chờ/copy.
- Trích dẫn đến tài liệu gốc (spec, source code, JEP, RFC, docs chính thức) cho các khẳng định quan trọng.
- Có thể chia 3.1, 3.2, ...

## 4. Code minh họa
- Java 21 (hoặc công cụ tương ứng của domain). Code ngắn, chạy được, có comment giải thích *tại sao*.
- Ví dụ "sai" và ví dụ "đúng" đặt cạnh nhau khi có thể.

### 4.x Legacy corner (Java 7 / JBoss) — nếu áp dụng
- Cùng vấn đề trên hệ thống Java 7 / JBoss AS 7: khác gì, làm được gì, giới hạn ở đâu.

## 5. Quan sát & đo đạc
- Lệnh, công cụ, metric cụ thể để **nhìn thấy** cơ chế trên chạy thật (jcmd, EXPLAIN, tcpdump, PromQL, ...).
- Output mẫu và cách đọc.

## 6. Trade-offs & khi nào KHÔNG dùng
| Lựa chọn | Được gì | Mất gì | Dùng khi |
| :--- | :--- | :--- | :--- |

## 7. Cạm bẫy production (ít nhất 3)
1. **<Tên bẫy>** — triệu chứng → nguyên nhân → cách tránh.

## 8. Liên hệ thực tế công việc
- Áp dụng vào codebase doanh nghiệp (hệ thống legacy, JDBC thuần, app server) như thế nào? Một việc cụ thể có thể làm ngay tuần này.

## 9. Self-check — Explain-back
> Trả lời **không nhìn tài liệu**, nói to trong 5 phút. Không trả lời được = chưa xong. (Không có đáp án ở đây; đáp án nằm trong solutions.md.)
1. ...
2. ...
3. ...
4. ...
5. ...

## 10. Cheatsheet 1 trang
- Gạch đầu dòng/bảng ngắn để ôn lại trong 2 phút.

## 11. Tài liệu tham khảo
- [Tên nguồn](link) — dùng cho phần nào.

---
➡️ Tiếp theo: [exercises.md](./exercises.md) · [lab/](./lab/README.md)
