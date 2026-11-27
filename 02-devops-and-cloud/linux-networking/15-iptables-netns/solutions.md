# LNX-15 · Đáp án — iptables/nftables & network namespace

> ⚠️ **Chỉ mở sau khi đã tự làm.** Đọc đáp án mà chưa tự làm = mất 80% giá trị bài tập.
> Mỗi đáp án được bọc trong `<details>` để tránh lộ khi lướt.

---

### E1
<details><summary>Xem đáp án</summary>

**Đáp án:**
- Khi có traffic từ Internet vào `<Host-IP>:8080`, gói tin chạm vào **`PREROUTING` (table `nat`)** *trước* khi kernel quyết định định tuyến!
- Rule `DNAT` do Docker tạo ra đổi địa chỉ đích từ `<Host-IP>:8080` sang **`<Container-IP>:80`** (ví dụ `172.17.0.2:80`).
- Vì địa chỉ đích mới không phải địa chỉ của Host, kernel đưa gói tin đi qua chain **`FORWARD`** chứ **không hề đi qua chain `INPUT`** — nên rule `INPUT -p tcp --dport 8080 -j DROP` hoàn toàn bị bỏ qua!
- **Cách chặn đúng:** Đặt rule ở đầu chain **`DOCKER-USER`** (nằm trong `FORWARD`): `iptables -I DOCKER-USER -i eth0 -p tcp --dport 8080 -j DROP` (hoặc ngay từ đầu chỉ publish nội bộ `-p 127.0.0.1:8080:80`).
</details>

---

### E2
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. Thứ tự thực thi qua 4 bảng tại cùng 1 hook là: **`raw` → `mangle` → `nat` → `filter`**.
2. Bảng `nat` được thiết kế chỉ chạy cho gói tin đầu tiên có trạng thái **`NEW`** (thường là gói `SYN`). Ngay sau khi `DNAT`/`MASQUERADE` thành công, **`nf_conntrack`** ghi cặp ánh xạ 2 chiều `(original tuple <-> reply tuple)` vào bảng trạng thái trong RAM. Toàn bộ `9.999` gói tiếp theo thuộc trạng thái `ESTABLISHED` tự động được Netfilter dịch ngược/xuôi trực tiếp theo `conntrack` mà không cần duyệt lại rule `nat`!
</details>

---

### E3
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. **`DNAT` (Destination NAT):** Chạy ở **`PREROUTING`** (cho gói từ ngoài vào) hoặc **`OUTPUT`** (cho gói sinh ra từ chính host); nó sửa **Destination IP (và/hoặc Destination Port)** để chuyển hướng gói tin tới đích thật (như Container IP hoặc Pod IP).
2. **`SNAT` / `MASQUERADE` (Source NAT):** Chạy ở **`POSTROUTING`** ngay trước khi gói tin rời khỏi NIC; nó sửa **Source IP (và/hoặc Source Port)** từ IP private container (`172.17.0.2`) thành IP của Host.
   - **`SNAT` (`--to-source 203.0.113.10`):** Dùng khi card mạng ra ngoài có **IP tĩnh** (nhanh hơn một chút vì không phải lookup IP của interface).
   - **`MASQUERADE`:** Tự động đọc IP hiện tại của interface đầu ra — tiện lợi cho **IP động (DHCP/Cloud)** hoặc bridge đa dạng, đánh đổi nhẹ CPU lookup khi tạo connection mới.
</details>

---

### E4
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. **Nối trực tiếp 2 namespace 1-1:** Chỉ cần tạo **1 cặp `veth` pair**, nhét đầu A vào `ns1` (`10.0.0.1/24`) và đầu B vào `ns2` (`10.0.0.2/24`) là ping được nhau ngay không cần bridge!
2. **Nối 10 namespace với nhau và ra Internet:** Tạo một **Linux Bridge** (`br0`) trong root namespace; mỗi `nsK` tạo 1 cặp `veth`, 1 đầu cắm vào `nsK`, đầu còn lại cắm vào bridge `br0`; đặt IP gateway trên `br0`, bật `net.ipv4.ip_forward = 1`, và thêm rule **`MASQUERADE`** ở `POSTROUTING` cho traffic rời khỏi interface ra ngoài (`eth0`).
</details>

---

### E5
<details><summary>Xem đáp án</summary>

