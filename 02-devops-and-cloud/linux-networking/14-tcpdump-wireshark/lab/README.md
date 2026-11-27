# LNX-14 · Lab — Bắt gói tin: tcpdump & Wireshark

> ⚠️ **Cần kiểm chứng trên Linux:** Máy soạn là Windows 11 không có WSL/Docker nên các lệnh `tcpdump`, `nsenter`, `kubectl debug` trong `scripts/tcpdump-cheatsheet.sh` và mở file `.pcap` bằng Wireshark GUI **chưa được chạy kiểm chứng trực tiếp** (xem §5). Phần **Java 21 `LoopbackTcpScenarioGenerator` (tái hiện `RST` thật trên loopback) & `MiniPcapAnalyzer` (sinh và phân tích file `.pcap` chuẩn RFC libpcap `0xA1B2C3D4`)** đã chạy kiểm chứng thật 100% offline.

> **Mục tiêu:** tái hiện gói `RST` thật trên TCP loopback (`Connection refused` & `SO_LINGER=0`), tự sinh file `.pcap` chuẩn định dạng Wireshark có tình huống **Server `RST`** và **3 DupACKs + TCP Retransmission**, rồi dùng Java phân tích tự động `Network RTT`, `Server Processing Time` và chẩn đoán nguyên nhân!
> **Thời lượng:** ~35 phút · **Cấp:** L2

## 0. Yêu cầu môi trường
| Công cụ | Phiên bản | Kiểm tra |
| :--- | :--- | :--- |
| JDK | 21+ | `java -version` |
| Maven | 3.9+ | `mvn -v` |
| `tcpdump` / Wireshark *(tùy chọn)* | — | `tcpdump --version` |

## 1. Cấu trúc
```text
lab/
├── README.md
├── pom.xml                                        # Maven project độc lập, Java 21
├── scripts/
│   └── tcpdump-cheatsheet.sh                      # Tổng hợp BPF filters, nsenter & kubectl debug trên Linux/K8s
├── src/
│   ├── main/java/lab/linux/pcap/
│   │   ├── MiniPcapAnalyzer.java                  # Đọc/ghi file .pcap chuẩn libpcap (0xA1B2C3D4) + tính RTT, Retrans, DupACK, RST
│   │   ├── LoopbackTcpScenarioGenerator.java      # Loopback TCP thật: Connection refused (SYN->RST) & SO_LINGER=0 RST
│   │   └── PcapLabRunner.java                     # Runner thực nghiệm
│   └── test/java/lab/linux/pcap/
│       └── PcapLabTest.java                       # JUnit 5 kiểm chứng
└── solutions/
    └── ZeroWindowDetectorSolution.java            # Lời giải E7: giải mã Window Size (offset 14..15) phát hiện TCP ZeroWindow
```

## 2. Chạy

Từ thư mục `lab/`:
```powershell
mvn -o -B verify
mvn -o -B compile exec:java
```

## 3. Các bước thí nghiệm

### Bước 1 — Chạy Loopback `RST` & Sinh/Phân tích file `.pcap` chuẩn Wireshark
- **Dự đoán trước khi chạy** (ghi vào `my-notes.md`):
  1. Khi server đóng kết nối bằng `setSoLinger(true, 0)`, client đang đọc `InputStream` sẽ nhận lỗi gì?
  2. Trong file `retransmission-burst.pcap`, có bao nhiêu gói `DupACK` và bao nhiêu gói `Retransmission`?
- Làm gì: chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`.

### Bước 2 — Trên Linux / Kubernetes (khi có môi trường Linux)
```bash
bash scripts/tcpdump-cheatsheet.sh
```

## 4. Kết quả tham khảo khi chạy

> **Output thực tế trên máy soạn** *(Windows 11 amd64, OpenJDK 26.0.1 `--release 21`, ngày 2026-11-27)*:
> - `mvn -o -B verify`: **2/2 tests pass**, 0 failures, 0 errors.
> - Loopback TCP thật:
>   - Port đóng (`SYN -> RST+ACK`): `Connection refused: getsockopt`
>   - Server `SO_LINGER=0` (`RST`): `SocketException: Connection reset`
> - Phân tích file `.pcap` chuẩn libpcap (`0xA1B2C3D4`):
>   - `http-server-rst.pcap`: `pkts = 5 | rtt = 0.40 ms | syn = 1 | rst = 1` → `Detected RST from 10.20.1.15:8080 after request`.
>   - `retransmission-burst.pcap`: `pkts = 10 | rtt = 0.50 ms | serverTime = 209.00 ms | dupAck = 3 | retrans = 1` → `Detected 1 TCP Retransmission(s) and 3 DupACK(s)`.

## 5. Cần kiểm chứng (chưa chạy trên Windows)
- Chạy `sudo tcpdump -i lo -nn -S -c 20 port 8080` trên Linux và `kubectl debug --image=nicolaka/netshoot` trên Kubernetes Pod.
- Mở 2 file `.pcap` chuẩn libpcap do `MiniPcapAnalyzer` tạo ra trong `Wireshark` GUI để kiểm tra trực quan Flow Graph & `tcp.analysis.*`.

## 6. Giải thích kết quả
- File `.pcap` chứa đầy đủ timestamp microsecond, Ethernet/IPv4/TCP header (`seq`, `ack`, `flags`, `window`) cho phép tách rạch ròi độ trễ mạng (`SYN -> SYN-ACK`), độ trễ xử lý tại server (`Req -> Resp`), mất gói (`DupACK + Retransmission`), và bên phát `RST` (xem [README §3](../README.md)).

## 7. Dọn dẹp
```powershell
mvn -o -B -q clean
```
