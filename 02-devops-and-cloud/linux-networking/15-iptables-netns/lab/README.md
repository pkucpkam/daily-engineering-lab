# LNX-15 · Lab — iptables/nftables & network namespace

> ⚠️ **Cần kiểm chứng trên Linux:** Máy soạn là Windows 11 không có WSL/Docker nên các lệnh Linux kernel (`ip netns`, `veth`, `bridge`, `iptables`, `kube-proxy` rules) trong `scripts/setup-2-netns-lab.sh` **chưa được chạy kiểm chứng trực tiếp** (xem §5). Phần **Java 21 `NetfilterPacketSimulator`** (mô phỏng đầy đủ 5 hooks, 4 tables, `conntrack`, `DOCKER -p` bypass `INPUT`, `kube-proxy` `1/N`, và 2 network namespaces nối qua bridge) đã chạy kiểm chứng thật 100% offline.

> **Mục tiêu:** nắm rõ đường đi từng gói tin qua **Netfilter 5 chains + `conntrack`**, tự chứng kiến vì sao `INPUT DROP` không chặn được `docker -p 8080:80` mà phải dùng `DOCKER-USER`, kiểm chứng chia tải xác suất `1/N` của `kube-proxy`, và có sẵn script Linux dựng 2 "container" bằng `ip netns` + `veth` + `MASQUERADE`.
> **Thời lượng:** ~35 phút · **Cấp:** L2

## 0. Yêu cầu môi trường
| Công cụ | Phiên bản | Kiểm tra |
| :--- | :--- | :--- |
| JDK | 21+ | `java -version` |
| Maven | 3.9+ | `mvn -v` |
| Linux (`iproute2`, `iptables`) *(tùy chọn)* | Kernel 4.x+ | `ip -V && iptables --version` |

## 1. Cấu trúc
```text
lab/
├── README.md
├── pom.xml                                        # Maven project độc lập, Java 21
├── scripts/
│   └── setup-2-netns-lab.sh                       # Dựng & dọn 2 netns + veth + bridge + MASQUERADE trên Linux
├── src/
│   ├── main/java/lab/linux/netfilter/
│   │   ├── NetfilterPacketSimulator.java          # Mô phỏng 5 hooks, DNAT, SNAT/MASQUERADE, conntrack, veth bridge
│   │   └── NetfilterLabRunner.java                # Runner thực nghiệm
│   └── test/java/lab/linux/netfilter/
│       └── NetfilterLabTest.java                  # JUnit 5 kiểm chứng
└── solutions/
    └── MultiBackendKubeProxySolution.java         # Mẫu phân tích công thức xác suất 1/(N-i) của kube-proxy
```

## 2. Chạy

Từ thư mục `lab/`:
```powershell
mvn -o -B verify
mvn -o -B compile exec:java
```

## 3. Các bước thí nghiệm

### Bước 1 — Chạy Netfilter Hook, Docker Bypass & `kube-proxy` Simulator
- **Dự đoán trước khi chạy** (ghi vào `my-notes.md`):
  1. Nếu bật Docker DNAT `-p 8080:80` và đặt rule `DROP dport 8080` trong chain `INPUT`, gói tin tới port `8080` có bị chặn không?
  2. Với 3 backend Pod trong `kube-proxy` mode `iptables`, xác suất cấu hình ở 3 rule liên tiếp phải bằng bao nhiêu để tải chia đều `~33.3%` mỗi Pod?
- Làm gì: chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`.

### Bước 2 — Trên Linux thật (khi có môi trường Linux/VM)
```bash
sudo bash scripts/setup-2-netns-lab.sh up
# Kiểm tra xong dọn sạch:
sudo bash scripts/setup-2-netns-lab.sh down
```

## 4. Kết quả tham khảo khi chạy

> **Output thực tế trên máy soạn** *(Windows 11 amd64, OpenJDK 26.0.1 `--release 21`, ngày 2026-11-27)*:
> - `mvn -o -B verify`: **3/3 tests pass**, 0 failures, 0 errors.
> - `NetfilterLabRunner`:
>   - Docker `-p 8080:80` khi đã chặn ở `INPUT`: `Delivered=true | Target=ns-docker-nginx (172.17.0.2:80)` (`PREROUTING -> DOCKER-DNAT -> FORWARD -> POSTROUTING`).
>   - Chặn tại `DOCKER-USER`: `Delivered=false | Action=DROP` (`DOCKER-USER_DROP(dport=80)`).
>   - `kube-proxy` `iptables` (`1/3 -> 1/2 -> 1.0`, `3.000` SYN): `{10.244.1.5:8080=974, 10.244.2.8:8080=996, 10.244.3.9:8080=1030}`.
>   - `ns1 (10.200.0.2) -> Internet`: `Delivered=true | SrcIP sau NAT=203.0.113.10` (`POSTROUTING MASQUERADE`).
> - `MultiBackendKubeProxySolution`: `N=4 endpoints -> [0.25, 0.3333333333333333, 0.5, 1.0]`.

## 5. Cần kiểm chứng (chưa chạy trên Windows)
- `sudo bash scripts/setup-2-netns-lab.sh up` trên Linux VM để tạo thật `ns1`, `ns2`, `br-lab0`, `veth`, thử `ping` giữa 2 namespace và `MASQUERADE` ra ngoài.
- Kiểm tra rule thật bằng `sudo iptables -t nat -L -n -v` và `sudo nft list ruleset`.

## 6. Giải thích kết quả
- Sau khi `PREROUTING DNAT` đổi IP đích sang container IP, gói tin rẽ vào `FORWARD` chứ không vào `INPUT`; còn `conntrack` giữ trạng thái ánh xạ 2 chiều để các gói sau đi đúng đường (xem [README §3](../README.md)).

## 7. Dọn dẹp
```powershell
mvn -o -B -q clean
```