**Đáp án:**
Vì các rule `iptables` được kernel đánh giá **tuần tự từ trên xuống dưới**; nếu packet đi tới rule thứ `k`, điều đó có nghĩa nó đã **trượt** toàn bộ `k - 1` rule ở phía trên!
Xét ví dụ 4 Pods (mỗi Pod cần xác suất thực sự `1/4 = 25%`):
- Rule 1 chọn Pod 1 với xác suất `0.25` (**`1/4`**).
- Nếu *không* rơi vào Pod 1 (`75%` còn lại), ta muốn chia đều `75%` đó cho **3 Pod còn lại** → Rule 2 phải có xác suất điều kiện là `25% / 75%` = **`1/3 = 0.33333`**!
- Còn lại 2 Pod (`50%`) → Rule 3 có xác suất `25% / 50%` = **`1/2 = 0.50`**!
- Còn lại 1 Pod duy nhất → Rule 4 luôn khớp (`1.0`).
Nếu ghi cả 4 dòng đều là `0.25`, Pod 1 nhận `25%`, Pod 2 chỉ nhận `0.75 × 0.25 = 18.75%`, và Pod 4 nhận phần còn dư `42.2%`!
</details>

---

### E6
<details><summary>Xem đáp án</summary>

**Hướng giải:**
Khi chạy `mvn -o -B verify` và `mvn -o -B compile exec:java`:
- Gói tới `Host:22`: đi qua `PREROUTING -> INPUT -> HOST_PROCESS`.
- Gói tới `Host:8080` khi đã cài rule Docker `-p 8080:80` và cài nhầm `DROP dport 8080` trong `INPUT`: ở `PREROUTING`, `DNAT` đổi đích thành `172.17.0.2:80` → kernel rẽ sang **`FORWARD`**, nên gói tin vẫn tới đích `CONTAINER_NS(172.17.0.2:80)`!
- Khi đặt rule `DROP` trong **`DOCKER-USER`** (chạy trong `FORWARD`): gói tin bị chặn đứng ngay lập tức tại `DOCKER-USER`!
</details>

---

### E7
<details><summary>Xem đáp án</summary>

**Hướng giải:**
- Trong kịch bản `KubeProxyService` 3 Pods: `3.000` kết nối mới (`SYN`) được phân phối theo xác suất điều kiện `1/3` → `1/2` → `1.0` nên cả 3 Pod nhận xấp xỉ `~1.000` kết nối mỗi Pod; các gói tiếp theo của cùng 4-tuple bám chặt theo **`conntrack`** đi về đúng Pod ban đầu.
- Trong kịch bản `ns1 <-> ns2` và `ns1 -> Internet`: các gói giữa `10.200.0.2` và `10.200.0.3` chạy qua bridge `br-lab0`, còn gói từ `10.200.0.2` đi ra Internet qua `eth0` được `MASQUERADE` thành IP public của host tại `POSTROUTING`.

**Code giải pháp mẫu `KubeProxy` multi-backend:** xem [lab/solutions/MultiBackendKubeProxySolution.java](./lab/solutions/MultiBackendKubeProxySolution.java).
</details>

---

### E8
<details><summary>Xem đáp án</summary>

**Đáp án:**
1. Ý nghĩa các bước chính:
   - `ip netns add ns1`: tạo không gian mạng tách biệt `ns1`.
   - `ip link add veth-ns1 type veth peer name br-veth1`: tạo sợi dây ảo 2 đầu dính nhau.
   - `ip link set veth-ns1 netns ns1`: đưa đầu dây thứ nhất chui vào bên trong `ns1`.
   - `ip link set br-veth1 master br-lab0`: cắm đầu dây còn lại vào switch bridge `br-lab0` ở root namespace.
   - `ip route add default via 10.200.0.1`: chỉ định gateway mặc định cho `ns1` là IP của `br-lab0`.
   - `ip_forward=1` + `MASQUERADE`: cho phép kernel host định tuyến gói giữa bridge với card mạng thật và đổi IP nguồn `10.200.0.x` thành IP host khi ra ngoài.
2. Nếu quên **`ip link set lo up`** trong `ns1`, giao diện loopback `127.0.0.1` vẫn đang `DOWN` → mọi cuộc gọi nội bộ tới `localhost` / `127.0.0.1` bên trong container đó đều lỗi `Network is unreachable` / `Cannot assign requested address`!
</details>

---

### E9
<details><summary>Xem đáp án</summary>

<details open><summary>Phân tích sự cố bảo mật Docker Postgres + Mất mạng container & tràn Conntrack</summary>

