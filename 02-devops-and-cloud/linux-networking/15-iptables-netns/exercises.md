# LNX-15 · Bài tập — iptables/nftables & network namespace

> Làm theo thứ tự. **Tự làm trước, chỉ mở hint khi đã thử ≥ 15 phút.** Đáp án ở [solutions.md](./solutions.md) — chỉ mở sau khi đã có câu trả lời của riêng bạn.
> Ghi thời gian làm từng bài vào `my-notes.md` để theo dõi tiến bộ.

| Cấp | Mục đích | Số lượng | Thời gian gợi ý |
| :--- | :--- | :---: | :---: |
| 🟢 L1 — Recall | Nắm chắc 5 chains, 4 tables, Docker `-p` vs `INPUT`, `netns`/`veth`, `kube-proxy` | 5 | 20' |
| 🟡 L2 — Apply | Mô phỏng Netfilter Hooks, DNAT Docker, `kube-proxy` `1/N`, và script `ip netns` | 3 | 75' |
| 🔴 L3 — Deep | Ticket Production: Chặn UFW nhưng Postgres Docker vẫn hở + K8s Service mất mạng | 1 | 90' |

---

## 🟢 L1 — Recall

### E1. Vì sao chặn port ở chain `INPUT` không chặn được `docker run -p 8080:80`?
Bạn chạy container `nginx` với `-p 8080:80`, sau đó chạy `iptables -A INPUT -p tcp --dport 8080 -j DROP`. Tại sao người dùng từ ngoài Internet vẫn truy cập được vào `http://<Host-IP>:8080` bình thường? Phải chèn rule chặn ở chain nào mới đúng?

---

### E2. Thứ tự chạy 4 tables & vai trò `nf_conntrack` trong NAT
1. Nếu một gói tin vừa đi vào `PREROUTING` và trên bảng `raw`, `mangle`, `nat` đều có rule, thứ tự thực thi qua các bảng diễn ra như thế nào?
2. Tại sao trong bảng `nat`, nếu kết nối TCP truyền `10.000` gói tin thì rule `DNAT` / `MASQUERADE` chỉ đếm gói tin **đầu tiên** (`pkts = 1`) còn `9.999` gói sau tự động được NAT?

---

### E3. Phân biệt `DNAT`, `SNAT` và `MASQUERADE` tại 2 đầu vòng đời gói tin
1. `DNAT` chạy ở chain nào và thay đổi trường nào trong IP header?
2. `SNAT` / `MASQUERADE` chạy ở chain nào, và sự khác biệt thực tế giữa `SNAT` vs `MASQUERADE` là gì khi dùng cho card mạng có IP tĩnh vs IP động?

---

### E4. Vai trò của `veth pair` + Linux Bridge khi tạo 2 "container" bằng `ip netns`
Nếu bạn tạo 2 namespace `ns1` và `ns2` bằng `ip netns add`, chúng giao tiếp với nhau bằng công cụ gì nếu:
1. Chỉ có 2 namespace muốn nối trực tiếp 1-1 với nhau.
2. Có `10` namespace (như 10 Docker container trên 1 host) cần nói chuyện với nhau và ra Internet qua `eth0`.

---

### E5. `kube-proxy` mode `iptables` vs `IPVS`: Tại sao phải dùng xác suất `1/N`?
Trong chain `KUBE-SVC-*` của một Service có 4 Pod backends, tại sao 4 rule chuyển tiếp sang `KUBE-SEP-*` lại mang các xác suất giảm dần hoặc tăng dần như **`0.25`**, **`0.33333`**, **`0.50`**, **`1.0`** thay vì tất cả đều ghi `0.25`?

---

## 🟡 L2 — Apply

