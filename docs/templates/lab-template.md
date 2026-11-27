# XXX-NN · Lab — <Tên lab>

> **Mục tiêu:** sau lab này bạn *nhìn thấy tận mắt* <cơ chế> và đo được <con số>.
> **Thời lượng:** ~<n> phút · **Cấp:** L2/L3

## 0. Yêu cầu môi trường
| Công cụ | Phiên bản | Kiểm tra |
| :--- | :--- | :--- |
| JDK | 21 | `java -version` |
| Maven | 3.9+ | `mvn -v` |
| Docker + Compose | 24+ | `docker compose version` |

> Lab chạy được trên **Windows (PowerShell / Git Bash) và Linux/macOS**. Nếu có lệnh khác nhau giữa các OS, ghi cả hai.

## 1. Cấu trúc
```
lab/
├── README.md
├── docker-compose.yml      # nếu cần hạ tầng (PostgreSQL, Redis, Kafka, ...)
├── pom.xml                 # Maven project độc lập, Java 21
├── src/main/java/...       # code demo — KHÔNG phụ thuộc lab khác
├── src/test/java/...       # test chứng minh hành vi
├── scripts/                # k6, sql, shell
└── solutions/              # lời giải cho bài tập L2/L3 cần code
```

## 2. Chạy
```bash
docker compose up -d        # nếu có
mvn -q compile exec:java -Dexec.mainClass=...
```

## 3. Các bước thí nghiệm
### Bước 1 — <tên>
- Làm gì: ...
- **Dự đoán trước khi chạy** (ghi vào my-notes.md): ...
- Quan sát: lệnh / output cần nhìn.

### Bước 2 — Thay đổi 1 biến
- ...

## 4. Kết quả của bạn
| Cấu hình | Throughput | p50 | p99 | Ghi chú |
| :--- | ---: | ---: | ---: | :--- |
| | | | | |

> Số tham khảo của người soạn (máy: <CPU/RAM/OS>): ... — số của bạn có thể khác, quan trọng là **xu hướng**.

## 5. Giải thích kết quả
- Vì sao số đo như vậy? Liên kết về README §3.

## 6. Biến thể để tự khám phá
- ...

## 7. Dọn dẹp
```bash
docker compose down -v
```