**1. Cơ chế gây ra 3 sự cố:**
- **Cổng `5432` Postgres vẫn mở dù `ufw default deny`:** UFW mặc định gắn rule chặn vào chain **`INPUT`**. Nhưng `-p 5432:5432` bảo Docker tạo rule **`DNAT` ở `PREROUTING`** đổi IP đích sang container IP (`172.17.0.x:5432`), khiến gói tin rẽ vào chain **`FORWARD`** chứ không qua `INPUT`! Hậu quả là cả Internet kết nối thẳng được vào Postgres qua IP public của VM!
- **Container mất Internet sau reboot (`ip_forward = 0`):** Kernel Linux cần bật `net.ipv4.ip_forward = 1` để chuyển tiếp gói tin từ interface bridge container (`br-*` / `docker0`) sang card mạng ra Internet (`eth0`). Reboot đã nạp giá trị `0` từ `/etc/sysctl.conf` làm đứt toàn bộ chiều `FORWARD` ra ngoài (trong khi container cùng bridge nói chuyện ở Layer 2 nên vẫn thấy nhau!).
- **Tràn `nf_conntrack` trên cụm K8s `4.500` Services:** `kube-proxy` chế độ `iptables` phụ thuộc `conntrack` cho mọi kết nối NAT + lưu lượng DNS UDP (`ndots:5` nhân 5×!). Vượt trần `nf_conntrack_max` làm kernel drop packet ngẫu nhiên.

**2. Lệnh sửa chuẩn xác:**
1. **Khóa Postgres `5432` đúng cách:**
   - Cách tốt nhất: khởi chạy lại container chỉ gắn loopback `-p 127.0.0.1:5432:5432` (hoặc đặt vào internal Docker network nếu chỉ app container cần nối).
   - Nếu bắt buộc giữ `-p 5432:5432` để host/LAN truy cập có chọn lọc: chặn từ interface ngoài ở chain **`DOCKER-USER`**:
     ```bash
     sudo iptables -I DOCKER-USER -i eth0 ! -s 10.0.0.0/8 -p tcp --dport 5432 -j DROP
     ```
2. **Khôi phục mạng ra ngoài cho container:**
   Sửa `/etc/sysctl.conf` (hoặc `/etc/sysctl.d/99-docker.conf`) thành `net.ipv4.ip_forward = 1` và chạy `sudo sysctl -p` (và kiểm tra lại rule `MASQUERADE` của Docker bằng `systemctl restart docker` nếu cần).
3. **Giảm tải `nf_conntrack` trên K8s:**
   Nâng `net.netfilter.nf_conntrack_max` (vd `262144`–`524288` tùy RAM), giảm `ndots:5` → `ndots:2` + bật **NodeLocal DNSCache** (`NOTRACK` / TCP upstream), và cân nhắc nâng cấp `kube-proxy` sang **IPVS** hoặc **Cilium eBPF** khi cluster lớn.

</details>
</details>

---

## Self-check (từ README §9) — đáp án gợi ý
<details><summary>Xem</summary>

1. **Đường đi packet qua 5 Netfilter hooks:** Vào Host: `PREROUTING -> INPUT -> Host App -> OUTPUT -> POSTROUTING`. Vào Container (sau DNAT): `PREROUTING (DNAT sang container IP) -> FORWARD (DOCKER-USER -> DOCKER) -> POSTROUTING -> Bridge -> veth -> netns`.
2. **Thứ tự 4 tables & `conntrack`:** `raw -> mangle -> nat -> filter`. Bảng `nat` chỉ xử lý gói đầu tiên (`NEW`) của luồng; các gói sau ăn theo bản ghi trong **`nf_conntrack`** để tự động NAT xuôi/ngược.
3. **Docker `-p 8080:80` & bypass `INPUT`:** Docker tạo `DNAT` ở `PREROUTING` (và `OUTPUT` cho local) chuyển `<Host>:8080` thành `<ContainerIP>:80`. Sau `PREROUTING`, đích là IP container nên packet rẽ sang **`FORWARD`**, né hoàn toàn `INPUT` (phải chặn tại `DOCKER-USER`).
4. **Dựng 2 `netns` thủ công:** Tạo `ns1`, `ns2` + bridge `br-lab0`, tạo 2 cặp `veth`, đưa 1 đầu vào từng `netns` (gán IP + `lo up` + `default route`), đầu kia gắn vào `br-lab0`, bật `net.ipv4.ip_forward=1`, và thêm `iptables -t nat -A POSTROUTING ... -j MASQUERADE`.
5. **`kube-proxy` iptables & giới hạn quy mô:** Tạo chuỗi `KUBE-SERVICES -> KUBE-SVC-*` (chia tải theo xác suất điều kiện `1/N`) `-> KUBE-SEP-*` (`DNAT` vào Pod IP). Nhược điểm: duyệt tuần tự `O(N)` và cập nhật rule chậm khi lên hàng nghìn Services — cần đổi sang `IPVS` hoặc `eBPF`.
</details>
