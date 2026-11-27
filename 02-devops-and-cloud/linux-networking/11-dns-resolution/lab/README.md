# LNX-11 · Lab — DNS resolution & troubleshooting

> ⚠️ **Cần kiểm chứng trên Linux:** Máy soạn là Windows 11 không có WSL/Docker nên các lệnh DNS Linux (`dig +trace`, `/etc/resolv.conf`, `resolvectl`, `tcpdump port 53`) trong `scripts/dns-troubleshoot.sh` **chưa được chạy kiểm chứng trực tiếp** (xem §5). Phần **Java 21 `MiniUdpDnsServer` (DNS UDP server RFC 1035 thật trên `127.0.0.1`), `JvmDnsCacheDemo` & `NdotsResolverSimulator`** đã chạy kiểm chứng thật 100% offline.

> **Mục tiêu:** dựng một DNS Server UDP thật nói chuẩn wire-format RFC 1035 ngay trên loopback để: (1) chứng kiến tác hại khi JVM cache DNS `-1` (Forever) lúc DB đổi IP vs `TTL = 1s`, và (2) đếm tận mắt `10` UDP packets của `ndots:5` vs `2` packets của `ndots:2` / trailing dot.
> **Thời lượng:** ~35 phút · **Cấp:** L2

## 0. Yêu cầu môi trường
| Công cụ | Phiên bản | Kiểm tra |
| :--- | :--- | :--- |
| JDK | 21+ | `java -version` |
| Maven | 3.9+ | `mvn -v` |
| Linux (`dnsutils`/`bind-utils`, `tcpdump`) *(tùy chọn)* | — | `dig -v` |

## 1. Cấu trúc
```text
lab/
├── README.md
├── pom.xml                                        # Maven project độc lập, Java 21
├── scripts/
│   └── dns-troubleshoot.sh                        # dig +trace, /etc/resolv.conf & tcpdump port 53 trên Linux
├── src/
│   ├── main/java/lab/linux/dns/
│   │   ├── MiniUdpDnsServer.java                  # DNS Server UDP RFC 1035 thật trên loopback 127.0.0.1
│   │   ├── JvmDnsCacheDemo.java                   # Thí nghiệm DNS Cache TTL=-1 vs TTL=1s khi đổi IP
│   │   ├── NdotsResolverSimulator.java            # Bắn UDP queries thật mô phỏng K8s ndots:5 vs ndots:2
│   │   └── DnsLabRunner.java                      # Runner thực nghiệm
│   └── test/java/lab/linux/dns/
│       └── DnsLabTest.java                        # JUnit 5 kiểm chứng
└── solutions/
    └── PodDnsConfigSolution.yaml                  # Mẫu cấu hình K8s Pod giảm ndots:5 -> ndots:2
```

## 2. Chạy

Từ thư mục `lab/`:
```powershell
mvn -o -B verify
mvn -o -B compile exec:java
```

## 3. Các bước thí nghiệm

### Bước 1 — Chạy UDP DNS Server RFC 1035 & kiểm chứng Cache / `ndots:5`
- **Dự đoán trước khi chạy** (ghi vào `my-notes.md`):
  1. Khi `MiniUdpDnsServer` đổi IP của `db.prod.internal` từ `10.10.1.10` sang `10.10.2.20`, client `ttl = -1` sẽ trả về IP nào và gửi thêm bao nhiêu gói UDP?
  2. Với 4 `search` domains Kubernetes (`A` + `AAAA`), gọi `api.partner.com` dưới `ndots:5` tốn bao nhiêu UDP queries so với `ndots:2`?
- Làm gì: chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`.

### Bước 2 — Trên Linux (khi có môi trường Linux)
```bash
bash scripts/dns-troubleshoot.sh api.github.com
```

## 4. Kết quả tham khảo khi chạy

> **Output thực tế trên máy soạn** *(Windows 11 amd64, OpenJDK 26.0.1 `--release 21`, ngày 2026-11-27)*:
> - `mvn -o -B verify`: **2/2 tests pass**, 0 failures, 0 errors.
> - `DnsLabRunner` (qua `MiniUdpDnsServer` UDP RFC 1035 thật trên `127.0.0.1`):
>   - JVM đang chạy: `networkaddress.cache.ttl=null` (JDK fallback `30s` khi không bật `SecurityManager`), `networkaddress.cache.negative.ttl=10`.
>   - Failover `db.prod.internal` (`10.10.1.10 -> 10.10.2.20`): `TTL=-1 (Forever) +120s` vẫn trả **`10.10.1.10` (`STALE!`, `totalUdpQueries = 1`)**; còn `TTL=1s +2s` cập nhật **`10.10.2.20` (`totalUdpQueries = 2`)**.
>   - `NdotsResolverSimulator` (`api.partner.com` với `4` K8s search domains, `A + AAAA`): `ndots:5` phát sinh **`10` UDP queries (`8 NXDOMAIN`)**, còn `ndots:2` và `api.partner.com.` chỉ phát sinh **`2` UDP queries (`0 NXDOMAIN`)**.

## 5. Cần kiểm chứng (chưa chạy trên Windows)
- `dig +trace api.github.com` và `dig +noall +answer +ttlid api.github.com` trên Linux.
- `sudo tcpdump -i any -nn port 53` trên một Pod K8s hoặc container Linux để bắt trực tiếp chuỗi truy vấn `search` domain do `ndots:5`.

## 6. Giải thích kết quả
- JVM DNS cache phụ thuộc vào `networkaddress.cache.ttl` (đọc duy nhất 1 lần lúc khởi tạo class `InetAddressCachePolicy`), còn `ndots:5` khuếch đại 5× lưu lượng DNS UDP khi gặp domain `< 5` dấu chấm (xem [README §3](../README.md)).

## 7. Dọn dẹp
```powershell
mvn -o -B -q clean
```
