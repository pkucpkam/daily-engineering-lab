# Linux & Networking

Khu vực lưu trữ kiến thức và bài thực hành chuyên sâu về hệ điều hành Linux và mạng máy tính dưới góc nhìn của Backend/DevOps Engineer.

---

## 🎯 Nội dung trọng tâm

1. **Linux Internals**:
   * Quản lý tiến trình (Processes, Threads, Zombie processes, Signal handling).
   * Memory management (Virtual Memory, Page Cache, Swap, OOM Killer).
   * File descriptors, I/O multiplexing (`select`, `poll`, `epoll`).
   * Performance troubleshooting tools: `top`, `htop`, `vmstat`, `iostat`, `strace`, `perf`.

2. **Networking**:
   * Mô hình OSI và TCP/IP Stack trong Linux kernel.
   * Quá trình bắt tay 3 bước TCP (Three-way handshake) và ngắt kết nối (4-way handshake).
   * TIME_WAIT, CLOSE_WAIT sockets và cách tránh socket exhaustion.
   * DNS resolution process và troubleshooting (`dig`, `nslookup`, `tcpdump`, `curl -v`).
   * Linux firewall: `iptables`, `nftables`, network namespaces.

> **Tiến độ chủ đề:** `14 / 15` bài đã soạn (`✍️ draft`) · `0` hoàn thành (`✅ done`) · Cập nhật: `2026-11-27`.
> **Tiến độ chủ đề:** `15 / 15` bài đã soạn (`✍️ draft`) · `0` hoàn thành (`✅ done`) · Cập nhật: `2026-11-27`.

---

<!-- CURRICULUM:START — sinh tự động từ CURRICULUM, cột Status cập nhật tay -->
## 📚 Danh sách bài

Loại: **Bài học** · Template: [lesson-template.md](../../docs/templates/lesson-template.md) · Quy chuẩn: [CONTENT-GUIDE](../../docs/CONTENT-GUIDE.md)

| ID | Bài | Giai đoạn | Status |
| :--- | :--- | :--- | :---: |
| LNX-01 | [Process, thread & signal](01-processes-signals/README.md) | Năm 1 · Q1 | ✍️ |
| LNX-02 | [File descriptor & I/O](02-file-descriptors-io/README.md) | Năm 1 · Q1 | ✍️ |
| LNX-03 | [Virtual memory, page cache, swap & OOM killer](03-memory-pagecache-oom/README.md) | Năm 1 · Q1 | ✍️ |
| LNX-04 | [systemd & quản lý service](04-systemd-services/README.md) | Năm 1 · Q1 | ✍️ |
| LNX-05 | [Công cụ hiệu năng Linux](05-linux-perf-tools/README.md) | Năm 1 · Q1 | ✍️ |
| LNX-06 | [Shell scripting tự động hóa](06-shell-scripting/README.md) | Năm 1 · Q1 | ✍️ |
| LNX-07 | [select, poll, epoll & event loop](07-epoll-io-multiplexing/README.md) | Năm 1 · Q1 | ✍️ |
| LNX-08 | [TCP: handshake, state machine, backlog](08-tcp-handshake-states/README.md) | Năm 1 · Q1 | ✍️ |
| LNX-09 | [TIME_WAIT, CLOSE_WAIT & socket exhaustion](09-time-wait-close-wait/README.md) | Năm 1 · Q1 | ✍️ |
| LNX-10 | [HTTP/1.1 vs HTTP/2 vs HTTP/3](10-http-versions/README.md) | Năm 1 · Q1 | ✍️ |
| LNX-11 | [DNS resolution & troubleshooting](11-dns-resolution/README.md) | Năm 1 · Q1 | ✍️ |
| LNX-12 | [TLS handshake & certificate chain](12-tls-handshake/README.md) | Năm 1 · Q1 | ✍️ |
| LNX-13 | [Load balancer L4 vs L7 & Nginx reverse proxy](13-load-balancer-nginx/README.md) | Năm 1 · Q1 | ✍️ |
| LNX-14 | [Bắt gói tin: tcpdump & Wireshark](14-tcpdump-wireshark/README.md) | Năm 1 · Q1 | ✍️ |
| LNX-15 | [iptables/nftables & network namespace](15-iptables-netns/README.md) | Năm 1 · Q1 | 🔲 |
| LNX-15 | [iptables/nftables & network namespace](15-iptables-netns/README.md) | Năm 1 · Q1 | ✍️ |

**Tài liệu gốc của chủ đề:** The Linux Command Line · Operating Systems: Three Easy Pieces · Systems Performance (Gregg) · High Performance Browser Networking (hpbn.co) · TCP/IP Illustrated

<!-- CURRICULUM:END -->