### E6. Mô phỏng đường đi Packet qua Netfilter Hooks: Host `INPUT` vs Docker `PREROUTING -> DNAT -> FORWARD`
**Bối cảnh:** Dùng [lab/](./lab/README.md) chạy `NetfilterLabTest` và `NetfilterLabRunner`.
**Yêu cầu:**
1. Chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`.
2. Quan sát hành trình qua các Netfilter hooks (`trace`) trong `NetfilterPacketSimulator`:
   - Gói tin tới port `22` của Host → đi qua `PREROUTING -> INPUT -> [HOST]`.
   - Gói tin tới port `8080` của Docker container (`-p 8080:80` -> `172.17.0.2:80`) ngay cả khi `INPUT` có rule **`DROP dport 8080`** → kiểm chứng gói tin **vẫn lọt tới `ns-docker` (`172.17.0.2:80`)** qua `PREROUTING (DNAT) -> FORWARD -> POSTROUTING`!
   - Thêm rule `DROP` vào **`DOCKER-USER` (nằm ở `FORWARD`)** → chứng minh gói tin bị chặn ngay lập tức!

**Tiêu chí hoàn thành:**
- [ ] `mvn -o -B verify` pass 100%.
- [ ] Nắm rõ trace path và chứng minh rule `INPUT DROP` bị DNAT bypass còn `DOCKER-USER DROP` chặn thành công.

<details><summary>💡 Hint</summary>

Netfilter quyết định định tuyến (`INPUT` hay `FORWARD`) **SAU** hook `PREROUTING`! Vì `PREROUTING` đã đổi địa chỉ đích từ `<Host-IP>:8080` thành `172.17.0.2:80`, kernel tra route thấy `172.17.0.2` thuộc `docker0` bridge chứ không phải địa chỉ local của Host → đẩy thẳng sang `FORWARD`.
</details>

---

### E7. Mô phỏng `kube-proxy` `iptables` Round-Robin xác suất (`1/N`) & Kết nối giữa 2 Network Namespace
**Bối cảnh:** Tiếp tục chạy kịch bản `KubeProxyService` và `setup-2-netns` trong `NetfilterPacketSimulator` (và đọc `lab/solutions/MultiBackendKubeProxySolution.java`).
**Yêu cầu:**
1. Gửi `3.000` gói `SYN` mới vào Service ClusterIP `10.96.10.20:80` với 3 Pods: kiểm chứng phân phối xác suất `0.3333` → `0.50` → `1.0` chia tải gần đúng `~1.000` mỗi Pod, và mọi gói tiếp theo của cùng kết nối (`ctstate ESTABLISHED`) đều đi theo bảng **`conntrack`** về đúng Pod ban đầu mà không bốc thăm lại!
2. Kiểm chứng giao tiếp giữa `ns1 (10.200.0.2)` → `br-lab0` → `ns2 (10.200.0.3)` và gói đi Internet từ `ns1` được **`MASQUERADE`** sang IP public của `eth0` tại `POSTROUTING`.

**Tiêu chí hoàn thành:**
- [ ] Pass test phân phối `kube-proxy` và giữ affinity bằng `conntrack`.
- [ ] Hiểu rõ từng dòng lệnh trong `lab/scripts/setup-2-netns-lab.sh`.

<details><summary>💡 Hint</summary>

Nếu gói thứ 2 của cùng kết nối TCP bị bốc thăm ngẫu nhiên sang Pod #2 thay vì Pod #1 đã nhận `SYN`, kết nối TCP sẽ gãy ngay bằng `RST`! Nhờ `conntrack` lưu lại 4-tuple ở gói `SYN` đầu tiên, toàn bộ gói `ACK`/`PSH`/`FIN` tiếp theo giữ nguyên đích Pod #1.
</details>

---

### E8. Đọc & kiểm chứng Script dựng 2 Container bằng `ip netns` thủ công (Linux Lab)
**Bối cảnh:** Mở `lab/scripts/setup-2-netns-lab.sh`.
**Yêu cầu:**
1. Giải thích tác dụng từng bước trong script (`ip netns add`, `ip link add ... type veth peer name ...`, `ip link set ... netns`, `ip link set ... master br-lab0`, `ip route add default via ...`, `sysctl net.ipv4.ip_forward=1`, `iptables -t nat -A POSTROUTING ... -j MASQUERADE`).
2. Nếu quên chạy bước `ip netns exec ns1 ip link set lo up`, ứng dụng Java chạy bên trong `ns1` gặp lỗi gì khi tự gọi `127.0.0.1`?

**Tiêu chí hoàn thành:**
- [ ] Giải thích chính xác tất cả các lệnh mạng trên.
- [ ] (Khi có máy Linux) chạy thử `sudo bash scripts/setup-2-netns-lab.sh up` và `down` để dọn sạch.

<details><summary>💡 Hint</summary>

Mỗi Network Namespace mới tạo đều có sẵn interface `lo` nhưng ở trạng thái **`DOWN`**! Quên kéo `lo up` làm ứng dụng bên trong không gọi nổi `localhost`/`127.0.0.1` (`Network is unreachable`).
</details>

---

## 🔴 L3 — Deep

### E9. Ticket Production: Hở cổng PostgreSQL Docker ra Internet + Mất mạng container sau khi reboot VM
**Tình huống:**
> Một VM production chạy Docker Engine để chạy nhanh 1 DB PostgreSQL phụ (`docker run -d --restart=always -p 5432:5432 postgres:16`) cùng một app phụ khác trong custom network.
> Lúc audit bảo mật & sau đợt reboot bảo trì VM:
> 1. Đội Security báo từ máy bên ngoài văn phòng vẫn quét thấy cổng **`5432`** mở toang trên IP public của VM mặc dù admin đã cấu hình **`ufw default deny incoming`** và không hề chạy `ufw allow 5432`!
> 2. Sau khi reboot VM lúc `03:00` sáng, các container trong namespace `172.18.0.0/16` gọi nhau qua bridge được nhưng **không thể `curl` ra API bên ngoài Internet** (`Connection timed out`), đồng thời thấy trong `/etc/sysctl.conf` có dòng cấu hình mới thêm: `net.ipv4.ip_forward = 0`.
> 3. Cuối cùng, một cụm K8s thử nghiệm có `4.500` Services báo lỗi `nf_conntrack: table full, dropping packet` vào giờ cao điểm.

**Yêu cầu:**
1. Giải thích **cơ chế bên trong** của từng vấn đề (1), (2), và (3).
2. Đưa ra **lệnh sửa chuẩn xác** cho cả 3 vấn đề (đặc biệt cách khóa cổng `5432` đúng chuẩn mà vẫn cho app trên cùng VM kết nối được vào Postgres!).

**Tiêu chí hoàn thành:**
- [ ] Chỉ ra đúng Docker `PREROUTING DNAT -> FORWARD` bypass `ufw INPUT`.
- [ ] Giải thích đúng tác dụng của `net.ipv4.ip_forward = 1` cho lưu lượng từ `docker0` bridge ra `eth0`.
- [ ] Đưa ra cách fix an toàn (bind `-p 127.0.0.1:5432:5432` hoặc rule `DOCKER-USER`, bật `ip_forward = 1`, nâng `nf_conntrack_max` / `NOTRACK` hoặc chuyển IPVS/Cilium).

<details><summary>💡 Hint</summary>

1. Không nên chỉ chặn `5432` bằng `ufw` — cách sạch nhất cho Docker là chỉ publish **`-p 127.0.0.1:5432:5432`** (hoặc chặn ở **`DOCKER-USER`**: `iptables -I DOCKER-USER -i eth0 -p tcp --dport 5432 -j DROP`)!
2. `net.ipv4.ip_forward = 0` tắt tính năng router của Linux kernel: gói tin từ container bridge `172.18.0.x` đi vào host nhưng bị kernel từ chối chuyển tiếp (`FORWARD`) sang `eth0` để ra Internet!
</details>
